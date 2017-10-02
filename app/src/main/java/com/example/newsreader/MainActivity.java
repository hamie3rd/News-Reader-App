package com.example.newsreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    //Global Variables///////////////////////////////////////
    ListView listViewNews;
    String[] newsIds;
    ArrayList<String> newsTitles;
    ArrayList<String> newsURLs;
    ArrayAdapter arrayAdapter;

    //pulling info from the API///////////////////////////////
    public class DownloadAPI extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();

                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    public void getStoryIds(){
        DownloadAPI task = new DownloadAPI();
        String result = "";
        try {
            result = task.execute("https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //split result into 'resultParts' as an array of strings for the ids
        String resultCopy = result.replace(" ", "").replace("[", ""). replace("]", "");
        if (result.length() > 0) {
            newsIds = resultCopy.split(",");
        }
    }

    public void getStories(){
        //fill this out to get the specific stories and fill out the stories array//
        for (int i = 0; i <= 20; i++) {
            DownloadAPI task = new DownloadAPI();
            String result = "";
            try {
                result = task.execute("https://hacker-news.firebaseio.com/v0/item/" + newsIds[i] + ".json?print=pretty").get();
                JSONObject resultJSON = new JSONObject(result);
                //place each 'title' and 'url' in their respective ArrayList//////////
                if (newsTitles.isEmpty() || newsTitles.size() < i){
                    newsTitles.add(resultJSON.getString("title"));
                } else{
                    newsTitles.set(i, resultJSON.getString("title"));
                }
                if (newsURLs.isEmpty() || newsURLs.size() < i){
                    newsURLs.add(resultJSON.getString("url"));
                } else {
                    newsURLs.set(i, resultJSON.getString("url"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsTitles = new ArrayList<>();
        newsURLs = new ArrayList<>();


        //Set up API///////////////////////////////////////////////////////////////////////////
        getStoryIds();
        getStories();

        //Setting up the Array and ListView/////////////
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, newsTitles);
        listViewNews = (ListView)findViewById(R.id.listViewNews);
        listViewNews.setAdapter(arrayAdapter);

        //ListView listeners/////////////////////////////////////////////////
        listViewNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                intent.putExtra("Url", newsURLs.get(position));
                startActivity(intent);
            }
        });


        //Testing with output to the log
        System.out.println(String.valueOf(newsTitles.size()));
        System.out.println(String.valueOf(newsURLs.size()));
        System.out.println(String.valueOf(newsIds.length));


    }
}
