package com.alexsykes.observer.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alexsykes.observer.ElapsedTimeListAdapter;
import com.alexsykes.observer.R;
import com.alexsykes.observer.data.FinishTimeDbHelper;
import com.opencsv.CSVWriter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

// TODO - note that times are only recorded for valid entries

public class TimeListActivity extends AppCompatActivity {
    // List theScores;
    ArrayList<HashMap<String, String>> theSummaryTimes;
    FinishTimeDbHelper finishTimeDbHelper;
    RecyclerView rv;
    SharedPreferences prefs;
    long starttime, startInterval;
    Button  processButton;
    ProgressDialog dialog = null;
    ArrayList<HashMap<String, String>> theFinishTimes;
    String email;
    String ts = "Default";
    int trialid;
    private String filename;
    final String uploadFilePath = "mnt/sdcard/Documents/Scoremonster/";
    int serverResponseCode = 0;
    String upLoadServerUri = null;
    File exportDir = new File(Environment.getExternalStoragePublicDirectory("Documents/Scoremonster"), "");

    String processURL = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_list);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        startInterval = prefs.getLong("startInterval", 0);
        starttime = prefs.getLong("starttime", 0);
        trialid = Integer.valueOf(prefs.getString("trialid","999"));

        // Create database connection
        finishTimeDbHelper = new FinishTimeDbHelper(this);
        theSummaryTimes = finishTimeDbHelper.getRidersFinishTimes(startInterval, trialid);

        // uploadButton = findViewById(R.id.uploadButton);
        processButton = findViewById(R.id.processButton);

        /*  PHP script paths  */
        upLoadServerUri = "http://www.trialmonster.uk/android/UploadToServer.php";
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()) {
                    // processCSV(processURL);
                    processTimes();
                    Toast.makeText(TimeListActivity.this, "Times uploaded",
                            Toast.LENGTH_SHORT).show();
                }
                else {

                    Toast.makeText(TimeListActivity.this, "Times cannot be uploaded at this time - no Internet connection. Please try again later",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkPrefs();

        rv = findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeAdapter();
    }
    private void checkPrefs() {
        // Get localPrefs and read values
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        trialid = Integer.valueOf(prefs.getString("trialid", "999"));
        starttime = prefs.getLong("starttime", -1);
        email = prefs.getString("email", "");
    }

    private void saveToCSV() {
        String number, finishtime, humantime;
        long dateValue;
        Date timestamp;

        try {
            exportDir = new File(getFilesDir(), filename);

            exportDir.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(exportDir));
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yy HH:mm:ss.S");
            humantime = DATE_FORMAT.format(starttime);

            String[] header = {"rider", "finishtime", String.valueOf(trialid), String.valueOf(starttime), email, humantime};
            String[] startData = {"0",String.valueOf(starttime), humantime };
            csvWrite.writeNext(header, false);
            csvWrite.writeNext(startData, false);

            // Get current data
            Cursor cursor = finishTimeDbHelper.getTimesForUpload(trialid);
            while (cursor.moveToNext()) {
                number = cursor.getString(0);

                // Check for accidental clicks (no rider number)
                if (!number.isEmpty()) {
                    finishtime = cursor.getString(1);
                    dateValue = Long.valueOf(finishtime);
                    timestamp = new Date(dateValue);
                    humantime = DATE_FORMAT.format(timestamp);
                    String[] arrStr = {number, finishtime, humantime
                    };

                    csvWrite.writeNext(arrStr, false);
                }
            }
            csvWrite.close();
            cursor.close();
        } catch (IOException e) {
            //Log.e("Child", e.getMessage(), e);
        }
    }

    private void processTimes() {
        /*
         * Processing the CSV done online
         * so we need an AsyncTask
         * The constrains defined here are
         * Void -> We are not passing anything
         * Void -> Nothing at progress update as well
         * String -> After completion it should return a string and it will be the json string
         * */
        class ProcessCSV extends AsyncTask<Void, Void, String> {

            //this method will be called before execution
            //you can display a progress bar or something
            //so that user can understand that he should wait
            //as network operation may take some time
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(TimeListActivity.this, "Scoremonster",
                        "Processing scores… this make take some time!", true);
                // Prepare CSV file

                Date date = new Date();
                //getTime() returns current time in milliseconds
                long time = date.getTime();
                ts = String.valueOf(time);
                filename = "times_" + ts + ".csv";
                saveToCSV();
            }

            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                // TODO Handle error response
                finishTimeDbHelper.markUploaded(trialid);
//                updateList();
                dialog.dismiss();
            }

            //in this method we are fetching the json string
            @Override
            protected String doInBackground(Void... voids) {

                int response = uploadFile(uploadFilePath + filename);

                // Email times for Manual Entry - trialid = 0
                try {
                    if (trialid == 0 ){
                        processURL = "http://www.trialmonster.uk/android/emailTimes.php?ts=" + ts + "&email=" + email;
                    } else {
                        processURL = "http://www.trialmonster.uk/android/addTimestodb.php";
                    }
                    //creating a URL
                    URL url = new URL(processURL);

                    //Opening the URL using HttpURLConnection
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    return con.getResponseMessage();

                } catch (Exception e) {
                    return null;
                }
            }
        }
        ProcessCSV processCSV = new ProcessCSV();
        processCSV.execute();
    }

    public int uploadFile(String sourceFileUri) {
        File directory = getFilesDir();
        File sourceFile = new File(directory, filename);


        //  String fileName = sourceFileUri;

        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        //File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    + uploadFilePath + "" + filename);

            runOnUiThread(new Runnable() {
                public void run() {
                 //   numberLabel.setText("Source File not exist :"+ uploadFilePath + "" + filename);
                    Toast.makeText(TimeListActivity.this, "Source File does not exist",
                            Toast.LENGTH_SHORT).show();
                }
            });

            return 0;

        } else {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", filename);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + filename + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Toast.makeText(TimerActivity.this, "Score Upload Complete",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                     //   numberLabel.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(TimeListActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                   //     numberLabel.setText("Got Exception : see logcat ");
                        Toast.makeText(TimeListActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file Exception", "Exception : "
                        + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;
        }
    }

    private void initializeAdapter() {
        ElapsedTimeListAdapter adapter = new ElapsedTimeListAdapter(theSummaryTimes);
        rv.setAdapter(adapter);
    }

    // Check for connectivity
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}