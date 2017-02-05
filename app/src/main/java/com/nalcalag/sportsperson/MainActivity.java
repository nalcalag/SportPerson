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
    RealmConfiguration sportspeople;
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
    double points_player1;
    double points_player2;
    TextView text_points_player1;
    TextView text_points_player2;
    TextView text_score;

    int user_score = 0;
    int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get data from JSON and save it in a Realm Database
        new ProcessJSON().execute(url);

        sportspeople = new RealmConfiguration.Builder(getBaseContext())
                .name("sportspeople.realm").build();
        realm = Realm.getInstance(sportspeople);

        // Get all players from RealmDataBase
        peopleListResults = realm.where(Person.class).findAll();


        //Set Views
        btn_player1 = (Button) findViewById(R.id.btn_player1);
        btn_player2 = (Button) findViewById(R.id.btn_player2);
        result = (TextView) findViewById(R.id.result);
        btn_play = (Button) findViewById(R.id.btn_play);
        text_points_player1 = (TextView) findViewById(R.id.points_player1);
        text_points_player2 = (TextView) findViewById(R.id.points_player2);
        text_score = (TextView) findViewById(R.id.score);

    }

    /**
     * Async task class to get json by making HTTP call
     */
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

            /**
             * Start the game
             * */

            startGame();
        }
    }

    private void startGame() {
        // Show Two Players
        showSportpeople();

        // Check user choice
        checkChoice();
    }

    private void checkChoice() {
        //Set Buttons click
        btn_player1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set Result layout
                setResultView();

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
                // Set Result layout
                setResultView();

                if (points_player2 > points_player1) {
                    userWin();
                } else {
                    userLose();
                }
            }
        });
    }

    private void setResultView() {
        // Disable choose Buttons
        btn_player1.setEnabled(false);
        btn_player2.setEnabled(false);
        // Show points
        text_points_player1.setText("Points: " + String.valueOf(String.format("%.2f", points_player1))); //Round to 2 decimal
        text_points_player1.setVisibility(View.VISIBLE);
        text_points_player2.setText("Points: " + String.valueOf(String.format("%.2f", points_player2))); //Round to 2 decimal
        text_points_player2.setVisibility(View.VISIBLE);
        // Show message and play again button
        result.setVisibility(View.VISIBLE);
        btn_play.setVisibility(View.VISIBLE);
    }

    private void userWin() {
        // Set Winner msg
        result.setText(R.string.win_message);
        result.setTextColor(Color.GREEN);
        user_score += 1;
        updateScore();
        newgame();
    }

    private void userLose() {
        // Set Winner msg
        result.setText(R.string.lose_message);
        result.setTextColor(Color.RED);
        score += 1;
        updateScore();
        newgame();
    }

    private void updateScore() {
        text_score.setText(String.valueOf(user_score) + " - " + String.valueOf(score));
    }

    private void newgame() {
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set new game view
                btn_player1.setEnabled(true);
                btn_player2.setEnabled(true);
                result.setVisibility(View.INVISIBLE);
                btn_play.setVisibility(View.INVISIBLE);
                text_points_player1.setVisibility(View.INVISIBLE);
                text_points_player2.setVisibility(View.INVISIBLE);

                // Start new game
                startGame();
            }
        });

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
            generateRandomPlayers(peopleListResults.size());

            //Get people with these ids
            //Person1
            player1 = peopleListResults.get(personId1);
            TextView name_player1 = (TextView) findViewById(R.id.name_player1);
            name_player1.setText(player1.getFullName());
            ImageView img_player1 = (ImageView) findViewById(R.id.image_player1);
            Picasso.with(this).load(player1.getPicture()).into(img_player1);
            points_player1 = player1.getPoints();

            //Person2
            player2 = peopleListResults.get(personId2);
            TextView name_player2 = (TextView) findViewById(R.id.name_player2);
            name_player2.setText(player2.getFullName());
            ImageView img_player2 = (ImageView) findViewById(R.id.image_player2);
            Picasso.with(this).load(player2.getPicture()).into(img_player2);
            points_player2 = player2.getPoints();
        }
    }

    private void generateRandomPlayers(int range) {
        personId1 = randomNumber(range) - 1;
        do {
            personId2 = randomNumber(range) - 1;
        } while (String.valueOf(personId2).equals(String.valueOf(personId1))); //Check it is not the same person
    }

    public int randomNumber(int range){
        int num;
        num = (int) (Math.random() * range) + 1;
        return num;
    }

    // Set menu
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
        if (id == R.id.exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchAbout(View view){
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

}
