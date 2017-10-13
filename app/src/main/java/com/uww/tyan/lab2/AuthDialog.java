package com.uww.tyan.lab2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class AuthDialog extends DialogFragment {

    public String fisrtPass = "";
    public String pass;
    public String getPass(){
        return pass;
    }


    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface AuthDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    AuthDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.dialog_style);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //   "final" is important for the closure to be created in the inner class
        final View dialog_view = inflater.inflate(R.layout.dialog, null);

        builder.setView(dialog_view)
        // Add action buttons
        .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Log.d("DIALOG", "positive clicked");

                // collect strings

                TextView tp = dialog_view.findViewById(R.id.passwd);
                pass = tp.getText().toString();
                mListener.onDialogPositiveClick(AuthDialog.this);
            }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // This will dismiss the dialog without the callback:
                // AuthDialog.this.getDialog().cancel();
                Log.d("DIALOG", "negative clicked");
                mListener.onDialogNegativeClick(AuthDialog.this);
            }
        })
        ;
        
        final AlertDialog dialog = builder.create();
        TextView mPasswd = dialog_view.findViewById(R.id.passwd);
        pass = mPasswd.getText().toString();

        Log.d("Dialog", "Passwd 2 is "+ mPasswd.getText().toString());

        mPasswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                TextView mPasswd = dialog_view.findViewById(R.id.passwd);
//                pass = mPasswd.getText().toString();
//
//                if (pass == fisrtPass) {
//                    TextView match = dialog_view.findViewById(R.id.match);
//                    match.setText("Match!");
//                    Log.d("Dialog", "changeTVMatch");
//                } else {
//                    TextView match = dialog_view.findViewById(R.id.match);
//                    match.setText("Not Match yet!");
//                    Log.d("Dialog", "changeTVNotMatch");}
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView mPasswd = dialog_view.findViewById(R.id.passwd);
                pass = mPasswd.getText().toString();

                Log.d("Dialog", "current pass is " + pass + " ,first pass is "+ fisrtPass);
                if (pass.toString().equals(fisrtPass.toString())) {
                    TextView match = dialog_view.findViewById(R.id.match);
                    match.setText("Match!");
                    Log.d("Dialog", "changeTVMatch");
                } else {
                    TextView match = dialog_view.findViewById(R.id.match);
                    match.setText("Not Match yet. Please go on.");
                    Log.d("Dialog", "changeTVNotMatch");}
            }


            @Override
            public void afterTextChanged(Editable s) {
//                TextView mPasswd = dialog_view.findViewById(R.id.passwd);
//                pass = mPasswd.getText().toString();
//
//                Log.d("Dialog", "current pass is " +pass+ " ,first pass is "+ fisrtPass);
//                if (pass.toString().equals(fisrtPass.toString())) {
//                    TextView match = dialog_view.findViewById(R.id.match);
//                    match.setText("Match!");
//                    Log.d("Dialog", "changeTVMatch");
//                } else {
//                    TextView match = dialog_view.findViewById(R.id.match);
//                    match.setText("Not Match yet!");
//                    Log.d("Dialog", "changeTVNotMatch");}
            }
        });
        return dialog;
    }

    /** Override the Fragment.onAttach() method to instantiate the NoticeDialogListener*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AuthDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
        Log.d("DIALOG", "attached");
    }
}
