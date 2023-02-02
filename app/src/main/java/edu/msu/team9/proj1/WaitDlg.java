package edu.msu.team9.proj1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import edu.msu.team9.proj1.Grid;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import edu.msu.team9.proj1.Cloud.Cloud;


public class WaitDlg extends DialogFragment {

    private int gameId;
    private int userId;
    private Grid grid;
    private waitMode mode;
    private int mode_number;
    enum waitMode {WAIT_FOR_JOINER, WAIT_FOR_SETUP, WAIT_FOR_TURN};
    private volatile boolean cancel;
    WaitDlg thisDlg = this;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setCancelable(false);
        setRetainInstance(true);

        if (savedInstanceState != null)
        {
            mode_number = savedInstanceState.getInt("mode_number");
            switch (mode_number)
            {
                case 0:
                    mode = waitMode.WAIT_FOR_JOINER;
                    break;
                case 1:
                    mode = waitMode.WAIT_FOR_SETUP;
                    break;
                case 2:
                    mode = waitMode.WAIT_FOR_TURN;
                    break;
            }

            gameId = savedInstanceState.getInt("gameid");
            userId = savedInstanceState.getInt("userid");
            grid = new Grid(getContext());
            grid.getFromBundle("GRID", savedInstanceState);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (mode)
        {
            case WAIT_FOR_JOINER:
                mode_number = 0;
                builder.setTitle(R.string.waitDlgTitle);
                // Add a cancel button
                builder.setNegativeButton(R.string.back_to_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                });
                break;
            case WAIT_FOR_SETUP:
                mode_number = 1;
                builder.setTitle(R.string.waitDlgSetupTitle);
                // Add a cancel button
                builder.setNegativeButton(R.string.surrenderButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        grid.surrender();
                        // Cancel just closes the dialog box
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                });
                break;
            case WAIT_FOR_TURN:
                mode_number = 2;
                builder.setTitle(R.string.waitDlgTurnTitle);
                // Add a cancel button
                builder.setNegativeButton(R.string.surrenderButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        grid.surrender();
                        // Cancel just closes the dialog box
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                });
                break;
        }
        final AlertDialog dlg = builder.create();
        switch (mode)
        {
            case WAIT_FOR_JOINER:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean ok = false;
                        while (!ok){
                            // Create a cloud object and get the game state object
                            Cloud cloud = new Cloud();
                            int joinerID = cloud.checkJoiner(gameId);
                            if (joinerID > 0 && grid.setup != null)
                            {
                                ok = true;
                                grid.setCurrentDisplay(Grid.Display.SETUP_SCREEN);
                                grid.setup.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        grid.startTimer();
                                    }
                                });
                                // both players are in the game and the dlg mode changes
                                setDlgMode(waitMode.WAIT_FOR_SETUP);
                                dismiss();
                            } else
                            {
                                int x = 420;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }).start();
                break;
            case WAIT_FOR_SETUP:
                break;
            case WAIT_FOR_TURN:
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        boolean ok = true;

                        while (!ok){
                            // Create a cloud object and get the game state object
                            Cloud cloud = new Cloud();
                            int id_of_current_player = cloud.getPlayerTurn(gameId);
                            // will be either 1 or 2
                            if (id_of_current_player == userId && grid.game != null)
                            {
                                ok = true;
                                grid.setCurrentDisplay(Grid.Display.GAME_SCREEN);
                                grid.sneaky_invalidate();
                                dismiss();
                            } else
                            {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();
                break;
        }

        return dlg;
    }

    public void forceDismiss(){
        this.dismiss();
    }

    public void setDlgMode(waitMode wait_mode){
        mode = wait_mode;
    }

    public waitMode getDlgMode(){
        return mode;
    }

    public void setGameID(int gameid){
        gameId = gameid;
    }

    public void setUserID(int userid){
        userId = userid;
    }

    public void setGrid(Grid grid1){
        grid = grid1;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        grid.putToBundle("GRID", outState);
        outState.putInt("mode_number", mode_number);
        outState.putInt("gameid", gameId);
        outState.putInt("userid", userId);

    }
}
