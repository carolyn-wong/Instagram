package com.example.instagram;

import com.example.instagram.fragments.TimelineFragment;
import com.example.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class ProfileFragment extends TimelineFragment {
    @Override
    protected void loadTopPosts() {
//        final ProgressBar progressBar = view.findViewById(R.id.progressBar);
//        progressBar.setVisibility(View.VISIBLE);
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop().withUser().whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        mPosts.add(objects.get(i));
                        postAdapter.notifyItemInserted(mPosts.size() - 1);
                        // on successful reload, signal that refresh has completed
                        swipeContainer.setRefreshing(false);
                    }
                } else {
                    e.printStackTrace();
                }
//                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
