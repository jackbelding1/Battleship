package edu.msu.team9.proj1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import edu.msu.team9.proj1.Cloud.Cloud;



/**
 * This Activity has 1 Thread running continuously
 * It gets stopped in onSaveInstanceState
 */
public class SetupActivity extends AppCompatActivity {

    private static final String PARAMETERS = "parameters";

    //private String player1_name;
    //private String player2_name;

    private Button button;
    private int userid;
    private int gameid;
    private boolean isCreator;
    private volatile boolean gameEnded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Bundle shared = getIntent().getExtras();

        //String t1 = shared.getString("player1_name");
        //String t2 = shared.getString("player2_name");
        isCreator = shared.getBoolean("isCreator");

        //getSetupView().addPlayerNames(t1);
        //getSetupView().addPlayerNames(t2);
        getSetupView().grid.setCreator(isCreator);

        gameid = shared.getInt("game_id");
        userid = shared.getInt("user_id");
        getSetupView().grid.setGameID(gameid);
        getSetupView().grid.setUserID(userid);
        getSetupView().grid.setDlgMode(WaitDlg.waitMode.WAIT_FOR_JOINER);


        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            button = (Button) findViewById(R.id.doneButtonLand);
        } else {
            button = (Button) findViewById(R.id.doneButton);
        }
        button.setEnabled(false);

        if(savedInstanceState != null) {
            getSetupView().getFromBundle(PARAMETERS, savedInstanceState);
            if(getSetupView().grid.getSelectedSquareCount() == 4)
            {
                button.setEnabled(true);
            }

        }
        if (isCreator)
        {
            getSetupView().grid.setDlgMode(WaitDlg.waitMode.WAIT_FOR_JOINER);
            getSetupView().grid.setCurrentDisplay(Grid.Display.WAITING_SCREEN);
            getSetupView().grid.setGrid(1);
            getSetupView().grid.setActivity(this);

            showWaitScreen(getSetupView().grid.getDlg());
            //getSetupView().setTextview((TextView) findViewById(R.id.setupName));
            //getSetupView().adjustPlayerName(turn);
        }
        else {
            getSetupView().grid.setGrid(2);
            getSetupView().grid.setActivity(this);
            getSetupView().grid.startTimer();
        }

        // Start checking for game end
        getSetupView().grid.gameThread.Start();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        getSetupView().putToBundle(PARAMETERS, bundle);
        StopThreads();
        getSetupView().grid.gameThread.Stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_to_login:
                getSetupView().grid.gameThread.Stop();
                onSurrender();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Handle connectionn loss
     */
    private Thread handleConnectionLoss()
    {
        return new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                boolean connected = true;
                while (connected)
                {
                    connected = getSetupView().grid.checkConnectionStatus();

                    //getSetupView().grid.loadGame(false, false, true);
                    //getSetupView().grid.getParams().gridMode = 1;
                    // getSetupView().grid.setCurrentDisplay(Grid.Display.SETUP_SCREEN);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    /**
     * When this player leaves the game during setup phase
     */
    private void informOpponent()
    {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                    boolean ok = getSetupView().grid.playerSurrendered();
                    getSetupView().grid.saveGame();
            }
        }).start();
    }


    /**
     * When this player leaves the game during setup phase
     */
    public void onSurrender()
    {
        getSetupView().grid.setSurrenderId(userid);
        informOpponent();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        int x = 0;
    }

    public void StopThreads()
    {
        gameEnded = true;
    }


    /**
     * @brief function that loads the winning activity. Passes the string of
     * the winning player
     */
    public void endGame(){
        int displayText = R.string.game_ended;

        if (getSetupView().grid.playerTimedOut()) {
            int timedOut_userId = getSetupView().grid.getTimeoutId();
            // this player timedOut
            if (userid == timedOut_userId) {
                displayText = R.string.this_player_timeout;
            } else {
                displayText = R.string.other_player_timeout;
            }
        } else if (getSetupView().grid.playerSurrendered())
        {
            int surrender_userId = getSetupView().grid.getTimeoutId();
            // this player timedOut
            if (userid == surrender_userId) {
                displayText = R.string.this_player_surrendered;
            } else {
                displayText = R.string.other_player_surrendered;
            }
        }
        Intent intent = new Intent(this, WinningActivity.class);
        intent.putExtra("displayText", displayText);
        startActivity(intent);
    }


    public void onTimeout()
    {
        getSetupView().grid.stopTimer();
        getSetupView().grid.setTimeoutId(userid);
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                // Create a cloud object
                Cloud cloud = new Cloud();
                getSetupView().grid.saveGame();
                cloud.incrementSetupCount(gameid);
            }
        }).start();
    }


    public void showWaitScreen(WaitDlg dlg){
        if (dlg.getDlgMode() == null){
            dlg.setDlgMode(WaitDlg.waitMode.WAIT_FOR_TURN);
        }
        dlg.show(getSupportFragmentManager(), "waiting_setup");
    }

    /**
     * @return (View) SetupView
     */
    public SetupView getSetupView(){
        return (SetupView) findViewById(R.id.setUpView);
    }

    /**
     * @return (View) GridView
     */
    private Grid getGridView(){
        return getSetupView().grid;
    }

    public void onStartGame(View view) {
        int squareCount = getSetupView().getSquareCount();
        if (squareCount == 4) {

            // When the Done button is pressed
            getSetupView().grid.stopTimer();
            new Thread(new Runnable() {

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    // Create a cloud object
                    Cloud cloud = new Cloud();
                    //final int counter = cloud.getSetupCount(gameid);
                    // even if both players call this at the same time
                    // (both submit their grids at the same time)
                    // counter will never be the same for both players at the same time
                    final int counter = cloud.incrementSetupCount(gameid);
                    boolean safeToLoad = false;
                    if (counter == 1)
                    {
                        boolean status = getSetupView().grid.saveGame();
                        getSetupView().grid.setDlgMode(WaitDlg.waitMode.WAIT_FOR_SETUP);
                        getSetupView().grid.setCurrentDisplay(Grid.Display.WAITING_SCREEN);

                        // player finished setup, player now waits for other player
                        getSetupView().grid.getDlg().show(getSupportFragmentManager(),
                                "waiting_setup");
                        //getSetupView().invalidate();
                        if(status)
                        {
                            cloud.incrementSetupCount(gameid);
                        }
                        boolean canStartGame = false;
                        int setup_count;
                        while (!canStartGame && !gameEnded)
                        {
                            setup_count = cloud.getSetupCount(gameid);
                            if (setup_count >= 4)
                            {
                                //cloud.loadGame(gameid);
                                getSetupView().grid.getParams().gridMode = 1;
                                canStartGame = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getSetupView().grid.gameThread.Stop();
                                        startTheGame(true);}
                                });
                            }
                        }
                    }
                    else if (counter >= 2){

                        // TODO: handle connection error
                        while(!safeToLoad && !gameEnded)
                        {
                            if(cloud.getSetupCount(gameid) == 3)
                            {
                                safeToLoad = true;
                                // get game state and load it in
                                getSetupView().grid.loadGame(true, false, false);
                                getSetupView().grid.getParams().gridMode = 1;
                            }
                        }
                        boolean save_success = getSetupView().grid.saveGame();
                        if (save_success)
                        {
                            cloud.incrementSetupCount(gameid);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getSetupView().grid.gameThread.Stop();
                                startTheGame(false);
                            }
                        });

                    }
                }
            }).start();
        }
    }

    public void startTheGame(boolean isFirst){
        Intent intent = new Intent(SetupActivity.this, GameActivity.class);
        Bundle bundle = new Bundle();

        getGridView().putToBundle("test", bundle);
        intent.putExtras(bundle);
        //intent.putExtra("player1_name", getSetupView().getPlayerNames(0));
        //intent.putExtra("player2_name", getSetupView().getPlayerNames(1));
        intent.putExtra("isCreator", isCreator);
        intent.putExtra("isFirst", isFirst);

        // Who will go first, decided during main activity at beginning of program
        intent.putExtra("game_id", gameid);
        intent.putExtra("user_id", userid);

        startActivity(intent);
        finish();
    }

    public void setStartButton(boolean b) {
        button.setEnabled(b);
    }
}