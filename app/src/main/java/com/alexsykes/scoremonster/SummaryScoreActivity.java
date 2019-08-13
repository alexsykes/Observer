package com.alexsykes.scoremonster;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


// TODO modify to take account of o and x - ?done in SQL


public class SummaryScoreActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://android.trialmonster.uk/";
    TableLayout resultTable;
    int trialid, numsections, numlaps;
    ArrayList<HashMap<String, String>> theResultList;
    ProgressDialog dialog = null;
    SharedPreferences localPrefs;
    int backgroundColor = Color.parseColor("#40bdc0d4");
    int white = Color.parseColor("#ffffff");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_score);

        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);

        numlaps = localPrefs.getInt("numlaps", 0);
        numsections = localPrefs.getInt("numsections", 0);

        trialid = getIntent().getExtras().getInt("trialid");
        String URL = BASE_URL + "getSummaryScores.php?id=" + trialid;
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
                dialog = ProgressDialog.show(SummaryScoreActivity.this, "Scoremonster", "Getting leaderboard…", true);
            }

            //this method will be called after execution

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                dialog.dismiss();
                theResultList = populateResultArrayList(s);
                // Define table for results
                resultTable = findViewById(R.id.result_table);
                displayResultTable(theResultList);

                resultTable = findViewById(R.id.result_table);
            }

            private ArrayList<HashMap<String, String>> populateResultArrayList(String json) {
                ArrayList<HashMap<String, String>> theResultList = new ArrayList<>();

                try {
                    // Parse string data into JSON
                    JSONArray jsonArray = new JSONArray(json);

                    for (int index = 0; index < jsonArray.length(); index++) {
                        HashMap<String, String> theResult = new HashMap<>();
                        String rider = jsonArray.getJSONObject(index).getString("rider");
                        String sections = jsonArray.getJSONObject(index).getString("sections");
                        String scorelist = jsonArray.getJSONObject(index).getString("scorelists");
                        String totalscore = jsonArray.getJSONObject(index).getString("totalscore");
                        theResult.put("rider", rider);
                        theResult.put("scorelist", scorelist);
                        theResult.put("sections", sections);
                        theResult.put("totalscore", totalscore);
                        theResultList.add(theResult);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return theResultList;
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

    private void displayResultTable(ArrayList<HashMap<String, String>> theResultList) {
        TableRow tr = new TableRow(this);
        TextView cell = new TextView(this);
        cell.setText("Rider");
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(8, 8, 8, 8);
        tr.addView(cell);


        for (int col = 1; col < numsections + 1; col++) {
            cell = new TextView(this);
            cell.setText("" + col);
            cell.setGravity(Gravity.CENTER);
            resultTable.setColumnShrinkable(col, true);

            cell.setWidth(2000);
            cell.setPadding(8, 8, 8, 8);
            tr.addView(cell);
        }
        cell = new TextView(this);
        cell.setText("Total");
        cell.setPadding(8, 8, 8, 8);
        cell.setWidth(120);
        cell.setGravity(Gravity.CENTER);
        tr.addView(cell);

        resultTable.addView(tr);


        int numRiders = theResultList.size();

        // Iterate through result list
        // Adding a row for each one
        for (int index = 0; index < numRiders; index++) {

            tr = new TableRow(this);
            if (index % 2 != 0) {
                tr.setBackgroundColor(backgroundColor);
            } else {
                tr.setBackgroundColor(white);
            }

            HashMap<String, String> theResult = theResultList.get(index);

            // Get data from arraylist
            String rider = theResult.get("rider");
            String sections = theResult.get("sections");
            String scorelist = theResult.get("scorelist");

            // Then split to create arrays for section scores
            // and create a pointer to go through arrays
            // // to take account of missing sections

            int pointer = 0;
            int numscores;
            String[] theSectionArray = sections.split(",");
            String[] theScoreArray = scorelist.split(",");
            String[] theScoreValues = new String[numsections];

            numscores = theSectionArray.length;

            //
            for (int i = 0; i < numscores; i++) {
                pointer = Integer.valueOf(theSectionArray[i]) - 1;
                theScoreValues[pointer] = theScoreArray[i];
            }

            // Add rider number at start of each line
            cell = new TextView(this);
            cell.setText(rider);
            cell.setGravity(Gravity.END);
            cell.setPadding(8, 8, 8, 8);
            tr.addView(cell);

            // Iterate through sections, adding rider number, section scores, total
            for (int section = 0; section < numsections; section++) {
                cell = new TextView(this);


                if (theScoreValues[section] != null) {
                    cell.setText(theScoreValues[section]);
                } else {
                    //   cell.setText("|");
                }

                cell.setPadding(40, 8, 8, 8);
                //cell.setGravity(Gravity.END);
                tr.addView(cell);
            }

            // Add cell for total
            cell = new TextView(this);
            String total = theResult.get("totalscore");
            cell.setText(total);
            cell.setGravity(Gravity.END);
            cell.setPadding(24, 8, 40, 8);
            tr.addView(cell);

            resultTable.addView(tr);
        }
    }
}
