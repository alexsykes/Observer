package com.alexsykes.scoremonster;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alexsykes.scoremonster.data.ScoreDbHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

//TODO Validate section number
public class SetupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String BASE_URL = "https://android.trialmonster.uk/";
    TextView observerTextInput, sectionTextInput, trialDetailView;
    int trialid, section, numsections, numlaps;
    boolean showDabPad;
    String observer, theTrialName, detail;
    SharedPreferences localPrefs;
    ScoreDbHelper theDB;
    RadioGroup dabPadSwitch;
    RadioButton dabPadSelect, numberPadSelect;
    Spinner trialSelect;
    ProgressDialog dialog = null;
    ArrayList<HashMap<String, String>> theTrialList;
    CheckBox resetCheckBox;
    String[] theTrials, theIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        theDB = new ScoreDbHelper(this);

        // Set up activity fields
        observerTextInput = findViewById(R.id.observerTextInput);
        sectionTextInput = findViewById(R.id.sectionTextInput);
        trialDetailView = findViewById(R.id.trialDetailView);
        dabPadSwitch = findViewById(R.id.padViewGroup);
        dabPadSelect = findViewById(R.id.dabPadSelect);
        numberPadSelect = findViewById(R.id.numberPadSelect);
        resetCheckBox = findViewById(R.id.resetCheckBox);

        resetCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(getApplicationContext(), "This will destroy all saved scores!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set up spinner
        trialSelect = (Spinner) findViewById(R.id.trialSelect);
        trialSelect.setOnItemSelectedListener(this);

        if (checkPrefs()) {

        }

        // Get trialList from server
        String URL = BASE_URL + "getTrialList.php";
        getJSONDataset(URL);
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
                dialog = ProgressDialog.show(SetupActivity.this, "Scoremonster", "Getting trial list", true);
            }

            /* this method will be called after execution

                s contains trial details in JSON string
             */

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                dialog.dismiss();

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

                // Setting the ArrayAdapter data on the Spinner
                trialSelect.setAdapter(aa);
                trialSelect.setSelection(aa.getPosition(theTrialName));
            }

            /*
            @param String json JSON string returned from MySQL
            @return ArrayList of trials data
             */
            private ArrayList<HashMap<String, String>> populateResultArrayList(String json) {
                ArrayList<HashMap<String, String>> theTrialList = new ArrayList<>();
                String date, name, id, club, numsections, numlaps;

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

                        // trial = club + " - " + name;
                        theTrial.put("id", id);
                        theTrial.put("date", date);
                        theTrial.put("club", club);
                        theTrial.put("name", name);
                        theTrial.put("numsections", numsections);
                        theTrial.put("numlaps", numlaps);
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

                try {
                    //creating a URL
                    URL url = new URL(urlWebService);

                    //Opening the URL using HttpURLConnection
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    //StringBuilder object to read the string from the service
                    StringBuilder sb = new StringBuilder();

                    //We will use a buffered reader to read the string from service
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    //A simple string to read values from each line
                    String json;

                    //reading until we don't find null
                    while ((json = bufferedReader.readLine()) != null) {

                        //appending it to string builder
                        sb.append(json + "\n");
                    }

                    //finally returning the read string
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }

            }
        }

        //creating asynctask object and executing it
        GetData getJSON = new GetData();
        getJSON.execute();
    }

    private boolean checkPrefs() {
        boolean prefsSet = true;
        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);
        trialid = localPrefs.getInt("trialid", 0);
        theTrialName = localPrefs.getString("theTrialName", null);
        numsections = localPrefs.getInt("numsections", 0);
        numlaps = localPrefs.getInt("numlaps", 0);
        observer = localPrefs.getString("observer", "");
        section = localPrefs.getInt("section", 0);
        showDabPad = localPrefs.getBoolean("showDabPad", false);

        observerTextInput.setText(observer);
        sectionTextInput.setText(String.valueOf(section));


        //trialTextInput.setText(String.valueOf(trialid));
        //numlapsTextInput.setText(String.valueOf(numlaps));
        //numsectionsTextInput.setText(String.valueOf(numsections));

        if (showDabPad) {
            dabPadSelect.setChecked(true);
        } else {
            numberPadSelect.setChecked(true);
        }

        if (observer.equals("") || section == 0 || trialid == 0 || numlaps == 0 || numsections == 0) {
            prefsSet = false;
        }
        return prefsSet;
    }

    public void setPrefs(View view) {

        //trialid = Integer.parseInt(trialTextInput.getText().toString());
        //numsections = Integer.parseInt(numsectionsTextInput.getText().toString());
        //numlaps = Integer.parseInt(numlapsTextInput.getText().toString());
        section = Integer.parseInt(sectionTextInput.getText().toString());
        observer = observerTextInput.getText().toString();
        // theTrialName = nameTextView.getText().toString();

        boolean reset = resetCheckBox.isChecked();

        if(reset){
        new AlertDialog.Builder(this)
                .setTitle("Reset Scores")
                .setMessage("Your saved scores will be deleted.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        theDB.clearResults();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
        }


        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);

        if (observer.equals("") || section == 0) {
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_LONG).show();
            return;
        } else {

            SharedPreferences.Editor editor = localPrefs.edit();
            editor.putString("theTrialName", theTrialName);
            editor.putInt("trialid", trialid);
            editor.putInt("numsections", numsections);
            editor.putInt("numlaps", numlaps);
            editor.putString("observer", observer);
            editor.putInt("section", section);
            editor.putBoolean("showDabPad", dabPadSelect.isChecked());
            boolean success = editor.commit();
            finish();
        }
    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {

        HashMap theTrial = theTrialList.get(position);
        numsections = Integer.parseInt(theTrial.get("numsections").toString());
        numlaps = Integer.parseInt(theTrial.get("numlaps").toString());
        trialid = Integer.parseInt(theTrial.get("id").toString());
        theTrialName = theTrial.get("name").toString();

        detail = theTrialName + "\n" + numlaps + " laps \n" + numsections + " sections";
        trialDetailView.setText(detail);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
// TODO Auto-generated method stub
    }

}
