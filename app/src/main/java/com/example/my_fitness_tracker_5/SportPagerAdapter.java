package com.example.my_fitness_tracker_5;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class SportPagerAdapter extends FragmentStateAdapter {

    private final List<String> sports;

    public SportPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> sports) {
        super(fragmentActivity);
        this.sports = sports;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return SportProgressFragment.newInstance(sports.get(position));
    }

    @Override
    public int getItemCount() {
        return sports.size();
    }
}
