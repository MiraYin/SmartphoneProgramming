package com.uww.tyan.lab2;







import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;


import java.util.ArrayList;


public class TabViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;

    public TabViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) { return fragments.get(position); }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "play";
            case 1:
                return "history";
            case 2:
                return "ranking";
            case 3:
                return "settings";
            default:
                break;
        }
        return null;

    }









}
