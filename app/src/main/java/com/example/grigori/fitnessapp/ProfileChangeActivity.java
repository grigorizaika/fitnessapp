package com.example.grigori.fitnessapp;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileChangeActivity extends AppCompatActivity {

    private static final String TAG = ProfileChangeActivity.class.getSimpleName();

    EditText mChangeNameEditText;
    EditText mChangePasswordEditText;
    EditText mChangeAboutEditText;
    Spinner mChangeGenderSpinner;

    FirebaseDatabase mFirebaseDatabase;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mChangeNameEditText = (EditText) findViewById(R.id.profile_change_name_et);
        mChangePasswordEditText = (EditText) findViewById(R.id.profile_change_password_et);
        mChangeAboutEditText = (EditText) findViewById(R.id.profile_change_about_et);
        mChangeGenderSpinner = (Spinner) findViewById(R.id.profile_change_gender_spinner);

        // Populate the spinner with genders array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genders_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChangeGenderSpinner.setAdapter(adapter);

        fillProfileInfo();
    }

    private void fillProfileInfo () {
        // Get the reference to the "users" node in database
        DatabaseReference reference = mFirebaseDatabase.getReference().child("users");

        final String currentUid = mUser.getUid();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Find the current user's node in database
                DataSnapshot userNode = dataSnapshot;
                for (DataSnapshot currentUserNode : dataSnapshot.getChildren()) {
                    if (currentUid.equals(currentUserNode.getKey())) {
                        Log.d(TAG, "Found match: " + currentUserNode.child("name").getValue(String.class));
                        userNode = currentUserNode;
                        break;
                    }
                }

                Log.d(TAG, "name: " + userNode.child("name").getValue(String.class));
                Log.d(TAG, "about: " + userNode.child("about").getValue(String.class));

                String userName = userNode.child("name").getValue(String.class);
                String userAbout    = userNode.child("about").getValue(String.class);


                mChangeNameEditText.setText(userName);
                mChangeAboutEditText.setText(userAbout);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Saves all the profile changes the user has made
     */
    public void onSaveProfileChangesClicked(View view) {
        // TODO Add the "Are you sure you want to make changes?" question

        // Find the user by id
        String currentUid = mUser.getUid();
        DatabaseReference reference = mFirebaseDatabase.getReference().child("users").child(currentUid);

        // Name change
        if (!mChangeNameEditText.getText().toString().isEmpty()) {
            String newName = mChangeNameEditText.getText().toString();
            reference.child("name").setValue(newName);
        } else {
            Toast.makeText(this, "Type in the name", Toast.LENGTH_SHORT).show();
        }

        // About change
        String newAbout = mChangeAboutEditText.getText().toString();
        reference.child("about").setValue(newAbout);

        // Gender change
        String newGender = mChangeGenderSpinner.getSelectedItem().toString();
        reference.child("gender").setValue(newGender);


        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
