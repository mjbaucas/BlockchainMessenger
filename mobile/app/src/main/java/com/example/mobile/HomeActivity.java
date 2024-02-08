package com.example.mobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.concurrent.atomic.AtomicReference;

import kotlin.collections.ArrayDeque;

public class HomeActivity extends AppCompatActivity {
    private Button buttonLogout;
    private Button buttonSend;
    private TextView welcomeText;
    private Spinner friendSelect;

    private EditText inputMessage;

    private LinearLayout messageHolder;

    Bundle values;

    String selectedFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        buttonLogout = findViewById(R.id.buttonLogout);
        buttonSend = findViewById(R.id.buttonSend);
        welcomeText = findViewById(R.id.welcomeText);
        friendSelect = findViewById(R.id.friendSelect);
        inputMessage = findViewById(R.id.inputMessage);
        messageHolder = findViewById(R.id.messageHolder);

        values = getIntent().getExtras();
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
                inputMessage.setEnabled(true);
                buttonSend.setEnabled(true);

                Object item = adapterView.getItemAtPosition(i);
                if (item != null) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        selectedFriend = item.toString().replace("\"", "");
                        jsonObject.put("friend", selectedFriend);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    String jsonString = jsonObject.toString();
                    try {
                        new GetMessages().execute(jsonString);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                inputMessage.setEnabled(false);
                buttonSend.setEnabled(false);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AttemptLogout().execute();
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve entered username and password
                String message = inputMessage.getText().toString();
                String selectedFriend = friendSelect.getSelectedItem().toString();

                if (message.isEmpty() && selectedFriend == null) {
                    Toast.makeText(HomeActivity.this, "You have no message typed.", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject jsonObjectSend = new JSONObject();
                        jsonObjectSend.put("from", values.getString("username"));
                        jsonObjectSend.put("to", selectedFriend);
                        jsonObjectSend.put("message",message);
                        String jsonStringSend = jsonObjectSend.toString();
                        new SendMessage().execute(jsonStringSend);

                        JSONObject jsonObjectGet = new JSONObject();
                        jsonObjectGet.put("friend", selectedFriend);
                        String jsonStringGet = jsonObjectGet.toString();
                        new GetMessages().execute(jsonStringGet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
                        Toast.makeText(HomeActivity.this, "Ran into an error logging out.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(HomeActivity.this, "Friend list retrieved successfully.", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(HomeActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(HomeActivity.this, "Ran into an error retrieving friend list.", Toast.LENGTH_SHORT).show();
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
                    StringBuilder response = new StringBuilder();

                    while ((responseLine = reader.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    handler.post( new Runnable(){
                        public void run(){
                            Toast.makeText(HomeActivity.this, "Messages retrieved successfully.", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(HomeActivity.this, response.toString(), Toast.LENGTH_SHORT).show();

                            JSONObject friendObj = null;
                            try {
                                friendObj = new JSONObject(response.toString());
                                if(friendObj.has("messages") && (Integer) friendObj.get("status") == 200){
                                    JSONArray tempArr = new JSONArray(friendObj.get("messages").toString());
                                    messageHolder.removeAllViews();
                                    for (int i = 0; i < tempArr.length(); i++){
                                        FrameLayout frameLayout = new FrameLayout(HomeActivity.this);
                                        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
                                        frameParams.weight = 2.0f;

                                        frameLayout.setLayoutParams(frameParams);

                                        JSONObject jsonObj = tempArr.getJSONObject(i);
                                        TextView textView = new TextView(HomeActivity.this);
                                        textView.setPadding(40, 20,40,20);
                                        textView.setTextSize(22);

                                        textView.setText(jsonObj.get("message").toString());
                                        textView.setTypeface(null, Typeface.BOLD);

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        params.setMargins(10, 10,10,10);

                                        Log.d("tag", jsonObj.get("from").toString());
                                        Log.d("tag", values.getString("username"));

                                        if (jsonObj.get("from").toString().equals(values.getString("username"))){
                                            textView.setBackgroundColor(Color.parseColor("#aa97b9"));
                                            params.gravity = Gravity.RIGHT;
                                        } else {
                                            textView.setBackgroundColor(Color.parseColor("#d3c7dd"));
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
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(HomeActivity.this, "Ran into an error retrieving messages.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }

    class SendMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings){
            Handler handler =  new Handler(Looper.getMainLooper());

            try{
                URL url = new URL("http://10.0.2.2:5000/message/send");
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
                    StringBuilder response = new StringBuilder();

                    while ((responseLine = reader.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    handler.post( new Runnable(){
                        public void run(){
                            Toast.makeText(HomeActivity.this, "Message sent successfully.", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(HomeActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(HomeActivity.this, "Ran into an error sending message.", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }
}


