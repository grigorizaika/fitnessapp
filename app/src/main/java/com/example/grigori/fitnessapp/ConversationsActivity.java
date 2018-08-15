package com.example.grigori.fitnessapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.grigori.fitnessapp.Profile.ConversationInfo;
import com.example.grigori.fitnessapp.Utils.ConversationsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConversationsActivity extends AppCompatActivity {

    public static final String TAG = ConversationsActivity.class.getSimpleName();

    @BindView(R.id.conversations_lv)
    ListView mConversationsListView;

    ConversationsAdapter mConversationsListAdapter;


    ArrayList<ConversationInfo> mConversationInfos;

    // Lists from which information will be gathered and supplied to mConversationInfos (above)
    ArrayList<String> conversationIds;
    ArrayList<String> otherUserIds;
    ArrayList<Bitmap> profilePictures;
    ArrayList<String> userNames;
    ArrayList<String> lastMessages;
    ArrayList<String> lastMessageDates;

    boolean conversationIdsFilled=false;
    boolean otherUserIdsFilled=false;
    boolean gotMessageData=false;
    boolean gotUserData=false;

    FirebaseUser mUser;
    FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        ButterKnife.bind(this);

        mDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mConversationInfos = new ArrayList<>();
        mConversationsListAdapter = new ConversationsAdapter(this, mConversationInfos);
        mConversationsListView.setAdapter(mConversationsListAdapter);
        mConversationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        getConversationInfoList();
        (new GetLastMessagesAndTheirDatesTask()).execute();
        (new GetUserDataTask()).execute();
        (new FillConversationsDataTask()).execute();

    }

    /**  Fills lists:
     *   ArrayList<String> conversationIds;
     *   ArrayList<String> otherUserIds;
     */
    private void getConversationInfoList() {
        DatabaseReference conversationsReference = mDatabase.getReference().child("conversations");
        conversationIds = new ArrayList<>();
        otherUserIds = new ArrayList<>();

        conversationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot currentConversationSnapshot : dataSnapshot.getChildren()) {
                    // If the user is a participant of a conversation,
                    // Add this conversation's key to the conversations list
                    // and add the name of the other user to the names list
                    if(currentConversationSnapshot.child("participant1").getValue(String.class).equals(mUser.getUid())) {

                        String conversationId = currentConversationSnapshot.getKey();
                        String otherUid = currentConversationSnapshot.child("participant2").getValue(String.class);

                        conversationIds.add(conversationId);
                        otherUserIds.add(otherUid);

                    }
                    else if (currentConversationSnapshot.child("participant2").getValue(String.class).equals(mUser.getUid())) {

                        String conversationId = currentConversationSnapshot.getKey();
                        String otherUid = currentConversationSnapshot.child("participant1").getValue(String.class);

                        conversationIds.add(conversationId);
                        otherUserIds.add(otherUid);
                    }
                }
                conversationIdsFilled=true;
                otherUserIdsFilled=true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class GetLastMessagesAndTheirDatesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                // check if the conversationIds list is filled, only fill with the lastMessages list
                if(conversationIdsFilled) {
                    lastMessages = new ArrayList<>();
                    lastMessageDates = new ArrayList<>();
                    DatabaseReference conversationsReference = mDatabase.getReference().child("conversations");

                    // For all conversations, get their last messages and dates
                    for(int i=0; i<conversationIds.size(); i++) {
                        String conversationKey = conversationIds.get(i);

                        Query lastMessageQuery = conversationsReference
                                .child(conversationKey)
                                .child("messages")
                                .orderByKey()
                                .limitToLast(1);

                        lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot currentMessageSnapshot : dataSnapshot.getChildren()) {
                                    // this for loop should only be executed once as dataSnapshot.getChildrenCount == 1
                                    // because limitToLast(1) is called on lastMessageQuery (above)
                                    String messageText = currentMessageSnapshot.child("text").getValue(String.class);
                                    String messageTextDate = currentMessageSnapshot.child("date").getValue(String.class);
                                    lastMessages.add(messageText);
                                    lastMessageDates.add(messageTextDate);
                                    Log.d(TAG, "Last message: " + messageText);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    gotMessageData=true;
                    break;
                }
            }
            return null;
        }
    }

    public class GetUserDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while(true) {
                if(otherUserIdsFilled) {
                    // profilePictures = new ArrayList<>();
                    userNames = new ArrayList<>();

                    DatabaseReference usersReference = mDatabase.getReference().child("users");

                    for(String currentUid : otherUserIds) {
                        Query userQuery = usersReference.child(currentUid);
                        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String userName = dataSnapshot.child("name").getValue(String.class);
                                userNames.add(userName);
                                Log.d(TAG, "User name: " + userName);
                                // TODO Get profile pictures, too
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                    gotUserData=true;
                    break;
                }
            }

            return null;
        }
    }

    public class FillConversationsDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while(true) {
                Log.d(TAG, "gotUserData: " + gotUserData);
                Log.d(TAG, "gotMessageData: " + gotMessageData);
                if(gotUserData && gotMessageData &&
                        (conversationIds.size()==lastMessages.size() &&
                                conversationIds.size()==userNames.size()
                        )
                        ) {
                    // Fill mConversationInfos with the data from
                    // ArrayList<String> conversationIds;
                    // ArrayList<String> otherUserIds;
                    // ArrayList<String> userNames
                    // ArrayList<String> lastMessages
                    // ArrayList<String> lastMessageDates
                    // ArrayList<Bitmap> profilePictures
                    Log.d(TAG, "conversationIds.size(): " + conversationIds.size());
                    Log.d(TAG, "otherUserIds.size(): " + otherUserIds.size());
                    Log.d(TAG, "userNames.size(): " + userNames.size());
                    Log.d(TAG, "lastMessages.size(): " + lastMessages.size());
                    Log.d(TAG, "lastMessageDates.size(): " + lastMessageDates.size());


                    for(int i = 0; i < conversationIds.size(); i++) {
                        ConversationInfo conversationInfo = new ConversationInfo();

                        conversationInfo.setId(conversationIds.get(i));
                        conversationInfo.setUid(otherUserIds.get(i));
                        conversationInfo.setUserName(userNames.get(i));
                        conversationInfo.setLastMessage(lastMessages.get(i));
                        conversationInfo.setLastMessageDate(lastMessageDates.get(i));

                        mConversationInfos.add(conversationInfo);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mConversationsListAdapter.notifyDataSetChanged();
                        }
                    });

                    break;
                }
            }

            return null;
        }
    }

}

// TODO The conversation data loading is a fng disaster
// TODO Sort the conversations by date of the last message and display the most recent ones
// TODO When a new message is received, show it at once