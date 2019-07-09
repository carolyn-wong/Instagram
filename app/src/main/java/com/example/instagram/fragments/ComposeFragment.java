package com.example.instagram.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.instagram.models.BitmapScaler;
import com.example.instagram.models.DeviceDimensionsHelper;
import com.example.instagram.R;
import com.example.instagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class ComposeFragment extends Fragment {

    private final String TAG = "ComposeFragment";
    public final static int CAPTURE_IMAGE_REQUEST_CODE = 1;
    public String photoFileName = "photo.jpg";
    private File photoFile;

    @BindView (R.id.etDescription) EditText etDescription;
    @BindView (R.id.btSetImage) Button btPost;
    @BindView (R.id.btRefresh) Button btRefresh;
    @BindView (R.id.btCamera) Button btCamera;
    @BindView (R.id.progressBar) ProgressBar progressBar;
    @BindView (R.id.ivPostPreview) ImageView ivPostPreview;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compose, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera(v);
            }
        });

        btPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String description = etDescription.getText().toString();
                final ParseUser user = ParseUser.getCurrentUser();
                if (photoFile == null || ivPostPreview.getDrawable() == null) {
                    Log.e(TAG, "No photo to submit");
                    Toast.makeText(getContext(), "No photo submitted", Toast.LENGTH_SHORT).show();
                    return;
                }
                createPost(description, photoFile, user);
            }
        });
    }

    public void launchCamera(View view) {
        // create intent to take pic and return control to calling app
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // create File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider, required for API >= 24
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // calling startActivityForResult() using intent that can't be handled by any app causes crash
        // as long as result non-null, safe to use intent
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // start image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    // returns File for photo stored on disk given fileName
    public File getPhotoFileUri(String fileName) {
        // get safe storage directory for photos
        // 'getExternalFileDir' on Context to access package-specific directories
        // so don't need to request external read/write runtime permissions
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // create storage directory if doesn't exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory");
        }

        // Return file target for photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    // called when camera app finishes
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                File takenPhotoUri = getPhotoFileUri(photoFileName);
                // camera photo on disk
                Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                // Get height or width of screen at runtime
                int screenWidth = DeviceDimensionsHelper.getDisplayWidth(getContext());
                // Resize a Bitmap maintaining aspect ratio based on screen width
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, screenWidth);
                // load image into a preview
                ivPostPreview.setImageBitmap(resizedBitmap);

                // TODO fix image rotation
                // TODO fix saving smaller bitmap to disk
//                // write smaller bitmap back to disk
//                // Configure byte output stream
//                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                // Compress the image further
//                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
//                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
//                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
//                try {
//                    resizedFile.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                FileOutputStream fos = null;
//                try {
//                    fos = new FileOutputStream(resizedFile);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                // Write the bytes of the bitmap to file
//                try {
//                    fos.write(bytes.toByteArray());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
            // result failed
            else {
                Toast.makeText(getContext(), "Picture not taken", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createPost(String description, File photoFile, ParseUser user) {
        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setImage(new ParseFile(photoFile));
        newPost.setUser(user);
        progressBar.setVisibility(View.VISIBLE);

        newPost.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("MainActivity", "Create post successful");
                    etDescription.setText("");
                    ivPostPreview.setImageResource(0);
                } else {
                    Log.d("MainActivity", "Error while saving");
                    e.printStackTrace();
                    return;
                }
                progressBar.setVisibility(View.INVISIBLE);

            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
