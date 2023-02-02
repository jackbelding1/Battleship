package edu.msu.team9.proj1;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.msu.team9.proj1.Cloud.Cloud;

/**
 * TODO: document your custom view class.
 */
public class GameView extends View {

    /** player names */
    private ArrayList<String> PlayerNames = new ArrayList<>();

    /** victory status */
    private boolean mWin;

    /** current turn */
    private int mTurn;

    /** player 1 hits */
    private int playerOneHits = 0;

    /** player 2 hits */
    private int playerTwoHits = 0;

    /**
     * x location.
     * We use relative x locations in the range 0-1 for the center
     * of the game piece.
     */
    private float x = .5f;

    /** y location*/
    private float y = 0.5f;

    /** The actual grid */
    public Grid grid;

    private TextView player_text;
    private String display_msg = "'s turn to attack";
    private String display_text;

    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        grid = new Grid(getContext());
        grid.setController(2);

        // Load attributes
        mWin = false;
        // loads stored turn in case of activity reload
        mTurn = 0;
    }

    /**
     *
     * @param obj TextView obj set by the GameActivity class
     */
    public void setTextview(TextView obj)
    {
        player_text = obj;
    }

    /**
     *
     * @param name player name added from GameActivity
     */
    public void addPlayerNames(String name)
    {
        PlayerNames.add(name);
    }

    /**
     * Update the text display based on player turns
     */
    public void adjustPlayerSpecificText()
    {
        if (player_text != null) {
            display_text = PlayerNames.get(mTurn) + display_msg;
            player_text.setText(display_text);
        }
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        //adjustPlayerSpecificText();
        //adjustPlayerSpecificText();
        grid.onDraw(canvas);
    }

    /**
     * Handle a touch event from the view.
     * @param event The motion event describing the touch
     * @return true if the touch is handled.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (grid.onTouchEvent(event))
        {
            invalidate();
            return true;
        }
        return false;
    }


    public Grid getGrid() {
        return grid;
    }

    public void getFromBundle(String key, Bundle bundle)
    {
        grid.getFromBundle(key, bundle);
        grid.setController(2);
    }

    public void putToBundle(String key, Bundle bundle)
    {
        grid.putToBundle(key, bundle);
    }
}