package com.uww.tyan.lab2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

public class SettingsFragment extends PreferenceFragment {

    private SignupActivity Signup;
    private String username;
    private String password;
    private String likes;
    private String id;
    private Uri profile;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setting);

        Bundle bundle = getArguments();
        if (bundle != null) {

            username = bundle.getString("username");
            Log.d("OnCreate", username);
            password = bundle.getString("password");
            Log.d("OnCreate", password);
            likes = bundle.getString("likes");
            Log.d("OnCreate", likes);
            id = bundle.getString("id");
            Log.d("OnCreate", id);
            try {
                profile = bundle.getParcelable("profile");
            } catch (Error e) {
                Log.d("settings", "no profile");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settingsfragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        TextView mName = getActivity().findViewById(R.id.sprofile);
        mName.setText("User: " + username);

        ImageView pf = getActivity().findViewById(R.id.sportrait);
        if(profile != null)
            pf.setImageURI(profile);

        Button mSignout = (Button)getActivity().findViewById(R.id.ssignout);
        mSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file;
                FileOutputStream fos = null;
                file = new File(Environment.getExternalStorageDirectory(), username+".txt");
                if(file.exists()) {
                    file.delete();
                    Log.d("signout","delete local");
                }

                Intent i = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });
    }
}

