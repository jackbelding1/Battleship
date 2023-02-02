package edu.msu.team9.proj1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.msu.team9.proj1.Cloud.Cloud;

public class GameActivity extends AppCompatActivity {
    private static final String PARAMETERS = "parameters";

    private Button button;
    private int gameid;
    private int userid;
    private boolean isCreator;
    private boolean finishedFirst;
    private volatile boolean gameEnded = false;
    private boolean firstTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle bundle = getIntent().getExtras();
        getGameView().getFromBundle("test", bundle);

        //String player1_name = bundle.getString("player1_name");
        //String player2_name = bundle.getString("player2_name");
        isCreator = bundle.getBoolean("isCreator");
        finishedFirst = bundle.getBoolean("isFirst");
        firstTurn = isCreator;

        gameid = bundle.getInt("game_id");
        userid = bundle.getInt("user_id");
        getGameView().grid.setGameID(gameid);
        getGameView().grid.setUserID(userid);
        getGameView().grid.setCreator(isCreator);

        button = (Button) findViewById(R.id.turnDone);
        button.setEnabled(false);

        if(savedInstanceState != null) {
            getGameView().getFromBundle(PARAMETERS, savedInstanceState);
            firstTurn = bundle.getBoolean("firstTurn");
            if(getGameView().grid.getPlayed())
            {
                button.setEnabled(true);
            }
        }

        getGameView().grid.setGameActivity(this);

        //getGameView().addPlayerNames(player1_name);
        //getGameView().addPlayerNames(player2_name);

        getGameView().adjustPlayerSpecificText();
        getGameView().setTextview((TextView) findViewById(R.id.player_move_text));
        getGameView().grid.setDlgMode(WaitDlg.waitMode.WAIT_FOR_TURN);

        if (isCreator)
        {
            getGameView().grid.setGrid(2);
            getGameView().grid.setCurrentDisplay(Grid.Display.GAME_SCREEN);
        }
        else {
            getGameView().grid.setGrid(1);
            getGameView().grid.setCurrentDisplay(Grid.Display.WAITING_SCREEN);
            showWaitScreen(getGameView().grid.getDlg());
        }
        getGameView().grid.gameThread.Start();
        getGameView().grid.turnThread.Start(firstTurn);
    }

//   private GameView getGameView(){ return (GameView) findViewById(R.id.gameView);}

    public void showWaitScreen(WaitDlg dlg){
        //if (dlg.getDlgMode() == null){
        //    dlg.setDlgMode(WaitDlg.waitMode.WAIT_FOR_TURN);
        //}
        dlg.show(getSupportFragmentManager(), "waiting_game");
        if (!getGameView().grid.getPlayed())
        {
            button.setEnabled(false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        getGameView().putToBundle(PARAMETERS, bundle);
        getGameView().grid.gameThread.Stop();
        getGameView().grid.turnThread.Stop();
        bundle.putBoolean("firstTurn", getGameView().grid.turnThread.getFirstTurn());
        getGameView().grid.stopTimer();

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
                onSurrender(getGameView());
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onSurrender(View view){
        // this player surrendered
        getGameView().grid.gameThread.Stop();
        getGameView().grid.turnThread.Stop();
        getGameView().grid.setSurrenderId(userid);
        onDone(view);
    }



    public void onTimeout(){
        // this player timed out
        getGameView().grid.turnThread.Stop();
        getGameView().grid.stopTimer();
        getGameView().grid.setTimeoutId(userid);
        onDone(getGameView());

    }

    public void onDone(View view) {
        if (getGameView().grid.done()) {
            getGameView().grid.turnThread.Stop();
            getGameView().grid.gameThread.Stop();
            getGameView().grid.setWinnerId(userid);
        }
        ReleaseTurn();
    }

    public void ReleaseTurn() {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                Cloud cloud = new Cloud();
                boolean save_success = getGameView().grid.saveGame();
                boolean proceed = false;
                if (save_success)
                {
                    proceed = true;
                } else {
                    Toast.makeText(getGameView().getContext(), R.string.error_save,
                            Toast.LENGTH_SHORT).show();
                }
                boolean success = cloud.update_turn(gameid);
                if (success && proceed && !gameEnded)
                {
                    getGameView().grid.stopTimer();
                    if (!getGameView().grid.hasAWinner())
                    {
                        getGameView().grid.turnThread.NotMyTurnAnymore();
                        getGameView().grid.instantiateTurnThread();
                        getGameView().grid.turnThread.Start(false);
                        getGameView().grid.setCurrentDisplay(Grid.Display.WAITING_SCREEN);
                        getGameView().getGrid().setPlayed(false);
                        getGameView().grid.setDlgMode(WaitDlg.waitMode.WAIT_FOR_TURN);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWaitScreen(getGameView().grid.getDlg());
                            }
                        });
                    } else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getGameView().grid.endGame();
                            }
                        });

                    }
                } else {
                    Toast.makeText(getGameView().getContext(), R.string.error_update_turn,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    /**
     * @brief function that loads the winning activity. Passes the string of
     * the winning player
     */
    public void endGame(){
        // cancel time outs
        getGameView().grid.stopTimer();
        int displayText = R.string.game_ended;
        int deleteCheck = 0;
        if (getGameView().grid.playerSurrendered())
        {
            int surrender_userId = getGameView().grid.getSurrenderId();
            // this player surrendered
            if (userid == surrender_userId)
            {
                displayText = R.string.this_player_surrendered;
            }
            else
            {
                displayText = R.string.other_player_surrendered;
                deleteCheck = 1;
            }
        } else if (getGameView().grid.hasAWinner()){
            int winner_userId = getGameView().grid.getWinnerId();
            // this player won
            if (userid == winner_userId)
            {
                displayText = R.string.this_player_won;
            }
            else
            {
                displayText = R.string.other_player_won;
                deleteCheck = 1;
            }
        } else if (getGameView().grid.playerTimedOut()){
            int timedOut_userId = getGameView().grid.getTimeoutId();
            // this player timedOut
            if (userid == timedOut_userId)
            {
                displayText = R.string.this_player_timeout;
            }
            else
            {
                displayText = R.string.other_player_timeout;
                deleteCheck = 1;
            }
        }
        Intent intent = new Intent(this, WinningActivity.class);
        intent.putExtra("displayText", displayText);
        intent.putExtra("deleteCheck", deleteCheck);
        intent.putExtra("gameid", gameid);
        startActivity(intent);
    }

    /**
     * Get the game view
     * @return GameView reference
     */
    public GameView getGameView() {
        return (GameView)this.findViewById(R.id.setupViewP);
    }

    private Grid getGridView(){
        return getGameView().grid;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }





    public void setGameButton(boolean b)
    {
        button.setEnabled(b);
    }

    public void userLeaveDlg() {
        // The user left the game
        // Instantiate a dialog box builder
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getGameView().getContext());

        // Parameterize the builder
        builder.setTitle(R.string.notify);
        builder.setMessage(R.string.leave_message);
        builder.setPositiveButton(android.R.string.ok, null);

        // Create the dialog box and show it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}