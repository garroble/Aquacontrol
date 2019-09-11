#include "Aquacontrol.h"
#include <OneWire.h>            // https://www.pjrc.com/teensy/td_libs_OneWire.html
#include <DallasTemperature.h>  // https://github.com/milesburton/Arduino-Temperature-Control-Library
#include <DS3231M.h>            // https://github.com/SV-Zanshin/DS3231M
#include <ESP8266WiFi.h>        // https://arduino-esp8266.readthedocs.io/en/latest/
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
#include <NTPClient.h>          // https://github.com/taranais/NTPClient
#include <PubSubClient.h>       // https://pubsubclient.knolleary.net/

OneWire             oneWire(SENS_THERM);    // Instance OneWire bus for Temp Sensor
DallasTemperature   TempSensor(&oneWire);   // Instance Temp Sensor
DS3231M_Class       RTClock;                // Instance DS3231M RTC Clock
WiFiClient          espClient;
WiFiUDP             ntpUDP;                 // UDP Link for NTP
NTPClient           timeClient(ntpUDP, "pool.ntp.org", GMTp1_OFF);
PubSubClient        mqtt_client(espClient);
AquariumData        Aquarium    = {false, false, false, false, 25};
AquariumControl     AquControl  = {false, false, false, false, false, false};
long                l_startCtlTime  = 0;
long                l_elapsCtlTime  = 0;

/* FUNCTIONS DECLARATION   */
// Relay_Default
// Set Variables and Relays to default output, NC for filter/aerator/heater and NO for lamp.
void Aquarium_Default() {
  pinMode(RELAY_LAMP, OUTPUT);
  pinMode(RELAY_AERA, OUTPUT);
  pinMode(RELAY_HEAT, OUTPUT);
  pinMode(RELAY_FILT, OUTPUT);
  digitalWrite(RELAY_LAMP, LOW);
  digitalWrite(RELAY_AERA, LOW);
  digitalWrite(RELAY_HEAT, LOW);
  digitalWrite(RELAY_FILT, LOW);
  Aquarium.b_Lamp     = false;
  Aquarium.b_Aerator  = true;
  Aquarium.b_Heater   = true;
  Aquarium.b_Filter   = true;
  TempSensor.requestTemperatures();
  Aquarium.f_Temperature = TempSensor.getTempCByIndex(0);
}

// WiFi_Setup
// Initiates WiFi Connectivity to predefined SSID
void WiFi_Setup() {
  delay(10);
  Serial.println();
  Serial.println("INITIALIZING WIFI...................");
  Serial.print("Connecting to: ");
  Serial.println(wifi_ssid);

  WiFi.begin(wifi_ssid, wifi_pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  ArduinoOTA.onStart([]() {
    String type;
    if (ArduinoOTA.getCommand() == U_FLASH) {
      type = "sketch";
    } else { // U_SPIFFS
      type = "filesystem";
    }

    // NOTE: if updating SPIFFS this would be the place to unmount SPIFFS using SPIFFS.end()
    Serial.println("Start updating " + type);
  });
  ArduinoOTA.onEnd([]() {
    Serial.println("\nEnd");
  });
  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
    Serial.printf("Progress: %u%%\r", (progress / (total / 100)));
  });
  ArduinoOTA.onError([](ota_error_t error) {
    Serial.printf("Error[%u]: ", error);
    if (error == OTA_AUTH_ERROR) {
      Serial.println("Auth Failed");
    } else if (error == OTA_BEGIN_ERROR) {
      Serial.println("Begin Failed");
    } else if (error == OTA_CONNECT_ERROR) {
      Serial.println("Connect Failed");
    } else if (error == OTA_RECEIVE_ERROR) {
      Serial.println("Receive Failed");
    } else if (error == OTA_END_ERROR) {
      Serial.println("End Failed");
    }
  });
  ArduinoOTA.begin();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

// MQTT_callback
// Handles incoming messages for the topics subscribed
void mqtt_callback(char* topic, byte* payload, unsigned int length) {
  Serial.println("-----------------------");
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);
 
  Serial.print("Message:");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
 
  Serial.println();
  Serial.println("-----------------------");

  if (strcmp(topic, MQTT_CTL_LAMP) == 0) {
    if (payload[1] == n_BYTE) { lampSet(true); }  // on
    else                      { lampSet(false); } // off     
  }
  else if (strcmp(topic, MQTT_CTL_LAMP_AUTO) == 0) {
    if (payload[1] == n_BYTE) { lampAutoSet(true); }  // on
    else                      { lampAutoSet(false); } // off      
  }
  else if (strcmp(topic, MQTT_CTL_AERA) == 0) {
    
  }
  else if (strcmp(topic, MQTT_CTL_HEAT) == 0) {
    if (payload[1] == n_BYTE) { heaterSet(true); }  // on
    else                      { heaterSet(false); } // off    
  }
  else if (strcmp(topic, MQTT_CTL_FILT) == 0) {
    if (payload[1] == n_BYTE) { filterSet(true); }  // on
    else                      { filterSet(false); } // off
  }
  else {
    
  }
}

// MQTT_Setup
// Initiates MQTT Connectivity to MQTT Broker
void MQTT_Setup() {
  Serial.println();
  Serial.println("INITIALIZING MQTT...................");
  mqtt_client.setServer(mqtt_server, mqtt_port);
  mqtt_client.setCallback(mqtt_callback);

  while (!mqtt_client.connected()) {
    Serial.println("Connecting to MQTT...");
 
    if (mqtt_client.connect("MQTT_Client", mqtt_user, mqtt_pass )) {
      Serial.println("connected");
    } else {
      Serial.print("failed with state ");
      Serial.print(mqtt_client.state());
      delay(2000);
    }
  }
  mqtt_client.subscribe(MQTT_CTL_LAMP);
  mqtt_client.subscribe(MQTT_CTL_LAMP_AUTO);
  mqtt_client.subscribe(MQTT_CTL_AERA);
  mqtt_client.subscribe(MQTT_CTL_AERA_AUTO);
  mqtt_client.subscribe(MQTT_CTL_HEAT);
  mqtt_client.subscribe(MQTT_CTL_FILT);
}

