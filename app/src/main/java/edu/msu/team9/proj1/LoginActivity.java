package edu.msu.team9.proj1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import edu.msu.team9.proj1.Cloud.Cloud;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button createAccButton;
    private TextView clientResponse;
    private CheckBox rememberMe;
    private static final String USER_KEY = "USERNAME";
    private static final String PASS_KEY = "PASSWORD";
    private static final String CHECKED_KEY = "CHECKED";
    private SharedPreferences sharedPref;
    private TextInputEditText user;
    private TextInputEditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginButton = (Button) findViewById(R.id.Login);
        createAccButton = (Button) findViewById(R.id.create_account);
        clientResponse = (TextView) findViewById(R.id.clientResponse);
        rememberMe = (CheckBox) findViewById(R.id.rememberCheckBox);
        user = (TextInputEditText) findViewById(R.id.userLogin);
        password = (TextInputEditText) findViewById(R.id.passLogin);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        user.setText(sharedPref.getString(USER_KEY, ""));
        password.setText(sharedPref.getString(PASS_KEY, ""));
        rememberMe.setChecked(sharedPref.getBoolean(CHECKED_KEY, false));
    }


    public void onLoginButton(View view)
    {
        TextView clientResponse = (TextView) findViewById(R.id.clientResponse);
        String username = Objects.requireNonNull(user.getText()).toString();
        String pass = Objects.requireNonNull(password.getText()).toString();
        if (username.toString().isEmpty() || pass.toString().isEmpty())
        {
            clientResponse.setText(R.string.login);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud service = new Cloud();
                int userid = service.checkUser(username,pass);
                if (userid != -1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startMain(userid, username);
                            savePreferences();
                        }
                    });
                } else {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(view.getContext(),
                                        R.string.client_response_txt,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        }).start();


    }


    public void changeText()
    {
        clientResponse.setText(R.string.client_response_txt);
    }

    public void onCreateAccountButton(View view)
    {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }

    public void startMain(int userid, String username)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userid", userid);
        intent.putExtra("current_username", username);
        startActivity(intent);
    }

    public void savePreferences()
    {
        SharedPreferences.Editor editor = sharedPref.edit();
        if(rememberMe.isChecked())
        {
            editor.putBoolean(CHECKED_KEY,true);
            editor.putString(USER_KEY, Objects.requireNonNull(user.getText()).toString());
            editor.putString(PASS_KEY, Objects.requireNonNull(password.getText()).toString());
        }
        else
        {
            editor.putBoolean(CHECKED_KEY,false);
            editor.putString(USER_KEY,"");
            editor.putString(PASS_KEY, "");
        }
        editor.apply();
    }
}
