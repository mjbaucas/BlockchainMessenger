package com.example.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import kotlin.collections.ArrayDeque;

public class HomeActivity extends AppCompatActivity {
    private Button buttonLogout;
    private TextView welcomeText;
    private Spinner friendSelect;

    private LinearLayout messageHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        buttonLogout = findViewById(R.id.buttonLogout);
        welcomeText = findViewById(R.id.welcomeText);
        friendSelect = findViewById(R.id.friendSelect);
        messageHolder = findViewById(R.id.messageHolder);


        Bundle values = getIntent().getExtras();
        if (values != null){
            welcomeText.setText(String.format("%s", values.getString("username")));

            try {
                JSONObject friendObj = new JSONObject(new GetFriends().execute().get());
                if(friendObj.has("friends") && (Integer) friendObj.get("status") == 200){
                    JSONArray tempArr = new JSONArray(friendObj.get("friends").toString());
                    ArrayList<String> friendList = new ArrayList<String>();
                    for (int i = 0; i < tempArr.length(); i++){
                        friendList.add(tempArr.getString(i));
                    }
                    ArrayAdapter<String> friendArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_list, friendList);
                    friendSelect.setAdapter(friendArrayAdapter);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        friendSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = adapterView.getItemAtPosition(i);
                if (item != null) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        String temp = item.toString().replace("\"", "");
                        jsonObject.put("friend", temp);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    String jsonString = jsonObject.toString();
                    try {
                        JSONObject friendObj = new JSONObject(new GetMessages().execute(jsonString).get());
                        if(friendObj.has("messages") && (Integer) friendObj.get("status") == 200){
                            JSONArray tempArr = new JSONArray(friendObj.get("messages").toString());
                            messageHolder.removeAllViews();
                            for (i = 0; i < tempArr.length(); i++){
                                FrameLayout frameLayout = new FrameLayout(HomeActivity.this);
                                LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
                                frameParams.weight = 2.0f;

                                frameLayout.setLayoutParams(frameParams);

                                JSONObject jsonObj = tempArr.getJSONObject(i);
                                TextView textView = new TextView(HomeActivity.this);
                                textView.setPadding(20, 20,20,20);
                                textView.setTextSize(22);

                                textView.setBackgroundColor(0xff66ff66);
                                textView.setText(jsonObj.get("message").toString());

                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                params.setMargins(10, 10,10,10);

                                Log.d("tag", jsonObj.get("from").toString());
                                Log.d("tag", values.getString("username"));

                                if (jsonObj.get("from").toString().equals(values.getString("username"))){
                                    params.gravity = Gravity.RIGHT;
                                } else {
                                    params.gravity = Gravity.LEFT;
                                }
                                params.weight = 1.0f;
                                textView.setLayoutParams(params);

                                messageHolder.addView(textView);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AttemptLogout().execute();
            }
        });
    }

    class AttemptLogout extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings){
            Handler handler =  new Handler(Looper.getMainLooper());

            try{
                URL url = new URL("http://10.0.2.2:5000/logout");
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                client.setRequestMethod("POST");
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))
                ) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;

                    while ((responseLine = reader.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    handler.post(new Runnable() {
                        public void run() {
                            //Toast.makeText(HomeActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                            try {
                                JSONObject jsonObj = new JSONObject(response.toString());
                                //Log.d("tag", jsonObj.toString(4));
                                Integer response = (Integer) jsonObj.get("status");
                                if (response == 200){
                                    Toast.makeText(HomeActivity.this, "Logout Successful.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(HomeActivity.this, "Logout: Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }

    class GetFriends extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings){
            Handler handler =  new Handler(Looper.getMainLooper());
            StringBuilder response = new StringBuilder();

            try{
                URL url = new URL("http://10.0.2.2:5000/friendslist");
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                client.setRequestMethod("GET");
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))
                ) {

                    String responseLine = null;

                    while ((responseLine = reader.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(HomeActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(HomeActivity.this, "Friend: Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return response.toString();
        }
    }

    class GetMessages extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings){
            Handler handler =  new Handler(Looper.getMainLooper());
            StringBuilder response = new StringBuilder();

            try{
                URL url = new URL("http://10.0.2.2:5000/messages");
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");

                client.setDoOutput(true);

                try (OutputStream os = client.getOutputStream()) {
                    byte [] input = strings[0]. getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))
                ){
                    String responseLine = null;

                    while ((responseLine = reader.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    handler.post( new Runnable(){
                        public void run(){
                            Toast.makeText(HomeActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(MainActivity.this, "Login Unuccessful.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return response.toString();
        }
    }
}


