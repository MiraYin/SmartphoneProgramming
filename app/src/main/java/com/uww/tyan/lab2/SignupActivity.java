package com.uww.tyan.lab2;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;


import android.os.Build;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Method;
import android.os.StrictMode;

import com.soundcloud.android.crop.Crop;


import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * A login screen that offers login via name/password/photo.
 */
public class SignupActivity extends AppCompatActivity implements AuthDialog.AuthDialogListener {

    /**
     * Id to identity WRITE_EXTERNAL_STORAGE and READ_EXTERNAL_STORAGE permission request.
     */
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 501;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 502;
    private static final int REQUEST_INTERNET = 503;

    /**
     * UI references.
     */
    private EditText mCharacterNameView;
    private EditText mPasswordView;
    private ImageView imageView;
    private View focusView = null;
    private TextView mnametest;
    private TextView NE;

    /**
     * User data.
     */
    private String character_name = "";
    private String full_name = "";
    private String password = "";
    private boolean match = false;
    private boolean name_ava = true;
    private boolean cancel = false;//use to check if error exists when saving
    private String name_test = "";
    private String ug_or_g = "";

    /**
     * dialog for pop-up.
     */
    private AuthDialog authd;

    /**
     * Camera and image related.
     */
    final int REQUEST_IMAGE_CAPTURE = 1;
    final int CROP_PHOTO = 2;
    Bitmap bitmap;
    private Uri imageUri;
    private Uri cropped;
    private String filename;

    private Handler dl;
    nametest checker = null;



