package com.example.grigori.fitnessapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.grigori.fitnessapp.Profile.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    public static final String TAG = SignupActivity.class.getSimpleName();

    @BindView(R.id.signup_password_et)
    EditText mPasswordEditText;
    @BindView(R.id.signup_repeat_password_et)
    EditText mRepeatPasswordEditText;
    @BindView(R.id.signup_email_et)
    EditText mEmailEditText;
    @BindView(R.id.signup_name_et)
    EditText mNameEditText;
    @BindView(R.id.signup_gender_spinner)
    Spinner mGenderSpinner;

    private FirebaseAuth mAuth;

    private String mGender = "Prefer not to tell";
    public static Calendar mBirthDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        ArrayAdapter<CharSequence> genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.genders_array, android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenderSpinner.setAdapter(genderSpinnerAdapter);
        mGenderSpinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }


    public void onConfirmButtonClicked(View view) {
        if (mPasswordEditText.getText().toString().equals(mRepeatPasswordEditText.getText().toString())
                && mPasswordEditText.getText().length() > 7) {

            String email = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            final String name = mNameEditText.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                // updateUI(user);

                                user.updateProfile(profileUpdate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User profile updated.");
                                                }
                                            }
                                        });

                                createUserInDatabase(user.getUid());

                                startActivity(new Intent(SignupActivity.this, MainActivity.class));

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());

                                if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                    Toast.makeText(SignupActivity.this, "Such email already exists.",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(SignupActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                                //  updateUI(null);
                            }

                            // ...
                        }
                    });

        } else if (!mPasswordEditText.getText().toString().equals(mRepeatPasswordEditText.getText().toString()))
        {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
        } else if (! (mPasswordEditText.getText().length() > 7)) {
            Toast.makeText(this, "Password should contain at least 8 characters", Toast.LENGTH_SHORT).show();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        mGender = (String) parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        mGender = "Prefer not to tell";
    }

    private void createUserInDatabase(String uid) {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String name = mNameEditText.getText().toString();


        UserProfile userProfile = new UserProfile();
        userProfile.setName(name);
        userProfile.setEmail(email);
        userProfile.setBirthDate(mBirthDate.toString());
        userProfile.setGender(mGender);
        userProfile.setAbout("");
        userProfile.setUid(uid);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("users");
        // TODO Add isSuccessful check
        reference.child(uid).setValue(userProfile);
    }


    public void onDatePickButtonClicked(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }



    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current date as the default date in the pickernbnb
            int year =  mBirthDate.get(Calendar.YEAR);
            int month = mBirthDate.get(Calendar.MONTH);
            int day =   mBirthDate.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            mBirthDate.set(year, month, day);

        }

    }

}
