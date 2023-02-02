package edu.msu.team9.proj1;

import android.content.Context;

import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import java.io.Serializable;

/**
 * TODO: document your custom view class.
 */
public class MainView extends View {


    public MainView(Context context) {
        super(context);
        init(null, 0);
    }

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MainView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
    }


}