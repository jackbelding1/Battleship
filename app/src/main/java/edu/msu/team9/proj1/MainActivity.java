package edu.msu.team9.proj1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import edu.msu.team9.proj1.Cloud.Cloud;


/**
 * This Activity has 1 Thread running continuously
 * It gets stopped in onSaveInstanceState
 */
public class MainActivity extends AppCompatActivity {
    private String current_username;
    private int userid;
    private int gameid;
    private Cloud.CatalogAdapter adapter;
    private final String baseString = "Logged in as: ";

    ListView list;
    TextView textView;
    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_login);

        Bundle shared = getIntent().getExtras();

        if (shared != null) {
            userid = shared.getInt("userid");
            current_username = shared.getString("current_username", "username error");
        }

        // game list and information
        list = (ListView) findViewById(R.id.listGames);
        textView = (TextView) findViewById(R.id.after_title);
        username = (TextView) findViewById(R.id.current_user);
        username.setText(baseString.concat(current_username));
        adapter = new Cloud.CatalogAdapter(list);
        adapter.setTextView(textView);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Get the id of the game we want to join
                gameid = adapter.getId(position);
                GameItem item = (GameItem) adapter.getItem(position);
                //String creator_name = item.getName();
                joinGame(gameid, view);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        adapter.stopThread();
    }

    /**
     * Called When a Player Creates a New Game
     * @param view view this function was called from
     */
    public void onCreateGame(View view) {
        new Thread(new Runnable() {
            @Override
            public void run(){
                Cloud service = new Cloud();
                final int gameId = service.createGame(userid);
                if (gameId >= 0){ // game successfully created
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onSetUpGame(true, gameId); // go to the setup screen and wait
                            adapter.stopThread(); // stop the game list from refreshing
                        }
                    });
                }
                else {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            // If we fail to create game, display a toast
                            Toast.makeText(view.getContext(),
                                    R.string.game_create_error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     *
     * @param isCreator did this player create this game?
     * @param gameId game id
     */
    public void onSetUpGame(boolean isCreator, int gameId) {
        Intent intent = new Intent(this, SetupActivity.class);
        intent.putExtra("game_id", gameId);
        intent.putExtra("user_id", userid);
        intent.putExtra("isCreator", isCreator);
        intent.putExtra("current_username", "JUSTIN");
        intent.putExtra("opponent_username", "JUTSIN");
        startActivity(intent);
    }


    /**
     * Called when it is time to create the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * Handle options menu selections
     *
     * @param item Menu item selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_play) {
            AboutDlg dlg = new AboutDlg();
            dlg.show(getSupportFragmentManager(), "About");
            return true;
        } else if (itemId == R.id.back_to_login)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onHowToPlaySelected(View view) {
        AboutDlg dlg = new AboutDlg();
        dlg.show(getSupportFragmentManager(), "About");
    }

    /**
     * Join a game
     *
     * @param gameId the game id to load
     * @param view view this was called from
     */
    private void joinGame(int gameId, final View view) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // Create a cloud object and get the game state object
                Cloud cloud = new Cloud();
                // TODO: Change return type from ArrayList<Grid> to GameResult?
                boolean ok;
                ok = cloud.joinGame(gameId, userid);
                if (ok)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            onSetUpGame(false, gameId);
                            adapter.stopThread();
                        }
                    });
                } else
                {
                    Toast.makeText(view.getContext(), R.string.join_error, Toast.LENGTH_SHORT).show();
                }
            }

        }).start();
    }


}
