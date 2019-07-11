package com.example.instagram.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Comment")
public class Comment extends ParseObject {
    public static final String KEY_BODY = "body";
    public static final String KEY_USER = "user";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_POST = "post";

    public String getBody() { return getString(KEY_BODY); }

    public void setBody(String body) {
        put(KEY_BODY, body);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public ParseUser getPost() {
        return getParseUser(KEY_POST);
    }

    public void setPost(ParseObject post) {
        put(KEY_POST, post);
    }

    // inner class to query comment model
    public static class Query extends ParseQuery<Comment> {
        //
        public Query() {
            super(Comment.class);
        }

        public Comment.Query getOlder(Date maxId) {
            whereLessThan("createdAt", maxId);
            return this;
        }

        // get most recent 20 comments
        public Comment.Query getTop() {
            setLimit(20);
            orderByDescending(KEY_CREATED_AT);
            // builder pattern, allow chain methods
            return this;
        }

        public Comment.Query withUser() {
            include("user");
            return this;
        }
    }
}
