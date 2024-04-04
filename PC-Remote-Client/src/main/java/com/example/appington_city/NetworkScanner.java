package com.example.appington_city;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Device {
    private String ipAddress;
    private String deviceInfo;

    public Device(String ipAddress, String deviceInfo) {
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }
}

public class NetworkScanner {

    private static final String TAG = "NetworkScanner";
    private static final int TIMEOUT = 250;
    private static final int PORT = 5500;

    public static List<Device> scanForDevices(List<String> ipAddresses) {
        List<Device> devices = new ArrayList<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Log.d(TAG, "Starting device scan...");


        if (ipAddresses != null) {
            Log.d(TAG, "Scanning specific IP addresses...");
            // Scan der spezifischen IP-Adressen aus dem ip_files-Ordner
            for (String ipAddress : ipAddresses) {
                executor.execute(() -> {
                    if (isReachable(ipAddress, PORT)) {
                        String deviceInfo = sendAPIRequest(ipAddress, PORT);
                        if (deviceInfo != null) {
                            Log.d(TAG, "Device found at IP: " + ipAddress + " Info: " + deviceInfo);
                            Device device = new Device(ipAddress, deviceInfo);
                            devices.add(device);
                        } else {
                            Log.d(TAG, "No device found at IP: " + ipAddress);
                        }
                    } else {
                        Log.d(TAG, "IP address unreachable: " + ipAddress);
                    }
                });
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Device scan complete.");

        return devices;
    }

    private static boolean isReachable(String ipAddress, int port) {
        try (Socket socket = new Socket()) {
            Log.d(TAG, "Checking reachability of IP: " + ipAddress + " Port: " + port);
            socket.connect(new InetSocketAddress(ipAddress, port), TIMEOUT);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String sendAPIRequest(String ipAddress, int port) {
        try {
            Log.d(TAG, "Sending API request to IP: " + ipAddress + " Port: " + port);
            HttpURLConnection connection = (HttpURLConnection) new URL("http://" + ipAddress + ":" + port + "/name").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(250);
            connection.setReadTimeout(250);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error sending API request to " + ipAddress + ":" + port + "/name - " + e.getMessage());
            return null;
        }
    }
}
