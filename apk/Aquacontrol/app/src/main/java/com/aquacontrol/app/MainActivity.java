package com.aquacontrol.app;

import android.app.Notification;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                                                 // Main Activity layout file

        //ActionBar actionBar = getSupportActionBar();                                            // Add Icon to title bar
        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setIcon(R.mipmap.ic_app_launcher);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //Generate unique client id for MQTT broker connection
        Random r = new Random();
        int i1 = r.nextInt(5000 - 1) + 1;
        clientid = "mqtt" + i1;

        // MQTT settings
        String urlBroker    = "";
        String username     = "";
        String password     = "";

        // Load settings
        final SharedPreferences sharedpreferences = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        if (sharedpreferences.contains(Constants.PREF_BROKER_URL)) {
            urlBroker = sharedpreferences.getString(Constants.PREF_BROKER_URL, "");
        }
        if (sharedpreferences.contains(Constants.PREF_BROKER_USER)) {
            username = sharedpreferences.getString(Constants.PREF_BROKER_USER, "");
        }
        if (sharedpreferences.contains(Constants.PREF_BROKER_PASS)) {
            password = sharedpreferences.getString(Constants.PREF_BROKER_PASS, "");
        }

        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(  getApplicationContext(),                        // Connect to MQTT Broker
                                                urlBroker,
                                                clientid,
                                                username,
                                                password
                                             );

        //Create listener for MQTT messages.
        mqttCallback();

        /********************/
        /*      FILTER      */
        /********************/
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

        /********************/
        /*      AERATOR     */
        /********************/

        Switch swAerator = (Switch) findViewById(R.id.aeratorSW);
        swAerator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic = Constants.MQTT_AERA_CTL;
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

        /********************/
        /*      HEATER      */
        /********************/
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

        final EditText etTempMax = (EditText) findViewById(R.id.tempMax);
        etTempMax.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String topic = Constants.MQTT_AQU_TEMP_TMAX;
                    String msg = etTempMax.getText().toString();
                    mqttPublishTo(topic, msg);
                    Toast.makeText(MainActivity.this, "Maximum temperature set", Toast.LENGTH_SHORT).show();
                    handled = true;
                }
                return handled;
            }
        });

        final EditText etTempMin = (EditText) findViewById((R.id.tempMin));
        etTempMin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String topic = Constants.MQTT_AQU_TEMP_TMIN;
                    String msg = etTempMin.getText().toString();
                    mqttPublishTo(topic, msg);
                    Toast.makeText(MainActivity.this, "Minimum temperature set", Toast.LENGTH_SHORT).show();
                    handled = true;
                }
                return handled;
            }
        });

        /********************/
        /*       LAMP       */
        /********************/

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
                Switch swLamp      = (Switch) findViewById(R.id.lampSW);
                Switch swLampProg1 = (Switch) findViewById(R.id.lampProg1SW);
                Switch swLampProg2 = (Switch) findViewById(R.id.lampProg2SW);
                Switch swLampProg3 = (Switch) findViewById(R.id.lampProg3SW);
                if (b) {
                    String msg = Constants.MQTT_ON;
                    swLamp.setEnabled(false);
                    swLampProg1.setEnabled(true);
                    swLampProg2.setEnabled(true);
                    swLampProg3.setEnabled(true);
                    mqttPublishTo(topic,msg);
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    swLamp.setEnabled(true);
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
                String topicOn = Constants.MQTT_LAMP_CTL_A0ON;
                String topicOff = Constants.MQTT_LAMP_CTL_A0OFF;
                if (b) {
                    String msg = Constants.MQTT_ON;
                    EditText lampOnTime = (EditText) findViewById(R.id.lampPR1OnTime);
                    EditText lampOffTime = (EditText) findViewById(R.id.lampPR1OffTime);
                    mqttPublishTo(topic,msg);
                    mqttPublishTo(topicOn,lampOnTime.getText().toString());
                    mqttPublishTo(topicOff,lampOffTime.getText().toString());
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
                        lampP1OffTime.setText(s_time);
                        mqttPublishTo(topic,s_time);
                    }
                }, currentHour, currentMinute, true);
                timePickerDialog.setTitle("Select OFF Time");
                timePickerDialog.show();
            }
        });

        Switch swLampProg2 = (Switch) findViewById(R.id.lampProg2SW);
        swLampProg2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic    = Constants.MQTT_LAMP_CTL_A1ACT;
                String topicOn  = Constants.MQTT_LAMP_CTL_A1ON;
                String topicOff = Constants.MQTT_LAMP_CTL_A1OFF;
                if (b) {
                    String msg = Constants.MQTT_ON;
                    EditText lampOnTime = (EditText) findViewById(R.id.lampPR2OnTime);
                    EditText lampOffTime = (EditText) findViewById(R.id.lampPR2OffTime);
                    mqttPublishTo(topic,msg);
                    mqttPublishTo(topicOn,lampOnTime.getText().toString());
                    mqttPublishTo(topicOff,lampOffTime.getText().toString());
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    mqttPublishTo(topic,msg);
                }
            }
        });

        final EditText  lampP2OnTime = findViewById(R.id.lampPR2OnTime);
        lampP2OnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String topic = Constants.MQTT_LAMP_CTL_A1ON;
                        String s_time = "0:00";
                        s_time = String.format("%02d:%02d", hourOfDay, minutes);
                        lampP2OnTime.setText(s_time);
                        mqttPublishTo(topic,s_time);
                    }
                }, currentHour, currentMinute, true);
                timePickerDialog.setTitle("Select ON Time");
                timePickerDialog.show();
            }

        });

        final EditText  lampP2OffTime = findViewById(R.id.lampPR2OffTime);
        lampP2OffTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String topic = Constants.MQTT_LAMP_CTL_A1OFF;
                        String s_time = "0:00";
                        s_time = String.format("%02d:%02d", hourOfDay, minutes);
                        lampP2OffTime.setText(s_time);
                        mqttPublishTo(topic,s_time);
                    }
                }, currentHour, currentMinute, true);
                timePickerDialog.setTitle("Select OFF Time");
                timePickerDialog.show();
            }
        });

        Switch swLampProg3 = (Switch) findViewById(R.id.lampProg3SW);
        swLampProg3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String topic    = Constants.MQTT_LAMP_CTL_A2ACT;
                String topicOn  = Constants.MQTT_LAMP_CTL_A2ON;
                String topicOff = Constants.MQTT_LAMP_CTL_A2OFF;
                if (b) {
                    String msg = Constants.MQTT_ON;
                    EditText lampOnTime = (EditText) findViewById(R.id.lampPR3OnTime);
                    EditText lampOffTime = (EditText) findViewById(R.id.lampPR3OffTime);
                    mqttPublishTo(topic,msg);
                    mqttPublishTo(topicOn,lampOnTime.getText().toString());
                    mqttPublishTo(topicOff,lampOffTime.getText().toString());
                }
                else {
                    String msg = Constants.MQTT_OFF;
                    mqttPublishTo(topic,msg);
                }
            }
        });

        final EditText  lampP3OnTime = findViewById(R.id.lampPR3OnTime);
        lampP3OnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String topic = Constants.MQTT_LAMP_CTL_A2ON;
                        String s_time = "0:00";
                        s_time = String.format("%02d:%02d", hourOfDay, minutes);
                        lampP3OnTime.setText(s_time);
                        mqttPublishTo(topic,s_time);
                    }
                }, currentHour, currentMinute, true);
                timePickerDialog.setTitle("Select ON Time");
                timePickerDialog.show();
            }

        });

        final EditText  lampP3OffTime = findViewById(R.id.lampPR3OffTime);
        lampP3OffTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String topic = Constants.MQTT_LAMP_CTL_A2OFF;
                        String s_time = "0:00";
                        s_time = String.format("%02d:%02d", hourOfDay, minutes);
                        lampP3OffTime.setText(s_time);
                        mqttPublishTo(topic,s_time);
                    }
                }, currentHour, currentMinute, true);
                timePickerDialog.setTitle("Select OFF Time");
                timePickerDialog.show();
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
                mqttSubscriteTo(Constants.MQTT_AQU_TEMP);
                mqttSubscriteTo(Constants.MQTT_AQU_FILT_STA);
                mqttSubscriteTo(Constants.MQTT_AQU_AERA_STA);
                mqttSubscriteTo(Constants.MQTT_AQU_HEAT_STA);
                mqttSubscriteTo(Constants.MQTT_AQU_TEMP_TMAX);
                mqttSubscriteTo(Constants.MQTT_AQU_TEMP_TMIN);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_AUTO);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A0ACT);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A0ON);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A0OFF);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A1ACT);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A1ON);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A1OFF);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A2ACT);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A2ON);
                mqttSubscriteTo(Constants.MQTT_LAMP_CTL_A2OFF);
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

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menuSetting:
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            String urlBroker = "";
            String username  = "";
            String password  = "";
            // Load settings
            SharedPreferences sharedpreferences = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
            if (sharedpreferences.contains(Constants.PREF_BROKER_URL)) {
                urlBroker = sharedpreferences.getString(Constants.PREF_BROKER_URL, "");
            }
            if (sharedpreferences.contains(Constants.PREF_BROKER_USER)) {
                username = sharedpreferences.getString(Constants.PREF_BROKER_USER, "");
            }
            if (sharedpreferences.contains(Constants.PREF_BROKER_PASS)) {
                password = sharedpreferences.getString(Constants.PREF_BROKER_PASS, "");
            }
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
                else if(topic.equals(Constants.MQTT_AQU_VERS)) {
                    TextView tvVersion = (TextView) findViewById(R.id.Version);
                    tvVersion.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_AQU_IP)) {
                    TextView tvIP = (TextView) findViewById(R.id.IP);
                    tvIP.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_AQU_RST)) {
                    TextView tvActive = (TextView) findViewById(R.id.timeActive);
                    String   time = getDate(message.toString());
                    tvActive.setText(time);
                }
                else if(topic.equals(Constants.MQTT_AQU_TIME)) {
                    TextView tvLast = (TextView) findViewById(R.id.lastMsg);
                    String   time = getDate(message.toString());
                    tvLast.setText(time);
                }
                else if(topic.equals(Constants.MQTT_AQU_FILT_STA)) {
                    Switch swFilter = (Switch) findViewById(R.id.filterSW);
                    aquaCtl(swFilter, message);
                }
                else if(topic.equals(Constants.MQTT_AQU_AERA_STA)) {
                    Switch swAerator = (Switch) findViewById(R.id.aeratorSW);
                    aquaCtl(swAerator, message);
                }
                else if(topic.equals(Constants.MQTT_AQU_HEAT_STA)) {
                    Switch swHeater = (Switch) findViewById(R.id.heaterSW);
                    aquaCtl(swHeater, message);
                }
                else if(topic.equals(Constants.MQTT_AQU_TEMP_TMAX)) {
                    EditText etTempMax = (EditText) findViewById(R.id.tempMax);
                    etTempMax.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_AQU_TEMP_TMIN)) {
                    EditText etTempMin = (EditText) findViewById(R.id.tempMin);
                    etTempMin.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_AQU_LAMP_STA)) {
                    Switch swLamp = (Switch) findViewById(R.id.lampSW);
                    aquaCtl(swLamp, message);
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_AUTO)) {
                    Switch swLampAuto = (Switch) findViewById(R.id.lampAutoSW);
                    aquaCtl(swLampAuto, message);
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A0ACT)) {
                    Switch swLampP1 = (Switch) findViewById(R.id.lampProg1SW);
                    aquaCtl(swLampP1, message);
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A0ON)) {
                    EditText etP1On = (EditText) findViewById(R.id.lampPR1OnTime);
                    etP1On.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A0OFF)) {
                    EditText etP1Off = (EditText) findViewById(R.id.lampPR1OffTime);
                    etP1Off.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A1ACT)) {
                    Switch swLampP2 = (Switch) findViewById(R.id.lampProg2SW);
                    aquaCtl(swLampP2, message);
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A1ON)) {
                    EditText etP2On = (EditText) findViewById(R.id.lampPR2OnTime);
                    etP2On.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A1OFF)) {
                    EditText etP2Off = (EditText) findViewById(R.id.lampPR2OffTime);
                    etP2Off.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A2ACT)) {
                    Switch swLampP3 = (Switch) findViewById(R.id.lampProg3SW);
                    aquaCtl(swLampP3, message);
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A2ON)) {
                    EditText etP3On = (EditText) findViewById(R.id.lampPR3OnTime);
                    etP3On.setText(message.toString());
                }
                else if(topic.equals(Constants.MQTT_LAMP_CTL_A2OFF)) {
                    EditText etP3Off = (EditText) findViewById(R.id.lampPR3OffTime);
                    etP3Off.setText(message.toString());
                }
                else {

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void aquaCtl(Switch sw,MqttMessage message ) {
        if (message.toString().matches(Constants.MQTT_ON)) {
            sw.setChecked(true);
        }
        else {
            sw.setChecked(false);
        }
    }

    private String getDate(String ourDate)
    {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(ourDate);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); //this format changeable
            dateFormatter.setTimeZone(TimeZone.getDefault());
            ourDate = dateFormatter.format(value);
        }
        catch (Exception e)
        {
            ourDate = "00-00-0000 00:00";
        }
        return ourDate;
    }
}
