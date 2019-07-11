package com.example.instagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.instagram.models.BitmapScaler;
import com.example.instagram.models.DeviceDimensionsHelper;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileSettingsActivity extends AppCompatActivity {

    @BindView(R.id.btSetImage) Button btSetImage;
    @BindView(R.id.btCamera) Button btCamera;
    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    public static final String KEY_PROFILE = "profileImage";

    private final String TAG = "ProfileSettingsActivity";
    public final static int CAPTURE_IMAGE_REQUEST_CODE = 1;
    public String photoFileName = "photo.jpg";
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        ButterKnife.bind(this);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            gotoLoginActivity();
        }

        // TODO make snackbar so user can submit blank photo if they don't want to set a profile photo
        btSetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ParseUser user = ParseUser.getCurrentUser();
                if (photoFile == null || ivProfileImage.getDrawable() == null) {
                    Log.e(TAG, "No photo to submit");
                    Toast.makeText(ProfileSettingsActivity.this, "No photo submitted", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveProfilePhoto(photoFile, user);
            }
        });


        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera(v);
            }
        });
    }

    private void saveProfilePhoto(File photoFile, ParseUser user) {
        user.put(KEY_PROFILE, new ParseFile(photoFile));
        progressBar.setVisibility(View.VISIBLE);

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("ProfileSettingsActivity", "Profile photo change successful");
                    Toast.makeText(ProfileSettingsActivity.this, "Photo successfully changed!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("ProfileSettingsActivity", "Error while saving");
                    e.printStackTrace();
                    return;
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void launchCamera (View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(ProfileSettingsActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // calling startActivityForResult() using intent that can't be handled by any app causes crash
        // as long as result non-null, safe to use intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            // start image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    // returns File for photo stored on disk given fileName
    public File getPhotoFileUri (String fileName){
        // get safe storage directory for photos
        // 'getExternalFileDir' on Context to access package-specific directories
        // so don't need to request external read/write runtime permissions
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

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
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
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
                ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
                ivProfileImage.setImageBitmap(resizedBitmap);

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
                Toast.makeText(this, "Picture not taken", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void gotoLoginActivity() {
        Intent i = new Intent(ProfileSettingsActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}


