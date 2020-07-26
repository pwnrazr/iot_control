package com.pwnrazr.iotcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    public static class http_comm extends Thread {  // Communications
        String receiveMsg = "";
        String[] receiveMsg_split = {"",""};
        boolean updateReady = false;

        String url;
        http_comm(String url){
            this.url = url;
        }
        void execute(final String... command) {
            new Thread() {
                public void run() {
                    try {
                        URL relay_node_url = new URL("http://" + url + "/?" + command[0]);
                        HttpURLConnection relay_node_connection = (HttpURLConnection) relay_node_url.openConnection();

                        Log.i("iot_control", Integer.toString(relay_node_connection.getResponseCode()));
                        Log.i("iot_control", relay_node_connection.getResponseMessage());
                        receiveMsg = relay_node_connection.getResponseMessage();
                        receiveMsg_split = receiveMsg.split(",");  //splits receiveMsg with comma(,) as separator
                        updateReady = true;

                        //relay_node_connection.disconnect();       //potentially unneeded
                    } catch (IOException e) {
                        Log.e("iot_control", e.toString());
                    }
                }
            }.start();
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

        //communications part
        final http_comm esp32_comm_0 = new http_comm("192.168.1.183");
        final http_comm relay_node_comm_0 = new http_comm("192.168.1.161");
        final http_comm relay_node_comm_1 = new http_comm("192.168.1.161");
        final http_comm relay_node_comm_2 = new http_comm("192.168.1.161");
        final http_comm relay_node_comm_3 = new http_comm("192.168.1.161");

        // Disable buttons
        esp32_led_toggle.setEnabled(false);
        relay0_toggle.setEnabled(false);
        relay1_toggle.setEnabled(false);
        relay2_toggle.setEnabled(false);
        relay3_toggle.setEnabled(false);

        // on app startup get status of things
        esp32_comm_0.execute("reqled");     //Request status of LED on app startup
        relay_node_comm_0.execute("req_relay0");
        relay_node_comm_1.execute("req_relay1");
        relay_node_comm_2.execute("req_relay2");
        relay_node_comm_3.execute("req_relay3");

        new CountDownTimer(5, 5)  //loop update buttons
        {
            public void onTick(long l) {}
            public void onFinish()
            {
                if (esp32_comm_0.updateReady) {   //Only update when there's actually something to update
                    if (esp32_comm_0.receiveMsg_split[0].equals("LED")) {
                        if (esp32_comm_0.receiveMsg_split[1].equals("ON")) {
                            esp32_led_toggle.setChecked(true);
                        } else {
                            esp32_led_toggle.setChecked(false);
                        }
                        esp32_led_toggle.setEnabled(true);
                        esp32_comm_0.updateReady = false;
                    }
                }
                if(relay_node_comm_0.updateReady) {
                    if (relay_node_comm_0.receiveMsg_split[0].equals("relay0")) {
                        if (relay_node_comm_0.receiveMsg_split[1].equals("on")) {
                            relay0_toggle.setChecked(true);
                        } else {
                            relay0_toggle.setChecked(false);
                        }
                        relay0_toggle.setEnabled(true);
                        relay_node_comm_0.updateReady = false;
                    }
                }
                if(relay_node_comm_1.updateReady) {
                    if (relay_node_comm_1.receiveMsg_split[0].equals("relay1")) {
                        if (relay_node_comm_1.receiveMsg_split[1].equals("on")) {
                            relay1_toggle.setChecked(true);
                        } else {
                            relay1_toggle.setChecked(false);
                        }
                        relay1_toggle.setEnabled(true);
                        relay_node_comm_1.updateReady = false;
                    }
                }
                if(relay_node_comm_2.updateReady) {
                    if (relay_node_comm_2.receiveMsg_split[0].equals("relay2")) {
                        if (relay_node_comm_2.receiveMsg_split[1].equals("on")) {
                            relay2_toggle.setChecked(true);
                        } else {
                            relay2_toggle.setChecked(false);
                        }
                        relay2_toggle.setEnabled(true);
                        relay_node_comm_2.updateReady = false;
                    }
                }
                if(relay_node_comm_3.updateReady) {
                    if (relay_node_comm_3.receiveMsg_split[0].equals("relay3")) {
                        if (relay_node_comm_3.receiveMsg_split[1].equals("on")) {
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