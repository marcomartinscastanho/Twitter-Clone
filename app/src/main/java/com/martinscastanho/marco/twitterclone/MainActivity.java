package com.martinscastanho.marco.twitterclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    protected EditText usernameEditText;
    protected EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // this is to hide the keyboard when you click outside of it
        ConstraintLayout backgroundLayout = findViewById(R.id.backgroundLayout);
        ImageView logoImageView = findViewById(R.id.logoImageView);
        backgroundLayout.setOnClickListener(this);
        logoImageView.setOnClickListener(this);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        if(isUserLoggedIn()){
            showUserList();
        }

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    public void confirmButtonPressed(View view){
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if(username.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "A Username and a Password are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        login(username, password);
    }

    public void login(final String username, final String password){
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null){
                    e.printStackTrace();
                    //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    // login failed, attempt sign in
                    signIn(username, password);
                    return;
                }
                if (user == null){
                    Log.e("ERROR", "user is null");
                }

                showUserList();
            }
        });
    }

    public void signIn(String username, String password){
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null){
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.i("SIGN IN", "Success!");
                    showUserList();
                }
            }
        });
    }

    public void showUserList(){
        Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
        startActivity(intent);
    }

    public Boolean isUserLoggedIn(){
        return ParseUser.getCurrentUser() != null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.backgroundLayout || v.getId() == R.id.logoImageView){
            // Dismiss the keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.i("KEY PRESSED", "some key was pressed");
        if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.i("KEY PRESSED", "ENTER was pressed");
            confirmButtonPressed(v);
        }

        return false;
    }
}
