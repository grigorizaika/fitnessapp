package com.example.grigori.fitnessapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.grigori.fitnessapp.Profile.UserProfile;
import com.firebase.ui.auth.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OtherProfileActivity extends AppCompatActivity {

    public static final String TAG = OtherProfileActivity.class.getSimpleName();

    // The uid of the user whose profile is displayed
    String mUid;
    UserProfile mUserProfile;

    @BindView(R.id.other_user_profile_pic_iv)
    ImageView mProfilePicImageView;
    @BindView(R.id.other_user_name_tv)
    TextView mUserNameTextView;
    @BindView(R.id.other_user_about_tv)
    TextView mUserAboutTextView;
    @BindView(R.id.other_user_posts_rv)
    RecyclerView mUserPostsRecyclerView;

    FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        ButterKnife.bind(this);

        mDatabase = FirebaseDatabase.getInstance();

        getUidFromIntent();
        initUserProfile();

    }

    private void getUidFromIntent() {
        // Get the uid from the intent that opened this activity
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mUid = (String) bundle.get("uid");
        Log.d(TAG, "Chosen UID: " + mUid);
    }

    /**
     * Gets the user profile with mUid from the database
     * Fills the views with user information
     */
    private void initUserProfile() {

        DatabaseReference usersReference = mDatabase.getReference().child("users");

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Find the user with mUid in the database
                for(DataSnapshot currentUserNode : dataSnapshot.getChildren()) {
                    if(currentUserNode.getKey().equals(mUid)) {
                        mUserProfile = currentUserNode.getValue(UserProfile.class);
                        break;
                    }
                }

                // Fill the views with user information
                mUserNameTextView.setText(mUserProfile.getName());
                mUserAboutTextView.setText(mUserProfile.getAbout());

                // Fill the image view with a user picture
                try {
                    String profilePicUrl = mUserProfile.getProfilePicUrl();
                    URL imageUrl = new URL(profilePicUrl);
                    new LoadImageFromUrlTask().execute(imageUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onSendMessageClicked(View view) {
        Intent openChatIntent = new Intent(OtherProfileActivity.this, ChatActivity.class);
        openChatIntent.putExtra("uid", mUid);
        startActivity(openChatIntent);
    }

    /**
     * Task that loads image from the url given in user profile
     */
    public class LoadImageFromUrlTask extends AsyncTask<URL, Void, Integer> {
        @Override
        protected Integer doInBackground(URL... urls) {
            URL imageUrl = urls[0];
            try {
                InputStream in = imageUrl.openStream();
                final Bitmap profilePicture = BitmapFactory.decodeStream(in);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProfilePicImageView.setImageBitmap(profilePicture);
                    }
                });
            } catch(Exception e) {
                String errorMessage = (e.getMessage() == null) ? "Image load failed" : e.getMessage();
                Log.e(TAG, errorMessage);
                e.printStackTrace();
            }
            return null;
        }
    }

}
