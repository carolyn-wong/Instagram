package com.example.instagram.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instagram.R;
import com.example.instagram.TimelinePostAdapter;
import com.example.instagram.models.EndlessRecyclerViewScrollListener;
import com.example.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

public class TimelineFragment extends Fragment {

    // initialize adapter, views, scroll listener
    protected TimelinePostAdapter postAdapter;
    protected ArrayList<Post> mPosts;
    RecyclerView rvPosts;
    protected SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvPosts = (RecyclerView) view.findViewById(R.id.rvPost);

        // initialize data source
        mPosts = new ArrayList<>();
        // construct adapter from data source
        postAdapter = new TimelinePostAdapter(getContext(), mPosts);
        // RecyclerView setup
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);
        rvPosts.setAdapter(postAdapter);

        loadTopPosts();

//        // retain instance so can call "resetStates" for fresh searches
//        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                Long maxTweetId = getMaxId();
//                populateTimeline(maxTweetId);
//            }
//        };
//        // add endless scroll listener to RecyclerView
//        rvPosts.addOnScrollListener(scrollListener);

        // set up refresh listener that triggers new data loading
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTopPosts();
            }
        });
        // configure refreshing colors
        swipeContainer.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));
    }

    protected void loadTopPosts() {
//        final ProgressBar progressBar = view.findViewById(R.id.progressBar);
//        progressBar.setVisibility(View.VISIBLE);
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop().withUser();

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