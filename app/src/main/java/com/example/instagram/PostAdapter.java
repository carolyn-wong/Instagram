package com.example.instagram;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.fragments.PostDetailsFragment;
import com.example.instagram.models.Post;
import com.example.instagram.models.TimeFormatter;
import com.parse.ParseFile;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private List<Post> posts;
    private final String KEY_PROFILE_IMAGE = "profileImage";
    // context defined as global variable so Glide in onBindViewHolder has access
    Context context;

    // pass Post array in constructor
    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    // for each row, inflate layout and cache references into ViewHolder
    // method invoked only when creating a new row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_post, parent, false);
        return new ViewHolder(postView);
    }

    // bind values based on element position
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get data according to position
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // create ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView (R.id.ivProfileImage) public ImageView ivProfileImage;
        @BindView (R.id.tvUsername) public TextView tvUsername;
        @BindView (R.id.ivPostImage) public ImageView ivPostImage;
        @BindView (R.id.tvUsername2) public TextView tvUsername2;
        @BindView (R.id.tvDescription) public TextView tvDescription;
        @BindView (R.id.tvCreatedAt) public TextView tvCreatedAt;
        @BindView (R.id.ivLike) public ImageView ivLike;
        @BindView (R.id.ivComment) public ImageView ivComment;
        @BindView (R.id.ivDirect) public ImageView ivDirect;
        @BindView (R.id.ivSave) public ImageView ivSave;

        // constructor takes in inflated layout
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String postId = posts.get(position).getObjectId();
                    if (ivLike.isSelected()) {
                        ivLike.setSelected(false);
                        MainActivity.removeLike(postId);
                    } else {
                        ivLike.setSelected(true);
                        MainActivity.addLike(postId);
                    }
                }
            });
        }

        public void bind(Post post) {
            // populate views according to data
            tvUsername.setText(post.getUser().getUsername());
            tvUsername2.setText(post.getUser().getUsername());
            tvCreatedAt.setText(TimeFormatter.getTimeDifference(post.getCreatedAt().toString()));
            tvDescription.setText(post.getDescription());
            ParseFile postImage = post.getImage();
            if (postImage != null) {
                Glide.with(context)
                        .load(postImage.getUrl())
                        .into(ivPostImage);
            }
            ParseFile profileImage = post.getUser().getParseFile(KEY_PROFILE_IMAGE);
            if (profileImage != null) {
                Glide.with(context)
                        .load(profileImage.getUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivProfileImage);
            }
            MainActivity.setLikeStatus(ivLike, post);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            // ensure position valid (exists in view)
            if (position != RecyclerView.NO_POSITION) {
                Log.d("PostAdapter", "View Post Details");
                Post post = posts.get(position);

                // send postId to details fragment
                PostDetailsFragment postDetailsFragment = new PostDetailsFragment();
                Bundle postIdBundle = new Bundle();
                postIdBundle.putString("post_id", post.getObjectId());
                postDetailsFragment.setArguments(postIdBundle);

                // add to backstack so can press back button to return to timeline
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                FragmentManager transaction;
                transaction = activity.getSupportFragmentManager();
                transaction.beginTransaction()
                        .replace(R.id.flContainer, postDetailsFragment, "postDetailsFragment")
                        .addToBackStack("postDetailsFragment")
                        .commit();
            }
        }
    }

    // RecyclerView adapter helper methods to clear items from or add items to underlying dataset
    // clean recycler elements
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // add list of posts - change list type depending on item type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }
}
