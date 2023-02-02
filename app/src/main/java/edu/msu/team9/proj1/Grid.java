package edu.msu.team9.proj1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

import edu.msu.team9.proj1.Cloud.Cloud;

/**
 * Custom class for our Grid
 */
public class Grid {

    /** Display States */
    enum Display {
        WAITING_SCREEN,
        SETUP_SCREEN,
        GAME_SCREEN
    }

    private WaitDlg dlg;

    /** Player 1 Grid */ // always for the creator
    public ArrayList<Square> p1_grid = new ArrayList<>();

    /** Player 2 Grid */ // always for the joiner
    public ArrayList<Square> p2_grid = new ArrayList<>();

    /** Reference to the gird of the current grid */
    private ArrayList<Square> current_grid;

    /** Percentage of the display width or height that occupied by the game */
    float SCALE_IN_VIEW = 0.9f;

    /** Paint for filling the area the game is in */
    private Paint fillPaint;

    /** Paint for outlining the area the game is in */
    private Paint outlinePaint;

    /** The size of the puzzle in pixels */
    private int gameSize;

    /** Left margin in pixels */
    private int marginX;

    /** Top margin in pixels */
    private int marginY;

    /** Bitmap of a boat */
    private Bitmap xImage;

    /** Bitmap of a boat */
    private Bitmap oImage;

    /** Bitmap of a boat */
    private Bitmap boatImage;

    /** boolean of a game starting */
    public boolean gameStart = false;

    /** Canvas Width */
    public int cWidth;

    /** Canvas Height */
    public int cHeight;

    private int mCount = 0;

    /** Ref to SetupActivity */
    public SetupActivity setup;

    /** Ref to GameActivity */
    public GameActivity game;

    /** bitmap scale */
    public float ScaleX;

    public boolean isCreator;

    public GameTimer timeoutTimer;

    public GameEndThread gameThread;

    public CheckTurnThread turnThread;

    public int local_user_id;


    /**
     * Local class to handle the touch status for one touch.
     * We will have one object of this type for each of the
     * two possible touches.
     */
    private static class Touch {
        /**
         * Change in x value from previous
         */
        public float dX = 0;

        /**
         * Change in y value from previous
         */
        public float dY = 0;

        /**
         * Touch id
         */
        public int id = -1;

        /**
         * Current x location
         */
        public float x = 0;

        /**
         * Current y location
         */
        public float y = 0;

        /**
         * Previous x location
         */
        public float lastX = 0;

        /**
         * Previous y location
         */
        public float lastY = 0;

        /**
         * Copy the current values to the previous values
         */
        public void copyToLast() {
            lastX = x;
            lastY = y;
        }

        /**
         * Compute the values of dX and dY
         */
        public void computeDeltas() {
            dX = x - lastX;
            dY = y - lastY;
        }

    }


    public Parameters getParams(){
        return params;
    }


    public int getWinnerId(){
        return params.winnerId;
    }

    public void setWinnerId(int userid)
    {
        params.winnerId = userid;
    }

    public int getUploadId(){
        return params.last_upload_user_id;
    }

    public void setUploadId(int userid)
    {
        params.last_upload_user_id = userid;
    }

    public int getSurrenderId(){
        return params.surrenderId;
    }

    public void setSurrenderId(int userid)
    {
        params.surrenderId = userid;
    }

    public int getTimeoutId(){
        return params.timeoutId;
    }

    public void setTimeoutId(int userid)
    {
        params.timeoutId = userid;
    }

    public boolean playerTimedOut()
    {
        return params.timeoutId != -1;
    }

    public boolean playerSurrendered()
    {
        return params.surrenderId != -1;
    }

    public void surrender()
    {
        if (params.gridMode == 1)
        {
            setup.onSurrender();
        }
        else {
            game.onSurrender(game.getGameView());
        }
    }

    public boolean hasAWinner()
    {
        return params.winnerId != -1;
    }

