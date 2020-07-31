package com.pwnrazr.iotcontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onBackPressed() {   // Start a new MainActivity so that values are reloaded
        Activity current = this;   // Get parent activity
        current.startActivity(new Intent(this, MainActivity.class));    // Start a same new one
        current.finish();   // Finish current
    }
    public class settings {
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        void write(String key, String value) {
            editor.putString(key, value);
            editor.apply();
            Log.i("iot_control", "Saved - Key:" + key + " Value:" + value);
        }
        String read(String key) {
            return sharedPref.getString(key, "ERROR");
        }
    }

    // Toolbar stuff
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // Not really needed but I'll just keep this here as a placeholder
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar settings_toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settings_toolbar);

        // Settings related
        final settings prefsSet = new settings();
        Button prefsSave_button = findViewById(R.id.settingsButton);
        final EditText in_esp32_ip = findViewById(R.id.input_esp32_ip);
        final EditText in_nodeRelay_ip = findViewById(R.id.input_noderelay_ip);

        in_esp32_ip.setText(prefsSet.read("esp32_ip"));
        in_nodeRelay_ip.setText(prefsSet.read("nodeRelay_ip"));

        prefsSave_button.setOnClickListener(new View.OnClickListener() {    // Save preferences button
            @Override
            public void onClick(View view) {
                prefsSet.write("esp32_ip", in_esp32_ip.getText().toString());
                prefsSet.write("nodeRelay_ip", in_nodeRelay_ip.getText().toString());
            }
        });
    }
}