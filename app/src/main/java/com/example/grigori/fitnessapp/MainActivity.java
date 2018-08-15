package com.example.grigori.fitnessapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.grigori.fitnessapp.Profile.Post;
import com.example.grigori.fitnessapp.Profile.UserProfile;
import com.example.grigori.fitnessapp.Utils.CustomExpandableListAdapter;
import com.example.grigori.fitnessapp.Utils.FeedAdapter;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;

import static java.util.Calendar.getInstance;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String FRAGMENT_NUTRITION = "FRAGMENT_NUTRITION";
    public static final String FRAGMENT_EXERCISE = "FRAGMENT_EXERCISES";
    public static final String FRAGMENT_DISCOVER = "FRAGMENT_DISCOVER";
    public static final String FRAGMENT_FEED = "FRAGMENT_FEED";

    private static final String ANONYMOUS = "anonymous";

    public static final int RC_SIGN_IN = 1;
    public static final int RC_PHOTO_PICKER = 2;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mStorage;
    ChildEventListener mChildEventListener;

    Uri mCurrentDownloadUrl = Uri.EMPTY;

    String mUsername;

    @BindView(R.id.navigation_main)
    BottomNavigationView mBottomNavigationView;

  //  @BindView(R.id.status_update_et)
    EditText mStatusUpdateEditText;

    public String[] mMealNameList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mStorage = FirebaseStorage.getInstance();

        completeAuthorization();

        bottomNavigationSetUp();





    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // updateUI(currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            // Get the chosen photo and add it to firebase storage
            StorageReference storageReference = mStorage.getReference()
                    .child("post_photos");
            Uri selectedImageUri = data.getData();

            ImageView pickedPhotoIV = (ImageView)findViewById(R.id.picked_photo_iv);
            Picasso.with(this).load(selectedImageUri).into(pickedPhotoIV);

            StorageReference photoRef = storageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mCurrentDownloadUrl = taskSnapshot.getDownloadUrl();


                }
            });
        }
    }

    // Adds onNavigationItemSelectedListener and sets up the initial fragment (NutritionFragment)
    private void bottomNavigationSetUp () {

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_nutrition:
                                NutritionFragment newInstance = NutritionFragment.newInstance();
                                replaceFragment(newInstance, FRAGMENT_NUTRITION);
                                return true;
                            case R.id.action_exercises:
                                replaceFragment(ExerciseFragment.newInstance(), FRAGMENT_EXERCISE);
                                return true;
                            case R.id.action_discover:
                                replaceFragment(DiscoverFragment.newInstance(), FRAGMENT_DISCOVER);
                                return true;
                            case R.id.action_feed:
                                replaceFragment(FeedFragment.newInstance(), FRAGMENT_FEED);
                                return true;
                        }

                        return false;
                    }
                }


        );

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FeedFragment newInstance = FeedFragment.newInstance();
        fragmentTransaction.add(R.id.fragment_container, newInstance, FRAGMENT_NUTRITION)
                .commit();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }


    private void replaceFragment (Fragment newFragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, newFragment, tag).commit();
    }



    private void completeAuthorization() {
        // TODO Add signing out
        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // Signed in
                    onSignedInInitialize(firebaseUser.getDisplayName());

                    checkAndAddUserIfNew(firebaseUser);

                } else {
                    // Signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);

                }
            }
        };
    }

    private void createUser (FirebaseAuth auth, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }

                    }
                });
    }

    private void onSignedInInitialize(String username) {
        mUsername = username;

    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
    }

    /*
    * If the user is new and has signed in with provider,
    * he should be added to the database as a new user
    * */
    private void checkAndAddUserIfNew(final FirebaseUser firebaseUser) {
        // Get the id to search for in database
        final String userId = firebaseUser.getUid();

        // Get the reference to the users node in database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean matchFound = false;
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "childKey:" + childDataSnapshot.getKey());
                    if (userId.equals(childDataSnapshot.getKey())) {
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound) {
                    String email = firebaseUser.getEmail();
                    String name = firebaseUser.getDisplayName();
                    String uid = firebaseUser.getUid();

                    UserProfile userProfile = new UserProfile();
                    userProfile.setName(name);
                    userProfile.setEmail(email);
                    userProfile.setAbout("");
                    userProfile.setUid(uid);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference().child("users");
                    // TODO Add isSuccessful check
                    reference.child(userId).setValue(userProfile);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void onProfilePictureClicked(View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent, null);
    }

    public void onPickDateClicked(View view) {
        // Open the datepicker dialog fragment
        // Replace the button text with a picked date
    }

    /**
     * Creates a new post and pushes it to the database
     * @param view
     *
     */
    public void onPostClicked(View view) {
        mStatusUpdateEditText = (EditText) findViewById(R.id.status_update_et);
        Calendar currentCalendar = Calendar.getInstance();

        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");


        // Prepare a post
        // TODO Get the actual name by accesseng child("users").child(uid)
        String username = mAuth.getCurrentUser().getDisplayName();
        String userID = mAuth.getCurrentUser().getUid();
        String uniqueID = "";
        String statusText = mStatusUpdateEditText.getText().toString();
        String dateString = sdf.format(date) +
                " " + currentCalendar.get(Calendar.DAY_OF_MONTH) +
                "." + currentCalendar.get(Calendar.MONTH) +
                "." + currentCalendar.get(Calendar.YEAR);
        long dateMillis = currentCalendar.getTime().getTime();
        Post newPost = new Post(username, userID, uniqueID, statusText, dateString, dateMillis);

        if (mCurrentDownloadUrl != Uri.EMPTY) {
            newPost.setPhotoUri(mCurrentDownloadUrl.toString());

            mCurrentDownloadUrl = Uri.EMPTY;
        }

        // Send post to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("posts");
        // TODO Add isSuccessful check

        // create a new node with unique key (pushID)
        reference = reference.child(mAuth.getCurrentUser().getUid()).push();
        // get that unique key
        String pushID = reference.getKey();
        // set it as a post's unique ID
        newPost.setUniqueID(pushID);
        // set the post to that new unique node
        reference.setValue(newPost);



        // Cleanup
        mStatusUpdateEditText.setText("");
        Toast.makeText(this, "Message posted", Toast.LENGTH_SHORT).show();

        Uri nullUri = Uri.EMPTY;
        ImageView pickedPhotoIV = (ImageView)findViewById(R.id.picked_photo_iv);
        Picasso.with(this).load(nullUri).into(pickedPhotoIV);

        // Hide the keyboard
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void onPickPhotoClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent.createChooser(intent, "Complete action choosing..."), RC_PHOTO_PICKER);
    }

    public void onSearchUsersClicked(View view) {
        Intent searchUsersIntent = new Intent(MainActivity.this, SearchUsersActivity.class);
        startActivity(searchUsersIntent);
    }

    public void onConversationsClicked(View view) {
        Intent openConversationsIntent = new Intent(MainActivity.this, ConversationsActivity.class);
        startActivity(openConversationsIntent);
    }
}
