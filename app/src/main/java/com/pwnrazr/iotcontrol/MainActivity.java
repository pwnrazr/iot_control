package com.pwnrazr.iotcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    public static class http_comm extends Thread {  // Communications
        String[] content_split = {"",""};
        boolean updateReady = false;

        String url;
        http_comm(String url){
            this.url = url;
        }
        void execute(final String... command) {
            new Thread() {
                public void run() {
                    try {
                        URL connection_url = new URL("http://" + url + "/" + command[0]);
                        HttpURLConnection connection = (HttpURLConnection) connection_url.openConnection();

                        // Get content
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null)
                            result.append(inputLine).append("\n");

                        if(connection.getResponseCode()!=204) {     // Guard from crashing due to result being empty when a command is sent (receive no content)
                            content_split = result.toString().split(",");
                            Log.i("iot_control", "content: " + content_split[0] + "=" + content_split[1]);
                            updateReady = true;
                        }

                        //connection.disconnect();       // Potentially unneeded
                    } catch (IOException e) {
                        Log.e("iot_control", e.toString());
                    }
                }
            }.start();
        }
    }

    public class settings {
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        final String esp32_ip_default = "192.168.1.183";
        final String nodeRelay_ip_default = "192.168.1.161";
        String esp32_ip = "";
        String nodeRelay_ip = "";
        EditText in_esp32_ip = findViewById(R.id.input_esp32_ip);
        EditText in_nodeRelay_ip = findViewById(R.id.input_noderelay_ip);

        void startup() {
            if(!read("esp32_ip").equals("ERROR")){
                esp32_ip = read("esp32_ip");
                in_esp32_ip.setText(read("esp32_ip"));
            } else {
                esp32_ip = esp32_ip_default;
                in_esp32_ip.setText(esp32_ip_default);
            }
            if(!read("nodeRelay_ip").equals("ERROR")){
                nodeRelay_ip = read("nodeRelay_ip");
                in_nodeRelay_ip.setText(read("nodeRelay_ip"));
            } else {
                nodeRelay_ip = nodeRelay_ip_default;
                in_nodeRelay_ip.setText(nodeRelay_ip_default);
            }
        }
        void write(String key, String value) {
            editor.putString(key, value);
            editor.apply();
            Log.i("iot_control", "Saved - Key:" + key + " Value:" + value);
        }
        String read(String key) {
            return sharedPref.getString(key, "ERROR");
        }
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {    // The actual program
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declarations

        // Settings related
        final settings prefsSet = new settings();
        prefsSet.startup();
        Button prefsSave_button = findViewById(R.id.saveButton);

        final ToggleButton esp32_led_toggle = findViewById(R.id.esp32_led_toggle);
        final ToggleButton relay0_toggle = findViewById(R.id.relay0_toggle);
        final ToggleButton relay1_toggle = findViewById(R.id.relay1_toggle);
        final ToggleButton relay2_toggle = findViewById(R.id.relay2_toggle);
        final ToggleButton relay3_toggle = findViewById(R.id.relay3_toggle);

        //communications part
        final http_comm esp32_comm_0 = new http_comm(prefsSet.esp32_ip);
        final http_comm relay_node_comm_0 = new http_comm(prefsSet.nodeRelay_ip);
        final http_comm relay_node_comm_1 = new http_comm(prefsSet.nodeRelay_ip);
        final http_comm relay_node_comm_2 = new http_comm(prefsSet.nodeRelay_ip);
        final http_comm relay_node_comm_3 = new http_comm(prefsSet.nodeRelay_ip);

        // Disable buttons
        esp32_led_toggle.setEnabled(false);
        relay0_toggle.setEnabled(false);
        relay1_toggle.setEnabled(false);
        relay2_toggle.setEnabled(false);
        relay3_toggle.setEnabled(false);

        // on app startup get status of things
        esp32_comm_0.execute("reqled");     // Request status of LED on app startup
        relay_node_comm_0.execute("req_relay0");
        relay_node_comm_1.execute("req_relay1");
        relay_node_comm_2.execute("req_relay2");
        relay_node_comm_3.execute("req_relay3");

        new CountDownTimer(5, 5)  // Loop update buttons
        {
            public void onTick(long l) {}
            public void onFinish()
            {
                if (esp32_comm_0.updateReady) {   // Only update when there's actually something to update
                    if (esp32_comm_0.content_split[0].equals("LED")) {
                        if (esp32_comm_0.content_split[1].equals("ON")) {
                            esp32_led_toggle.setChecked(true);
                        } else {
                            esp32_led_toggle.setChecked(false);
                        }
                        esp32_led_toggle.setEnabled(true);
                        esp32_comm_0.updateReady = false;
                    }
                }
                if(relay_node_comm_0.updateReady) {
                    if (relay_node_comm_0.content_split[0].equals("relay0")) {
                        if (relay_node_comm_0.content_split[1].equals("on")) {
                            relay0_toggle.setChecked(true);
                        } else {
                            relay0_toggle.setChecked(false);
                        }
                        relay0_toggle.setEnabled(true);
                        relay_node_comm_0.updateReady = false;
                    }
                }
                if(relay_node_comm_1.updateReady) {
                    if (relay_node_comm_1.content_split[0].equals("relay1")) {
                        if (relay_node_comm_1.content_split[1].equals("on")) {
                            relay1_toggle.setChecked(true);
                        } else {
                            relay1_toggle.setChecked(false);
                        }
                        relay1_toggle.setEnabled(true);
                        relay_node_comm_1.updateReady = false;
                    }
                }
                if(relay_node_comm_2.updateReady) {
                    if (relay_node_comm_2.content_split[0].equals("relay2")) {
                        if (relay_node_comm_2.content_split[1].equals("on")) {
                            relay2_toggle.setChecked(true);
                        } else {
                            relay2_toggle.setChecked(false);
                        }
                        relay2_toggle.setEnabled(true);
                        relay_node_comm_2.updateReady = false;
                    }
                }
                if(relay_node_comm_3.updateReady) {
                    if (relay_node_comm_3.content_split[0].equals("relay3")) {
                        if (relay_node_comm_3.content_split[1].equals("on")) {
                            relay3_toggle.setChecked(true);
                        } else {
                            relay3_toggle.setChecked(false);
                        }
                        relay3_toggle.setEnabled(true);
                        relay_node_comm_3.updateReady = false;
                    }
                }
                //////
                start();
            }
        }.start();

        prefsSave_button.setOnClickListener(new View.OnClickListener() {    // Save preferences button
            @Override
            public void onClick(View view) {
                prefsSet.write("esp32_ip", prefsSet.in_esp32_ip.getText().toString());
                prefsSet.write("nodeRelay_ip", prefsSet.in_nodeRelay_ip.getText().toString());

                // Restart function
                Activity current = MainActivity.this;   // Get parent activity
                current.startActivity(new Intent(MainActivity.this.getApplicationContext(),current.getClass()));    // Start a same new one
                current.finish();   // Finish current
            }
        });

        esp32_led_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {  // Ambient LED
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    esp32_comm_0.execute("led1=1");
                } else {
                    esp32_comm_0.execute("led1=0");
                }
            }
        });

        relay0_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {     // Relay 0
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    relay_node_comm_0.execute("relay0=1");
                } else {
                    relay_node_comm_0.execute("relay0=0");
                }
            }
        });

        relay1_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {     // Relay 1
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    relay_node_comm_1.execute("relay1=1");
                } else {
                    relay_node_comm_1.execute("relay1=0");
                }
            }
        });

        relay2_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {     // Relay 2
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    relay_node_comm_2.execute("relay2=1");
                } else {
                    relay_node_comm_2.execute("relay2=0");
                }
            }
        });

        relay3_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {     // Relay 3
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    relay_node_comm_3.execute("relay3=1");
                } else {
                    relay_node_comm_3.execute("relay3=0");
                }
            }
        });
    }
}