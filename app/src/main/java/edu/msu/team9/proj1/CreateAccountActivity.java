package edu.msu.team9.proj1;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import edu.msu.team9.proj1.Cloud.Cloud;

public class CreateAccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
        Intent intent = getIntent();

    }

    public void onCreateAccount(View view) {


        TextInputEditText user = (TextInputEditText) findViewById(R.id.user);
        TextInputEditText password = (TextInputEditText) findViewById(R.id.p1);
        TextInputEditText rePassword = (TextInputEditText) findViewById(R.id.p2);
        TextView response = (TextView) findViewById(R.id.clientResponse);
        String p1 = Objects.requireNonNull(password.getText()).toString();
        String p2 = Objects.requireNonNull(rePassword.getText()).toString();
        String us = Objects.requireNonNull(user.getText()).toString();
        if (!p1.isEmpty() && !p2.isEmpty()) {
            if (p1.equals(p2)) {
                if (!us.isEmpty()) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {

                            Cloud service = new Cloud();

                            boolean result = service.createUser(us, p1);

                            if (result) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        launchLogin();
                                    }
                                });
                            } else {
                                response.setText(R.string.user_exists);
                            }

                        }

                    }).start();



                } else {
                    response.setText(R.string.enter_username_please);
                }

            } else {

                response.setText(R.string.not_match);
            }

        }




    }

    public void launchLogin()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
