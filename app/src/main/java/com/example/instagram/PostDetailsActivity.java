package com.example.instagram;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.models.Post;
import com.example.instagram.models.TimeFormatter;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostDetailsActivity extends AppCompatActivity {

    @BindView (R.id.ivProfileImage) public ImageView ivProfileImage;
    @BindView (R.id.tvUsername) public TextView tvUsername;
    @BindView (R.id.ivPostImage) public ImageView ivPostImage;
    @BindView (R.id.tvUsername2) public TextView tvUsername2;
    @BindView (R.id.tvDescription) public TextView tvDescription;
    @BindView (R.id.tvCreatedAt) public TextView tvCreatedAt;
    @BindView (R.id.ivLike) ImageView ivLike;
    @BindView (R.id.ivComment) public ImageView ivComment;
    @BindView (R.id.ivDirect) public ImageView ivDirect;
    @BindView (R.id.ivSave) public ImageView ivSave;
    @BindView (R.id.progressBar) public ProgressBar progressBar;
    private final String KEY_PROFILE_IMAGE = "profileImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        ButterKnife.bind(this);
        progressBar.setVisibility(View.VISIBLE);

        final String postId = getIntent().getStringExtra("post_id");
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // try to find item from cache, otherwise go to network
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
        // query for post with user
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
                }
                else {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

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
}