    /**
     -----------------------------OnCreate--------------------------------
     Tasks:
      1. show layout
      2. request external storage permission
      3. for SDK>=24, disable Death OnFileUriExposure
      4. implement alreadyHaveAccount -> clear when input happens by setting
         before/on/after textChange for clear/alreadyHaveAccount
      6. set onFocusChangeListener and onEditorActionListener for mPasswordView
         to pop-up password confirmation
      7. set onClickListener for imageView to take photos and crop
      8. set onClickListener for save bottom to check if all necessary info is
         ready and password matching has passed, if so, save the user data and
         photo (if any) to the external storage, as "external-file.txt" and
         "external-bit.png", respectively.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //1. show layout
        setContentView(R.layout.activity_signup);

        //3. for SDK>=24, disable Death OnFileUriExposure
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //2. request external storage permission and Internet permission
        RequestStorage();
        RequestInternet();

        dl = new Handler();

        mCharacterNameView = findViewById(R.id.character_name);
        mPasswordView = findViewById(R.id.password);
        imageView = findViewById(R.id.image_view);
        mnametest = findViewById(R.id.nametest);
        NE = findViewById(R.id.net_err);


        //4. Set Text Change on Clear button.
        final Button mAlready = (Button) findViewById(R.id.already);
        mCharacterNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mCharacterNameView.getText().length() != 0 || mPasswordView.getText().length() != 0) {
                    mAlready.setText("Clear");
                } else {mAlready.setText("Already have an account");}
                Log.d("Login", "Character name is "+mCharacterNameView.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mCharacterNameView.getText().length() != 0 || mPasswordView.getText().length() != 0) {
                    mAlready.setText("Clear");
                } else {mAlready.setText("Already have an account");}
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //6. set onEditorActionListener for mPasswordView to pop-up password confirmation
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            //@Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if ((id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) && !mPasswordView.getText().toString().isEmpty()) {
                    password = mPasswordView.getText().toString();
//                    Log.d("test", "password in onCreate is"+password);
                    authd = new AuthDialog();
                    authd.fisrtPass = password;
                    authd.show(getFragmentManager(), "AuthDialogFragment");
                    return true;
                }
                return false;
            }
        });

        //6. set onFocusChangeListener for mPasswordView to pop-up password confirmation
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            //@Override
            public void onFocusChange(View view, boolean hasFocus) {
                password = mPasswordView.getText().toString();
//                Log.d("test", "password in onCreate is"+password);
                if(!hasFocus && !password.isEmpty() && !match){
                    Log.d("attempt", password);
                    authd = new AuthDialog();
                    authd.fisrtPass = password;
                    authd.show(getFragmentManager(), "AuthDialogFragment");
                }
            }
        });

        mCharacterNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Log.d("edit","on edit");
                if ((i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_NULL) && !mCharacterNameView.getText().toString().isEmpty()) {
                    Log.d("edit","on edit");
                    checker = new nametest();
                    checker.execute(mCharacterNameView.getText().toString());
                    return true;
                }
                return false;
            }
        });

        mCharacterNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus && !mCharacterNameView.getText().toString().isEmpty()){
                    Log.d("name avail check", "OnFocus");
                    checker = new nametest();
                    checker.execute(mCharacterNameView.getText().toString());
                }
            }
        });

        //7. set onClickListener for imageView to take photos and crop
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = new Date(System.currentTimeMillis());
                filename = format.format(date);
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File outputImage = new File(path,filename+".jpg");
                //File outputImage = new File(getCacheDir(), "cache.jpg");
                try {
                    if(outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        // set up already-hava-account
        mAlready.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAlready.getText().toString().equalsIgnoreCase("Clear")) {
                    mCharacterNameView.setText("");
                    character_name = "";
                    mnametest.setText("");
                    mPasswordView.setText("");
                    password = "";
                    bitmap = null;
                    int id = getResources().getIdentifier("@android:drawable/" + "ic_menu_camera", null, null);
                    imageView.setImageResource(id);
                    match = false;
                } else {
                    Intent i = new Intent(SignupActivity.this , LoginActivity.class);
                    //start Login
                    startActivity(i);
                }
            }
        });

        //8. set onClickListener for save bottom
        Button mSave = findViewById(R.id.save);
        mSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if password matching has passed, if not, pop-up dialog and do matching
                Log.d("onclick",match?"true":"false");
                if(!match){
                    Log.d("intomatch","not match");
                    password = mPasswordView.getText().toString();
                    //pop-up dialog and do matching
                    authd = new AuthDialog();
                    authd.fisrtPass = password;
                    authd.show(getFragmentManager(), "AuthDialogFragment");
                    Log.d("into match","after popup");

                }else{
                    //check if all necessary info is ready
                    //if so, save the user data and photo (if any) to the external storage
                    attemptSave();
                }
            }
        });

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.ugrad:
                if (checked)
                    ug_or_g = "ug";
                    break;
            case R.id.grad:
                if (checked)
                    ug_or_g = "g";
                    break;
        }
    }

    /**
     -----------------------------onSaveInstanceState--------------------------------
     Tasks: save everything for restore later
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save everything
        //Log.d("STATE", "onSaveState");
        //outState.putParcelable("IMG", bitmap);
        outState.putParcelable("IMG", cropped);
        outState.putString("CN", mCharacterNameView.getText().toString());
        outState.putString("PW", mPasswordView.getText().toString());
        outState.putBoolean("MATCH", match);
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
        //retrieve everything

        //bitmap = savedInstanceState.getParcelable("IMG");
        //if bitmap == null, then imageview should show the camera icon
        cropped = savedInstanceState.getParcelable("IMG");
        if(cropped == null) {
            int id = getResources().getIdentifier("@android:drawable/" + "ic_menu_camera", null, null);
            imageView.setImageResource(id);
        }else
            imageView.setImageURI(cropped);

        //restore user info and set view
        character_name = savedInstanceState.getString("CN");
        mCharacterNameView.setText(character_name);
        full_name = savedInstanceState.getString("FN");
        password = savedInstanceState.getString("PW");
        mPasswordView.setText(password);
        match = savedInstanceState.getBoolean("MATCH");
        name_test = savedInstanceState.getString("test");
    }

    /**
     -----------------------------onActivityResult--------------------------------
     Tasks: deal with request_image_capture and crop_image
     in request_image_capture, we store the captured image and further call API to crop
     in crop_image, we get result and update imageView
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(SignupActivity.this, "ActivityResult resultCode error", Toast.LENGTH_SHORT).show();
            return;
        }

        switch(requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                //sendBroadcast to scan file so that we can see the captured photo in library (i.e. Photo)
	            Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		        intentBc.setData(imageUri);
		        this.sendBroadcast(intentBc);

                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = new Date(System.currentTimeMillis());
                filename = format.format(date);
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File outputImage = new File(path,filename+".jpg");

                try {
                    if(outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                cropped = Uri.fromFile(outputImage);
                //Call soundcloud API to crop image, store results in destination.
                Crop.of(imageUri, cropped).asSquare().start(this, CROP_PHOTO);
                break;

            case CROP_PHOTO:
                //sendBroadcast to scan file so that we can see the cropped photo in library (i.e. Photo)
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(cropped);
                this.sendBroadcast(intent);
                imageView.setImageURI(cropped);
                break;

            default:
                break;
        }
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
     * Attempts to save.
     * If there are form errors (missing fields), the
     * errors are presented and no saving attempt is made.
     */
    private void attemptSave() {

        mCharacterNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the saving attempt.
        character_name = mCharacterNameView.getText().toString();
        password = mPasswordView.getText().toString();

        //if cancel == true, then there exists error, we cannot save and must cancel
        cancel = false;

        if (TextUtils.isEmpty(character_name)) {
            mCharacterNameView.setError("This field is required");
            focusView = mCharacterNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("This field is required");
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            //set focus for missing fields
            focusView.requestFocus();
            return;
        } else {
            checker = new nametest();
            checker.execute(mCharacterNameView.getText().toString());
            if(!name_ava) {
                if (mnametest.getText().toString().contentEquals("Username not available")){
                    mCharacterNameView.setError("Please choose another username");
                }else {
                    mCharacterNameView.setError("Connection Error");
                }
                focusView = mCharacterNameView;
                focusView.requestFocus();
                return;
            }else{
                //satisfy all requirement, upload to server
                /*Toast.makeText(this, "Saving...",
                        Toast.LENGTH_LONG).show();*/
                String toPost = buildJSONRequest(mCharacterNameView.getText().toString(),
                        mPasswordView.getText().toString());
                doPost(toPost);
            }
        }
    }

