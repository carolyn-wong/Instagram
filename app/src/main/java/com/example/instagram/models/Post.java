package com.example.instagram.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

// class name must match Parse class
@ParseClassName("Post")
public class Post extends ParseObject {
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_LIKES = "likes";

//    private static final String KEY_MEDIA = "media";

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    // ParseFile - class in SDK that allows accessing files stored with Parse
    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

//    public ParseFile getMedia() {
//        return getParseFile("media");
//    }
//
//    public void setMedia(ParseFile parseFile) {
//        put("media", parseFile);
//    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    // inner class to query post model
    public static class Query extends ParseQuery<Post> {
        //
        public Query() {
            super(Post.class);
        }

        public Query getOlder(Date maxId) {
            whereLessThan("createdAt", maxId);
            return this;
        }

        // get most recent 20 posts
        public Query getTop() {
            setLimit(20);
            orderByDescending(KEY_CREATED_AT);
            // builder pattern, allow chain methods
            return this;
        }

        public Query withUser() {
            include("user");
            return this;
        }
    }

}
