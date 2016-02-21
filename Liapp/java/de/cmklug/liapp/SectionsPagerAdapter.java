package de.cmklug.liapp;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ChartFragment.newInstance(0, "Scan");
            case 1:
                return LogFragment.newInstance(1, "Log"); //Todo: language
            case 2:
                //return AboutFragment.newInstance(2, "About");
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return "Scan";
            case 1:
                return  "Log";
            //case 2:
                //return "About";

        }
        return null;
    }
}