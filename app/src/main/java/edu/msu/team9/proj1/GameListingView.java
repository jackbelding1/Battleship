package edu.msu.team9.proj1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class GameListingView extends View {


    public GameListingView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameListingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameListingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    private void init(AttributeSet attrs, int defStyle) {

    }
}
