package com.example.appington_city;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {


    private static final String TAG = "SettingsActivity";
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "app_settings";
    private static final String DARK_MODE_KEY = "dark_mode_enabled";
    private static final String SENSITIVITY_KEY = "stick_sensitivity";
    private static final String IP_KEY = "connection_ip";

    private SeekBar seekBar;
    private int stickSensitivity;
    private TextView deviceListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        deviceListTextView = findViewById(R.id.deviceListTextView);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);


        initializeDarkModeSwitch();
        initializeSeekBar();
        startNetworkScanning();
    }

    private void initializeDarkModeSwitch() {
        Switch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        boolean isDarkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_KEY, false);
        darkModeSwitch.setChecked(isDarkModeEnabled);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Dark mode switch state changed: " + isChecked);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DARK_MODE_KEY, isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    private void initializeSeekBar() {
        seekBar = findViewById(R.id.seekBar);
        stickSensitivity = sharedPreferences.getInt(SENSITIVITY_KEY, 5);
        seekBar.setMax(9);
        seekBar.setProgress((stickSensitivity - 5) / 5);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                stickSensitivity = progress * 5 + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveSensitivityToSharedPreferences(stickSensitivity);
            }
        });
    }

    private void startQRCodeScanner() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivity(intent);
    }












    private void saveSensitivityToSharedPreferences(int sensitivity) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SENSITIVITY_KEY, sensitivity);
        editor.apply();
    }

    private void saveIpToSharedPreferences(String ip) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IP_KEY, ip);
        editor.apply();
    }

    private void startNetworkScanning() {
        new Thread(() -> {
            List<String> ipAddresses = loadIPAddressesFromFiles();
            List<Device> devices = NetworkScanner.scanForDevices(ipAddresses);
            runOnUiThread(() -> {
                if (devices.isEmpty()) {
                    deviceListTextView.setText("No devices found.");
                } else {
                    updateDeviceList(devices);
                    deviceListTextView.setText("Found Devices:");
                }
            });
        }).start();
    }

    private List<String> loadIPAddressesFromFiles() {
        List<String> ipAddresses = new ArrayList<>();
        File ipFilesDir = new File(getFilesDir(), "ip_files");
        if (ipFilesDir.exists() && ipFilesDir.isDirectory()) {
            File[] files = ipFilesDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        try {
                            JSONObject jsonObject = new JSONObject(readFileContents(file));
                            String ipAddress = jsonObject.getString("ip_address");
                            ipAddresses.add(ipAddress);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return ipAddresses;
    }

    private String readFileContents(File file) {
        StringBuilder contents = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contents.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + file.getAbsolutePath(), e);
        }
        return contents.toString();
    }


    private void updateDeviceList(List<Device> devices) {
        runOnUiThread(() -> {
            LinearLayout deviceListLayout = findViewById(R.id.deviceListLinearLayout);
            deviceListLayout.removeAllViews();

            for (Device device : devices) {
                String ipAddress = device.getIpAddress();
                String deviceInfo = device.getDeviceInfo();

                Button deviceButton = new Button(this);
                deviceButton.setText(deviceInfo);
                deviceButton.setOnClickListener(v -> {
                    String clickedDevice = ipAddress;
                    Log.d(TAG, "Button has been clicked: " + clickedDevice);

                    checkPassword(this, clickedDevice,"");
                });

                deviceListLayout.addView(deviceButton);
            }
        });
    }

    public void again_clicked(View again){
        findViewById(R.id.againButton);
        LinearLayout deviceListLayout = findViewById(R.id.deviceListLinearLayout);
        deviceListLayout.removeAllViews();
        startNetworkScanning();

    }




    public  void checkPassword(Context context, String ipAddress,String password) {
        String passwordCheckUrl = "http://" + ipAddress + ":5500/password";


        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    // Senden einer leeren Post-Anfrage
                    return sendPostRequest(params[0], password);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String response) {

                if (response != null && response.equals("400")) {

                    showErrorToast(context,"Password required");
                    createPasswordInput(context, ipAddress);
                } else if (response != null && response.equals("200")) {

                    saveIpToSharedPreferences(ipAddress);
                    showSuccessToast(context, "Successfully connected");
                } else {

                    showErrorToast(context, "Unexpected response from the server" );
                }
            }
        }.execute(passwordCheckUrl);
    }

            private static String sendPostRequest(String urlString, String postData) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(postData.getBytes());
            outputStream.flush();
            outputStream.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void createPasswordInput(Context context, String ipAddress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Password");

        final EditText input = new EditText(context);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();

                checkPassword(context, ipAddress, password);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void ip_clicked(View ip){
        findViewById(R.id.ipbutton);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("IP Actions");

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.VERTICAL);

        Button manualInputButton = new Button(this);
        manualInputButton.setText("Manual Input");
        manualInputButton.setOnClickListener(v -> {

            showManualInputDialog();
        });
        buttonLayout.addView(manualInputButton);

        Button scanQRCodeButton = new Button(this);
        scanQRCodeButton.setText("Scan QR Code");
        scanQRCodeButton.setOnClickListener(v -> {

            startQRCodeScanner();
        });
        buttonLayout.addView(scanQRCodeButton);

        Button ipListButton = new Button(this);
        ipListButton.setText("IP List");
        ipListButton.setOnClickListener(v -> {

            showDeleteIpDialog();
        });
        buttonLayout.addView(ipListButton);

        builder.setView(buttonLayout);

        builder.setPositiveButton("Cancel", null);


        builder.show();
    }

    private void showManualInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manual Input");


        final EditText input = new EditText(this);
        builder.setView(input);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ipAddress = input.getText().toString();
                // Speichern der IP-Adresse
                saveIpAddress(SettingsActivity.this, ipAddress);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        builder.show();
    }

    public void showDeleteIpDialog() {

        File ipFilesDir = new File(getFilesDir(), "ip_files");
        File[] files = ipFilesDir.listFiles();
        if (files != null && files.length > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete IP Address");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    String fileName = file.getName();
                    String ipAddress = fileName.replaceAll("_", "\\.").replaceAll(".json", "");

                    LinearLayout rowLayout = new LinearLayout(this);
                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                    TextView ipTextView = new TextView(this);
                    ipTextView.setText(ipAddress);
                    rowLayout.addView(ipTextView);

                    Button deleteButton = new Button(this);
                    deleteButton.setText("Delete");
                    deleteButton.setOnClickListener(v -> {

                        deleteIpAddress(this, fileName); // Kontext hinzugefügt

                        showDeleteIpDialog();
                    });
                    rowLayout.addView(deleteButton);

                    layout.addView(rowLayout);
                }
            }

            builder.setView(layout);

            builder.setNegativeButton("Cancel", (dialog, which) -> {

                dialog.dismiss();
            });


            builder.show();
        } else {
            Toast.makeText(this, "No IP addresses found", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteIpAddress(Context context, String fileName) {

        File ipFilesDir = new File(context.getFilesDir(), "ip_files");
        File file = new File(ipFilesDir, fileName);
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(context, "IP Address deleted: " + fileName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete IP Address: " + fileName, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File not found: " + fileName, Toast.LENGTH_SHORT).show();
        }
    }


    private static void showSuccessToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private static void showErrorToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }



    public static void saveIpAddress(Context context, String ipAddress) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ip_address", ipAddress); // IP-Adresse im JSON-Objekt speichern
            String fileName = ipAddress.replaceAll("\\.", "_") + ".json"; // Name der Datei


            File ipFilesDir = new File(context.getFilesDir(), "ip_files");
            if (!ipFilesDir.exists()) {
                if (!ipFilesDir.mkdirs()) {
                    Log.e("saveIpAddress", "Failed to create directory: " + ipFilesDir.getAbsolutePath());
                    return;
                }

                Log.d("saveIpAddress", "Directory created at: " + ipFilesDir.getAbsolutePath());
            }


            File file = new File(ipFilesDir, fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.close();

            Log.d("saveIpAddress", "IP address saved successfully: " + ipAddress);

            Log.d("saveIpAddress", "File path: " + file.getAbsolutePath());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            Log.e("saveIpAddress", "Error saving IP address: " + e.getMessage());
        }
    }








}
