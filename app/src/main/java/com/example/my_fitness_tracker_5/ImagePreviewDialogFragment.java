package com.example.my_fitness_tracker_5;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ImagePreviewDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_BASE64 = "image_base64";

    public static ImagePreviewDialogFragment newInstance(String imageBase64) {
        ImagePreviewDialogFragment fragment = new ImagePreviewDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_BASE64, imageBase64);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_image_preview, container, false);

        ImageView imageView = view.findViewById(R.id.imageView_large);

        if (getArguments() != null) {
            String imageBase64 = getArguments().getString(ARG_IMAGE_BASE64);
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                try {
                    Bitmap bitmap = decodeBase64(imageBase64);
                    imageView.setImageBitmap(bitmap);
                } catch (IllegalArgumentException e) {
                    Log.e("ImagePreviewDialog", "Invalid Base64 string: " + imageBase64);
                }
            }
        }

        return view;
    }

    private Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
