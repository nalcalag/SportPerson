package com.nalcalag.sportsperson;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    JSONObject response;
    Realm realm;
    RealmResults<Person> peopleListResults;
    String url = "https://gist.githubusercontent.com/joshheald/d26b89b0fbaf4e26cb423913ada21b83/raw/174a7ac0916919d4ae171adcfc0af78811b185f3/sportspeople.json";
    int personId1;
    int personId2;
    Person player1;
    Person player2;

    Button btn_player1;
    Button btn_player2;
    TextView result;
    Button btn_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get data from JSON and save it in a Realm Database
        new ProcessJSON().execute(url);

        RealmConfiguration sportspeople = new RealmConfiguration.Builder(getBaseContext())
                .name("sportspeople.realm").build();
        realm = Realm.getInstance(sportspeople);

        // Get all players from RealmDataBase
        peopleListResults = realm.where(Person.class).findAll();

        // Show Two Players
        showSportpeople();

        // Check user choice
        checkChoice();

    }

    private void checkChoice() {
        //Set Buttons Views
        btn_player1 = (Button) findViewById(R.id.btn_player1);
        btn_player2 = (Button) findViewById(R.id.btn_player2);
        result = (TextView) findViewById(R.id.result);
        btn_play = (Button) findViewById(R.id.btn_play);


        // Get player's points
        final double points_player1 = player1.getPoints();
        final double points_player2 = player2.getPoints();

        //Set Buttons click
        btn_player1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (points_player1 > points_player2) {
                    userWin();
                } else {
                    userLose();
                }
            }
        });

        btn_player2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (points_player2 > points_player1) {
                    userWin();
                } else {
                    userLose();
                }
            }
        });
    }

    private void userWin() {
        // Set Winner msg
        result.setVisibility(View.VISIBLE);
        result.setText("@strings/win_message");
        result.setTextColor(Color.GREEN);
        //Show button to play again
        btn_play.setVisibility(View.VISIBLE);
    }

    private void userLose() {
        // Set Winner msg
        result.setVisibility(View.VISIBLE);
        result.setText("@strings/lose_message");
        result.setTextColor(Color.RED);
        //Show button to play again
        btn_play.setVisibility(View.VISIBLE);
    }

    private void showSportpeople() {
        if(peopleListResults.isEmpty()){ // Not RealmDatabe in the phone
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("No network connection, check device settings and try again.");
            alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.show();
        }else{
            //Get two random player's id
            personId1 = randomNumber(peopleListResults.size()) - 1;
            personId2 = randomNumber(peopleListResults.size()) - 1;
            while (personId1 == personId2) {
                personId2 = randomNumber(peopleListResults.size()) - 1;
            }

            //Get people with these ids
            //Person1
            player1 = peopleListResults.get(personId1);
            TextView name_player1 = (TextView) findViewById(R.id.name_player1);
            name_player1.setText(player1.getFullName());
            ImageView img_player1 = (ImageView) findViewById(R.id.image_player1);
            Picasso.with(this).load(player1.getPicture()).into(img_player1);

            //Person2
            player2 = peopleListResults.get(personId2);
            TextView name_player2 = (TextView) findViewById(R.id.name_player2);
            name_player2.setText(player2.getFullName());
            ImageView img_player2 = (ImageView) findViewById(R.id.image_player2);
            Picasso.with(this).load(player2.getPicture()).into(img_player2);
        }
    }

    public int randomNumber(int range){
        int num;
        num = (int) (Math.random() * range) + 1;
        return num;
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
                    // Getting JSON Array node
                    JSONArray jsonArray = response.getJSONArray("sportspeople");
                    Log.d("JSON", jsonArray.toString());

                    RealmList<Person> peopleList = new RealmList<>();
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
