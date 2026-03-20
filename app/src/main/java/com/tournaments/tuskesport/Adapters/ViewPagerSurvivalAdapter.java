package com.tournaments.tuskesport.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.tournaments.tuskesport.LW_MatchFragment;
import com.tournaments.tuskesport.LW_ResultFragment;
import com.tournaments.tuskesport.Surv_MatchFragment;
import com.tournaments.tuskesport.Surv_ResultFragment;

public class ViewPagerSurvivalAdapter extends FragmentPagerAdapter {

    public ViewPagerSurvivalAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new Surv_MatchFragment();
        }
        else {
            return new Surv_ResultFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int pos) {
        if(pos==0) {
            return "Matches";
        }
        else {
            return "Results";
        }
    }
}
