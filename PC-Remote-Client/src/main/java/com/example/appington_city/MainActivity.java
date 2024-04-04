package com.example.appington_city;

import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.os.Bundle;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.DataOutputStream;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }







    public void settings_clicked(View settings) {
        findViewById(R.id.settingsButton);
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void video_clicked(View video){
        findViewById(R.id.videoButton);

        Intent i = new Intent(this, MediaActivity.class);
        startActivity(i);
    }

    public void free_clicked(View free){
        findViewById(R.id.freeButton);

        Intent i = new Intent(this, FilesActivity.class);
        startActivity(i);
    }

    public void music_clicked(View music){
        findViewById(R.id.musicButton);

        Intent i = new Intent(this, PCActivity.class);
        startActivity(i);
    }






    static String IP = "http://192.168.0.101:5500";

    public static void apiCall(String route) {
        try {
            String apiUrl = IP + route;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();
        } catch (Exception e) {
            Log.e("API_CALL_ERROR", "Fehler beim API-Aufruf", e);
            e.printStackTrace();
        }
    }

    public static void apiPOST(String route, String postData) {
        try {
            String apiUrl = IP + route;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(postData);
            out.flush();
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();
        } catch (Exception e) {
            Log.e("API_POST_ERROR", "Fehler beim API-POST", e);
            e.printStackTrace();
        }
    }


}
