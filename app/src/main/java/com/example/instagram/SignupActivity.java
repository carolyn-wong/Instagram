package com.example.instagram;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {

    private TextView tvUsername;
    private TextView tvPassword;
    private TextView tvEmail;
    private TextView tvHandle;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvPassword = (TextView) findViewById(R.id.tvPassword);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvHandle = (TextView) findViewById(R.id.tvHandle);

        ParseUser user = new ParseUser();

        username = tvUsername.getText().toString();
        password = tvPassword.getText().toString();

        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(tvEmail.getText().toString());
        user.put("handle", tvHandle.getText().toString());

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // let user use app
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            Intent i = new Intent(SignupActivity.this, HomeActivity.class);
                            startActivity(i);
                        }
                    });
                } else {
                    Log.e("SignupActivity", "Signup failure");
                    e.printStackTrace();
                }
            }
        });
    }
}
