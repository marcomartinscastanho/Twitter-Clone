package com.martinscastanho.marco.twitterclone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimelineActivity extends AppCompatActivity {
    ListView timelineListView;
    List<Map<String, String>> tweets;
    SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        setTitle("Timeline");

        timelineListView = findViewById(R.id.timelineListView);
        tweets = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this, tweets, android.R.layout.simple_list_item_2, new String[]{"content", "author"}, new int[]{android.R.id.text1, android.R.id.text2});
        timelineListView.setAdapter(simpleAdapter);

        populateTimeline();
    }

    public void populateTimeline(){
        checkFollowingNotEmpty();

        ParseQuery<ParseObject> tweetQuery = new ParseQuery<>("Tweet");
        tweetQuery.orderByDescending("createdAt");
        tweetQuery.whereContainedIn("username", ParseUser.getCurrentUser().getList("follows"));
        tweetQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e != null){
                    e.printStackTrace();
                }
                else if(objects.size() == 0){
                    Log.e("ERROR", "objects is empty");
                }
                else {
                    for(ParseObject tweet : objects){
                        Map<String, String> tweetMap = new HashMap<>();
                        tweetMap.put("content", tweet.getString("tweet"));
                        tweetMap.put("author", tweet.getString("username"));
                        tweets.add(tweetMap);
                    }
                    simpleAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void checkFollowingNotEmpty(){
        List following = ParseUser.getCurrentUser().getList("follows");

        if(following == null || following.isEmpty()){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Nothing to show");
            alert.setMessage("Follow some users first");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.show();
        }
    }
}