    //public void endGame()
    //{
    //    if (params.gridMode == 1)
    //    {
    //        setup.endGame();
    //    }
    //    else{
    //        game.endGame();
    //    }
    //}


    /**
     * Timer for the game. Automatically surrenders if player doesn't
     * finish turn after two minutes
     * */
    public class GameTimer{
        Timer timer;
        TimerTask timerTask;
        AppCompatActivity activity;

        public GameTimer(AppCompatActivity act){
            activity = act;
        }

        public void startTimer() {
            //set a new Timer
            timer = new Timer();

            //initialize the TimerTask's job
            initializeTimerTask();

            //schedule the timer run after 2 minutes
            timer.schedule(timerTask, 10000, 120000); //
        }

        public void stoptimertask() {
            //stop the timer, if it's not already null
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        public void resetTimer(){
            stoptimertask();
            startTimer();
        }

        public void initializeTimerTask() {
            timerTask = new TimerTask() {
                public void run() {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            onTimeout();
                        }
                    });
                }
            };
        }
    }

    public void onTimeout()
    {
        if (params.gridMode == 1)
        {
            setup.onTimeout();
        } else {
            game.onTimeout();
        }
    }

    public class CheckTurnThread {
        private Thread t;
        private volatile boolean cancel;
        private volatile boolean myTurn;
        private volatile boolean firstTurn;
        public CheckTurnThread(){
            t = new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override

                public void run() {
                    boolean load_success;
                    int turnId;

                    while (!myTurn && !cancel)
                    {
                        Cloud cloud = new Cloud();
                        turnId = cloud.getPlayerTurn(params.gameId);

                        if (turnId == local_user_id)
                        {
                            myTurn = true;
                            startTimer();
                            load_success = loadGame(false, false, false);
                            setCurrentDisplay(Grid.Display.GAME_SCREEN);
                            params.played = false;
                            if (load_success)
                            {
                                params.gridMode = 2;

                                if (!firstTurn)
                                {
                                    getDlg().dismiss();
                                } else {
                                    firstTurn = false;
                                }
                            }
                            else {
                                myTurn = false;
                                Toast.makeText(getLocalView(), R.string.error_save,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        public void Start(boolean isCreator)
        {
            if (t != null)
            {
                firstTurn = isCreator;
                cancel = false;
                t.start();
            }
        }

        public void Stop(){
            cancel = true;
            t.interrupt();
        }

        public void setFirstTurn(boolean b)
        {
            firstTurn = b;
        }

        public boolean getFirstTurn()
        {
            return firstTurn;
        }

        public void NotMyTurnAnymore()
        {
            myTurn = false;
        }
    }

    public class GameEndThread {
        private Thread t;
        private volatile boolean cancel;
        public GameEndThread(){
            t = new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    boolean gameEnded = false;
                    while (!gameEnded && !cancel)
                    {

                        boolean load_success = loadGame(true, true, true);
                        //boolean load_success = true;
                        if (load_success)
                        {
                            gameEnded = checkGameEnded();
                            if (gameEnded)
                            {
                                if (params.gridMode == 1)
                                {
                                    setup.StopThreads();
                                    gameThread.Stop();
                                } else
                                {
                                    turnThread.Stop();
                                    gameThread.Stop();
                                }
                                deleteGame();
                                endGame();
                            }
                        }
                        else {
                            Toast.makeText(getLocalView(), R.string.error_save,
                                    Toast.LENGTH_SHORT).show();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        public void Start()
        {
            if (t != null)
            {
                cancel = false;
                t.start();
            }
        }

        public void Stop(){
            cancel = true;
            t.interrupt();
        }
    }


    /** Collection of data maintaining the state of the game */
    public static class Parameters implements Serializable {

        public Display currentDisplay;
        private int gameId;
        private int winnerId = -1;
        private int surrenderId = -1;
        private int timeoutId = -1;
        private int last_upload_user_id = -1;

        // 1: SetupView
        // 2: GameView
        public int gridMode;

        // has the current player made a move yet?
        public boolean played = false;

        // number of squares selected in setup activity
        public int selectedSquareCount = 0;

        // save the state of each square for each grid
        public ArrayList<Integer> gridStates1;
        public ArrayList<Integer> gridStates2;

        // save the location of each square for each grid
        public ArrayList<Float> gridLocations1;
        public ArrayList<Float> gridLocations2;
    }

    /**
     * Changes what the player sees based on this state
     */
    public void setCreator(boolean state) {
        isCreator = state;
        changeDisplay();
    }

    public void displayDlg()
    {
        game.showWaitScreen(dlg);
    }

    public boolean checkGameEnded()
    {
        return hasAWinner() || playerSurrendered() || playerTimedOut();
    }

    public void endGame(){
        // cancel time outs
        stopTimer();

        AppCompatActivity act = getActivity();
        int displayText = R.string.game_ended;
        int deleteCheck = 0;
        if (playerSurrendered())
        {
            int surrender_userId = getSurrenderId();
            // this player surrendered
            if (local_user_id == surrender_userId)
            {
                displayText = R.string.this_player_surrendered;
            }
            else
            {
                displayText = R.string.other_player_surrendered;
                deleteCheck = 1;
            }
        } else if (hasAWinner()){
            int winner_userId = getWinnerId();
            // this player won
            if (local_user_id == winner_userId)
            {
                displayText = R.string.this_player_won;
            }
            else
            {
                displayText = R.string.other_player_won;
                deleteCheck = 1;
            }
        } else if (playerTimedOut()){
            int timedOut_userId = getTimeoutId();
            // this player timedOut
            if (local_user_id == timedOut_userId)
            {
                displayText = R.string.this_player_timeout;
            }
            else
            {
                displayText = R.string.other_player_timeout;
                deleteCheck = 1;
            }
        }


        Intent intent = new Intent(act, WinningActivity.class);
        intent.putExtra("displayText", displayText);
        intent.putExtra("deleteCheck", deleteCheck);
        act.startActivity(intent);
    }


    /**
     * This function will only be called from inside a thread
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean loadGame(boolean keepLocalGrid, boolean keepLocalVariables, boolean keepLocalSquares) {
        String game_state;
        Cloud cloud = new Cloud();
        game_state = cloud.loadGame(params.gameId);
        boolean result = true;
        try {
            if (game_state.equals("ConnectionError"))
            {
             result = false;
            }
            else if (!game_state.equals(""))
            {
                Parameters param = StringToParam(game_state);
                copyParams(param, keepLocalGrid, keepLocalVariables, keepLocalSquares);
                ArrayList<Square> saved_grid1 = p1_grid;
                ArrayList<Square> saved_grid2 = p2_grid;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * This function will only be called from inside a thread
     */
    public boolean checkConnectionStatus()
    {
        Cloud cloud = new Cloud();
        return cloud.checkConnection();
    }

    /**
     * This function will only be called from inside a thread
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean saveGame(){
        String gameState = null;
        try {
            gameState = ParamToString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Cloud cloud = new Cloud();
        return cloud.saveGame(gameState, params.gameId);
    }


    public int deleteGame(){
        Cloud cloud = new Cloud();
        return cloud.deleteGame(params.gameId);
    }


    public void changeDisplay()
    {
        switch (params.gridMode)
        {
            case 1:
                if (isCreator)
                {
                    setCurrentDisplay(Grid.Display.WAITING_SCREEN);
                } else
                {
                    setCurrentDisplay(Grid.Display.SETUP_SCREEN);
                }
                break;
            case 2:
                if (isCreator)
                {
                    setCurrentDisplay(Display.GAME_SCREEN);
                } else
                {
                    setCurrentDisplay(Display.WAITING_SCREEN);
                }
                break;
        }

    }

    public void startTimer()
    {
        timeoutTimer.startTimer();
    }

    public void stopTimer()
    {
        timeoutTimer.stoptimertask();
    }

    /** Sets the SetupActivity reference of the grid */
    public void setActivity(SetupActivity act) {
        setup = act;
        timeoutTimer = new GameTimer(setup);
        gameThread = new GameEndThread();
    }

    public AppCompatActivity getActivity()
    {
        if (params.gridMode == 1)
        {
            return setup;
        } else {
            return game;
        }
    }

    public Context getLocalView()
    {
        if (params.gridMode == 1)
        {
            return setup.getSetupView().getContext();
        } else {
            return game.getGameView().getContext();
        }
    }

    /** Sets the GameActivity reference of the grid */
    public void setGameActivity(GameActivity act) {
        game = act;
        timeoutTimer = new GameTimer(game);
        gameThread = new GameEndThread();
        turnThread = new CheckTurnThread();
    }

    public void instantiateTurnThread()
    {
        turnThread = new CheckTurnThread();
    }

    /** Set the controller of the grid class (1: SetupView, 2: GameView) */
    public void setController(int n) {
        params.gridMode = n;
    }

    /** Get the grid controller */
    public int getController() {
        return params.gridMode;
    }

    /** getter and setter for params.selectedSquareCount */
    public int getSelectedSquareCount() {
        return params.selectedSquareCount;
    }
    public void setSelectedSquareCount(int n) {
        params.selectedSquareCount = n;
    }

    /** First touch status */
    private Touch touch1;

    /** Second touch status */
    private Touch touch2;

    /** our param object */
    private Grid.Parameters params;


    /** Constructor */
    public Grid(Context context) {
        init(context);
    }

    /** initialize this class */
    private void init(Context context) {

        touch1 = new Touch();
        touch2 = new Touch();
        params = new Grid.Parameters();
        dlg = new WaitDlg();
        dlg.setGrid(this);


        // Create paint for filling the area the game will played
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(0xff4169E1);

        // Grid outline paint
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(5);

        xImage = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.hit_marker);

        oImage = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.miss_marker);

        boatImage = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.patrolboat_1x1);

        /* *
         * make the squares!
         * */
        for (int i = 0; i < 16; i++) {
            p1_grid.add(new Square());
            p2_grid.add(new Square());
        }
    }

    public void sneaky_invalidate(){
        if (params.gridMode == 1)
        {
            setup.getSetupView().invalidate();
        } else {
            game.getGameView().invalidate();
        }
    }

    @SuppressLint("CanvasSize")
    protected void onDraw(Canvas canvas) {

        cWidth = canvas.getWidth();
        cHeight = canvas.getHeight();

        // Determine the minimum of the two dimensions
        int minDim = Math.min(cWidth, cHeight);

        // Set the size of our display grid
        gameSize = (int) (minDim * SCALE_IN_VIEW);

        // Compute the margins so we center the game
        marginX = (cWidth - gameSize) / 2;
        marginY = (cHeight - gameSize) / 2;

        // Draw the outline of the game
        canvas.save();
        canvas.drawRect(marginX, marginY,
                marginX + gameSize, marginY + gameSize, fillPaint);

        int delta = gameSize / 4;
        int count = 0;
        float left;
        float top;
        Grid.Location loc;
        while (count <= 4) {
            canvas.drawLine(marginX, marginY + (delta * count),
                    marginX + gameSize,
                    marginY + (delta * count), outlinePaint);
            count++;
        }
        while (count > 0) {
            count--;
            canvas.drawLine(marginX + (delta * count), marginY,
                    marginX + (delta * count), marginY + gameSize, outlinePaint);
        }


        switch (params.currentDisplay){
            case WAITING_SCREEN:
                break;

            case SETUP_SCREEN:
                for (Square element : current_grid) {
                    if (!element.CheckSquare()) {
                        continue;
                    }
                    int index = current_grid.indexOf(element);
                    ScaleX = getScale(gameSize, boatImage);
                    loc = alignment(index, boatImage, ScaleX);
                    left = loc.x1 * gameSize + marginX;
                    top = loc.y1 * gameSize + marginY;

                    canvas.save();
                    canvas.scale(ScaleX, ScaleX);
                    canvas.drawBitmap(boatImage, left/ScaleX, top/ScaleX, null);
                    canvas.restore();
                }
                break;

            case GAME_SCREEN:
                mCount = 0;
                for (Square squ : current_grid) {
                    if (squ.getSquareState() == 22) {
                        mCount++;

                        // draw boat
                        ScaleX = getScale(gameSize, boatImage);
                        loc = alignment(current_grid.indexOf(squ), boatImage, ScaleX);
                        left = loc.x1 * gameSize + marginX;
                        top = loc.y1 * gameSize + marginY;
                        canvas.save();
                        canvas.scale(ScaleX, ScaleX);
                        canvas.drawBitmap(boatImage, left/ScaleX, top/ScaleX, null);
                        canvas.restore();

                        // draw x
                        ScaleX = getScale(gameSize, xImage);
                        loc = alignment(current_grid.indexOf(squ), xImage, ScaleX);
                        left = loc.x1 * gameSize + marginX;
                        top = loc.y1 * gameSize + marginY;
                        canvas.save();
                        canvas.scale(ScaleX, ScaleX);
                        canvas.drawBitmap(xImage, left/ScaleX, top/ScaleX, null);
                        canvas.restore();

                    } else if (squ.getSquareState() == 12) {

                        // draw o
                        ScaleX = getScale(gameSize, oImage);
                        loc = alignment(current_grid.indexOf(squ), oImage, ScaleX);
                        left = loc.x1 * gameSize + marginX;
                        top = loc.y1 * gameSize + marginY;
                        canvas.save();
                        canvas.scale(ScaleX, ScaleX);
                        canvas.drawBitmap(oImage, left/ScaleX, top/ScaleX, null);
                        canvas.restore();;
                    }
                }
        }
        canvas.restore();
    }


    /**
     * Handle a touch event from this view
     *
     * @param event the touch event
     * @return true if handles, false otherwise
     */
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        float relX = event.getX();
        float relY = event.getY();

        int id = event.getPointerId(event.getActionIndex());


        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                touch1.id = id;
                touch2.id = -1;
                getPositions(event);
                touch1.copyToLast();
                Log.i("onTouchEvent", "ACTION_DOWN");
                int index = OnTouched(relX, relY);

                if (index == -1)
                    return false;

                handleTouch(getController(), index);
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (touch1.id >= 0 && touch2.id < 0) {
                    touch2.id = id;
                    getPositions(event);
                    touch2.copyToLast();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("onTouchEvent", "ACTION_MOVE: " + event.getX() + "," + event.getY());
                getPositions(event);
                move();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touch1.id = -1;
                touch2.id = -1;
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                if (id == touch2.id) {
                    touch2.id = -1;
                } else if (id == touch1.id) {
                    // Make what was touch2 now be touch1 by
                    // swapping the objects.
                    Touch t = touch1;
                    touch1 = touch2;
                    touch2 = t;
                    touch2.id = -1;
                }
                return true;
        }
        return false;
    }


    /**
     * Get the positions for the two touches and put them
     * into the appropriate touch objects.
     *
     * @param event the motion event
     */
    private void getPositions(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {

            // Get the pointer id
            int id = event.getPointerId(i);

            // Convert to image coordinates
            float x = (event.getX(i) - marginX) / SCALE_IN_VIEW;
            float y = (event.getY(i) - marginY) / SCALE_IN_VIEW;


            if (id == touch1.id) {
                touch1.copyToLast();
                touch1.x = x;
                touch1.y = y;
            } else if (id == touch2.id) {
                touch2.copyToLast();
                touch2.x = x;
                touch2.y = y;
            }
        }

    }

    /**
     * Handle movement of the touches
     */
    private void move() {
        // If no touch1, we have nothing to do
        // This should not happen, but it never hurts
        // to check.
        if (touch1.id < 0) {
            return;
        }

        if (touch1.id >= 0) {
            // At least one touch
            // We are moving
            touch1.computeDeltas();

        }
        if (touch2.id >= 0) {
            // Two touches

            /*
             * scaling
             */
            float d1 = distance(touch1.lastX, touch1.lastY, touch2.lastX, touch2.lastY);
            float d2 = distance(touch1.x, touch1.y, touch2.x, touch2.y);
            SCALE_IN_VIEW *= d2 / d1;
            if (SCALE_IN_VIEW < 0.69f) {
                SCALE_IN_VIEW = 0.69f;
            }
            if (SCALE_IN_VIEW > 1) {
                SCALE_IN_VIEW = 1;
            }

        }
    }

    /**
     * Calculates the distance between two points
     *
     * @param x1 the inital x
     * @param x2 the destination x
     * @param y1 the intial y
     * @param y2 the destination y
     * @return the distance
     */
    private float distance(float x1, float y1, float x2, float y2) {
        float dx = (x2 - x1) * (x2 - x1);
        float dy = (y2 - y1) * (y2 - y1);
        double distance = Math.sqrt(dy + dx);
        return (float) distance;
    }

    /**
     *
     * @param scale game size
     * @param image image we are scaling
     * @return scale multiplier
     */
    public float getScale(float scale, Bitmap image) {
        // measure width and game size on the same scale
        float width = image.getWidth()/((float) scale);
        return 0.125f/width;
    }

    public void handleTouch(int n, int index) {
        switch (n) {
            // handles a touch from setup view
            case 1:
                if (params.selectedSquareCount < 4 && !getSquare(index).CheckSquare()) {
                    getSquare(index).SelectSquare();
                    params.selectedSquareCount += 1;
                    if (params.selectedSquareCount == 4) {
                        setup.setStartButton(true);
                    }
                } else if (params.selectedSquareCount > 0 && getSquare(index).CheckSquare()) {
                    getSquare(index).UnselectSquare();
                    params.selectedSquareCount -= 1;
                    if (params.selectedSquareCount != 4) {
                        setup.setStartButton(false);
                    }
                }
                break;

            // handles a touch from game view
            case 2:
                if (!params.played) {
                    if (getSquare(index).getSquareState() == 11 || getSquare(index).getSquareState() == 21) {
                        game.setGameButton(true);
                    }
                    ArrayList<Square> g = current_grid;
                    ArrayList<Square> g1 = p1_grid;
                    ArrayList<Square> g2 = p2_grid;

                    params.played = getSquare(index).hitSquare();

                }
                break;
        }
    }

    public boolean getPlayed() {
        return params.played;
    }

    public void setPlayed(boolean set) {
        params.played = set;
    }


    public int OnTouched(float x, float y) {
        float relX = (x - marginX) / gameSize;
        float relY = (y - marginY) / gameSize;
        int a = -1;
        if (relY > 0 && relY < 1 && relX > 0 && relX < 1) {
            if (relY < .25) {
                if (relX < .25) {
                    current_grid.get(0).SetLocation(0, 0);
                    return 0;
                }

                if (relX < .5) {
                    current_grid.get(1).SetLocation(0.25F, 0);
                    return 1;
                }

                if (relX < .75) {
                    current_grid.get(2).SetLocation(0.5F, 0);
                    return 2;
                }

                if (relX < 1) {
                    current_grid.get(3).SetLocation(0.75F, 0);
                    return 3;
                }
            }

            if (relY < .5) {
                if (relX < .25) {
                    current_grid.get(4).SetLocation(0, 0.25F);
                    return 4;
                }

                if (relX < .5) {
                    current_grid.get(5).SetLocation(0.25F, 0.25F);
                    return 5;
                }

                if (relX < .75) {
                    current_grid.get(6).SetLocation(0.5F, 0.25F);
                    return 6;
                }

                if (relX < 1) {
                    current_grid.get(7).SetLocation(0.75F, 0.25F);
                    return 7;
                }
            }

            if (relY < .75) {
                if (relX < .25) {
                    current_grid.get(8).SetLocation(0, 0.5F);
                    return 8;
                }

                if (relX < .5) {
                    current_grid.get(9).SetLocation(0.25F, 0.5F);
                    return 9;
                }

                if (relX < .75) {
                    current_grid.get(10).SetLocation(0.5F, 0.5F);
                    return 10;
                }

                if (relX < 1) {
                    current_grid.get(11).SetLocation(0.75F, 0.5F);
                    return 11;
                }
            }

            if (relY < 1) {
                if (relX < .25) {
                    current_grid.get(12).SetLocation(0, 0.75F);
                    return 12;
                }

                if (relX < .5) {
                    current_grid.get(13).SetLocation(0.25F, 0.75F);
                    return 13;
                }

                if (relX < .75) {
                    current_grid.get(14).SetLocation(0.5F, 0.75F);
                    return 14;
                }

                if (relX < 1) {
                    current_grid.get(15).SetLocation(0.75F, 0.75F);
                    return 15;
                }
            }
        }

        return a;
    }


    private class Location {
        float x1 = 0;
        float y1 = 0;
    }


    /**
     * Aligns the center of the bitmap with the center of the square
     *
     * @param index
     * @param image
     * @return GridView Location
     */
    public Grid.Location alignment(int index, Bitmap image, float scale) {
        Grid.Location loc = new Grid.Location();
        float width = scale * image.getWidth() / ((float) gameSize);
        float height = scale * image.getHeight() / ((float) gameSize);

        /*
         After finding the top left corner,
         this aligns the center of the bitmap
         with the center of the square
         */
        loc.x1 = current_grid.get(index).GetLocationX() + (.125f - width / 2);
        loc.y1 = current_grid.get(index).GetLocationY() + (.125f - height / 2);

        return loc;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String ParamToString() throws IOException {
        Bundle bundle = new Bundle();
        putToBundle("update_params", bundle);

        // turn the params object into a string
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(stream);
        output.writeObject(params);
        output.close();
        // handles exceptions that arise from weird characters
        return Base64.getEncoder().encodeToString(stream.toByteArray());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Parameters StringToParam(String gameState) throws IOException, ClassNotFoundException {


        byte [] decodedString = Base64.getDecoder().decode(gameState);
        ByteArrayInputStream input = new ByteArrayInputStream(decodedString);
        ObjectInputStream stream = new ObjectInputStream(input);

        Parameters params = (Parameters) stream.readObject();
        stream.close();
        //copyParams(params);
        return params;
    }

    /**
     * Save the view state to a bundle
     *
     * @param key    key name to use in the bundle
     * @param bundle bundle to save to
     */
    public void putToBundle(String key, Bundle bundle) {


        params.gridStates1 = new ArrayList<>();
        params.gridStates2 = new ArrayList<>();

        params.gridLocations1 = new ArrayList<>();
        params.gridLocations2 = new ArrayList<>();

        for (Square element : p1_grid) {
            params.gridStates1.add(element.getSquareState());
            params.gridLocations1.add(element.GetLocationX());
            params.gridLocations1.add(element.GetLocationY());
        }
        for (Square element : p2_grid) {
            params.gridStates2.add(element.getSquareState());
            params.gridLocations2.add(element.GetLocationX());
            params.gridLocations2.add(element.GetLocationY());
        }

        bundle.putSerializable(key, params);
        bundle.putInt("localUserID", local_user_id);
        //String serialized = bundle.getString("420");
        //int x = 420;
    }

    /**
     * Get the view state from a bundle
     *
     * @param key    key name to use in the bundle
     * @param bundle bundle to load from
     */
    public void getFromBundle(String key, Bundle bundle) {
        params = (Grid.Parameters) bundle.getSerializable(key);
        copyParams(params, false, false, false);
        local_user_id = bundle.getInt("localUserID");
    }


    public void copyParams(Parameters newerParams, boolean keepLocalGrid, boolean keepLocalVariables, boolean keepLocalSquares)
    {
        // database: g1 g2
        // local g1 g2
        float x;
        float y;
        ArrayList<Square> saved_grid1 = p1_grid;
        ArrayList<Square> saved_grid2 = p2_grid;

        int saved = params.selectedSquareCount;
        Display saved_display = params.currentDisplay;
        int saved_gridMode = params.gridMode;
        boolean saved_played = params.played;
        if (!keepLocalSquares) {
            if (keepLocalGrid) {
                if (isCreator) {
                    for (int i = 0; i < p2_grid.size(); i++) {
                        p2_grid.get(i).setSquareState(newerParams.gridStates2.get(i));
                        p2_grid.get(i).reverseUpdateSquareState();
                        x = newerParams.gridLocations2.get(2 * i);
                        y = newerParams.gridLocations2.get(2 * i + 1);
                        p2_grid.get(i).SetLocation(x, y);
                    }
                } else {
                    for (int i = 0; i < p1_grid.size(); i++) {
                        p1_grid.get(i).setSquareState(newerParams.gridStates1.get(i));
                        p1_grid.get(i).reverseUpdateSquareState();
                        x = newerParams.gridLocations1.get(2 * i);
                        y = newerParams.gridLocations1.get(2 * i + 1);
                        p1_grid.get(i).SetLocation(x, y);
                    }
                }

            } else {
                for (int i = 0; i < p1_grid.size(); i++) {
                    p1_grid.get(i).setSquareState(newerParams.gridStates1.get(i));
                    p1_grid.get(i).reverseUpdateSquareState();
                    x = newerParams.gridLocations1.get(2 * i);
                    y = newerParams.gridLocations1.get(2 * i + 1);
                    p1_grid.get(i).SetLocation(x, y);
                }

                for (int i = 0; i < p2_grid.size(); i++) {
                    p2_grid.get(i).setSquareState(newerParams.gridStates2.get(i));
                    p2_grid.get(i).reverseUpdateSquareState();
                    x = newerParams.gridLocations2.get(2 * i);
                    y = newerParams.gridLocations2.get(2 * i + 1);
                    p2_grid.get(i).SetLocation(x, y);
                }
            }
        }
        if (keepLocalVariables) {
            params.surrenderId = newerParams.surrenderId;
            params.winnerId = newerParams.winnerId;
            params.timeoutId = newerParams.timeoutId;
        } else {
            params = newerParams;
        }

        params.selectedSquareCount = saved;
        params.currentDisplay = saved_display;
        params.gridMode = saved_gridMode;
        params.played = saved_played;

    }

    /**
     * Get a square from the current grid
     *
     * @Param index the square to get
     * @Return Square the square from the current grid
     */
    public Square getSquare(int index) {
        return current_grid.get(index);
    }

    public void setGrid(int start) {
        switch (start) {
            case 1:
                current_grid = p1_grid;
                break;
            case 2:
                current_grid = p2_grid;
                break;
        }
    }

    public void swapGrid(int swap_from) {
        if (current_grid == null)
            setGrid(swap_from);

        if (gameStart) {
            game.setGameButton(false);
        }

        switch (swap_from) {
            case 1:
                current_grid = p2_grid;

                break;
            case 2:
                current_grid = p1_grid;
                break;
        }
    }


    public void setCurrentDisplay(Display option){
        params.currentDisplay = option;
    }

    public void setGameID(int id){
        params.gameId = id;
        dlg.setGameID(params.gameId);
    }

    public void setUserID(int id){
        local_user_id = id;
        dlg.setUserID(id);
    }

    public void setDlgMode(WaitDlg.waitMode mode){
        dlg.setDlgMode(mode);
    }

    public void showDlg()
    {
        if (params.gridMode == 1) {
            setup.showWaitScreen(dlg);
        } else {
            game.showWaitScreen(dlg);
        }
    }

    public WaitDlg getDlg()
    {
        return dlg;
    }

    /**
     * Check if game has been won
     *
     * @Return boolean if player has found 4 ships
     */
    public boolean done() {
        return mCount == 4;
    }
}
