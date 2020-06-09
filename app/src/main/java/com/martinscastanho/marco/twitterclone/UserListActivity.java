package com.martinscastanho.marco.twitterclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class UserListActivity extends AppCompatActivity {
    ListView userListView;
    ArrayList<String> usernames;
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list_activity);

        setTitle("User List");

        userListView = findViewById(R.id.userListView);
        userListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        usernames = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, usernames);
        userListView.setAdapter(arrayAdapter);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView) view;
                if(checkedTextView.isChecked()){
                    follow(usernames.get(position));
                }
                else{
                    unfollow(usernames.get(position));
                }
                ParseUser.getCurrentUser().saveInBackground();
            }
        });

        populateUserList();
    }

    public void follow(String username){
        ParseUser.getCurrentUser().add("follows", username);
    }

    public void unfollow(String username){
        ParseUser.getCurrentUser().getList("follows").remove(username);
        List tempFollowing = ParseUser.getCurrentUser().getList("follows");
        ParseUser.getCurrentUser().remove("follows");
        ParseUser.getCurrentUser().put("follows", tempFollowing);
    }

    public void populateUserList(){
        ParseQuery<ParseUser> userParseQuery = ParseUser.getQuery();
        userParseQuery.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        userParseQuery.addAscendingOrder("username");
        userParseQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e != null){
                    e.printStackTrace();
                    return;
                }
                if(objects.size() == 0){
                    Log.e("ERROR", "objects is empty");
                }
                for(ParseUser user : objects){
                    usernames.add(user.getUsername());
                }
                arrayAdapter.notifyDataSetChanged();

                for(String username : usernames){
                    if(ParseUser.getCurrentUser().getList("follows").contains(username)){
                        userListView.setItemChecked(usernames.indexOf(username), true);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.tweet_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.tweet){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText tweetEditText = new EditText(this);
            alert.setTitle("New Tweet");
            alert.setView(tweetEditText);
            alert.setPositiveButton("Tweet", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tweet(tweetEditText.getText().toString());
                }
            });
            alert.show();
        }
        else if(item.getItemId() == R.id.timeline){
            Intent intent = new Intent(getApplicationContext(), TimelineActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.logout){
            ParseUser.logOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void tweet(String tweet){
        ParseObject tweetObject = new ParseObject("Tweet");
        tweetObject.put("tweet", tweet);
        tweetObject.put("username", ParseUser.getCurrentUser().getUsername());
        tweetObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(UserListActivity.this, "There has been an issue sharing the tweet", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(UserListActivity.this, "Tweet sent!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
