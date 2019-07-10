package com.example.instagram.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.CommentAdapter;
import com.example.instagram.MainActivity;
import com.example.instagram.R;
import com.example.instagram.models.Comment;
import com.example.instagram.models.EndlessRecyclerViewScrollListener;
import com.example.instagram.models.Post;
import com.example.instagram.models.TimeFormatter;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.parse.Parse.getApplicationContext;

public class PostDetailsFragment extends Fragment {

    @BindView(R.id.ivProfileImage) public ImageView ivProfileImage;
    @BindView (R.id.tvUsername) public TextView tvUsername;
    @BindView (R.id.ivPostImage) public ImageView ivPostImage;
    @BindView (R.id.tvUsername2) public TextView tvUsername2;
    @BindView (R.id.tvBody) public TextView tvDescription;
    @BindView (R.id.tvCreatedAt) public TextView tvCreatedAt;
    @BindView (R.id.tvNumLikes) public TextView tvNumLikes;
    @BindView (R.id.ivLike) ImageView ivLike;
    @BindView (R.id.ivComment) public ImageView ivComment;
    @BindView (R.id.ivDirect) public ImageView ivDirect;
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
                    displayPost = post;
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
                // edit text
                // allow sending of comments to parse server with user and postId

                final ParseUser user = ParseUser.getCurrentUser();
//                if (photoFile == null || ivPostPreview.getDrawable() == null) {
//                    Log.e(TAG, "No photo to submit");
//                    Toast.makeText(getContext(), "No photo submitted", Toast.LENGTH_SHORT).show();
//                    return;
//                }
            }
        });

        rvComments = (RecyclerView) view.findViewById(R.id.rvComment);

        // initialize data source
        mComments = new ArrayList<>();
        // construct adapter from data source
        commentAdapter = new CommentAdapter(getContext(), mComments);
        // RecyclerView setup
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvComments.setLayoutManager(linearLayoutManager);
        rvComments.setAdapter(commentAdapter);

        loadTopComments(new Date(0));

        // retain instance so can call "resetStates" for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date maxPostId = getMaxDate();
                Log.d("COMMENT DATE", maxPostId.toString());
                loadTopComments(getMaxDate());
            }
        };
        // add endless scroll listener to RecyclerView
        rvComments.addOnScrollListener(scrollListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    protected void loadTopComments(Date maxDate) {
        progressBar.setVisibility(View.VISIBLE);
        final Comment.Query commentsQuery = new Comment.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for posts older than the oldest
        if (maxDate.equals(new Date(0))) {
            commentAdapter.clear();
            commentsQuery.getTop().withUser().whereEqualTo(Comment.KEY_POST, postId);
        } else {
            commentsQuery.getOlder(maxDate).getTop().withUser().whereEqualTo(Comment.KEY_POST, postId);
        }

        commentsQuery.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        mComments.add(objects.get(i));
                        commentAdapter.notifyItemInserted(mComments.size() - 1);
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

    private void createComment(String body, Post post, ParseUser user) {
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
                    Toast.makeText(getContext(), "Comment added", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("PostDetailsActivity", "Error: unable to make comment");
                    e.printStackTrace();
                    return;
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
