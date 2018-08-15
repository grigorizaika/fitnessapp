package com.example.grigori.fitnessapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grigori.fitnessapp.Profile.UserProfile;
import com.firebase.ui.auth.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Thread.sleep;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = ProfileActivity.class.getSimpleName();

    boolean stopSupplyUserData = false;

    public static final int RC_PHOTO_PICKER = 2;

    FirebaseDatabase mFirebaseDatabase;

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    UserProfile mUserProfile;

    String mProfilePictureUrl;

    Uri mCurrentDownloadUrl = Uri.EMPTY;

    @BindView(R.id.collapsingToolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.add_profile_picture_button)
    ImageButton mAddProfilePictureButton;

    @BindView(R.id.toolbar_profile_iv)
    ImageView mProfilePictureImageView;

    @BindView(R.id.about_tv)
    TextView mAboutTextView;

    @BindView(R.id.gender_tv)
    TextView mGenderTextView;

    String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //getSupportActionBar().hide();
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);

        initCurrentUserProfile();

        stopSupplyUserData = false;
        (new SupplyUserDataThread()).start();

        setAboutMeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCurrentUserProfile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSupplyUserData = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            // TODO: after changing the profile picture, delete the old one from storage
            mAddProfilePictureButton = (ImageButton) findViewById(R.id.add_profile_picture_button);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference().child("profile_pictures");
            storageReference = storageReference.child(mUser.getUid());

            Uri selectedImage = data.getData();

            StorageReference photoRef = storageReference.child(selectedImage.getLastPathSegment());
            photoRef.putFile(selectedImage).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mCurrentDownloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d("ProfileActivity", "putFile.addOnSuccessListener's onSuccess is triggered! Url is: " + mCurrentDownloadUrl);

                    DatabaseReference userProfileReference = mFirebaseDatabase.getReference().child("users");
                    userProfileReference = userProfileReference.child(mUser.getUid()).child("profilePicUrl");
                    userProfileReference.setValue(mCurrentDownloadUrl.toString());
                }

            });

            mProfilePictureImageView = (ImageView) findViewById(R.id.toolbar_profile_iv);
            Picasso.with(this).load(selectedImage).into(mProfilePictureImageView);
        }
    }



    void initCurrentUserProfile() {
        // Get all the Firebase data
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // Get reference to users in database
        DatabaseReference userProfileReference = mFirebaseDatabase.getReference().child("users");
        // Choose your own node in database
        userProfileReference = userProfileReference.child(mUser.getUid());

        // Query for your user data
        Query userProfileQuery = userProfileReference.orderByKey();
        userProfileQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the user data
                mUserProfile = dataSnapshot.getValue(UserProfile.class);
                Log.d("ProfileActivity", "Url: " + mUserProfile.getProfilePicUrl());

                mCollapsingToolbarLayout.setTitle(mUserProfile.getName());
                mAboutTextView.setText(mUserProfile.getAbout());
                mGenderTextView.setText(mUserProfile.getGender());

//                // Set picture url using the url from user info in database
//                mProfilePictureUrl = mUserProfile.getProfilePicUrl();
//                if (mProfilePictureUrl != "" && !mProfilePictureUrl.isEmpty()) {
//                    // Load the picture into a view
//                    Picasso.with(ProfileActivity.this).load(mProfilePictureUrl).into(mProfilePictureImageView);
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ProfileActivity", "Cancelled");
            }
        });

    }


    class SupplyUserDataThread extends Thread {
        public void run() {

            while(!stopSupplyUserData) {

                    do {
                        Log.d("SupplyUserDataThread", "SupplyUserDataThread: Waiting for mUserProfile to load");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {}
                    } while (mUserProfile == null && !stopSupplyUserData);

                    Log.d("SupplyUserDataThread", "SupplyUserDataThread: mUserProfile has loaded");

                    do {
                        mProfilePictureUrl = mUserProfile.getProfilePicUrl();
                        if (mProfilePictureUrl != null && !mProfilePictureUrl.isEmpty()) {
//
                            // Get the profile picture from Url
                            try {
                                InputStream in = new java.net.URL(mProfilePictureUrl).openStream();
                                final Bitmap profilePicture = BitmapFactory.decodeStream(in);

                                // Update the view with a new picture
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProfilePictureImageView.setImageBitmap(profilePicture);
                                    }
                                });


                            } catch (Exception e) {
                                Log.e("ProfileActivity", e.getMessage());
                            }



                            Log.d("SupplyUserDataThread", "SupplyUserDataThread: Loading the image");
                            try {
                                // TODO Increase the sleeping time?
                                Thread.sleep(2000);
                            } catch (InterruptedException ie) {}
                        } else {
                            Log.d("SupplyUserDataThread", "SupplyUserDataThread: Picture Url is empty");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {}
                        }

                    } while (mUserProfile != null && !stopSupplyUserData);

            }
        }
    }

    void setAboutMeListener () {
        final LinearLayout aboutMeLayout = (LinearLayout) findViewById(R.id.about_me_layout);

        aboutMeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup.LayoutParams params = aboutMeLayout.getLayoutParams();

                if (params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    params.height = (int) (56 * getResources().getDisplayMetrics().density); // set height 56dp
                    ((ImageView) findViewById(R.id.about_arrow_iv)).setImageResource(R.drawable.down_ic);
                }
                else {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    ((ImageView) findViewById(R.id.about_arrow_iv)).setImageResource(R.drawable.up_ic);
                }

                aboutMeLayout.setLayoutParams(params);
            }
        });
    }


    public void onProfileChangeClicked(View view) {
        Intent profileChangeIntent = new Intent(ProfileActivity.this, ProfileChangeActivity.class);
        startActivity(profileChangeIntent);
    }

    public void onSignOutButtonClicked(View view) {
        Log.d(TAG, "Clicked the sign out button");
        mAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, InitialActivity.class));
    }

    public void onAddProfilePictureClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent.createChooser(intent, "Complete action choosing..."), RC_PHOTO_PICKER);
    }
}

