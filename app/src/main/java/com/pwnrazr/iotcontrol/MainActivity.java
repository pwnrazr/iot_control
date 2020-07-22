package com.pwnrazr.iotcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    // Global variables
    static String esp32_receiveMsg = "";
    static String relay_node_receiveMsg = "";
    static String[] esp32_receiveMsg_split;
    static String[] relay_node_receiveMsg_split;

    static boolean esp32_updateReady = false;
    static boolean relay_node_updateReady = false;

    static class esp32_comm extends AsyncTask<String, Void, String> {  // Communication to ESP32 nodeMCU
        @Override
        protected String doInBackground(String... params) {
            try {
                URL esp32_url = new URL("http://192.168.1.179/?" + params[0]);
                HttpURLConnection esp32_connection = (HttpURLConnection) esp32_url.openConnection();

                Log.i("iot_control",Integer.toString(esp32_connection.getResponseCode()));
                Log.i("iot_control",esp32_connection.getResponseMessage());
                esp32_receiveMsg = esp32_connection.getResponseMessage();
                esp32_receiveMsg_split = esp32_receiveMsg.split(",");  //splits receiveMsg with comma(,) as separator
                esp32_updateReady = true;

                BufferedReader in = new BufferedReader(new InputStreamReader(esp32_connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    result.append(inputLine).append("\n");

                in.close();
                esp32_connection.disconnect();
                return result.toString();
            } catch (IOException e) {
                Log.e("iot_control",e.toString());
            }

            return null;
        }
    }
    static class relay_node_comm extends AsyncTask<String, Void, String> {  // Communication to ESP8266 relay node
        @Override
        protected String doInBackground(String... params) {
            try {
                URL relay_node_url = new URL("http://192.168.1.161/?" + params[0]);
                HttpURLConnection relay_node_connection = (HttpURLConnection) relay_node_url.openConnection();

                Log.i("iot_control",Integer.toString(relay_node_connection.getResponseCode()));
                Log.i("iot_control",relay_node_connection.getResponseMessage());
                relay_node_receiveMsg = relay_node_connection.getResponseMessage();
                relay_node_receiveMsg_split = relay_node_receiveMsg.split(",");  //splits receiveMsg with comma(,) as separator
                relay_node_updateReady = true;

                BufferedReader in = new BufferedReader(new InputStreamReader(relay_node_connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    result.append(inputLine).append("\n");

                in.close();
                relay_node_connection.disconnect();
                return result.toString();
            } catch (IOException e) {
                Log.e("iot_control",e.toString());
            }

            return null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {    // The actual program
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declarations
        final ToggleButton esp32_led_toggle = findViewById(R.id.esp32_led_toggle);

        final ToggleButton relay0_toggle = findViewById(R.id.relay0_toggle);
        final ToggleButton relay1_toggle = findViewById(R.id.relay1_toggle);
        final ToggleButton relay2_toggle = findViewById(R.id.relay2_toggle);
        final ToggleButton relay3_toggle = findViewById(R.id.relay3_toggle);

        // on app startup get status of things
        new esp32_comm().execute("reqled");     //Request status of LED on app startup

        new relay_node_comm().execute("req_relay0");
        new relay_node_comm().execute("req_relay1");
        new relay_node_comm().execute("req_relay2");
        new relay_node_comm().execute("req_relay3");

        new CountDownTimer(5, 5)  //loop update buttons
        {
            public void onTick(long l) {}
            public void onFinish()
            {
                if(esp32_updateReady) {   //Only update when there's actually something to update
                    TextView debugText = findViewById(R.id.debugText);
                    debugText.setText(String.format("%s=%s",esp32_receiveMsg_split[0], esp32_receiveMsg_split[1]));
                    if (esp32_receiveMsg_split[0].equals("LED")) {
                        if (esp32_receiveMsg_split[1].equals("ON")) {
                            esp32_led_toggle.setChecked(true);
                        } else {
                            esp32_led_toggle.setChecked(false);
                        }
                    }
                    esp32_updateReady = false;
                }
                if(relay_node_updateReady){
                    TextView debugText = findViewById(R.id.debugText);
                    debugText.setText(String.format("%s=%s",relay_node_receiveMsg_split[0], relay_node_receiveMsg_split[1]));
                    if(relay_node_receiveMsg_split[0].equals("relay0")) {
                        if(relay_node_receiveMsg_split[1].equals("on")) {
                            relay0_toggle.setChecked(true);
                        } else {
                            relay0_toggle.setChecked(false);
                        }
                    }
                    if(relay_node_receiveMsg_split[0].equals("relay1")) {
                        if(relay_node_receiveMsg_split[1].equals("on")) {
                            relay1_toggle.setChecked(true);
                        } else {
                            relay1_toggle.setChecked(false);
                        }
                    }
                    if(relay_node_receiveMsg_split[0].equals("relay2")) {
                        if(relay_node_receiveMsg_split[1].equals("on")) {
                            relay2_toggle.setChecked(true);
                        } else {
                            relay2_toggle.setChecked(false);
                        }
                    }
                    if(relay_node_receiveMsg_split[0].equals("relay3")) {
                        if(relay_node_receiveMsg_split[1].equals("on")) {
                            relay3_toggle.setChecked(true);
                        } else {
                            relay3_toggle.setChecked(false);
                        }
                    }
                    relay_node_updateReady = false;
                }
                start();
            }
        }.start();

        esp32_led_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {  // Ambient LED
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    new esp32_comm().execute("led1=1");
                } else {
                    new esp32_comm().execute("led1=0");
                }
            }
        });

        relay0_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {     // Relay 0
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    new relay_node_comm().execute("relay0=1");
                } else {
                    new relay_node_comm().execute("relay0=0");
                }
            }
        });

        relay1_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {     // Relay 1
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    new relay_node_comm().execute("relay1=1");
                } else {
                    new relay_node_comm().execute("relay1=0");
                }
            }
        });

        relay2_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {     // Relay 2
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    new relay_node_comm().execute("relay2=1");
                } else {
                    new relay_node_comm().execute("relay2=0");
                }
            }
        });

        relay3_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {     // Relay 3
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    new relay_node_comm().execute("relay3=1");
                } else {
                    new relay_node_comm().execute("relay3=0");
                }
            }
        });
    }
}