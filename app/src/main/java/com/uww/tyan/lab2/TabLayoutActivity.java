package com.uww.tyan.lab2;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;


import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.uww.tyan.lab2.view.SlidingTabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.app.PendingIntent.getActivity;


/**
 * Created by tyan on 10/04/2017.
 */

public class TabLayoutActivity extends AppCompatActivity {

    public static TabLayoutActivity instance = null;


    private SlidingTabLayout slidingTabLayout;
    private ViewPager mViewPager;
    private ArrayList<Fragment> fragments;
    private TabViewPagerAdapter mViewPagerAdapter;

    public static Context getInstance() {
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //1. show layout
        setContentView(R.layout.tablayout);

        //3. for SDK>=24, disable Death OnFileUriExposure
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Bundle bundle = getIntent().getExtras();

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tab);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        fragments = new ArrayList<Fragment>();

        Fragment fragment;
        fragment = new HistoryFragment();
        fragment.setArguments(bundle);
        fragments.add(fragment);
        fragment = new PlayFragment();
        fragment.setArguments(bundle);
        fragments.add(fragment);
        fragment = new RankingFragment();
        fragment.setArguments(bundle);
        fragments.add(fragment);
        fragment = new SettingsFragment();
        fragment.setArguments(bundle);
        fragments.add(fragment);

        mViewPagerAdapter = new TabViewPagerAdapter(getFragmentManager(), fragments);

        mViewPager.setAdapter(mViewPagerAdapter);

        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mViewPager);
    }

    @Override
    public void onResume() {
        super.onResume();
        instance = this;
    }


    @Override
    public void onPause() {
        super.onPause();
        instance = null;
    }

}
