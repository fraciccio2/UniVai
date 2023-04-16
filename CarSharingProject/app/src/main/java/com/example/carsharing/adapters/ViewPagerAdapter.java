package com.example.carsharing.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.carsharing.fragments.RidesInFragment;
import com.example.carsharing.fragments.RidesOutFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new RidesInFragment();
        }
        return new RidesOutFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
