package com.tournaments.gamerclash.Adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.tournaments.gamerclash.UnifiedMatchFragment;
import com.tournaments.gamerclash.UnifiedResultFragment;

public class UnifiedViewPagerAdapter extends FragmentPagerAdapter {

    private final String category;

    public UnifiedViewPagerAdapter(@NonNull FragmentManager fm, String category) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.category = category;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("category", category);

        if (position == 0) {
            UnifiedMatchFragment fragment = new UnifiedMatchFragment();
            fragment.setArguments(bundle);
            return fragment;
        } else {
            UnifiedResultFragment fragment = new UnifiedResultFragment();
            fragment.setArguments(bundle);
            return fragment;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int pos) {
        if (pos == 0) {
            return "Matches";
        } else {
            return "Results";
        }
    }
}
