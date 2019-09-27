package com.aquacontrol.app;

import android.app.Notification;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private String TAG = "MainActivity";
    private PahoMqttClient pahoMqttClient;
    private String clientid = "";
    private Timer myTimer;
    //Callback when bottom navigation item is selected
/*    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
            BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

            switch (item.getItemId()) {
                case R.id.navigation_manual:

                    return true;

                case R.id.navigation_auto:

                    return true;
                case R.id.navigation_settings:
                    System.exit(0);
                    return true;
            }
            return false;
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                                                 // Main Activity layout file

        //ActionBar actionBar = getSupportActionBar();                                            // Add Icon to title bar
        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setIcon(R.mipmap.ic_app_launcher);

        //Generate unique client id for MQTT broker connection
        Random r = new Random();
        int i1 = r.nextInt(5000 - 1) + 1;
        clientid = "mqtt" + i1;

        //Get Edit field values from layout GUI
        String urlBroker    = Constants.MQTT_BROKER_URL;    //etBroker.getText().toString().trim();
        String username     = Constants.MQTT_CLIENT_UN;     //etUName.getText().toString().trim();
        String password     = Constants.MQTT_CLIENT_PW;     //etPWord.getText().toString().trim();

        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(  getApplicationContext(),                        // Connect to MQTT Broker
                                                urlBroker,
                                                clientid,
                                                username,
                                                password
                                             );
        //Register Bottom Navigation Callback
/*
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);      // Set nav menu "Select" callback
        BottomNavigationViewHelper.disableShiftMode(navigation);                                // Make all Text Visible
*/

        //Create listener for MQTT messages.
        mqttCallback();

        //Attend switches changes
        Switch swFilter = (Switch) findViewById(R.id.filterSW);
        swFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic = Constants.MQTT_FILT_CTL;
                if (b) {
                    String msg = Constants.MQTT_ON;
                    mqttPublishTo(topic,msg);
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    mqttPublishTo(topic,msg);
                }
            }
        });

        Switch swHeater = (Switch) findViewById(R.id.heaterSW);
        swHeater.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic = Constants.MQTT_HEAT_CTL;
                if (b) {
                    String msg = Constants.MQTT_ON;
                    mqttPublishTo(topic,msg);
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    mqttPublishTo(topic,msg);
                }
            }
        });

        Switch swLamp = (Switch) findViewById(R.id.lampSW);
        swLamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic = Constants.MQTT_LAMP_CTL;
                if (b) {
                    String msg = Constants.MQTT_ON;
                    mqttPublishTo(topic,msg);
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    mqttPublishTo(topic,msg);
                }
            }
        });

        Switch swAutoLamp = (Switch) findViewById(R.id.lampAutoSW);
        swAutoLamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic = Constants.MQTT_LAMP_CTL_AUTO;
                Switch swLampProg1 = (Switch) findViewById(R.id.lampProg1SW);
                Switch swLampProg2 = (Switch) findViewById(R.id.lampProg2SW);
                Switch swLampProg3 = (Switch) findViewById(R.id.lampProg3SW);
                if (b) {
                    String msg = Constants.MQTT_ON;
                    swLampProg1.setEnabled(true);
                    swLampProg2.setEnabled(true);
                    swLampProg3.setEnabled(true);
                    mqttPublishTo(topic,msg);
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    swLampProg1.setEnabled(false);
                    swLampProg2.setEnabled(false);
                    swLampProg3.setEnabled(false);
                    mqttPublishTo(topic,msg);
                }
            }
        });

        Switch swLampProg1 = (Switch) findViewById(R.id.lampProg1SW);
        swLampProg1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic = Constants.MQTT_LAMP_CTL_A0ACT;
                if (b) {
                    String msg = Constants.MQTT_ON;
                    mqttPublishTo(topic,msg);
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    mqttPublishTo(topic,msg);
                }
            }
        });

        final EditText  lampP1OnTime = findViewById(R.id.lampPR1OnTime);
        lampP1OnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String topic = Constants.MQTT_LAMP_CTL_A0ON;
                        String s_time = "0:00";
                        s_time = String.format("%02d:%02d", hourOfDay, minutes);
                        lampP1OnTime.setText(s_time);
                        mqttPublishTo(topic,s_time);
                    }
                }, currentHour, currentMinute, true);
                timePickerDialog.setTitle("Select ON Time");
                timePickerDialog.show();
            }

        });

        final EditText  lampP1OffTime = findViewById(R.id.lampPR1OffTime);
        lampP1OffTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String topic = Constants.MQTT_LAMP_CTL_A0OFF;
                        String s_time = "0:00";
                        s_time = String.format("%02d:%02d", hourOfDay, minutes);
                        lampP1OnTime.setText(s_time);
                        mqttPublishTo(topic,s_time);
                    }
                }, currentHour, currentMinute, true);
                timePickerDialog.setTitle("Select ON Time");
                timePickerDialog.show();
            }
        });

        Switch swLampProg2 = (Switch) findViewById(R.id.lampProg2SW);
        swLampProg2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic = Constants.MQTT_LAMP_CTL_A1ACT;
                if (b) {
                    String msg = Constants.MQTT_ON;
                    mqttPublishTo(topic,msg);
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    mqttPublishTo(topic,msg);
                }
            }
        });

        Switch swLampProg3 = (Switch) findViewById(R.id.lampProg3SW);
        swLampProg3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic = Constants.MQTT_LAMP_CTL_A2ACT;
                if (b) {
                    String msg = Constants.MQTT_ON;
                    mqttPublishTo(topic,msg);
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    mqttPublishTo(topic,msg);
                }
            }
        });

        //TODO: Improve subscribing avoiding 1 second delay on APP launch
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mqttSubscriteTo(Constants.MQTT_AQU_VERS);
                mqttSubscriteTo(Constants.MQTT_AQU_RST);
                mqttSubscriteTo(Constants.MQTT_AQU_IP);
                mqttSubscriteTo(Constants.MQTT_AQU_TIME);
                mqttSubscriteTo(Constants.MQTT_AQU_LAMP_STA);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_AUTO);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A0ACT);
                mqttSubscriteTo(Constants.MQTT_AQU_AERA_STA);
                mqttSubscriteTo(Constants.MQTT_AQU_HEAT_STA);
                mqttSubscriteTo(Constants.MQTT_AQU_FILT_STA);
                mqttSubscriteTo(Constants.MQTT_AQU_TEMP);
            }
        }, 1000);


        //Create Timer to report MQTT connection status every 1 second
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ScheduleTasks();
            }

        }, 0, 1000);
    }
    private void ScheduleTasks()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(RunScheduledTasks);
    }


    private Runnable RunScheduledTasks = new Runnable() {
        public void run() {
            //This method runs in the same thread as the UI.

            //Check MQTT Connection Status
            String urlBroker = Constants.MQTT_BROKER_URL;
            String username  = Constants.MQTT_CLIENT_UN;
            String password  = Constants.MQTT_CLIENT_PW;
            ActionBar actionBar = getSupportActionBar();

            //Generate unique client id for MQTT broker connection
            Random r = new Random();
            int i1 = r.nextInt(5000 - 1) + 1;
            clientid = "mqtt" + i1;

            if(pahoMqttClient.mqttAndroidClient.isConnected() ) {
                actionBar.setSubtitle("Connected");
            }
            else {
                actionBar.setSubtitle("Disconnected");
                //Connect to Broker
                client = pahoMqttClient.getMqttClient(getApplicationContext(), urlBroker, clientid, username, password);
                //Set Mqtt Message Callback
                mqttCallback();
            }
        }
    };

    private void mqttSubscriteTo(String topic) {
        if(pahoMqttClient.mqttAndroidClient.isConnected() ) {
            try {
                pahoMqttClient.subscribe(client, topic, 1);

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void mqttPublishTo(String topic, String msg) {
        if(pahoMqttClient.mqttAndroidClient.isConnected() ) {
            try {
                pahoMqttClient.publishMessage(client, msg, 1, topic);
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    // Called when a subscribed message is received
    protected void mqttCallback() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                //msg("Connection lost...");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.equals(Constants.MQTT_AQU_TEMP)) {
                    //Add custom message handling here (if topic = "aquarium/temperature")
                    TextView tvTemperature = (TextView) findViewById(R.id.temperature);
                    tvTemperature.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_AQU_FILT_STA)) {
                    Switch swFilter = (Switch) findViewById(R.id.filterSW);
                    if (message.toString().matches(Constants.MQTT_ON)) {
                        swFilter.setChecked(true);
                    }
                    else {
                        swFilter.setChecked(false);
                    }
                }
                else if(topic.equals(Constants.MQTT_AQU_HEAT_STA)) {
                    Switch swHeater = (Switch) findViewById(R.id.heaterSW);
                    if (message.toString().matches(Constants.MQTT_ON)) {
                        swHeater.setChecked(true);
                    }
                    else {
                        swHeater.setChecked(false);
                    }
                }
                else if(topic.equals(Constants.MQTT_AQU_LAMP_STA)) {
                    Switch swLamp = (Switch) findViewById(R.id.lampSW);
                    if (message.toString().matches(Constants.MQTT_ON)) {
                        swLamp.setChecked(true);
                    }
                    else {
                        swLamp.setChecked(false);
                    }
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_AUTO)) {
                    Switch swLamp = (Switch) findViewById(R.id.lampAutoSW);
                    if (message.toString().matches(Constants.MQTT_ON)) {
                        swLamp.setChecked(true);
                    }
                    else {
                        swLamp.setChecked(false);
                    }
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A0ACT)) {
                    Switch swLamp = (Switch) findViewById(R.id.lampProg1SW);
                    if (message.toString().matches(Constants.MQTT_ON)) {
                        swLamp.setChecked(true);
                    }
                    else {
                        swLamp.setChecked(false);
                    }
                }
                else {

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}
