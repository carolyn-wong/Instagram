package com.example.instagram.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import com.example.instagram.R;
import com.example.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

public class UserTimelineFragment extends TimelineFragment {

    private ProgressBar progressBar;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void loadTopPosts(final Date maxId) {
        progressBar.setVisibility(View.VISIBLE);
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop().withUser().whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    if(maxId.equals(new Date(0))) {
                        postAdapter.clear();
                    }
                    for (int i = 0; i < objects.size(); ++i) {
                        mPosts.add(objects.get(i));
                        postAdapter.notifyItemInserted(mPosts.size() - 1);
                        // on successful reload, signal that refresh has completed
                        swipeContainer.setRefreshing(false);
                    }
                } else {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
