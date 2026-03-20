package com.tournaments.tuskesport.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.tournaments.tuskesport.BR_MatchFragment;
import com.tournaments.tuskesport.BR_ResultFragment;
import com.tournaments.tuskesport.B_MatchFragment;
import com.tournaments.tuskesport.B_ResultFragment;

public class ViewPagerBonusMatchAdapter extends FragmentPagerAdapter {

    public ViewPagerBonusMatchAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new B_MatchFragment();
        }
        else {
            return new B_ResultFragment();
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
