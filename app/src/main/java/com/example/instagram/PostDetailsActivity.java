package com.example.instagram;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.models.Comment;
import com.example.instagram.models.EndlessRecyclerViewScrollListener;
import com.example.instagram.models.Post;
import com.example.instagram.models.TimeFormatter;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostDetailsActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(PostDetailsActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.profile_photo:
                Intent profileIntent = new Intent(PostDetailsActivity.this, ProfileSettingsActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.logout:
                logoutUser();
                gotoLoginActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @BindView (R.id.ivProfileImage) public ImageView ivProfileImage;
    @BindView (R.id.tvUsername) public TextView tvUsername;
    @BindView (R.id.ivPostImage) public ImageView ivPostImage;
    @BindView (R.id.tvUsername2) public TextView tvUsername2;
    @BindView (R.id.tvBody) public TextView tvDescription;
    @BindView (R.id.tvCreatedAt) public TextView tvCreatedAt;
    @BindView (R.id.tvNumLikes) public TextView tvNumLikes;
    @BindView (R.id.ivLike) ImageView ivLike;
    @BindView (R.id.ivComment) public ImageView ivComment;
    @BindView (R.id.ivSave) public ImageView ivSave;
    @BindView (R.id.progressBar) public ProgressBar progressBar;

    private final String KEY_PROFILE_IMAGE = "profileImage";
    private String postId;
    Post displayPost;
    // initialize adapter, views, scroll listener
    protected CommentAdapter commentAdapter;
    protected ArrayList<Comment> mComments;
    RecyclerView rvComments;
    private EndlessRecyclerViewScrollListener scrollListener;
    View.OnClickListener userClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        ButterKnife.bind(this);
        progressBar.setVisibility(View.VISIBLE);

        postId = getIntent().getStringExtra("post_id");

        rvComments = (RecyclerView) findViewById(R.id.rvComment);
        // initialize data source
        mComments = new ArrayList<>();
        // construct adapter from data source
        commentAdapter = new CommentAdapter(this, mComments);
        // RecyclerView setup
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(linearLayoutManager);
        rvComments.setAdapter(commentAdapter);

        // retain instance so can call "resetStates" for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date maxCommentId = getMaxDate();
                Log.d("COMMENT DATE", maxCommentId.toString());
                final Post.Query postQuery = new Post.Query();
                postQuery.getInBackground(postId, new GetCallback<Post>() {
                    @Override
                    public void done(Post post, ParseException e) {
                        if (e == null) {
                            loadTopComments(getMaxDate(), post);
                        }
                        else {
                            e.printStackTrace();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };
        // add endless scroll listener to RecyclerView
        rvComments.addOnScrollListener(scrollListener);

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // try to find item from cache, otherwise go to network
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
        // query for post and include user info
        final Post.Query postQuery = new Post.Query();
        postQuery.withUser().getInBackground(postId, new GetCallback<Post>() {
            @Override
            public void done(final Post post, ParseException e) {
                if (e == null) {
                    displayPost = post;
                    tvUsername.setText(post.getUser().getUsername());
                    tvUsername2.setText(post.getUser().getUsername());
                    tvDescription.setText(post.getDescription());
                    tvCreatedAt.setText(TimeFormatter.getTimeDifference(post.getCreatedAt().toString()));
                    Glide.with(getApplicationContext())
                            .load(post.getImage().getUrl())
                            .into(ivPostImage);
                    ParseFile profileImage = post.getUser().getParseFile(KEY_PROFILE_IMAGE);
                    if (profileImage != null) {
                        Glide.with(getApplicationContext())
                                .load(profileImage.getUrl())
                                .apply(RequestOptions.circleCropTransform())
                                .into(ivProfileImage);
                    }
                    MainActivity.setLikeStatus(ivLike, post);
                    MainActivity.getNumLikes(tvNumLikes, post);
                    loadTopComments(new Date(0), post);
                    Log.d("PostDetailsActivity", "FIRST LOAD");
                    userClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ParseUser user = post.getUser();
                            Intent intent = new Intent(PostDetailsActivity.this, UserTimelineActivity.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                        }
                    };
                    tvUsername.setOnClickListener(userClickListener);
                    tvUsername2.setOnClickListener(userClickListener);
                    ivProfileImage.setOnClickListener(userClickListener);
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
                    MainActivity.removeLike(tvNumLikes, postId);
                } else {
                    ivLike.setSelected(true);
                    MainActivity.addLike(tvNumLikes, postId);
                }
            }
        });

        ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog(PostDetailsActivity.this);
            }
        });
    }

    protected void loadTopComments(Date maxDate, Post post) {
        Log.d("PostDetailsActivity", "load top comments");
        progressBar.setVisibility(View.VISIBLE);
        final Comment.Query commentsQuery = new Comment.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for posts older than the oldest
        if (maxDate.equals(new Date(0))) {
            Log.d("PostDetailsActivity", "NEW DATE IS 0");
            Log.d("PostDetailsActivity", postId);
            commentAdapter.clear();
            commentsQuery.getTop().withUser().whereEqualTo(Comment.KEY_POST, post);
        } else {
            commentsQuery.getOlder(maxDate).getTop().withUser().whereEqualTo(Comment.KEY_POST, post);
        }

        commentsQuery.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> objects, ParseException e) {
                if (e == null) {
                    Log.d("PostDetailsActivity", Integer.toString(objects.size()));
                    for (int i = 0; i < objects.size(); ++i) {
                        mComments.add(objects.get(i));
                        commentAdapter.notifyItemInserted(mComments.size() - 1);
                        Log.d("PostDetailsActivity", "comment added");
                    }
                } else {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // get date of oldest post
    protected Date getMaxDate() {
        int postsSize = mComments.size();
        if(postsSize == 0) {
            return(new Date(0));
        }
        else {
            Comment oldest = mComments.get(mComments.size() - 1);
            return oldest.getCreatedAt();
        }
    }

    private void createComment(String body, final Post post, ParseUser user) {
        final Comment newComment = new Comment();
        newComment.setBody(body);
        newComment.setPost(post);
        newComment.setUser(user);
        progressBar.setVisibility(View.VISIBLE);

        newComment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PostDetailsActivity", "Create comment successful");
                    loadTopComments(new Date(0), post);
                } else {
                    Log.d("PostDetailsActivity", "Error: unable to make comment");
                    e.printStackTrace();
                    return;
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // Source: https://alvinalexander.com/source-code/android-mockup-prototype-dialog-text-field
    private void showCommentDialog(Context context) {
        final EditText etComment = new EditText(context);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Write a comment...")
                .setView(etComment)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(etComment.getText());
                        onFinishCommentDialog(task);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void onFinishCommentDialog(final String inputText) {
        final Post.Query postQuery = new Post.Query();
        postQuery.getInBackground(postId, new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                if (e == null) {
                    createComment(inputText, post, ParseUser.getCurrentUser());
                }
                else {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void logoutUser() {
        ParseUser.logOut();
    }

    private void gotoLoginActivity() {
        Intent i = new Intent(PostDetailsActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
