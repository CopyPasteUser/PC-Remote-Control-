package com.example.appington_city;



import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MediaActivity extends AppCompatActivity {

    private static final String SENSITIVITY_KEY = "stick_sensitivity";
    private static final String IP_KEY = "connection_ip";
    private static final String PREF_NAME = "app_settings";
    private SharedPreferences sharedPreferences;
    private static String IP;
    private static final String Port = ":5500";

    private int loadSensitivityFromSharedPreferences() {


        return sharedPreferences.getInt(SENSITIVITY_KEY, 5);
    }


    private ThumbstickView thumbstickView;
    private String vector = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        IP = sharedPreferences.getString(IP_KEY, "default_value_if_not_found");
        Log.d("ip",IP);
        thumbstickView = findViewById(R.id.thumbstickView);


        thumbstickView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        // Berechnen der Distanz vom Thumbstick-Mittelpunkt zur aktuellen Berührungsposition
                        float distanceX = event.getX() - thumbstickView.getWidth() / 2;
                        float distanceY = event.getY() - thumbstickView.getHeight() / 2;

                        // Log für die Distanz


                        int vectorX = Math.round(distanceX) / loadSensitivityFromSharedPreferences();
                        int vectorY = Math.round(distanceY) / loadSensitivityFromSharedPreferences();

                        vector = vectorX + "/" + vectorY;
                        new Thread(() -> mouseMove(vector)).start();
                        Log.d("Thumbstick", "Vector: " + vector);
                        return true;
                    case MotionEvent.ACTION_UP:
                        vector = "";
                        Log.d("Thumbstick", "Vector reset");
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    public void netflix_clicked(View netflix) {
        findViewById(R.id.netflixButton);
        new Thread(() -> openNetflix()).start();
    }

    public void youtube_clicked(View youtube) {
        findViewById(R.id.youtubeButton);
        new Thread(() -> openYoutube()).start();
    }

    public void disney_clicked(View disney) {
        findViewById(R.id.disneyButton);
        new Thread(() -> openDisney()).start();
    }

    public void fullscreen_clicked(View fullscreen) {
        findViewById(R.id.fullscreenButton);
        new Thread(() -> goFullscreen()).start();
    }

    public void left_clicked(View left) {
        findViewById(R.id.leftButton);
        new Thread(() -> sendLeftclick()).start();
    }

    public void stop_clicked(View stop) {
        findViewById(R.id.stopButton);
        new Thread(() -> sendSpacebar()).start();
    }



    public void closing_clicked(View closing) {
        findViewById(R.id.closingButton);
        new Thread(() -> closeProgramm()).start();
    }

    public void soundUP_clicked(View soundUP) {
        findViewById(R.id.soundUPButton);
        new Thread(() -> handleSound(0.1)).start();
    }

    public void soundDOWN_clicked(View soundDOWN) {
        findViewById(R.id.soundDOWNButton);
        new Thread(() -> handleSound(-0.1)).start();
    }

    public void scrollUP_clicked(View scrollUP) {
        findViewById(R.id.scrollUPButton);
        new Thread(() -> scrollMouse(100)).start();
    }

    public  void scrollDOWN_clicked(View scrollDOWN) {
        findViewById(R.id.scrollDOWNButton);
        new Thread(() -> scrollMouse(-100)).start();
    }

    public void rewind_clicked(View rewind) {
        findViewById(R.id.rewindButton);
        new Thread(() -> rewind()).start();
    }

    public void ff_clicked(View ff) {
        findViewById(R.id.ffButton);
        new Thread(() -> ff()).start();
    }

    public void spotify_clicked(View spotify) {
        findViewById(R.id.spotifyButton);
        new Thread(() -> openSpotify()).start();
    }

    public static void openSpotify() {
        apiCall("/spo");
    }

    public static void mouseMove(String Vector) {
        apiPOST("/mouseMove", Vector);
        Log.d("ip",String.valueOf(IP+"/mouseMove"));
    }






    public static void apiCall(String route) {
        try {

            String apiUrl = "http://" + IP + Port + route;
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

            String apiUrl = "http://" + IP + Port + route;
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

    public static void sendKeystrokes(String input) {
        apiPOST("/sendKeys", input);
    }
    public static void openNetflix() {
        apiCall("/net");
    }

    public static void goFullscreen() {
        apiCall("/fuScr");
    }

    public static void rewind() {
        apiCall("/left");
    }

    public static void ff() {
        apiCall("/right");
    }

    public static void openYoutube() {
        apiCall("/you");
    }

    public static void openDisney() {
        apiCall("/dis");
    }

    public static void sendSpacebar() {
        apiCall("/space");
    }

    public static void sendLeftclick(){
        apiCall("/lc");
    }


    public static void closeProgramm() {
        apiCall("/clP");
    }
    public static void handleSound(double volume) {
        apiPOST("/sound", String.valueOf(volume));
    }
    public static void scrollMouse(int amount) {
        apiPOST("/scroll", String.valueOf(amount));
    }
}