    /**
     * Save user info into external storage
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
     * positive click on dialog, if match, set match == true, else set cancel == true
     */
    public void onDialogPositiveClick(DialogFragment dialog){
        Log.d("MAIN", "positive clicked");
        if(password.equals(authd.getPass())){
            match = true;
        }else {
            Toast.makeText(this, "Passwords do not match. Please re-enter.",
                    Toast.LENGTH_LONG).show();
            focusView = mPasswordView;
            cancel = true;
        }
    }

    /**
     * negative click on dialog, set cancel == true
     */
    public void onDialogNegativeClick(DialogFragment dialog){
        Log.d("MAIN", "negative clicked");
        Toast.makeText(this, "Please confirm password.",
                Toast.LENGTH_LONG).show();
        focusView = mPasswordView;
        cancel = true;
    }

    public class nametest extends AsyncTask<String, Void, Integer> {
        private String res;

        protected Integer doInBackground(String... params) {
            String username = params[0];
            String avail = "";
            Log.d("nametest", username);
            try {
                URL url = new URL("http://cs65.cs.dartmouth.edu/nametest.pl?name="+username);
                URLConnection urlConnection = url.openConnection();
                InputStream in = urlConnection.getInputStream();
                res = copyInputStreamToString(in);
                try {
                    JSONObject o = new JSONObject(res);
                    try{
                        avail = o.getString("avail");
                        if(avail.contentEquals("true")){
                            Log.d("check","true");
                            return 1;
                        }else{
                            Log.d("check","false");
                            return 2;
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            catch( Exception e){
                Log.d("THREAD ?", e.toString());
                res = e.toString();
                return 3;
            }
            return 3;
        }

        @Override
        protected void onPostExecute(Integer result) {
            //Solution for findViewById in different class:
            // https://stackoverflow.com/questions/30598871/cannot-resolve-method-findviewbyidint

            if(result==1){
                Log.d("nametest","true");
                mnametest.setText("Username is available");
                name_ava = true;
            } else {
                name_ava = false;
                if(result==2)
                    mnametest.setText("Username not available");
                else
                    mnametest.setText("Connection Error:" + res);
            }
        }

        private String copyInputStreamToString(InputStream in) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line = r.readLine();
            return line;
        }
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

    // for simple testing
    private String buildJSONRequest(String name, String pw, String like, String id){
        JSONObject o = new JSONObject();
        try {
            o.put("name", name);
            o.put( "password", pw);
            o.put( "likes", like);
            o.put( "id", id);
        }
        catch( JSONException e){
            Log.d("JSON", e.toString());
        }
        return o.toString();
    }

    private String buildJSONRequest(String name, String pw){
        return buildJSONRequest(name, pw, "","");
    }

    public void doPost(final String req){
        new Thread(new Runnable() {

            String result = null; // closed over by the post()-ed run().

            @Override
            public void run() {
                try {
                    URL url = new URL("http://cs65.cs.dartmouth.edu/profile.pl");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    try {
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept-Encoding", "identity");
                        conn.setFixedLengthStreamingMode(req.length());
                        OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                        out.write(req.getBytes());
                        out.flush();
                        out.close();
                        InputStream in = new BufferedInputStream(conn.getInputStream());
                        result = readStream(in);
                        conn.disconnect();
                    }
                    catch(Exception e){
                        Log.d("THREAD", e.toString());
                        postResultsToUI("Connection Error: "+ e.toString());
                        conn.disconnect();
                        return;
                    }
                }
                catch( Exception e){
                    Log.d("THREAD", e.toString());
                    postResultsToUI("Connection Error: "+ e.toString());
                    return;
                }

                if( result!= null ) {
                    Log.d("NET POST", result);
                    SaveSE_string(mCharacterNameView.getText().toString()+".txt", req);
                    // send data to next activity
                    Bundle bundle = new Bundle();
                    bundle.putString("username",mCharacterNameView.getText().toString());
                    bundle.putString("password",mPasswordView.getText().toString());
                    bundle.putString("likes","");
                    bundle.putString("id","");
                    bundle.putParcelable("profile",cropped);
                    Intent i = new Intent(SignupActivity.this, TabLayoutActivity.class);
                    i.putExtras(bundle);
                    startActivity(i);
                }
                else{
                    Log.d("NET ERR", "empty result");
                    postResultsToUI("Connection error: empty");
                }
            }
        }).start();
    }

    private void postResultsToUI(final String res){
        dl.post(new Runnable() {
            @Override
            public void run() {
                NE.setText(res);
            }
        });
    }
}


