package com.example.my_fitness_tracker_5;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private ImageView imageViewProfilePicture;
    private TextView textViewEmail;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // Enable the back arrow
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);
        textViewEmail = findViewById(R.id.textViewEmail);
        Button buttonLogout = findViewById(R.id.buttonLogout);
        Button buttonChangePicture = findViewById(R.id.buttonChangePicture);
        Button buttonChangePassword = findViewById(R.id.buttonChangePassword);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            fetchUserProfile(user.getUid());
        }

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        buttonChangePicture.setOnClickListener(v -> openImagePicker());

        buttonChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        imageViewProfilePicture.setOnClickListener(v -> {
            if (imageViewProfilePicture.getTag() != null) {
                showImagePreview((String) imageViewProfilePicture.getTag());
            }
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri uri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            String imageBase64 = encodeBase64(bitmap);
                            imageViewProfilePicture.setImageBitmap(bitmap);
                            imageViewProfilePicture.setTag(imageBase64);
                            uploadImageToFirebase(imageBase64);
                        } catch (IOException e) {
                            Log.e("ProfileActivity", "Failed to load image", e);
                            Toast.makeText(ProfileActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchUserProfile(String uid) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String email = document.getString("email");
                    String profileImageUrl = document.getString("profileImageUrl");

                    textViewEmail.setText(email);
                    if (profileImageUrl != null) {
                        loadImageFromUrl(profileImageUrl);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfileActivity.this, "get failed with " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Intent chooser = Intent.createChooser(intent, "Select Picture");
        imagePickerLauncher.launch(chooser);
    }

    private void uploadImageToFirebase(String imageBase64) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            StorageReference profileImageRef = storageReference.child("profileImages/" + uid);

            byte[] data = Base64.decode(imageBase64, Base64.DEFAULT);

            UploadTask uploadTask = profileImageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> saveProfileImageUrlToFirestore(uid, uri.toString()))).addOnFailureListener(e -> {
                Log.d("ProfileActivity", "Failed to upload image: " + e.getMessage());
                Toast.makeText(ProfileActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveProfileImageUrlToFirestore(String uid, String url) {
        db.collection("users").document(uid).update("profileImageUrl", url)
                .addOnSuccessListener(aVoid -> {
                    loadImageFromUrl(url);
                    Toast.makeText(ProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.d("ProfileActivity", "Failed to update profile image: " + e.getMessage());
                    Toast.makeText(ProfileActivity.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadImageFromUrl(String url) {
        Glide.with(this).asBitmap().load(url).into(new BitmapImageViewTarget(imageViewProfilePicture) {
            @Override
            protected void setResource(Bitmap resource) {
                if (resource != null) {
                    imageViewProfilePicture.setImageBitmap(resource);
                    String imageBase64 = encodeBase64(resource);
                    imageViewProfilePicture.setTag(imageBase64);
                } else {
                    Log.e("ProfileActivity", "Failed to load image: Bitmap is null");
                }
            }

            @Override
            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                imageViewProfilePicture.setImageBitmap(resource);
                String imageBase64 = encodeBase64(resource);
                imageViewProfilePicture.setTag(imageBase64);
            }
        });
    }

    private String encodeBase64(Bitmap image) {
        if (image == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void showImagePreview(String imageBase64) {
        if (imageBase64 != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            ImagePreviewDialogFragment.newInstance(imageBase64).show(fragmentManager, "image_preview");
        } else {
            Toast.makeText(this, "Failed to load image preview", Toast.LENGTH_SHORT).show();
        }
    }
}
