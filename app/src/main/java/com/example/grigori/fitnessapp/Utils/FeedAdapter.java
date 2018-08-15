package com.example.grigori.fitnessapp.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grigori.fitnessapp.Profile.Post;
import com.example.grigori.fitnessapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private int mNumberItems;
    final private ListItemClickListener mItemClickListener;
    private ArrayList<Post> mPostsList;
    Context mContext;

    public FeedAdapter (Context context, int numberOfItems, ArrayList<Post> posts, ListItemClickListener listener) {
        mNumberItems = numberOfItems;
        mItemClickListener = listener;
        mPostsList = posts;
        mContext = context;
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.feed_list_item;

        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        FeedViewHolder viewHolder = new FeedViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FeedViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mPostsList.size();
    }

    class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.profile_pic_iv)
            ImageView profileImageView;
            @BindView(R.id.profile_name_tv)
            TextView nameTextView;
            @BindView(R.id.post_content_tv)
            TextView postContentTextView;
            @BindView(R.id.post_date_tv)
            TextView dateTextView;
            @BindView(R.id.post_photo_iv)
            ImageView postPhotoImageView;
            @BindView(R.id.more_button)
            ImageButton moreButton;

            public FeedViewHolder (View itemView) {
                super(itemView);
            //    ButterKnife.bind(itemView);

                nameTextView = (TextView) itemView.findViewById(R.id.profile_name_tv);
                postContentTextView = (TextView) itemView.findViewById(R.id.post_content_tv);
                dateTextView = (TextView) itemView.findViewById(R.id.post_date_tv);
                postPhotoImageView = (ImageView) itemView.findViewById(R.id.post_photo_iv);
                moreButton = (ImageButton) itemView.findViewById(R.id.more_button);
                itemView.setOnClickListener(this);
        }

        /**
         * 1. Binds the data from posts in database to recyclerview views
         * 2. The removal of posts is handled here in remove button onClickListener
         * which is placed in popupmenu
         * @param position
         */
        private void bind(final int position) {
            Log.d("FeedAdapter", mPostsList.get(position).getUsername());
            // Set the user information to a post
            nameTextView.setText(mPostsList.get(position).getUsername());
            postContentTextView.setText(mPostsList.get(position).getStatusText());
            dateTextView.setText(mPostsList.get(position).getDateString());

            // Set a photo to a post
            String photoUri = new String();
            photoUri = mPostsList.get(position).getPhotoUri();
            if((photoUri != null) && !photoUri.isEmpty())
            Picasso.with(mContext).load(photoUri).into(postPhotoImageView);


            // Get current user id and compare it to the post user's id
            final String currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Hide the options button if the post doesn't belong to current user
            //if (currentUID != mPostsList.get(position).getUserID()) {
             //   moreButton.setVisibility(View.INVISIBLE);
            //}


            final String postId = mPostsList.get(position).getUniqueID();

            // Create a popup menu on more button click
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(view.getContext(), moreButton);
                    popupMenu.inflate(R.menu.feed_options);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {

                                // Handling post removal
                                case R.id.action_remove_post:
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("posts");
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            // go through all posts and display post ids
                                            dataSnapshot = dataSnapshot.child(currentUID);
                                            for (DataSnapshot currentSnapshot : dataSnapshot.getChildren()) {
                                                if (currentSnapshot.getKey().equals(postId)) {
                                                    currentSnapshot.getRef().removeValue();

                                                    mPostsList.remove(position);
                                                    FeedAdapter.this.notifyDataSetChanged();

                                                    Toast.makeText(mContext, "Post has been deleted", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }

            });


        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mItemClickListener.onListItemClick(clickedPosition);
        }
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }


}
