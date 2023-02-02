package edu.msu.team9.proj1;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class SetupView extends View {

    /** Display States */
    enum Display {
        WAITING_SCREEN,
        SETUP_SCREEN,
        GAME_SCREEN
    }
    public Grid grid;

    private String display_text;
    private TextView player_text;
    private ArrayList<String> PlayerNames = new ArrayList<>();
    private boolean isCreator;


    public SetupView(Context context) {
        super(context);
        init(null, 0);
    }

    public SetupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SetupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);

    }


    public void setDisplay(Grid.Display option)
    {
        grid.setCurrentDisplay(option);
    }


    public int getSquareCount()
    {
        return grid.getSelectedSquareCount();
    }

    public void setSquareCount(int n)
    {
        grid.setSelectedSquareCount(n);
    }

    private void init(AttributeSet attrs, int defStyle) {

        grid = new Grid(getContext());
        grid.setController(1);
    }

    /**
     * Used in the battleship placement phase to assign
     * one of four battleships to a square in the grid
     * @param index battleship index
     */
    public void placeBattleship(int index, ArrayList<Square> grid)
    {
        this.grid.getSquare(index).SelectSquare();
    }

    /**
     * Used in the battleship placement phase to remove
     * a battleship from the grid
     * @param index battleship index
     */
    public void removeBattleship(int index, ArrayList<Square> grid)
    {
        this.grid.getSquare(index).UnselectSquare();
    }

    /**
     * Handle a touch event from this view
     * @param event the touch event
     * @return true if handles, false otherwise
     */
    public boolean onTouchEvent(MotionEvent event) {
        if (grid.onTouchEvent(event))
        {
            invalidate();
            return true;
        }
        return false;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        grid.onDraw(canvas);
    }

    /**
     * Save the set up to a bundle
     * @param bundle The bundle we save to
     */
    public void saveInstanceState(Bundle bundle) {

    }

    /**
     * Load the set up from a bundle
     * @param bundle The bundle we save to
     */
    public void loadInstanceState(Bundle bundle) {

    }

    /**
     *
     * @param name player name added from SetupActivity
     */
    public void addPlayerNames(String name)
    {
        PlayerNames.add(name);
    }

    /**
     *
     * @param player Player to get
     */
    //public String getPlayerNames(Integer player)
    //{
    //    return PlayerNames.get(player);
    //}

    /**
     * Update the text display based on player turns
     */
    public void adjustPlayerName(Integer turn)
    {
        display_text = PlayerNames.get(turn) + "'s turn";
        player_text.setText(display_text);
    }

    /**
     *
     * @param obj TextView obj set by the SetupActivity class
     */
    public void setTextview(TextView obj)
    {
        player_text = obj;
    }


    public void getFromBundle(String key, Bundle bundle)
    {
        grid.getFromBundle(key, bundle);
        grid.setController(1);
    }

    public void putToBundle(String key, Bundle bundle)
    {
        grid.putToBundle(key, bundle);
    }

}