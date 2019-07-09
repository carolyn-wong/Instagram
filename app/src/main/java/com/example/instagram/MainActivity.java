package com.example.instagram;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.instagram.fragments.ComposeFragment;
import com.example.instagram.fragments.TimelineFragment;
import com.example.instagram.fragments.UserTimelineFragment;
import com.example.instagram.models.Post;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView (R.id.bottom_navigation) BottomNavigationView bottomNavigationView;

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
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.profile_photo:
                Intent profileIntent = new Intent(MainActivity.this, ProfileSettingsActivity.class);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            gotoLoginActivity();
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_compose:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new UserTimelineFragment();
                        break;
                    case R.id.action_home:
                    default:
                        fragment = new TimelineFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }


    private void gotoLoginActivity() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void logoutUser() {
        ParseUser.logOut();
    }

    public static void addLike(String postId) {
        final Post.Query query = new Post.Query();
        query.getInBackground(postId, new GetCallback<Post>() {
            public void done(Post post, ParseException e) {
                if (e == null) {
                    post.increment("likes");
                    post.saveInBackground();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void removeLike(String postId) {
        final Post.Query query = new Post.Query();
        query.getInBackground(postId, new GetCallback<Post>() {
            public void done(Post post, ParseException e) {
                if (e == null) {
                    post.increment("likes", -1);
                    post.saveInBackground();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}
