
package com.uww.tyan.lab2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;


import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.text.TextUtils;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import android.os.StrictMode;

import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;



public class LoginActivity extends AppCompatActivity {
    /**
     * Id to identity WRITE_EXTERNAL_STORAGE and READ_EXTERNAL_STORAGE permission request.
     */
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 501;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 502;
    private static final int REQUEST_INTERNET = 503;

    /**
     * UI references.
     */
    private EditText mUserNameView;
    private EditText mPasswordView;
    private View focusView = null;
    private TextView mnametest;
    private TextView EM;//err_msg

    /**
     * User data.
     */
    private String user_name = "";
    private String password = "";
    private boolean cancel = false;//use to check if error exists when saving
    private String likes = "";
    private String id = "";

    /**
     * Handler
     */
    private Handler dl;




    /**
     -----------------------------OnCreate--------------------------------
     Tasks:
     1. show layout
     2. request external storage permission and Internet permission
     3. for SDK>=24, disable Death OnFileUriExposure
     4. set clear buttom
     5. set Login buttom, which Connect to server (Using GET), to check if
     the username and password matches. If so, retrieve the user data, store
     locally and send to next activity. If not, update the error to UI.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // for SDK>=24, disable Death OnFileUriExposure
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //request external storage permission and Internet permission
        RequestStorage();
        RequestInternet();

        dl = new Handler();

        mUserNameView = findViewById(R.id.user_name);
        mPasswordView = findViewById(R.id.password);
        mnametest = findViewById(R.id.nametest);
        EM = findViewById(R.id.err_msg);


        //Set Text Change on Clear button.
        final Button mClear = (Button) findViewById(R.id.clear);
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("mClear", "Clicked");
                mUserNameView.setText("");
                user_name = "";
                mnametest.setText("");
                EM.setText("");
                mPasswordView.setText("");
                password = "";
            }
        });

        //set onClickListener for log in bottom
       final Button mSignin = findViewById(R.id.signin);
        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        final Button mCreate = findViewById(R.id.create);
        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this , SignupActivity.class);
                startActivity(i);
            }
        });
    }

    /**
     -----------------------------onSaveInstanceState--------------------------------
     Tasks: save everything for restore later
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Log.d("STATE", "onSaveState");
        outState.putString("UN", mUserNameView.getText().toString());
        outState.putString("PW", mPasswordView.getText().toString());
        outState.putString("test",mnametest.getText().toString());
    }

    /**
     -----------------------------onRestoreInstanceState--------------------------------
     Tasks: retrieve everything from onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Log.d("STATE", "onRestoreState");
        //restore user info and set view
        user_name = savedInstanceState.getString("UN");
        mUserNameView.setText(user_name);
        password = savedInstanceState.getString("PW");
        mPasswordView.setText(password);
    }

    /**
     -----------------------------RequestStorage--------------------------------
     Tasks: Request Storage permission for read and write external storage
     */
    private void RequestStorage() {
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return;
        }else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    /**
     -----------------------------RequestInternet--------------------------------
     Tasks: Request Storage permission for Internet
     */
    private void RequestInternet() {
        if (checkSelfPermission(INTERNET) == PackageManager.PERMISSION_GRANTED)
            return;
        else
            requestPermissions(new String[]{INTERNET}, REQUEST_INTERNET);
    }

    /**
     -----------------------------attemptLogin-----------------------------------
     * Attempts to Log in.
     * If there are form errors (missing fields), the errors are presented
     * and log in attempt fails, else retrieve the data from server and check
     * if password and username matches. If so, jump into TabLayoutActivity and send
     * retrieved data to that activity.
     */
    private void attemptLogin() {
        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the attempt.
        user_name = mUserNameView.getText().toString();
        password = mPasswordView.getText().toString();

        //if cancel == true, then there exists error, we cannot save and must cancel
        cancel = false;

        if (TextUtils.isEmpty(user_name)) {
            mUserNameView.setError("User Name is required");
            focusView = mUserNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Forgot your password?");
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel){
            //set focus for missing fields
            focusView.requestFocus();
            return;
        }else{
            Log.d("attempLogin","logging");
            //connect server to check if username and password matches, if so, start TabLayoutActivity
            doGet();
        }
    }

    /**
     --------------------------------SaveSE_string----------------------------------
     * Save user info (String tosave, retrieved from server) into external storage
     */
    public void SaveSE_string(String EXTERNAL_FILENAME, String tosave){
        File file;
        FileOutputStream fos = null;
        file = new File (Environment.getExternalStorageDirectory(), EXTERNAL_FILENAME);

        //if the file exists, delete it and re-create
        try {
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch(IOException e) {
            e.printStackTrace();
        }

        //saving res retrieved from server to username.txt
        try {
            fos = new FileOutputStream(file);
            fos.write(tosave.getBytes());
            fos.close();
            Log.d("saveseString",tosave);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     --------------------------------------doGet-------------------------------------
     * Connect to server (Using GET), to check if the username and password matches. If so,
     * retrieve the user data, store locally and send to next activity. If not, update the
     * error to UI.
     */
    public void doGet(){
        final String UN = mUserNameView.getText().toString();
        final String PW = mPasswordView.getText().toString();

        new Thread(new Runnable() {
            String res = null;
            @Override
            public void run() {
                try {
                    URL url = new URL("http://cs65.cs.dartmouth.edu/profile.pl?name=" + UN + "&password="+ PW);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    try {
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Accept-Encoding", "identity");
                        InputStream in = new BufferedInputStream(conn.getInputStream());
                        res = readStream(in);
                        try{
                            JSONObject o = new JSONObject(res);//org.json library
                            try{
                                likes = o.getString("likes");
                                id = o.getString("id");

                                //saving data locally, named after username
                                SaveSE_string(UN+".txt",res);
                                // send data to next activity
                                Bundle bundle = new Bundle();
                                bundle.putString("username",UN);
                                bundle.putString("password",PW);
                                bundle.putString("likes",likes);
                                bundle.putString("id",id);
                                //bundle.putParcelable("profile", );
                                Intent i = new Intent(LoginActivity.this, TabLayoutActivity.class);
                                i.putExtras(bundle);
                                startActivity(i);
                            }catch(JSONException e1){
                                //then there must be error
                                String error = o.getString("error");
                                //feedback to UI
                                //EM.setText(error);
                                postResultsToUI(error);
                            }
                        } catch (JSONException e2){
                            Log.d("doGET", e2.toString());
                            postResultsToUI(e2.toString());
                        }
                    } finally {
                        conn.disconnect();
                    }
                }
                catch( Exception e){
                    Log.d("doGET", e.toString());
                    postResultsToUI(e.toString());
                }
            }
        }).start();
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    /**
     --------------------------------------postResultsToUI---------------------------------------
     * Communicating with the UI thread from a worker thread:
     * postResultsToUI, actually don't need to be this complicated in this case, simply do EM.setText
     * in doGET will also work.
     */
    private void postResultsToUI(final String res){
        dl.post(new Runnable() {
            @Override
            public void run() {
                EM.setText(res);
            }
        });
    }
}



