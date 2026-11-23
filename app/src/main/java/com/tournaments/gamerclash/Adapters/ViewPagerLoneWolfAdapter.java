package com.tournaments.gamerclash.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.tournaments.gamerclash.CS_MatchFragment;
import com.tournaments.gamerclash.CS_ResultFragment;
import com.tournaments.gamerclash.LW_MatchFragment;
import com.tournaments.gamerclash.LW_ResultFragment;

public class ViewPagerLoneWolfAdapter extends FragmentPagerAdapter {

    public ViewPagerLoneWolfAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new LW_MatchFragment();
        }
        else {
            return new LW_ResultFragment();
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
