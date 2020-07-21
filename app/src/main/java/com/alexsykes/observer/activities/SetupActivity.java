package com.alexsykes.observer.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alexsykes.observer.R;
import com.alexsykes.observer.data.FinishTimeDbHelper;
import com.alexsykes.observer.data.ScoreDbHelper;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

// TODO Manual entry needs fields unhiding
// TODO Cancel button does not work on spinner
// TODO Add check on sectionNumber
public class SetupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Set up data fields
    private static final String BASE_URL = "https://android.trialmonster.uk/";
    int trialid, section, numLaps, numSections;
    String observer, theTrialName, detail, email;
    // long startTime;
    String[] theTrials, theIDs;
    ArrayList<HashMap<String, String>> theTrialList;

    SharedPreferences localPrefs;

    // Database access
    ScoreDbHelper theScoreDB;
    FinishTimeDbHelper theFinishTimeDB;

    Spinner trialSelect;
    ProgressDialog dialog = null;
    CheckBox resetCheckBox, confirmCheckBox;
    LinearLayout scoreDetails, trialDetailsInput;
   // TextInputLayout observerView, sectionNumberView, trialNameView, emailView, numSectionsView, numLapsView;
    TextView observerTextInput, sectionNumberTextInput, trialNameTextInput, emailTextInput, numSectionsTextInput, numLapsTextInput, trialDetailView;
    ImageView warningImageView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        theScoreDB = new ScoreDbHelper(this);
        theFinishTimeDB = new FinishTimeDbHelper(this);

        // Set up layout labels and fields
        setUp();
        // Get saved values or defaults
        checkPrefs();

        // Check for online status
        if (isOnline()) {
            // Get trialList from server
            String URL = BASE_URL + "getTrialList.php";
            scoreDetails.setVisibility(View.VISIBLE);
            trialDetailsInput.setVisibility(View.GONE);
            try {
                getJSONDataset(URL);
            } catch (NullPointerException e) {
                Toast.makeText(SetupActivity.this, "Empty data", Toast.LENGTH_LONG).show();
            }
        } else {
            // If no internet, use form with any known data
            trialSelect.setVisibility(View.GONE);
            trialDetailsInput.setVisibility(View.VISIBLE);
            numLapsTextInput.setText(String.valueOf(numLaps));
            sectionNumberTextInput.setText(String.valueOf(section));
            trialNameTextInput.setText(theTrialName);
            Toast.makeText(SetupActivity.this, "Cannot load trials list - no Internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private void setUp() {
        // Setup LinearLayouts
        trialDetailsInput = findViewById(R.id.trialDetailsInput);
        scoreDetails = findViewById(R.id.scoreDetails);

        // Set up activity fields
        observerTextInput = findViewById(R.id.observerTextInput);
        trialNameTextInput = findViewById(R.id.trialNameTextInput);
        emailTextInput = findViewById(R.id.emailTextInput);
        sectionNumberTextInput = findViewById(R.id.sectionNumberTextInput);
        numSectionsTextInput = findViewById(R.id.numSectionsTextInput);
        numLapsTextInput = findViewById(R.id.numLapsTextInput);
        trialNameTextInput = findViewById(R.id.trialNameTextInput);

        // Label
        trialDetailView = findViewById(R.id.trialDetailView);

        // Reset
        resetCheckBox = findViewById(R.id.resetCheckBox);
        confirmCheckBox = findViewById(R.id.confirmCheckBox);
        warningImageView = findViewById(R.id.warningImageView);

        // Save button
        button = findViewById(R.id.button);

        // trialNameView = findViewById(R.id.trialNameView);


        confirmCheckBox.setVisibility(View.GONE);
        warningImageView.setVisibility(View.GONE);

        resetCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    confirmCheckBox.setVisibility(View.VISIBLE);
                    warningImageView.setVisibility(View.VISIBLE);
                    button.setEnabled(false);

                } else {
                    confirmCheckBox.setVisibility(View.GONE);
                    warningImageView.setVisibility(View.GONE);
                    button.setEnabled(true);
                }
            }
        });


        confirmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    confirmCheckBox.setVisibility(View.VISIBLE);
                    button.setEnabled(true);

                } else {
                    button.setEnabled(false);
                }
            }
        });


        // Set up spinner
        trialSelect = findViewById(R.id.trialSelect);
        trialSelect.setOnItemSelectedListener(this);

        // Sync inputs to saved values
