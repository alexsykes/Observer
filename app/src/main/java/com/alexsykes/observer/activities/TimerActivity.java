package com.alexsykes.observer.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alexsykes.observer.NumberPadFragment;
import com.alexsykes.observer.R;
import com.alexsykes.observer.TimerAdapter;
import com.alexsykes.observer.data.FinishTimeContract;
import com.alexsykes.observer.data.FinishTimeDbHelper;
import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/* TODO Upload on close
    Add timeout / warning on upload failure
    Check for trialid from Setup
 */


public class TimerActivity extends AppCompatActivity  {
    // Constants
    public static final int TEXT_REQUEST = 1;
    private static final int NOT_SYNCED = -1;
    MediaPlayer mediaPlayer;

    final String uploadFilePath = "mnt/sdcard/Documents/Scoremonster/";
    // String uploadFileName = "times.csv";
    File exportDir = new File(Environment.getExternalStoragePublicDirectory("Documents/Scoremonster"), "");
    // Layout components
    NumberPadFragment numberPadFragment;
    TextView numberLabel, timeLabel;
    Button finishButton, processButton, startButton;
    LinearLayout dataEntry, setUp;
    RecyclerView rv;
    ProgressDialog dialog = null;

    // Global variables
    String[] theTrials, theIDs;
    int trialid;
    int serverResponseCode = 0;
    long starttime;
    String upLoadServerUri = null;
    String processURL = null;
    String getTrialsURL = null;
    String riderNumber;
    String theTrialName;
    String email;
    String ts = "Default";
    boolean isStartTimeSet;
    SharedPreferences localPrefs;
    ArrayList<HashMap<String, String>> theFinishTimes;
    private String filename;
    // Data handling
    private FinishTimeDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Activity
        setContentView(R.layout.activity_timer);

        // Add custom ActionBar
        Toolbar myToolbar = findViewById(R.id.timer_toolbar);
        myToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(myToolbar);
        myToolbar.getMenu();
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Add numberPad fragment
        numberPadFragment = new NumberPadFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.top, numberPadFragment).commit();

        // Local widgets
        numberLabel = findViewById(R.id.numberLabel);
        timeLabel = findViewById(R.id.timeLabel);
        finishButton = findViewById(R.id.finishButton);
        startButton = findViewById(R.id.startButton);
        processButton = findViewById(R.id.processButton);
        dataEntry = findViewById(R.id.dataEntry);
        setUp = findViewById(R.id.setUp);

        mDbHelper = new FinishTimeDbHelper(this);

        /*  PHP script paths  */
        upLoadServerUri = "http://www.trialmonster.uk/android/UploadToServer.php";

        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
