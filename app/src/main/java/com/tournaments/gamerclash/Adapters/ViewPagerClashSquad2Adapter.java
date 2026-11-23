package com.tournaments.gamerclash.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.tournaments.gamerclash.CS2_MatchFragment;
import com.tournaments.gamerclash.CS2_ResultFragment;
import com.tournaments.gamerclash.CS_MatchFragment;
import com.tournaments.gamerclash.CS_ResultFragment;

public class ViewPagerClashSquad2Adapter extends FragmentPagerAdapter {

    public ViewPagerClashSquad2Adapter(@NonNull FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new CS2_MatchFragment();
        }
        else {
            return new CS2_ResultFragment();
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
