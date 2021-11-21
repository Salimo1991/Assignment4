package com.example.assignment4;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    SQLiteDatabase database;
    ArrayAdapter arrayAdapter;

    ArrayList<String> ArrayList1 =  new ArrayList<>();
    ArrayList<String> ArrayList2 =  new ArrayList<>();



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        database =   this.openOrCreateDatabase  ("Articles", MODE_PRIVATE, null) ;

        database.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId, INTEGER, title VARCHAR, content VARCHAR)");



        DownloadTask task =   new DownloadTask();

        try {

            task.execute ("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

        } catch  (Exception e) {

        }

        ListView listView = findViewById (R.id.listview);

        arrayAdapter = new ArrayAdapter (this, android.R.layout.simple_list_item_1,  ArrayList1);


        listView.setAdapter  (arrayAdapter);

        listView.setOnItemClickListener   (new AdapterView.OnItemClickListener()  {


            @Override

            public void onItemClick   ( AdapterView<?>  adapterView , View view , int i ,  long l )  {


                  WebView webView = findViewById(R.id.webview);

                 webView.getSettings().setJavaScriptEnabled(true);

                webView.setWebViewClient(new WebViewClient());

                webView.loadData("content", "text/html", "UTF-8");}});
        task(); }

    public void task() {

        Cursor cur = database.rawQuery("SELECT * FROM articles",  null);


        int cont =  cur.getColumnIndex("content");

        int tit =  cur.getColumnIndex("title");


        if (cur.moveToFirst()  ) {


            ArrayList1.clear();

            ArrayList2.clear();

            do {

                ArrayList1.add(cur.getString(tit));

                ArrayList2.add(cur.getString(cont));


            }  while (cur.moveToNext());

            arrayAdapter.notifyDataSetChanged  ();
        }
    }






    public class    DownloadTask   extends   AsyncTask    < String, Void,  String >   {

        @Override

        protected  String    doInBackground   (String ... urls)  {

            String result  =  "";


            URL  url;

            ttpURLConnection   urlConnection =    null;


            try  {

                url =   new URL  (urls[0]);

                urlConnection =    (HttpURLConnection) url.openConnection();


                InputStream inputStream =   urlConnection. getInputStream() ;

                InputStreamReader inputStreamReader  =    new  InputStreamReader (inputStream);

                int data =    inputStreamReader.read();

                while    (data != -1 )  {

                    char current  =    (char) data;

                    result +=    current;

                    data =   inputStreamReader.read(); }


                int nb =  20;

                JSONArray   jsonArray =    new   JSONArray   (result);


                if  (  jsonArray.length()  <    20) {

                    nb=   jsonArray.length  ();

                }


                database.execSQL  ("DELETE FROM articles") ;



                for   (int i=0; i < nb;  i++) {

                    String m = jsonArray.getString(i);


                    url = new/6 URL ("https://hacker-news.firebaseio.com/v0/item/" +   m  +  ".json?print=pretty");


                    urlConnection =   (HttpURLConnection) url.openConnection();


                    inputStream =   urlConnection.getInputStream();

                    inputStreamReader =  new InputStreamReader(inputStream);

                    data =  inputStreamReader.read();

                    String articleInfo = "";

                    while   (data != -1)  {


                        char current =   (char) data;

                        articleInfo + =   current;

                        data =   inputStreamReader.read();
                    }

                    JSONObject jsonObject =   new JSONObject(articleInfo);


                    if (!jsonObject.isNull ("title")     &&     !jsonObject.isNull("url")) {

                        String articleTitle =    jsonObject.getString("title");

                        String articleUrl   =    jsonObject.getString("url");


                        url = new URL(articleUrl);

                        urlConnection =  (HttpURLConnection)  url.openConnection();

                        inputStream =   urlConnection.getInputStream();

                        inputStreamReader =   new InputStreamReader  (inputStream);


                        data =  inputStreamReader.read();

                        String   articleContent = "";

                        while  (data != -1)  {

                            char current = (char)  data;

                            articleContent +=   current;

                            data =   inputStreamReader.read();

                        }


                        Log.i ("HTML", articleContent);


                        String ins = "INSERT INTO articles (articleId, title, content) VALUES (?, ?, ?)";

                        SQLiteStatement stat =  database.compileStatement  (ins);

                        stat.bindString (1,m);

                        stat.bindString (2,articleTitle);
                        stat.bindString (3,articleContent);

                        stat.execute();   }}



                Log.i  ("URL Content",  result);

                return result;



            } catch  (Exception e) {


                e.printStackTrace();


            }

            return null;

        }


        @Override

        protected void onPostExecute  (String s)  {

            super.onPostExecute(s);

            task(); }}}
