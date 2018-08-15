package com.example.grigori.fitnessapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.grigori.fitnessapp.Profile.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    public static final String TAG = ChatActivity.class.getSimpleName();

    String mCurrentConversationKey="";

    String mOtherUserUid;

    @BindView(R.id.chat_messages_lv)
    ListView mChatMessagesListView;
    @BindView(R.id.message_text_et)
    EditText mMessageEditText;

    ArrayAdapter<String> mChatListMessagesAdapter;

    ArrayList<ChatMessage>  mMessagesList;
    ArrayList<String> mMessagesTextList;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mOtherUserUid = bundle.getString("uid");

        // Find the current conversation and create a list of messages
        findCurrentConversationKey();

        mMessagesList = new ArrayList<>();
        mMessagesTextList = new ArrayList<>();
        mChatListMessagesAdapter = new ArrayAdapter<String>(ChatActivity.this,
                android.R.layout.simple_list_item_1, mMessagesTextList);
        mChatMessagesListView.setAdapter(mChatListMessagesAdapter);

        new LoadMessagesTask().execute();
    }

    private void findCurrentConversationKey() {
        // Get a reference to conversations in database
        final DatabaseReference conversationsReference = mDatabase.getReference().child("conversations");

        conversationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot foundDialog = dataSnapshot;
                for (DataSnapshot currentConversation : dataSnapshot.getChildren()) {
                    if(     // If both users are the participants of the conversation
                            (currentConversation.child("participant1").getValue(String.class) != null
                                    && currentConversation.child("participant2").getValue(String.class) != null)
                                    && ((currentConversation.child("participant1").getValue(String.class).equals(mCurrentUser.getUid())
                                    && currentConversation.child("participant2").getValue(String.class).equals(mOtherUserUid))
                                    || ((currentConversation.child("participant2").getValue(String.class).equals(mCurrentUser.getUid())
                                    && currentConversation.child("participant1").getValue(String.class).equals(mOtherUserUid)) ))
                            )
                    {
                        mCurrentConversationKey = currentConversation.getKey();
                        break;
                    }
                }
                if (mCurrentConversationKey.equals("")) // If the conversation isn't found above, i.e. doesn't exist yet
                {
                    DatabaseReference newConversationReference = conversationsReference.push();
                    newConversationReference.child("participant1").setValue(mCurrentUser.getUid());
                    newConversationReference.child("participant2").setValue(mOtherUserUid);
                    mCurrentConversationKey = newConversationReference.getKey();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public class LoadMessagesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            while (true) {
                if(!mCurrentConversationKey.equals("")) {
                    initMessagesList();
                    break;
                }
            }
            return null;
        }
    }

    private void initMessagesList() {
        // TODO Get rid of mMessageTextList, ovverride ArrayAdapter and work directly with mMessagesList

        DatabaseReference messagesReference = mDatabase.getReference()
                .child("conversations").child(mCurrentConversationKey).child("messages");
        Log.d(TAG, "mCurrentConversationKey" + mCurrentConversationKey);

        messagesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Messages count: " + dataSnapshot.getChildrenCount());
                mMessagesList.clear();
                mMessagesTextList.clear();
                for(DataSnapshot currentMessageSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Message key " + currentMessageSnapshot.getKey());
                    ChatMessage currentMessage = currentMessageSnapshot.getValue(ChatMessage.class);
                    mMessagesList.add(currentMessage);

                    // Get rid of that here, too
                    String messageAuthor = (currentMessage.getSender()==mAuth.getCurrentUser().getUid())?"You: ":"Pal: ";
                    mMessagesTextList.add(messageAuthor + currentMessage.getText());
                    Log.d(TAG, "Found message: " + currentMessage.getText());
                    mChatListMessagesAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Send the message when send button is clicked
     */
    public void onSendChatMessageClicked(View view) {
        if(!mMessageEditText.getText().toString().isEmpty()) {
            // Get the reference to the current conversation's messages
            DatabaseReference messagesReference = mDatabase.getReference()
                    .child("conversations").child(mCurrentConversationKey).child("messages");
            writeMessageToReference(messagesReference);
        }
    }

    private void writeMessageToReference(DatabaseReference reference) {
        ChatMessage newMessage = new ChatMessage();
        newMessage.setSender(mCurrentUser.getUid());
        newMessage.setReceiver(mOtherUserUid);
        newMessage.setRead(false);
        newMessage.setText(mMessageEditText.getText().toString());
        // TODO Add timestamp to message

        reference.push().setValue(newMessage);

        mMessageEditText.setText("");
    }
}
