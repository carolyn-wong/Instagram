package com.example.instagram.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.MainActivity;
import com.example.instagram.R;
import com.example.instagram.models.Post;
import com.example.instagram.models.TimeFormatter;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.parse.Parse.getApplicationContext;

public class PostDetailsFragment extends Fragment {

    @BindView(R.id.ivProfileImage) public ImageView ivProfileImage;
    @BindView (R.id.tvUsername) public TextView tvUsername;
    @BindView (R.id.ivPostImage) public ImageView ivPostImage;
    @BindView (R.id.tvUsername2) public TextView tvUsername2;
    @BindView (R.id.tvDescription) public TextView tvDescription;
    @BindView (R.id.tvCreatedAt) public TextView tvCreatedAt;
    @BindView (R.id.tvNumLikes) public TextView tvNumLikes;
    @BindView (R.id.ivLike) ImageView ivLike;
    @BindView (R.id.ivComment) public ImageView ivComment;
    @BindView (R.id.ivDirect) public ImageView ivDirect;
    @BindView (R.id.ivSave) public ImageView ivSave;
    @BindView (R.id.progressBar) public ProgressBar progressBar;
    private final String KEY_PROFILE_IMAGE = "profileImage";
    private String postId;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // get postId passed in by adapter
        postId = getArguments().getString("post_id");
        View view = inflater.inflate(R.layout.fragment_post_details, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // try to find item from cache, otherwise go to network
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
        // query for post and include user info
        final Post.Query postQuery = new Post.Query();
        postQuery.withUser().getInBackground(postId, new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                if (e == null) {
                    tvUsername.setText(post.getUser().getUsername());
                    tvUsername2.setText(post.getUser().getUsername());
                    tvDescription.setText(post.getDescription());
                    tvCreatedAt.setText(TimeFormatter.getTimeDifference(post.getCreatedAt().toString()));
                    Glide.with(getApplicationContext())
                            .load(post.getImage().getUrl())
                            .into(ivPostImage);
                    Glide.with(getApplicationContext())
                            .load(post.getUser().getParseFile(KEY_PROFILE_IMAGE).getUrl())
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivProfileImage);
                    MainActivity.setLikeStatus(ivLike, post);
                    MainActivity.getNumLikes(tvNumLikes, post);
                }
                else {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        // TODO set so that "likes" automatically changes when clicking on like button
        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivLike.isSelected()) {
                    ivLike.setSelected(false);
                    MainActivity.removeLike(postId);
                } else {
                    ivLike.setSelected(true);
                    MainActivity.addLike(postId);
                }
            }
        });

        ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