/*                    if (trialid == 0 ){
                        processURL = "http://www.trialmonster.uk/android/emailTimes.php?ts=" + ts;
                    } else {
                        processURL = "http://www.trialmonster.uk/android/addTimestodb.php";
                    }*/
                    processCSV();
                } else {

                    Toast.makeText(TimerActivity.this, "Cannot Upload - no Internet connection", Toast.LENGTH_LONG).show();
                }
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
        checkPrefs();
    }

    private void checkPrefs() {
        // Get localPrefs and read values
        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);
        trialid = localPrefs.getInt("trialid", 999);
        starttime = localPrefs.getLong("starttime", -1);
        email = localPrefs.getString("email", "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Switch to timesheet Activity
            case R.id.timesheet:
                Intent intent = new Intent(this, TimeSheetActivity.class);
                intent.putExtra("trialid", trialid);
                startActivityForResult(intent, TEXT_REQUEST);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timer_menu, menu);
        return super.onCreateOptionsMenu(menu);
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


        riderNumber = numberLabel.getText().toString();
        if (riderNumber.equals("")) {

            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
            toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP2, 150);
             Toast.makeText(this, "Missing rider number", Toast.LENGTH_SHORT).show();
        } else {

            // Get time to start the clock
            long time = System.currentTimeMillis();
            String finishTime = dateFormat.format(time);
            long ridertime = time - starttime;

            timeLabel.setText(finishTime);
            riderNumber = numberLabel.getText().toString();

            // ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
            // Check for numberof completed laps
            // Gets the database in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_RIDER, riderNumber);
            values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_TIME, String.valueOf(time));

            values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_SYNC, NOT_SYNCED);

            long newRowId = db.insert(FinishTimeContract.FinishTimeEntry.TABLE_NAME, null, values);
            // Toast.makeText(this, "Time saved", Toast.LENGTH_LONG).show();
            numberLabel.setText("");
            playSoundFile(R.raw.ting);
        }
    }

    // Check for startTime already set in preferences
    @Override
    protected void onStart() {
        super.onStart();

        // Get shared preferences for trialid, section
        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);
        trialid = localPrefs.getInt("trialid", 999);
        isStartTimeSet = localPrefs.getBoolean("isStartTimeSet", false);
        starttime = localPrefs.getLong("starttime", -1);

        // Hide startButton if clock already started
        if (isStartTimeSet) {
            setUp.setVisibility(View.GONE);
            dataEntry.setVisibility(View.VISIBLE);
            processButton.setVisibility(View.VISIBLE);
            updateList();
        } else {
            dataEntry.setVisibility(View.GONE);
            processButton.setVisibility(View.GONE);
            setUp.setVisibility(View.VISIBLE);
            // Get trialList from server
            String URL = getTrialsURL;
            try {
               // getJSONDataset(URL);
            } catch (NullPointerException e) {
                Toast.makeText(TimerActivity.this, "Empty data", Toast.LENGTH_LONG).show();
            }
        }
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

    private void saveToCSV() {
        String number, finishtime;

/*        Date date = new Date();
        //getTime() returns current time in milliseconds
        long time = date.getTime();
        ts = String.valueOf(time);
        filename = "times_" + ts + ".csv";*/
        // Get timestamp and add to filename

        try {
            exportDir = new File(getFilesDir(), filename);

            exportDir.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(exportDir));

            String[] header = {"rider", "finishtime", String.valueOf(trialid), String.valueOf(starttime), email};

            csvWrite.writeNext(header, false);

            // Get current data
            Cursor cursor = mDbHelper.getTimesForUpload();
            while (cursor.moveToNext()) {
                number = cursor.getString(0);

                // Check for accidental clicks (no rider number)
                if (!number.isEmpty()) {
                    finishtime = cursor.getString(1);
                    String[] arrStr = {number, finishtime
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
                    numberLabel.setText("Source File not exist :"
                            + uploadFilePath + "" + filename);
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

    /*public void uploadTimes(View view) {
        saveToCSV();
        uploadFile(uploadFilePath + "" + uploadFileName);
    } */

    private void processCSV() {
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
                mDbHelper.markUploaded();
                updateList();
                dialog.dismiss();
            }

            //in this method we are fetching the json string
            @Override
            protected String doInBackground(Void... voids) {

                int response = uploadFile(uploadFilePath + filename);
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

    public void startClock(View view) {
        // Get time to start the clock
         starttime = System.currentTimeMillis();

        // Save in Prefs
        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);
        SharedPreferences.Editor editor = localPrefs.edit();
        editor.putLong("starttime", starttime);
        editor.putBoolean("isStartTimeSet", true);
        editor.putInt("trialid", trialid);
        editor.putString("theTrialName", theTrialName);
        editor.commit();

        // Set up activity for data entry
        startButton.setEnabled(false);
        startButton.setVisibility(View.GONE);
        mDbHelper.clearTimes();
        setUp.setVisibility(View.GONE);
        dataEntry.setVisibility(View.VISIBLE);
        processButton.setVisibility(View.VISIBLE);
    }


    // Check for connectivity
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    //play a soundfile
    public void playSoundFile(Integer fileName) {
        mediaPlayer = MediaPlayer.create(this, fileName);
        mediaPlayer.start();
    }
}
