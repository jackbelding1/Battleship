package edu.msu.team9.proj1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import edu.msu.team9.proj1.Cloud.Cloud;

public class WinningActivity extends AppCompatActivity {

    private int gameid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int displayText = R.string.game_ended;
        int deleteCheck = 0;
        setContentView(R.layout.activity_winning);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            displayText = bundle.getInt("displayText", R.string.game_ended);
        }
        TextView dlg = (TextView)findViewById(R.id.winningText);
        dlg.setText(displayText);
    }

    public void onRestart(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}