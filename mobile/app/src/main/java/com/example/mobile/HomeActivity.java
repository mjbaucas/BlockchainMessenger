package com.example.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {
    private Button buttonLogout;

    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        buttonLogout = findViewById(R.id.buttonLogout);
        welcomeText = findViewById(R.id.welcomeText);

        Bundle values = getIntent().getExtras();
        if (values != null){
            welcomeText.setText(String.format("%s", values.getString("username")));
        }
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
}


