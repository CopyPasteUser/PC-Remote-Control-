package com.example.appington_city;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PCActivity extends AppCompatActivity {

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
    private EditText editText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        IP = sharedPreferences.getString(IP_KEY, "default_value_if_not_found");
        Log.d("ip", IP);
        thumbstickView = findViewById(R.id.thumbstickView);
        editText = findViewById(R.id.editText);

        thumbstickView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        float distanceX = event.getX() - thumbstickView.getWidth() / 2;
                        float distanceY = event.getY() - thumbstickView.getHeight() / 2;

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


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count > before) {
                    char lastChar = s.charAt(start + count - 1);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendKeystrokes(String.valueOf(lastChar));
                        }
                    }).start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {


                        if (editText.length() > 0) {
                            editText.getText().delete(editText.length() - 1, editText.length());
                        }

                        new Thread(() -> sendBackspace()).start();
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_ENTER) {

                        new Thread(() -> sendEnter()).start();
                        return true;
                    }
                }
                return false;
            }
        });

        initializeVerticalSeekBar();
        initializeHorizontalSeekBar();
        setCustomBarsColors();
    }

    private void initializeVerticalSeekBar() {
        SeekBar verticalSeekBar = findViewById(R.id.verticalSeekBar);
        final int MAX_VALUE = 100;
        final int MIN_VALUE = -100;
        final int STEP_SIZE = 20;


        verticalSeekBar.setMax((MAX_VALUE - MIN_VALUE) / STEP_SIZE);

        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                int value = MIN_VALUE + (progress * STEP_SIZE);

                Log.d("VerticalSeekBar", "Progress: " + progress + ", Value: " + value);
                new Thread(() -> scrollMouse(value)).start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        verticalSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float y = event.getY();
                int height = verticalSeekBar.getHeight();
                int progress = (int) (verticalSeekBar.getMax() * (1 - y / height));
                verticalSeekBar.setProgress(progress);
                return false;
            }
        });
    }

    private void initializeHorizontalSeekBar() {
        SeekBar horizontalSeekBar = findViewById(R.id.horizontalSeekBar);


        horizontalSeekBar.setMax(10);

        horizontalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int lastProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("SeekBar", "onProgressChanged: Progress=" + progress + ", fromUser=" + fromUser);
                if (fromUser) {
                    // Berechnen Sie die Differenz
                    int diff = progress - lastProgress;
                    Log.d("SeekBar", "Diff=" + diff);


                    float soundChange = 0.1f * Math.signum(diff);
                    Log.d("SeekBar", "Sound Change=" + soundChange);


                    lastProgress = progress;

                    new Thread(() -> handleSound(soundChange)).start();
                }
            }






            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });




    }

    private void setCustomBarsColors() {

        View verticalCustomBar = findViewById(R.id.verticalSeekBar);
        View horizontalCustomBar = findViewById(R.id.horizontalSeekBar);


        int darkModeColor = ContextCompat.getColor(this, R.color.white);
        int dayModeColor = ContextCompat.getColor(this, R.color.black);


        if (isDarkModeEnabled()) {
            verticalCustomBar.setBackgroundColor(darkModeColor);
            horizontalCustomBar.setBackgroundColor(darkModeColor);
        } else {
            verticalCustomBar.setBackgroundColor(dayModeColor);
            horizontalCustomBar.setBackgroundColor(dayModeColor);
        }
    }



    private boolean isDarkModeEnabled() {
        int nightModeFlags = getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }











    public void screenshot_clicked(View screenshot) {
        findViewById(R.id.screenshotButton);
        new Thread(() -> takeScreenshot()).start();
    }

    public void right_clicked(View right) {
        findViewById(R.id.rightButton);
        new Thread(() -> sendRightclick()).start();
    }

    public void left_clicked(View left) {
        findViewById(R.id.leftButton);
        new Thread(() -> sendLeftclick()).start();
    }

    public void space_clicked(View space) {
        findViewById(R.id.spaceButton);
        new Thread(() -> sendSpacebar()).start();
    }

    public void closing_clicked(View closing) {
        findViewById(R.id.closingButton);
        new Thread(() -> closeProgramm()).start();
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


    public static void scrollMouse(int amount) {
        apiPOST("/scroll", String.valueOf(amount));
    }

    public static void closeProgramm() {
        apiCall("/clP");
    }
    public static void sendSpacebar() {
        apiCall("/space");
    }
    public static void takeScreenshot() { apiCall("/screenshot"); }
    public static void sendLeftclick(){
        apiCall("/lc");
    }
    public static void sendRightclick() {
        apiCall("/rc");
    }

    public static void sendBackspace() {
        apiCall("/backspace");
    }

    public static void sendEnter() {
        apiCall("/enter");
    }

    public static void mouseMove(String Vector) {
        apiPOST("/mouseMove", Vector);
        Log.d("ip",String.valueOf(IP+"/mouseMove"));
    }
    public static void sendKeystrokes(String input) {
        apiPOST("/sendKeys", input);
    }
    public static void handleSound(double volume) {
        apiPOST("/sound", String.valueOf(volume));
    }


}


