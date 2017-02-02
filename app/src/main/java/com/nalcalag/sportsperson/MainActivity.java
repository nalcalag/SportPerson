package com.nalcalag.sportsperson;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    JSONObject response;
    Realm realm;
    String url = "https://gist.githubusercontent.com/joshheald/d26b89b0fbaf4e26cb423913ada21b83/raw/174a7ac0916919d4ae171adcfc0af78811b185f3/sportspeople.json";
    RealmList<Person> peopleList = new RealmList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get data from JSON and save it in a Realm Database
        new ProcessJSON().execute(url);

        RealmConfiguration sportspeople = new RealmConfiguration.Builder(getBaseContext())
                .name("sportspeople.realm").build();
        realm = Realm.getInstance(sportspeople);
    }

    public void launchAbout(View view){
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    private class ProcessJSON extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... strings) {
            String result;
            String urlString = strings[0];

            // Creating new JSON Parser
            JSONParser jParser = new JSONParser();
            result = jParser.GetHTTPData(urlString);

            // Return the data from specified url
            return result;
        }

        protected void onPostExecute(String stream) {
            if (stream != null) {
                try{
                    response = new JSONObject(stream);
//                  // Getting JSON Array node
                    JSONArray jsonArray = response.getJSONArray("sportspeople");
                    Log.d("JSON", jsonArray.toString());

                    // looping through All Sportspeople
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);

                        //encode unix date to string
                        int id = c.getInt("id");
                        String firstName = c.getString("first_name");
                        String lastName = c.getString("last_name");
                        String fullName = c.getString("full_name");
                        double points = c.getDouble("points");
                        String picture = c.getString("profile_picture_url");

                        //Set up Info_Publication RealmObject
                        realm.beginTransaction();

                        Person player = realm.createObject(Person.class);
                        player.setId(id);
                        player.setFirstName(firstName);
                        player.setLastName(lastName);
                        player.setFullName(fullName);
                        player.setPoints(points);
                        player.setPicture(picture);

                        peopleList.add(player);

                        realm.commitTransaction();
                    }

                }
                catch(final JSONException e){
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            }  else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            launchAbout(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