void setResetData() {
  String  timeString;
  String  tempString;
  int     i_datetime = 0;
  // Send reset time
  timeClient.update();
  timeString = timeClient.getFormattedDate();
  i_datetime = timeString.indexOf("T");
  Serial.print("DATE: ");
  Serial.print(timeString.substring(0,i_datetime));
  Serial.print(" | TIME (GMT+1): ");
  Serial.println(timeString.substring(i_datetime+1, timeString.length()-1));
  mqtt_client.publish(MQTT_AQU_RST, timeString.c_str());
  if (Aquarium.b_Lamp) { mqtt_client.publish(MQTT_AQU_LAMP, "on"); }
  else                 { mqtt_client.publish(MQTT_AQU_LAMP, "off"); }
  if (Aquarium.b_Aerator) { mqtt_client.publish(MQTT_AQU_AERA, "on"); }
  else                    { mqtt_client.publish(MQTT_AQU_AERA, "off"); }
  if (Aquarium.b_Heater) { mqtt_client.publish(MQTT_AQU_HEAT, "on"); }
  else                   { mqtt_client.publish(MQTT_AQU_HEAT, "off"); }
  if (Aquarium.b_Filter) { mqtt_client.publish(MQTT_AQU_FILT, "on"); }
  else                   { mqtt_client.publish(MQTT_AQU_FILT, "off"); }
  tempString = Aquarium.f_Temperature;
  mqtt_client.publish(MQTT_AQU_TEMP, tempString.c_str());
}

// pubTimestamp
// Publish Aquarium Timestamp for last message
void pubTimestamp() {
  String  timeString;  
  timeClient.update();
  timeString = timeClient.getFormattedDate();
  mqtt_client.publish(MQTT_AQU_TIME, timeString.c_str());
}

// pubTemperature
// Publish Aquarium Temperature
void pubTemperature() {
  String  tempString;
  Aquarium.f_Temperature = TempSensor.getTempCByIndex(0);
  tempString = Aquarium.f_Temperature;
  mqtt_client.publish(MQTT_AQU_TEMP, tempString.c_str());
  pubTimestamp();
}

// filterSet
// Set Filter on/off
void filterSet(bool b_turn) {
  Aquarium.b_Filter   = b_turn;
  AquControl.b_Filter = b_turn;
  if (b_turn) {
    Serial.println("Filter set to ON");
    digitalWrite(RELAY_FILT, LOW);
    mqtt_client.publish(MQTT_AQU_FILT, "on");
  }
  else {
    Serial.println("Filter set to OFF");
    digitalWrite(RELAY_FILT, HIGH);
    mqtt_client.publish(MQTT_AQU_FILT, "off");
  }
  pubTimestamp();
}

// heaterSet
// Set Heater on/off
void heaterSet(bool b_turn) {
  Aquarium.b_Heater    = b_turn;
  AquControl.b_Heater  = b_turn;
  if (b_turn) {
    Serial.println("Heater set to ON");
    digitalWrite(RELAY_HEAT, LOW);
    mqtt_client.publish(MQTT_AQU_HEAT, "on");
  }
  else {
    Serial.println("Heater set to OFF");
    digitalWrite(RELAY_HEAT, HIGH);
    mqtt_client.publish(MQTT_AQU_HEAT, "off");
  }
  pubTimestamp();
}

// lampSet
// Set Lamp in automatic mode or manually on/off
void lampSet(bool b_turn) {
  AquControl.b_Lamp = b_turn;
  if (AquControl.b_LampAuto == false) {
    Aquarium.b_Lamp = b_turn;
    if (b_turn) {
      Serial.println("Lamp set to ON");
      digitalWrite(RELAY_LAMP, LOW);
      mqtt_client.publish(MQTT_AQU_LAMP, "on");
    }
    else {
      Serial.println("Lamp set to OFF");
      digitalWrite(RELAY_LAMP, HIGH);
      mqtt_client.publish(MQTT_AQU_LAMP, "off");
    }
  }
  pubTimestamp();
}

// lampAutoSet
// Set Lamp automatic mode on/off
void lampAutoSet(bool b_turn) {
  AquControl.b_LampAuto = b_turn;
  if (b_turn) {
    Serial.println("Lamp Automatic control set to ON");
  }
  else {
    Serial.println("Lamp Automatic control set to OFF");
  }
  pubTimestamp();  
}

/*  ARDUINO SETUP   */
void setup() {
  // put your setup code here, to run once:
  Serial.begin(SERIAL_SPEED);
  TempSensor.begin();
  Aquarium_Default();
  RTClock.begin();
  WiFi_Setup();
  timeClient.begin();
  MQTT_Setup();
  setResetData();

  l_startCtlTime = millis();
}

/*  ARDUINO LOOP   */
void loop() {
  // put your main code here, to run repeatedly:
  ArduinoOTA.handle();
  mqtt_client.loop();

  l_elapsCtlTime  = millis() - l_startCtlTime;
  if (l_elapsCtlTime > (CTL_TIME*1000)) {
    Serial.println(" ---> Control loop <--- ");
    pubTemperature();
    l_startCtlTime = millis();
  }
}
