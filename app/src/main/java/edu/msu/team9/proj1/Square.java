package edu.msu.team9.proj1;

import android.graphics.Canvas;
import android.graphics.Paint;


public class Square {

    /**
     * Percentage of the display width or height that
     * is occupied by the grid.
     */
    final static float SCALE_IN_VIEW = 0.9f;

    /**
     * Paint for filling the area the puzzle is in
     */
    private Paint fillPaint;

    /**
     * Paint for outlining the area the puzzle is in
     */
    private Paint outlinePaint;

    private boolean isSelected;

    private boolean isHit;

    private boolean isMiss;

    /**
     * states:
     * 11: not selected, not hit
     * 21: selected, not hit
     * 22: selected, hit
     * 12: not selected, hit
     */

    private int squareState;

    float locX = 0;
    float locY = 0;

    public Square()
    {
        // Create paint for filling the area the puzzle will
        // be solved in.
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(0xffcccccc);

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(0xff00ff00);
        isSelected = false;
        isHit = false;
        isMiss = false;
        updateSquareState();
    }

    /**
     * Used in the Setup screen.
     * Select the touched square
     */
    public void SelectSquare(){
        isSelected = true;
        updateSquareState();
    }

    /**
     * Set the location for the square
     */
    public void SetLocation(float x, float y){
        locX = x;
        locY = y;
    }

    /**
     * getter for square state
     * @return state
     */
    public int getSquareState()
    {
        return squareState;
    }

    /**
     * setter for square state
     * @param n: state
     */
    public void setSquareState(int n)
    {
        squareState = n;
    }

    /**
     * change the state of the square
     * based on multiple conditions
     */
    public void updateSquareState()
    {
        if (isSelected)
        {
            if (isHit)
            {
                setSquareState(22);
                return;
            }
            setSquareState(21);
        }
        else {
            if (isHit)
            {
                setSquareState(12);
                return;
            }
            setSquareState(11);
        }
    }

    public void reverseUpdateSquareState()
    {
        switch (squareState)
        {
            case 11:
                isSelected = false;
                isHit = false;
                break;
            case 12:
                isSelected = false;
                isHit = true;
                break;
            case 22:
                isSelected = true;
                isHit = true;
                break;
            case 21:
                isSelected = true;
                isHit = false;
                break;
        }
    }

    /**
     * Get the location x for the square
     */
    public float GetLocationX(){
        return locX;
    }

    /**
     * Get the location y for the square
     */
    public float GetLocationY(){
        return locY;
    }

    /**
     * Used in the Setup screen.
     * Unselect the touched square
     */
    public void UnselectSquare() {
        if (isSelected)
            isSelected = false;
        updateSquareState();
    }

    /**
     * Check if this square is selected
     */
    public boolean CheckSquare() {
        return isSelected;
    }

    public boolean getIsHit() {
        return isHit;
    }

    public boolean getIsMiss() {
        return isMiss;
    }

    /**
     * Attempt to hit square, determine if is a hit or a miss
     */
    public boolean hitSquare() {
        if (isHit) {
            return false;
        }
        isHit = true;
        updateSquareState();
        return true;
    }

    /**
     * If we need to unhit square for any reason
     */
    public void unhitSquare() {
        isHit = false;
        isMiss = false;
        updateSquareState();
    }
}