//        observerTextInput.setText(observer);
//        sectionNumberTextInput.setText(String.valueOf(section));
//        emailTextInput.setText(email);
//        numSectionsTextInput.setText(String.valueOf(numSections));
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void getJSONDataset(final String urlWebService) {
        /*
         * As fetching the json string is a network operation
         * And we cannot perform a network operation in main thread
         * so we need an AsyncTask
         * The constrains defined here are
         * Void -> We are not passing anything
         * Void -> Nothing at progress update as well
         * String -> After completion it should return a string and it will be the json string
         * */
        class GetData extends AsyncTask<Void, Void, String> {

            //this method will be called before execution

            @Override
            protected void onPreExecute() {

                super.onPreExecute();
                // Show dialog during server transaction
                // dialog = ProgressDialog.show(SetupActivity.this, "Scoremonster", "Getting trial list", true);
                dialog = new ProgressDialog(SetupActivity.this);
                dialog.setMessage("Loading…");
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }


            /* this method will be called after execution

                s contains trial details in JSON string
             */

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                dialog.dismiss();
                String tempName = "Manual Entry";

                // Populate ArrayList with JSON data
                theTrialList = populateResultArrayList(s);

                theTrials = new String[theTrialList.size()];
                theIDs = new String[theTrialList.size()];

                for (int index = 0; index < theTrialList.size(); index++) {
                    theTrials[index] = theTrialList.get(index).get("name");
                    theIDs[index] = theTrialList.get(index).get("id");
                }

                // Set up Spinner
                ArrayAdapter aa = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, theTrials);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                trialSelect.setSelection(aa.getPosition(theTrialName));

                // Setting the ArrayAdapter data on the Spinner
                trialSelect.setAdapter(aa);
                if (trialid == 0) {
                    trialSelect.setSelection(aa.getPosition(tempName));
                }
            }

            /*
            @param String json JSON string returned from MySQL
            @return ArrayList of trials data
             */
            private ArrayList<HashMap<String, String>> populateResultArrayList(String json) {
                ArrayList<HashMap<String, String>> theTrialList = new ArrayList<>();
                String date, name, id, club, numsections, numlaps, starttime, email;

                try {
                    // Parse string data into JSON
                    JSONArray jsonArray = new JSONArray(json);

                    for (int index = 0; index < jsonArray.length(); index++) {
                        HashMap<String, String> theTrial = new HashMap<>();
                        id = jsonArray.getJSONObject(index).getString("id");
                        date = jsonArray.getJSONObject(index).getString("date");
                        club = jsonArray.getJSONObject(index).getString("club");
                        name = jsonArray.getJSONObject(index).getString("name");
                        numsections = jsonArray.getJSONObject(index).getString("numsections");
                        numlaps = jsonArray.getJSONObject(index).getString("numlaps");
                        starttime = jsonArray.getJSONObject(index).getString("starttime");
                        email = jsonArray.getJSONObject(index).getString("email");

                        // trial = club + " - " + name;
                        theTrial.put("id", id);
                        theTrial.put("date", date);
                        theTrial.put("club", club);
                        theTrial.put("name", name);
                        theTrial.put("numsections", numsections);
                        theTrial.put("numlaps", numlaps);
                        theTrial.put("starttime", starttime);
                        theTrial.put("email", email);
                        theTrialList.add(theTrial);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return theTrialList;
            }

            //in this method we are fetching the json string
            @Override
            protected String doInBackground(Void... voids) {
                int TIMEOUT_VALUE = 1000;
                try {
                    //creating a URL
                    URL url = new URL(urlWebService);

                    //Opening the URL using HttpURLConnection
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(TIMEOUT_VALUE);
                    con.setReadTimeout(TIMEOUT_VALUE);
                    //StringBuilder object to read the string from the service
                    StringBuilder sb = new StringBuilder();

                    //We will use a buffered reader to read the string from service
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    //A simple string to read values from each line
                    String json;

                    //reading until we don't find null
                    while ((json = bufferedReader.readLine()) != null) {
                        json = json + "\n";
                        //appending it to string builder
                        sb.append(json);
                    }

                    //finally returning the read string
                    return sb.toString().trim();
                } catch (SocketTimeoutException e) {
                    Toast.makeText(SetupActivity.this, "SocketTimeoutException", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        //creating asynctask object and executing it
        GetData getJSON = new GetData();
        getJSON.execute();
    }

    private void checkPrefs() {
        // Get localPrefs and read values
        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);
        trialid = localPrefs.getInt("trialid", 0);
        theTrialName = localPrefs.getString("theTrialName", null);
        numLaps = localPrefs.getInt("numLaps", 0);
        observer = localPrefs.getString("observer", "");
        email = localPrefs.getString("email", "");
        section = localPrefs.getInt("section", 0);
        numSections = localPrefs.getInt("numSections", 0);

        // Put values into text fields
        observerTextInput.setText(observer);
        numLapsTextInput.setText(String.valueOf(numLaps));
        sectionNumberTextInput.setText(String.valueOf(section));
        trialNameTextInput.setText(theTrialName);
        emailTextInput.setText(email);
        numLapsTextInput.setText(String.valueOf(numLaps));
        numSectionsTextInput.setText(String.valueOf(numSections));
    }

    public void setPrefs(View view) {
        // Field validation routine
        boolean hasErrors = false;

        // Common to all trials
        // Set errorMsg with initial message
        String errorMsg = "The following error(s) need to be corrected:";

        // Check that observer field is complete
        observer = observerTextInput.getText().toString();
        if (observer.equals("")) {
            // If empty, then append message
            hasErrors = true;
            errorMsg += "\nThe observer field is empty";
        }
        if (trialid == 0) {

            // Check that trial name field is complete
            theTrialName = trialNameTextInput.getText().toString();
            if (theTrialName.equals("")) {
                // If empty, then append message
                hasErrors = true;
                errorMsg += "\nThe trial name field is empty";
            }

            // Check that email field is complete
            email = emailTextInput.getText().toString();
            if (email.equals("")) {
                // If empty, then append message
                hasErrors = true;
                errorMsg += "\nThe email field is empty";
            }

            // Check number of laps validity
            numLaps = Integer.parseInt(numLapsTextInput.getText().toString());
            // If out of range, then append message
            if (numLaps == 0) {
                hasErrors = true;
                errorMsg += "\nInvalid number of laps";
            }

            // Check number of laps validity
            numSections = Integer.parseInt(numSectionsTextInput.getText().toString());
            // If out of range, then append message
            if (numSections == 0) {
                hasErrors = true;
                errorMsg += "\nInvalid number of laps";
            }

            // Check number of laps validity
            section = Integer.parseInt(sectionNumberTextInput.getText().toString());
            // If out of range, then append message

        }

        // Check that section field is populated after manual data has been entered
        if (sectionNumberTextInput.getText().toString().equals("")) {
            // If empty, then append message
            hasErrors = true;
            errorMsg += "\nThe section field is empty";
        }

        // Inform user if errors
        if (hasErrors) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        } else {
            // otherwise save values
            localPrefs = getSharedPreferences("monster", MODE_PRIVATE);
            SharedPreferences.Editor editor = localPrefs.edit();
            editor.putString("theTrialName", theTrialName);
            editor.putInt("trialid", trialid);
            editor.putInt("numLaps", numLaps);
            editor.putInt("numSections", numSections);
            editor.putString("observer", observer);
            editor.putString("email", email);
            editor.putInt("section", section);
            editor.commit();


            // Read resetCheckBox
            boolean reset = resetCheckBox.isChecked();

            if (reset) {
                theScoreDB.clearResults();
                theFinishTimeDB.clearTimes();
            }
            finish();
        }
    }

    // Reading trial details into variables
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {

        HashMap theTrial = theTrialList.get(position);
        trialid = Integer.parseInt(theTrial.get("id").toString());
        String message = "TrialID: " + trialid;

        if (trialid == 0) {
            trialDetailsInput.setVisibility(View.VISIBLE);
//            numLapsView.setVisibility(View.VISIBLE);
//            sectionNumberView.setVisibility(View.VISIBLE);
//            trialNameView.setVisibility(View.VISIBLE);
//
//            numLapsTextInput.setText(String.valueOf(numLaps));
//            sectionNumberTextInput.setText(String.valueOf(section));
//            trialNameTextInput.setText(theTrialName);
//            emailTextInput.setText(email);
//            numLapsTextInput.setText(String.valueOf(numLaps));
//            numSectionsTextInput.setText(String.valueOf(numSections));
        } else {
            trialDetailsInput.setVisibility(View.GONE);
            numLaps = Integer.parseInt(theTrial.get("numlaps").toString());
            numSections = Integer.parseInt(theTrial.get("numsections").toString());
            trialid = Integer.parseInt(theTrial.get("id").toString());
            theTrialName = theTrial.get("name").toString();
            email = theTrial.get("email").toString();
//            numLapsView.setVisibility(View.INVISIBLE);
//            sectionNumberTextInput.setVisibility(View.INVISIBLE);
//            trialNameView.setVisibility(View.INVISIBLE);
        }
        detail = theTrialName + "\n" + numLaps + " laps \n" + numSections + " sections \nEmail: " + email;
        trialDetailView.setText(detail);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
