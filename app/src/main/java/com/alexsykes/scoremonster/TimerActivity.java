package com.alexsykes.scoremonster;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alexsykes.scoremonster.data.FinishTimeContract;
import com.alexsykes.scoremonster.data.FinishTimeDbHelper;
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
import java.util.HashMap;

public class TimerActivity extends AppCompatActivity {
    final String uploadFilePath = "mnt/sdcard/Documents/Scoremonster/";
    final String uploadFileName = "times.csv";
    ProgressDialog dialog = null;
    String upLoadServerUri = null;
    String processURL = null;
    long startTime;

    NumberPadFragment numberPadFragment;
    TextView numberLabel, timeLabel;
    String riderNumber;
    int trialid;
    int serverResponseCode = 0;
    Button finishButton, processButton;
    private FinishTimeDbHelper mDbHelper;
    private String filename;
    Cursor theTimesCursor;
    ArrayList<HashMap<String, String>> theFinishTimes;
    RecyclerView rv;
    File exportDir = new File(Environment.getExternalStoragePublicDirectory("Documents/Scoremonster"), "");
    Intent intent;
    SharedPreferences localPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer_alt);

        // Get shared preferences for trialid, section
        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);
        trialid = localPrefs.getInt("trialid", 999);
        startTime = localPrefs.getLong("startTime", 0);

        numberPadFragment = new NumberPadFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.top, numberPadFragment).commit();
        numberLabel = findViewById(R.id.numberLabel);
        timeLabel = findViewById(R.id.timeLabel);
        finishButton = findViewById(R.id.finishButton);
        mDbHelper = new FinishTimeDbHelper(this);


        processButton = findViewById(R.id.processButton);

        /*  PHP script path  */
        upLoadServerUri = "http://www.trialmonster.uk/android/UploadToServer.php";
        processURL = "http://www.trialmonster.uk/android/addTimestodb.php";

        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCSV(processURL);
            }
        });

        // Set up button to add a new finisher
        // then update time display
        finishButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveFinishTime();
                updateList();
                return false;
            }
        });

        updateList();
    }

    private void updateList() {
        //theTimesCursor = mDbHelper.getFinishTimes();
        theFinishTimes = mDbHelper.getTimes();

        rv = findViewById(R.id.rvTimer);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeAdapter();
    }

    private void initializeAdapter() {
        TimerAdapter adapter = new TimerAdapter(theFinishTimes);
        rv.setAdapter(adapter);
    }

    private void saveFinishTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        // Date finish = new Date();
        // String finishTime  = dateFormat.format(finish);


        // Get time to start the clock
        long time = System.currentTimeMillis();
        String finishTime = dateFormat.format(time);
        long ridertime = time - startTime;

        timeLabel.setText(finishTime);
        riderNumber = numberLabel.getText().toString();

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        // Check for numberof completed laps
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_RIDER,riderNumber);
        values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_TIME, String.valueOf(time));
        values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_RIDE_TIME, String.valueOf(ridertime));

        long newRowId = db.insert(FinishTimeContract.FinishTimeEntry.TABLE_NAME, null,values);
        Toast.makeText(this, "Time saved", Toast.LENGTH_LONG).show();
        numberLabel.setText("");
    }

    @Override
    protected void onStart() {

        super.onStart();
    }


    public void addDigit(View view) {
        // Get length of rider riderNumber
        numberLabel = findViewById(R.id.numberLabel);
        riderNumber = numberLabel.getText().toString();
        int len = riderNumber.length();

        // Get id from clicked button to get clicked digit
        int intID = view.getId();
        Button button = view.findViewById(intID);
        String digit = button.getText().toString();

        // Compare with backspace
        if (digit.equals("⌫")) {
            if (len > 0) {
                riderNumber = riderNumber.substring(0, len - 1);
            }
        } else if (digit.equals("C")) {
            riderNumber = "";
        } else {
            riderNumber = riderNumber + digit;
            if (len > 2)
                riderNumber = riderNumber.substring(1, 4);
        }

        if (riderNumber.equals("0")) {
            riderNumber = "";
        }
        numberLabel.setText(riderNumber);
    }

    private boolean saveToCSV() {
        String number, finishtime, ridetime;

        filename = "times.csv";

        try {
            exportDir = new File(getFilesDir(), filename);

            exportDir.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(exportDir));

            String[] header = {"rider", "finishtime", String.valueOf(trialid), String.valueOf(startTime)};

            csvWrite.writeNext(header, false);

            // Get current data
            Cursor cursor = mDbHelper.getTimesForUpload();
            while (cursor.moveToNext()) {
                number = cursor.getString(0);

                // Check for accidental clicks (no rider number)
                if (!number.isEmpty()) {
                    finishtime = cursor.getString(1);
                    //ridetime = cursor.getString(2);

                    String[] arrStr = {number, finishtime
                    };

                    csvWrite.writeNext(arrStr, false);
                }
            }
            csvWrite.close();
            cursor.close();
            return true;

        } catch (IOException e) {
            //Log.e("Child", e.getMessage(), e);
            return false;
        }
    }

    public int uploadFile(String sourceFileUri) {
        File directory = getFilesDir();
        File sourceFile = new File(directory, filename);


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
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
                    + uploadFilePath + "" + uploadFileName);
// TODO change numberLabel to something better
            runOnUiThread(new Runnable() {
                public void run() {
                    numberLabel.setText("Source File not exist :"
                            + uploadFilePath + "" + uploadFileName);
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
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

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

                // send multipart form data necesssary after file data...
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
                            Toast.makeText(TimerActivity.this, "Score Upload Complete",
                                    Toast.LENGTH_SHORT).show();
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
                        numberLabel.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(TimerActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        numberLabel.setText("Got Exception : see logcat ");
                        Toast.makeText(TimerActivity.this, "Got Exception : see logcat ",
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

    public void uploadTimes(View view) {
        saveToCSV();
        uploadFile(uploadFilePath + "" + uploadFileName);
    }

    private void processCSV(final String urlWebService) {
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
                dialog = ProgressDialog.show(TimerActivity.this, "Scoremonster",
                        "Processing scores… this make take some time!", true);
                // Prepare CSV file
                saveToCSV();
            }

            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                dialog.dismiss();
            }

            //in this method we are fetching the json string
            @Override
            protected String doInBackground(Void... voids) {

                int response = uploadFile(uploadFilePath + uploadFileName);
                try {
                    //creating a URL
                    URL url = new URL(urlWebService);

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
}
