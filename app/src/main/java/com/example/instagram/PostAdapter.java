package com.example.instagram;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.models.Post;
import com.example.instagram.models.TimeFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private List<Post> mPosts;
    private Activity mContext;
    // context defined as global variable so Glide in onBindViewHolder has access
    Context context;

    // pass Tweets array in constructor
    public PostAdapter(Activity context, List<Post> posts) {
        mContext = context;
        mPosts = posts;
    }

    // for each row, inflate layout and cache references into ViewHolder

    // method invoked only when creating a new row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        // inflate layout, need to get context first
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View postView = inflater.inflate(R.layout.item_post, parent, false);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }

    // bind values based on element position
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get data according to position
        Post post = mPosts.get(position);

        // populate views according to data
//        holder.tvUsername.setText(post.getUser());
//        holder.tvHandle.setText(post.get);
        holder.tvCreatedAt.setText(TimeFormatter.getTimeDifference(post.getCreatedAt().toString()));
        holder.tvDescription.setText(post.getDescription());

        Glide.with(context)
                .load(post.getImage().getUrl())
                .into(holder.ivPostImage);

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    // create ViewHolder class
    // TODO SET VECTOR DRAWABLES FOR LIKES
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView (R.id.ivProfileImage) public ImageView ivProfileImage;
        @BindView (R.id.tvUsername) public TextView tvUsername;
        @BindView (R.id.ivPostImage) public ImageView ivPostImage;
        @BindView (R.id.tvHandle) public TextView tvHandle;
        @BindView (R.id.tvDescription) public TextView tvDescription;
        @BindView (R.id.tvCreatedAt) public TextView tvCreatedAt;
        @BindView (R.id.ivLike) ImageView ivLike;
        @BindView (R.id.ivComment) public ImageView ivComment;
        @BindView (R.id.ivDirect) public ImageView ivDirect;
        @BindView (R.id.ivSave) public ImageView ivSave;

        // constructor takes in inflated layout
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
//            ivLike.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            // ensure position valid (exists in view)
            if (position != RecyclerView.NO_POSITION) {
                Log.d("PostAdapter", "View Post Details");
                Post post = mPosts.get(position);
                Intent intent = new Intent(context, PostDetails.class);
                intent.putExtra("post_id", post.getObjectId());
                context.startActivity(intent);
            }
        }

        @OnClick(R.id.ivLike)
        public void onClickLike() {

        }

    }

    // RecyclerView adapter helper methods to clear items from or add items to underlying dataset
    // clean recycler elements
    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    // add list of posts - change list type depending on item type used
    public void addAll(List<Post> list) {
        mPosts.addAll(list);
        notifyDataSetChanged();
    }
}
