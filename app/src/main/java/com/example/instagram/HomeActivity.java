package com.example.instagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.instagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String imagePath="./src/androidTest/ic_launcher.png";
    private EditText etDescription;
    private Button btCreate;
    private Button btRefresh;
    private Button btCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        etDescription = findViewById(R.id.etDescription);
        btCreate = findViewById(R.id.btCreate);
        btRefresh = findViewById(R.id.btRefresh);
        btCamera = findViewById(R.id.btCamera);

        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String description = etDescription.getText().toString();
                final ParseUser user = ParseUser.getCurrentUser();

                final File file = new File(imagePath);
                final ParseFile parseFile = new ParseFile(file);

                createPost(description, parseFile, user);
            }
        });

        btRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTopPosts();
            }
        });

        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCamera(v);
            }
        });
    }

    private void createPost(String description, ParseFile imageFile, ParseUser user) {
        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setImage(imageFile);
        newPost.setUser(user);

        newPost.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("HomeActivity", "Create post successful");
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadTopPosts() {
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop().withUser();

        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        Log.d("HomeActivity", "Post[" + i + "] = "
                                + objects.get(i).getDescription()
                                + "\nusername = " + objects.get(i).getUser().getUsername());
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public final String APP_TAG = "Instagram";
    public final static int CAPTURE_IMAGE_REQUEST_CODE = 1;
    public String photoFileName = "photo.jpg";
    File photoFile;

    public void onLaunchCamera(View view) {
        // create intent to take pic and return control to calling app
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // create File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider, required for API >= 24
        Uri fileProvider = FileProvider.getUriForFile(HomeActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // calling startActivityForResult() using intent that can't be handled by any app causes crash
        // as long as result non-null, safe to use intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            // start image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    // returns File for photo stored on disk given fileName
    public File getPhotoFileUri(String fileName) {
        // get safe storage directory for photos
        // 'getExternalFileDir' on Context to access package-specific directories
        // so don't need to request external read/write runtime permissions
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // create storage directory if doesn't exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "Failed to create directory");
        }

        // Return file target for photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    // called when camera app finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                File takenPhotoUri = getPhotoFileUri(photoFileName);
                // camera photo on disk
                Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                // Get height or width of screen at runtime
                int screenWidth = DeviceDimensionsHelper.getDisplayWidth(this);
                // Resize a Bitmap maintaining aspect ratio based on screen width
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, screenWidth);
                // load image into a preview
                ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                ivPreview.setImageBitmap(resizedBitmap);

                // write smaller bitmap back to disk
                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                try {
                    resizedFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(resizedFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // Write the bytes of the bitmap to file
                try {
                    fos.write(bytes.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Previous code for full size bitmap here:
                // camera photo already on disk
                // Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            }
            // result failed
            else {
                Toast.makeText(this, "picture not taken", Toast.LENGTH_SHORT).show();
            }
        }
    }
}