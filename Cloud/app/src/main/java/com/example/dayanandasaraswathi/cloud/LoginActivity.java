package com.example.dayanandasaraswathi.cloud;

/**
 * Created by DayanandaSaraswathi on 12/8/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
    public static final String SHARED_PREFS = "CongnitoCred";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        SharedPreferences settings = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        if (settings.getString("loggedin", "").toString().equals("loggedin")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

        Button b = (Button) findViewById(R.id.loginbutton);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText username = (EditText) findViewById(R.id.login);
                EditText password = (EditText) findViewById(R.id.password);

                if(username.getText().toString().length() > 0 && password.getText().toString().length() > 0 ) {
                    if(username.getText().toString().equalsIgnoreCase("CloudUser") && password.getText().toString().equalsIgnoreCase("useast1")) {
                        SharedPreferences settings = getSharedPreferences(SHARED_PREFS, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("loggedin", "loggedin");
                        editor.commit();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(LoginActivity.this,
                                "Unable to Login. Please verify user credentials",
                                Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(LoginActivity.this,
                            "Unable to Login. Please verify user credentials",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
