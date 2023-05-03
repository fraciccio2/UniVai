package com.it.univai.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.it.univai.fragments.RidesInFragment;
import com.it.univai.fragments.RidesOutFragment;
import com.it.univai.fragments.YourRidesFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new RidesInFragment();
            case 2:
                return new YourRidesFragment();
            default:
                return new RidesOutFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
