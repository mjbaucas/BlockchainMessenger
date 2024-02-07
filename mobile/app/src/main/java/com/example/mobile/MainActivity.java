package com.example.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private EditText inputUsername, inputPassword;
    private Button buttonLogin;

    Timer checkTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        checkTimer = new Timer();
        checkTimer.schedule(new TimerTask() {
            public void run(){
                new CheckSession().execute();
            }
        }, 0, 5000);

        // Set a click listener for the login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve entered username and password
                String username = inputUsername.getText().toString();
                String password = inputPassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "One of your fields are empty", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("username", username);
                        jsonObject.put("password", password);
                        String jsonString = jsonObject.toString();
                        new AttemptLogin().execute(jsonString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    class CheckSession extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings){
            Handler handler =  new Handler(Looper.getMainLooper());

            try{
                URL url = new URL("http://10.0.2.2:5000/check");
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                client.setRequestMethod("GET");
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
                            Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                            try {
                                JSONObject jsonObj = new JSONObject(response.toString());
                                //Log.d("tag", jsonObj.toString(4));
                                Integer response = (Integer) jsonObj.get("status");
                                if (response == 200 && jsonObj.has("username")){
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class).putExtra("username", jsonObj.getString("username")));
                                } else {
                                    startActivity(new Intent(MainActivity.this, MainActivity.class));
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
                        Toast.makeText(MainActivity.this, "Login: Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }

    class AttemptLogin extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings){
            Handler handler =  new Handler(Looper.getMainLooper());

            try{
                URL url = new URL("http://10.0.2.2:5000/login");
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
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;

                    while ((responseLine = reader.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    handler.post( new Runnable(){
                        public void run(){
                            //Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                            try {
                                JSONObject jsonObj = new JSONObject(response.toString());
                                //Log.d("tag", jsonObj.toString(4));
                                Integer response = (Integer) jsonObj.get("status");
                                if (response == 200 && jsonObj.has("username")){
                                    Toast.makeText(MainActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class).putExtra("username", jsonObj.getString("username")));
                                } else {
                                    Toast.makeText(MainActivity.this, "Login Unsuccessful.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, MainActivity.class));
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
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(MainActivity.this, "Login Unuccessful.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }
}