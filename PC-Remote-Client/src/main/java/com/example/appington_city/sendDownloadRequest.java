package com.example.appington_city;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class sendDownloadRequest {

    private static final String TAG = "DownloadRequest";

    public static void sendDownloadRequest(String url, String postString) {
        try {

            String postData = "{\"filename\": \"" + postString + "\"}";


            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);


            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }


            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadFolder.exists()) {
                    downloadFolder.mkdirs();
                }


                String fileExtension = getFileExtension(postString);


                File targetFolder = getTargetFolderForExtension(downloadFolder, fileExtension);


                if (!targetFolder.exists()) {
                    targetFolder.mkdirs();
                }

                // Datei speichern
                File file = new File(targetFolder, postString);
                try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                    Log.d(TAG, "Datei erfolgreich heruntergeladen und gespeichert: " + file.getAbsolutePath());
                }
            } else {
                Log.e(TAG, "Fehler beim Herunterladen der Datei. Statuscode: " + responseCode);
            }


            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }


    private static File getTargetFolderForExtension(File downloadFolder, String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            case "mp4":
            case "mkv":
            case "avi":
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            case "mp3":
            case "wav":
            case "flac":
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            case "pdf":
            case "doc":
            case "docx":
            case "xls":
            case "xlsx":
            case "ppt":
            case "pptx":
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            default:
                return downloadFolder;
        }
    }



    public static ArrayList<downloadable_file> sendGetRequest(String ipAddress) throws IOException {
        String endpoint = "http://" + ipAddress + ":5500" + "/downloadfolder"; // Endpunkt der API
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        ArrayList<downloadable_file> fileList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }


            JSONArray jsonArray = new JSONArray(response.toString());


            for (int i = 0; i < jsonArray.length(); i++) {
                String fileName = jsonArray.getString(i);
                downloadable_file file = new downloadable_file(fileName);
                fileList.add(file);
            }
            Log.d(TAG, "Received file list: " + fileList.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        connection.disconnect();
        return fileList;
    }

}

class downloadable_file {
    private  String path;

    public downloadable_file(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path;
    }
}
