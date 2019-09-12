package com.alexsykes.scoremonster.activities;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alexsykes.scoremonster.R;
import com.alexsykes.scoremonster.SummaryListAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

// TODO: 2019-08-03 Get results from entry list

public class SummaryActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://android.trialmonster.uk/";
    int trialid, section;
    ArrayList<HashMap<String, String>> theResultList;
    RecyclerView rv;
    LinearLayoutManager llm;
    ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        llm = new LinearLayoutManager(this);


        trialid = getIntent().getExtras().getInt("trialid");
        section = getIntent().getExtras().getInt("section");
        String URL = BASE_URL + "getSectionScores.php?id=" + trialid + "&section=" + section;
        getJSONDataset(URL);
    }

    //this method is actually fetching the json string
    // Dataset is defined by URL

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
                dialog = ProgressDialog.show(SummaryActivity.this, "Scoremonster", "Getting leaderboard…", true);
            }

            //this method will be called after execution

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                dialog.dismiss();
                theResultList = populateResultArrayList(s);
                displayResultArrayList();
            }

            private ArrayList<HashMap<String, String>> populateResultArrayList(String json) {
                ArrayList<HashMap<String, String>>theResultList = new ArrayList<>();

                try {
                    // Parse string data into JSON
                    JSONArray jsonArray = new JSONArray(json);

                    for (int index = 0; index < jsonArray.length(); index++) {
                        HashMap<String, String> theResult = new HashMap<>();
                        String rider = jsonArray.getJSONObject(index).getString("rider");
                        String laps = jsonArray.getJSONObject(index).getString("laps");
                        String scores = jsonArray.getJSONObject(index).getString("scores");
                        String total = jsonArray.getJSONObject(index).getString("total");
                        theResult.put("rider", rider);
                        theResult.put("laps", laps);
                        theResult.put("scores", scores);
                        theResult.put("total", total);
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

    private void displayResultArrayList() {

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        initializeAdapter();
    }

    private void initializeAdapter() {
        SummaryListAdapter adapter = new SummaryListAdapter(theResultList);
        rv.setAdapter(adapter);
    }

}
