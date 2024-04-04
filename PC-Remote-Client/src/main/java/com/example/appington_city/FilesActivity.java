package com.example.appington_city;

import static com.example.appington_city.sendDownloadRequest.sendDownloadRequest;
import static com.example.appington_city.sendDownloadRequest.sendGetRequest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FilesActivity extends AppCompatActivity {

    private static final String IP_KEY = "connection_ip";
    private static final String PREF_NAME = "app_settings";
    private SharedPreferences sharedPreferences;
    private String TAG = "FileUploader";
    private static String IP;
    private static final String Port = ":5500";
    private static final int REQUEST_PICK_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        IP = sharedPreferences.getString(IP_KEY, "default_value_if_not_found");
        setContentView(R.layout.activity_files);

        Button uploadButton = findViewById(R.id.uploadbutton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload_clicked();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<downloadable_file> fileList = sendGetRequest(IP);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            renderButtons(fileList);
                        }
                    });
                } catch (final IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Handle error here, e.g., show a toast
                            Log.e(TAG, "Error retrieving file list: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                Uri selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    Log.d(TAG, "Selected file URI: " + selectedFileUri.toString()); // Lognachricht hinzugefügt
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadFile(selectedFileUri);
                        }
                    }).start();
                }
            }
        }
    });

    private void upload_clicked() {
        Log.d(TAG, "Upload button clicked");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }

    private void uploadFile(Uri selectedFileUri) {
        try {
            Log.d(TAG, "Starting file upload process");
            URL url = new URL("http://" + IP + ":5500/upload");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();


            connection.setRequestMethod("POST");
            connection.setDoOutput(true);


            String boundary = Long.toHexString(System.currentTimeMillis());
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);


            String fileName = getFileName(selectedFileUri);
            if (fileName.isEmpty()) {
                Log.e(TAG, "Failed to extract file name from URI");
                return;
            }


            try (OutputStream output = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(output, true)) {


                writer.println("--" + boundary);
                writer.println("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"");
                writer.println("Content-Type: application/octet-stream");
                writer.println();


                try (InputStream inputStream = getContentResolver().openInputStream(selectedFileUri)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }

                writer.println();


                writer.println("--" + boundary + "--");
            }


            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "File uploaded successfully");
                showUploadResultToast("File uploaded successfully");
            } else {
                Log.e(TAG, "Failed to upload file. Response code: " + responseCode);
                showUploadResultToast("Failed to upload file. Response code: " + responseCode);
            }


            connection.disconnect();
            Log.d(TAG, "File upload process finished");

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error uploading file: " + e.getMessage());
            showUploadResultToast("Error uploading file: " + e.getMessage());
        }
    }


    private void showUploadResultToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private String getFileName(Uri uri) {
        String fileName = "";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                fileName = cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name from URI: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }









    private void renderButtons(ArrayList<downloadable_file> fileList) {
        LinearLayout layout = findViewById(R.id.button_layout);
        layout.removeAllViews();

        for (downloadable_file file : fileList) {
            Button button = new Button(this);
            button.setText(file.getPath());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = file.getPath();
                    String url = "http://" + IP +":5500/download";


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendDownloadRequest(url, path);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showSuccessToast(FilesActivity.this, "Successfully downloaded file");
                                }
                            });
                        }
                    }).start();
                }
            });
            layout.addView(button);
        }
    }

    private static void showSuccessToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
