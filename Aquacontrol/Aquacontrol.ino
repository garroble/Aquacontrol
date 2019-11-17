#include "Aquacontrol.h"
#include <OneWire.h>            // https://www.pjrc.com/teensy/td_libs_OneWire.html
#include <DallasTemperature.h>  // https://github.com/milesburton/Arduino-Temperature-Control-Library
#include <DS3231M.h>            // https://github.com/SV-Zanshin/DS3231M
#include <ESP8266WiFi.h>        // https://arduino-esp8266.readthedocs.io/en/latest/
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
#include <DNSServer.h>
#include <ESP8266WebServer.h>
#include <WiFiManager.h>        // https://github.com/kentaylor/WiFiManager
#include <NTPClient.h>          // https://github.com/taranais/NTPClient
#include <PubSubClient.h>       // https://pubsubclient.knolleary.net/

OneWire             oneWire(SENS_THERM);    // Instance OneWire bus for Temp Sensor
DallasTemperature   TempSensor(&oneWire);   // Instance Temp Sensor
DS3231M_Class       RTClock;                // Instance DS3231M RTC Clock
WiFiClient          espClient;
WiFiUDP             ntpUDP;                 // UDP Link for NTP
WiFiManager         wifiManager;            // WiFi Manager
NTPClient           timeClient(ntpUDP, "pool.ntp.org", GMTp0_OFFSET);
PubSubClient        mqtt_client(espClient);
AquariumData        Aquarium;
wl_status_t         wifi_status = WL_DISCONNECTED;
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
  digitalWrite(RELAY_LAMP, LAMP_OFF);
  digitalWrite(RELAY_AERA, AERA_ON);
  digitalWrite(RELAY_HEAT, HEAT_ON);
  digitalWrite(RELAY_FILT, FILT_ON);
  Aquarium.Lamp.b_Status                = false;
  Aquarium.Lamp.b_AutoMode              = false;
  Aquarium.Lamp.b_Control               = false;
  for (int i_Cont = 0; i_Cont < MAX_AUTO_PROG; i_Cont++) {
    Aquarium.Lamp.Program[i_Cont].b_Active   = false;
    Aquarium.Lamp.Program[i_Cont].i_ONtime   = 0;
    Aquarium.Lamp.Program[i_Cont].i_OFFtime  = 0;    
  }
  Aquarium.Aerator.b_Status                = true;
  Aquarium.Aerator.b_AutoMode              = false;
  Aquarium.Aerator.b_Control               = true;
  for (int i_Cont = 0; i_Cont < MAX_AUTO_PROG; i_Cont++) {
    Aquarium.Aerator.Program[i_Cont].b_Active   = false;
    Aquarium.Aerator.Program[i_Cont].i_ONtime   = 0;
    Aquarium.Aerator.Program[i_Cont].i_OFFtime  = 0;    
  }
  Aquarium.Heater.b_Status                = true;
  Aquarium.Heater.b_AutoMode              = false;
  Aquarium.Heater.b_Control               = true;
  for (int i_Cont = 0; i_Cont < MAX_AUTO_PROG; i_Cont++) {
    Aquarium.Heater.Program[i_Cont].b_Active   = false;
    Aquarium.Heater.Program[i_Cont].i_ONtime   = 0;
    Aquarium.Heater.Program[i_Cont].i_OFFtime  = 0;    
  }
  Aquarium.Filter.b_Status                = true;
  Aquarium.Filter.b_AutoMode              = false;
  Aquarium.Filter.b_Control               = true;
  for (int i_Cont = 0; i_Cont < MAX_AUTO_PROG; i_Cont++) {
    Aquarium.Filter.Program[i_Cont].b_Active   = false;
    Aquarium.Filter.Program[i_Cont].i_ONtime   = 0;
    Aquarium.Filter.Program[i_Cont].i_OFFtime  = 0;    
  }
  Aquarium.b_RTCavailable = false;
  TempSensor.requestTemperatures();
  Aquarium.Temperature.f_Current = TempSensor.getTempCByIndex(0);
  Aquarium.Temperature.f_Max = TEMP_MAX;
  Aquarium.Temperature.f_Min = TEMP_MIN;
}

/*****************************************/
/*             SETUP SERVICES            */
/*****************************************/
// WiFiConfig_Setup
// Initiates WiFi Configuration Webpage on ESP AP
wl_status_t WiFiConfig_Setup() {
  wl_status_t wifi_status = WL_DISCONNECTED;
  
  wifiManager.setConfigPortalTimeout(WIFI_CONF_TIMEOUT);
  wifiManager.setDebugOutput(false);
  Serial.println(F("INITIALIZING WIFI Manager..........."));
  Serial.println("Launching Aquacontrol AP to set/change WiFi SSID/PASS");
  Serial.print("If not used after ");
  Serial.print(WIFI_CONF_TIMEOUT);
  Serial.println(" seconds, then use stored data");
  if (!wifiManager.startConfigPortal(WIFI_CONF_AP_SSID,WIFI_CONF_AP_PASS)) {
     Serial.println("Not connected to WiFi, try with predefined values.");
  }

  WiFi.waitForConnectResult();
  wifi_status = WiFi.status();
  if (wifi_status == WL_CONNECTED) {
    Serial.println("");
    Serial.println(F("WiFi connected"));
    Serial.println(F("IP address: "));
    Serial.println(WiFi.localIP());
  }
  wifiManager.~WiFiManager();
  return wifi_status;
}

// WiFi_Setup
// Initiates WiFi Connectivity to predefined SSID
wl_status_t WiFi_Setup() {
  int           i_Cont = 0;
  wl_status_t   wl_Status = WL_DISCONNECTED;
  delay(10);
  Serial.println();
  Serial.println(F("INITIALIZING WIFI..................."));
  Serial.print(F("Connecting to: "));
  Serial.println(wifi_ssid);

  WiFi.begin(wifi_ssid, wifi_pass);

  while ((WiFi.status() != WL_CONNECTED) && (i_Cont < WIFI_CONN_TIMEOUT)) {
    delay(500);
    Serial.print(".");
    i_Cont++;
  }

  wl_Status = WiFi.status();

  randomSeed(micros());

  Serial.println("");
  if (wl_Status == WL_CONNECTED) {
    Serial.println(F("WiFi connected"));
    Serial.print(F("IP address: "));
    Serial.println(WiFi.localIP());
  }
  else {
    Serial.println(F("WiFi NOT connected"));
  }
  return wl_Status;
}

// OTA_Setup
// Initiates OTA Service
void OTA_Setup() {
  // Setup OTA service
  ArduinoOTA.onStart([]() {
    String type;
    if (ArduinoOTA.getCommand() == U_FLASH) {
      type = "sketch";
    } else { // U_SPIFFS
      type = "filesystem";
    }

    // NOTE: if updating SPIFFS this would be the place to unmount SPIFFS using SPIFFS.end()
    Serial.println();
    Serial.println(F("INITIALIZING OTA ..................."));
    Serial.println("Start updating " + type);
  });
  
  ArduinoOTA.onEnd([]() {
    Serial.println("\nOTA finished");
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
}

// MQTT_callback
// Handles incoming messages for the topics subscribed
void mqtt_callback(char* topic, byte* payload, unsigned int length) {
  Serial.println(F("-----------------------"));
  Serial.print(F("Message arrived in topic: "));
  Serial.println(topic);
 
  Serial.print(F("Message:"));
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
 
  Serial.println();
  Serial.println(F("-----------------------"));

  if (strcmp(topic, MQTT_LAMP_CTL) == 0) {
    if (payload[1] == n_BYTE) { lampSetManual(true); }  // on
    else                      { lampSetManual(false); } // off     
  }
  else if (strcmp(topic, MQTT_LAMP_CTL_AUTO) == 0) {
    if (payload[1] == n_BYTE) { lampSetAutomode(true); }  // on
    else                      { lampSetAutomode(false); } // off      
  }
  else if (strcmp(topic, MQTT_AERA_CTL) == 0) {
    if (payload[1] == n_BYTE) { aeraSetManual(true); }  // on
    else                      { aeraSetManual(false); } // off     
  }
  else if (strcmp(topic, MQTT_AERA_CTL_AUTO) == 0) {
    if (payload[1] == n_BYTE) { aeraSetAutomode(true); }  // on
    else                      { aeraSetAutomode(false); } // off      
  }
  else if (strcmp(topic, MQTT_HEAT_CTL) == 0) {
    if (payload[1] == n_BYTE) { heaterSetManual(true); }  // on
    else                      { heaterSetManual(false); } // off    
  }
  else if (strcmp(topic, MQTT_FILT_CTL) == 0) {
    if (payload[1] == n_BYTE) { filterSetManual(true); }  // on
    else                      { filterSetManual(false); } // off
  }
  else if (strcmp(topic, MQTT_LAMP_CTL_A0ACT) == 0) {
    if (payload[1] == n_BYTE) { lampSetActiveProg(0,true); }  // on
    else                      { lampSetActiveProg(0,false); } // off
  }   
  else if (strcmp(topic, MQTT_LAMP_CTL_A0ON) == 0) {
    lampSetAutoProg(0,LAMP_ON,payload);
  } 
  else if (strcmp(topic, MQTT_LAMP_CTL_A0OFF) == 0) {
    lampSetAutoProg(0,LAMP_OFF,payload);
  }
  else if (strcmp(topic, MQTT_LAMP_CTL_A1ACT) == 0) {
    if (payload[1] == n_BYTE) { lampSetActiveProg(1,true); }  // on
    else                      { lampSetActiveProg(1,false); } // off
  } 
  else if (strcmp(topic, MQTT_LAMP_CTL_A1ON) == 0) {
    lampSetAutoProg(1,LAMP_ON,payload);
  } 
  else if (strcmp(topic, MQTT_LAMP_CTL_A1OFF) == 0) {
    lampSetAutoProg(1,LAMP_OFF,payload);
  }
  else if (strcmp(topic, MQTT_LAMP_CTL_A2ACT) == 0) {
    if (payload[1] == n_BYTE) { lampSetActiveProg(2,true); }  // on
    else                      { lampSetActiveProg(2,false); } // off
  } 
  else if (strcmp(topic, MQTT_LAMP_CTL_A2ON) == 0) {
    lampSetAutoProg(2,LAMP_ON,payload);
  } 
  else if (strcmp(topic, MQTT_LAMP_CTL_A2OFF) == 0) {
    lampSetAutoProg(2,LAMP_OFF,payload);
  }
  else if (strcmp(topic, MQTT_AQU_TEMP_TMAX) == 0) {
    tempSet(true, payload);
  }
  else if (strcmp(topic, MQTT_AQU_TEMP_TMIN) == 0) {
    tempSet(false, payload);
  }
  else {
    Serial.println("Unknonw message");
  }
}

// MQTT_Setup
// Initiates MQTT Connectivity to MQTT Broker
void MQTT_Setup() {
  Serial.println();
  Serial.println(F("INITIALIZING MQTT..................."));
  mqtt_client.setServer(mqtt_server, mqtt_port);
  mqtt_client.setCallback(mqtt_callback);

  while (!mqtt_client.connected()) {
    Serial.println(F("Connecting to MQTT..."));
 
    if (mqtt_client.connect("MQTT_Client", mqtt_user, mqtt_pass )) {
      Serial.println("connected");
    } else {
      Serial.print("failed with state ");
      Serial.println(mqtt_client.state());
      delay(2000);
    }
  }
  mqtt_client.publish(MQTT_AQU_IP,WiFi.localIP().toString().c_str(),true);
  
  mqtt_client.subscribe(MQTT_LAMP_CTL);
  mqtt_client.subscribe(MQTT_LAMP_CTL_AUTO);
  mqtt_client.subscribe(MQTT_LAMP_CTL_A0ACT);
  mqtt_client.subscribe(MQTT_LAMP_CTL_A0ON);
  mqtt_client.subscribe(MQTT_LAMP_CTL_A0OFF);
  mqtt_client.subscribe(MQTT_LAMP_CTL_A1ACT);
  mqtt_client.subscribe(MQTT_LAMP_CTL_A1ON);
  mqtt_client.subscribe(MQTT_LAMP_CTL_A1OFF);
  mqtt_client.subscribe(MQTT_LAMP_CTL_A2ACT);
  mqtt_client.subscribe(MQTT_LAMP_CTL_A2ON);
  mqtt_client.subscribe(MQTT_LAMP_CTL_A2OFF);
  
  mqtt_client.subscribe(MQTT_AERA_CTL);
  mqtt_client.subscribe(MQTT_AERA_CTL_AUTO);
  
  mqtt_client.subscribe(MQTT_HEAT_CTL);
  mqtt_client.subscribe(MQTT_FILT_CTL);

  mqtt_client.subscribe(MQTT_AQU_TEMP_TMAX);
  mqtt_client.subscribe(MQTT_AQU_TEMP_TMIN);
}

// RTC_Setup
// Initiates RTC clock
void RTC_Setup() {
  char      inputBuffer[32];
  String    timeString;
  Serial.println();
  Serial.println(F("INITIALIZING RTC..................."));
  if (RTClock.begin(I2C_SDA, I2C_SCL)) {
    Serial.println();
    Serial.print("RTC Initialized!: ");
    if (wifi_status == WL_CONNECTED) {
      // Set RTC Time based on NTP server
      timeClient.update();
      timeString = timeClient.getFormattedDate();
      DateTime  RTCtime(timeClient.getEpochTime());
      RTClock.adjust(RTCtime);
    }
    DateTime now = RTClock.now();
    sprintf(inputBuffer,"%04d-%02d-%02d %02d:%02d:%02d", now.year(),
              now.month(), now.day(), now.hour(), now.minute(), now.second());
    Serial.println(inputBuffer);
    Aquarium.b_RTCavailable = true;
  }
  else {
    Serial.println("Failed to init RTC");
  }
}

/*****************************************/
/*      PUBLISH DATA ON MQTT BROKER      */
/*****************************************/
// pubResetData
// Publish the initialization status of the Aquarium to a safe configuration
void pubResetData() {
  String  timeString;
  String  tempString;
  int     i_datetime = 0;
  // Send reset time
  timeClient.update();
  timeString = timeClient.getFormattedDate();
  i_datetime = timeString.indexOf("T");
  Serial.print("DATE: ");
  Serial.print(timeString.substring(0,i_datetime));
  Serial.print(" | TIME (UTC): ");
  Serial.println(timeString.substring(i_datetime+1, timeString.length()-1));
  mqtt_client.publish(MQTT_AQU_RST, timeString.c_str(), true);
  if (Aquarium.Lamp.b_Status)    { mqtt_client.publish(MQTT_AQU_LAMP_STA, "on", true); }
  else                           { mqtt_client.publish(MQTT_AQU_LAMP_STA, "off", true); }
  if (Aquarium.Aerator.b_Status) { mqtt_client.publish(MQTT_AQU_AERA_STA, "on", true); }
  else                           { mqtt_client.publish(MQTT_AQU_AERA_STA, "off", true); }
  if (Aquarium.Heater.b_Status)  { mqtt_client.publish(MQTT_AQU_HEAT_STA, "on", true); }
  else                           { mqtt_client.publish(MQTT_AQU_HEAT_STA, "off", true); }
  if (Aquarium.Filter.b_Status)  { mqtt_client.publish(MQTT_AQU_FILT_STA, "on", true); }
  else                           { mqtt_client.publish(MQTT_AQU_FILT_STA, "off", true); }
  tempString = Aquarium.Temperature.f_Current;
  mqtt_client.publish(MQTT_AQU_TEMP, tempString.c_str(), true);
  mqtt_client.publish(MQTT_AQU_VERS, VERSION, true);
}

// pubTimestamp
// Publish Aquarium Timestamp for last message
void pubTimestamp() {
  String  timeString;
  char    timeBuf[20];
 
  if(timeClient.update()) {
    timeString = timeClient.getFormattedDate();
  }
  else {
    DateTime now = RTClock.now();
    sprintf(timeBuf, "%02d-%02d-%02dT%02d:%02d:%02d", now.year(), now.month(), now.day(),
      now.hour(), now.minute(), now.second());
    timeString = timeBuf;
  }
  mqtt_client.publish(MQTT_AQU_TIME, timeString.c_str(), true);
}

// pubTemperature
// Publish Aquarium Temperature
void pubTemperature() {
  String  tempString;
  TempSensor.requestTemperatures();
  Aquarium.Temperature.f_Current = TempSensor.getTempCByIndex(0);
  tempString = Aquarium.Temperature.f_Current;
  mqtt_client.publish(MQTT_AQU_TEMP, tempString.c_str(), true);
  pubTimestamp();
  Serial.print("----> Aquarium Temperature: ");
  Serial.println(Aquarium.Temperature.f_Current);
}

/********************/
/*      FILTER      */
/********************/
// filterSet
// Set Filter on/off
void filterSet(bool b_turn) {
  if (Aquarium.Filter.b_Status != b_turn) {
    Aquarium.Filter.b_Status = b_turn;
    if (b_turn) {
      Serial.println(F("Filter set to ON"));
      digitalWrite(RELAY_FILT, FILT_ON);
      mqtt_client.publish(MQTT_AQU_FILT_STA, "on", true);
    }
    else {
      Serial.println(F("Filter set to OFF"));
      digitalWrite(RELAY_FILT, FILT_OFF);
      mqtt_client.publish(MQTT_AQU_FILT_STA, "off", true);
    }
    pubTimestamp();
  }
}

void filterSetManual(bool b_turn) {
  if (Aquarium.Filter.b_AutoMode == false) {
    Aquarium.Filter.b_Control = b_turn;
    filterSet(b_turn);
  }
}

/********************/
/*      HEATER      */
/********************/
// heaterSet
// Set Heater on/off
void heaterSet(bool b_turn) {
  if (Aquarium.Heater.b_Status != b_turn) {
    Aquarium.Heater.b_Status = b_turn;
    if (b_turn) {
      Serial.println(F("Heater set to ON"));
      digitalWrite(RELAY_HEAT, HEAT_ON);
      mqtt_client.publish(MQTT_AQU_HEAT_STA, "on", true);
    }
    else {
      Serial.println(F("Heater set to OFF"));
      digitalWrite(RELAY_HEAT, HEAT_OFF);
      mqtt_client.publish(MQTT_AQU_HEAT_STA, "off", true);
    }
    pubTimestamp();
  }
}

void heaterSetManual(bool b_turn) {
  if (Aquarium.Heater.b_AutoMode == false) {
    Aquarium.Heater.b_Control = b_turn;
    heaterSet(b_turn);
  }
}

// heatLoopCtl
// Control heater if temperature exceeds maximum or minimum temperature.
void heatLoopCtl() {
  if (Aquarium.Temperature.f_Current == DEVICE_DISCONNECTED_C) {
    Serial.println(" ---> ALARM: No thermistor connected");
  }
  else if (Aquarium.Temperature.f_Current > Aquarium.Temperature.f_Max) {
    Aquarium.Heater.b_Control = false;
    heaterSet(false);
    Serial.println(" ---> ALARM: Over temperature");
  }
  else if (Aquarium.Temperature.f_Current < Aquarium.Temperature.f_Min) {
    Aquarium.Heater.b_Control = true;
    heaterSet(true);
    Serial.println(" ---> ALARM: Under temperature");
  } else {
    Aquarium.Heater.b_Control = true;
    heaterSet(true);
  }
}

/********************/
/*     AERATOR      */
/********************/
// aeraSet
// Set Aerator on/off
void aeraSet(bool b_turn) {
  if (Aquarium.Aerator.b_Status != b_turn) {
    Aquarium.Aerator.b_Status = b_turn;
    if (b_turn) {
      Serial.println(F("Aerator set to ON"));
      digitalWrite(RELAY_AERA, AERA_ON);
      mqtt_client.publish(MQTT_AQU_AERA_STA, "on", true);
    }
    else {
      Serial.println(F("Aerator set to OFF"));
      digitalWrite(RELAY_AERA, AERA_OFF);
      mqtt_client.publish(MQTT_AQU_AERA_STA, "off", true);
    }
    pubTimestamp();
  }
}

// aeraSetManual
// Set Aerator manually on/off
void aeraSetManual(bool b_turn) {
  Aquarium.Aerator.b_Control = b_turn;
  if (Aquarium.Aerator.b_AutoMode == false) {
    aeraSet(b_turn);
  }
}

// aeraSetAutomode
// Set Aerator automatic mode on/off
void aeraSetAutomode(bool b_turn) {
  Aquarium.Aerator.b_AutoMode = b_turn;
  if (b_turn) {
    Serial.println(F("Aerator Automatic control set to ON"));
  }
  else {
    Serial.println(F("Aerator Automatic control set to OFF"));
  }
  pubTimestamp();  
}

// aeraSetAutoProg
// Set Aerator Automatic Programs data
void aeraSetAutoProg(byte b_Prog, bool b_ProgOn,bool b_OnOff, byte* message) {
  // Expect 09:25 or 21:25
  int   i_hour   = 0;
  int   i_minute = 0;
  char  c_time[5];
  i_hour    = ((int)message[0] - '0')*10 + ((int)message[1] - '0');
  i_minute  = ((int)message[3] - '0')*10 + ((int)message[4] - '0');
  Serial.print("Set Auto Aerator ");
  Serial.print(b_Prog);
  //Aquarium.Aerator.Program[b_Prog].b_Active = b_ProgOn;
  if (b_ProgOn) {
    Serial.print(" program to ");
    if (b_OnOff) {
      Serial.print("turn ON the aerator at ");
      Aquarium.Aerator.Program[b_Prog].i_ONtime = i_hour * 60 + i_minute;
    }
    else {
      Serial.print("turn OFF the aerator at ");
      Aquarium.Aerator.Program[b_Prog].i_OFFtime = i_hour * 60 + i_minute;
    }
    sprintf(c_time, "%02d:%02d", i_hour, i_minute);
    Serial.println(c_time);
  }
}

// aeraLoopCtl
// When set to auto, control the lamps based on ON/OFF hours.
void aeraLoopCtl() {
  int       i_Now = 0;
  
  if (Aquarium.Aerator.b_AutoMode == true) {
    if (wifi_status == WL_CONNECTED) {  
      timeClient.update();
      i_Now = timeClient.getHours()*60 + timeClient.getMinutes();
    }
    else {
      DateTime now = RTClock.now();
      i_Now = now.hour() * 60 + now.minute();
    }
    for (int i_Cont = 0; i_Cont < MAX_AUTO_PROG; i_Cont++) {
      if (Aquarium.Aerator.Program[i_Cont].b_Active) {
        if (Aquarium.Aerator.Program[i_Cont].i_OFFtime > Aquarium.Aerator.Program[i_Cont].i_ONtime) {
          if( (i_Now >= Aquarium.Aerator.Program[i_Cont].i_ONtime) &&
              (i_Now <  Aquarium.Aerator.Program[i_Cont].i_OFFtime) ) {
            aeraSet(true);
          }
          else {
            aeraSet(false);
          }
        }
        else {
          if( (i_Now >= Aquarium.Aerator.Program[i_Cont].i_ONtime) ||
              (i_Now <  Aquarium.Aerator.Program[i_Cont].i_OFFtime) ) {
            aeraSet(true);
          }
          else {
            aeraSet(false);
          }
        }
      }
    }
  }
}

/********************/
/*       LAMP       */
/********************/
// lampSet
// Set Lamp on/off
void lampSet(bool b_turn) {
  if (Aquarium.Lamp.b_Status != b_turn) {
    Aquarium.Lamp.b_Status = b_turn;
    if (b_turn) {
      Serial.println(F("Lamp set to ON"));
      digitalWrite(RELAY_LAMP, LAMP_ON);
      mqtt_client.publish(MQTT_AQU_LAMP_STA, "on", true);
    }
    else {
      Serial.println(F("Lamp set to OFF"));
      digitalWrite(RELAY_LAMP, LAMP_OFF);
      mqtt_client.publish(MQTT_AQU_LAMP_STA, "off", true);
    }
    pubTimestamp();
  }
}

// lampSetManual
// Set Lamp manually on/off
void lampSetManual(bool b_turn) {
  Aquarium.Lamp.b_Control = b_turn;
  if (Aquarium.Lamp.b_AutoMode == false) {
    lampSet(b_turn);
  }
}

// lampSetAutomode
// Set Lamp automatic mode on/off
void lampSetAutomode(bool b_turn) {
  Aquarium.Lamp.b_AutoMode = b_turn;
  if (b_turn) {
    Serial.println(F("Lamp Automatic control set to ON"));
  }
  else {
    Serial.println(F("Lamp Automatic control set to OFF"));
  }
  pubTimestamp();  
}

// lampSetActiveProg
// Set Lamp automatic mode on/off
void lampSetActiveProg(byte b_Prog, bool b_ProgOn) {
  Aquarium.Lamp.Program[b_Prog].b_Active = b_ProgOn;
  Serial.print(F("Lamp Auto Program "));
  Serial.print(b_Prog);
  if (b_ProgOn) {
    Serial.println(F(" set to ACTIVE"));
  }
  else {
    Serial.println(F(" set to OFF"));
  }
  pubTimestamp();  
}

// lampSetAutoProg
// Set Lamp Automatic Programs data
void lampSetAutoProg(byte b_Prog, bool b_OnOff, byte* message) {
  // Expect 09:25 or 21:25
  int   i_hour   = 0;
  int   i_minute = 0;
  char  c_time[5];
  i_hour    = ((int)message[0] - '0')*10 + ((int)message[1] - '0');
  i_minute  = ((int)message[3] - '0')*10 + ((int)message[4] - '0');
  Serial.print("Set Auto Lamp ");
  Serial.print(b_Prog);
  //Aquarium.Lamp.Program[b_Prog].b_Active = b_ProgOn;
  Serial.print(" program to ");
  if (b_OnOff) {
    Serial.print("turn ON the ligths at ");
    Aquarium.Lamp.Program[b_Prog].i_ONtime = i_hour * 60 + i_minute;
  }
  else {
    Serial.print("turn OFF the ligths at ");
    Aquarium.Lamp.Program[b_Prog].i_OFFtime = i_hour * 60 + i_minute;
  }
  sprintf(c_time, "%02d:%02d", i_hour, i_minute);
  Serial.println(c_time);
  pubTimestamp();  
}

// lampLoopCtl
// When set to auto, control the lamps based on ON/OFF hours.
void lampLoopCtl() {
  int       i_Now = 0;
  bool      b_LampSet = false;
  
  if (Aquarium.Lamp.b_AutoMode == true) {
    if (wifi_status == WL_CONNECTED) {  
      timeClient.update();
      i_Now = timeClient.getHours()*60 + timeClient.getMinutes();
    }
    else {
      DateTime now = RTClock.now();
      i_Now = now.hour() * 60 + now.minute();
    }
    for (int i_Cont = 0; i_Cont < MAX_AUTO_PROG; i_Cont++) {
      if (Aquarium.Lamp.Program[i_Cont].b_Active) {
        if (Aquarium.Lamp.Program[i_Cont].i_OFFtime > Aquarium.Lamp.Program[i_Cont].i_ONtime) {
          if( (i_Now >= Aquarium.Lamp.Program[i_Cont].i_ONtime) &&
              (i_Now <  Aquarium.Lamp.Program[i_Cont].i_OFFtime) ) {
            b_LampSet = true;
          }
        }
        else {
          if( (i_Now >= Aquarium.Lamp.Program[i_Cont].i_ONtime) ||
              (i_Now <  Aquarium.Lamp.Program[i_Cont].i_OFFtime) ) {
            b_LampSet = true;
          }
        }
      }
    }
    lampSet(b_LampSet);
  }
}

// tempSet
// Set Temperatures for Maxium and Minimum
void tempSet(byte b_Prog, byte* message) {
  if (b_Prog) {
    Aquarium.Temperature.f_Max = ((int)message[0] - '0')*10 + ((int)message[1] - '0');
  }
  else {
    Aquarium.Temperature.f_Min = ((int)message[0] - '0')*10 + ((int)message[1] - '0');
  }
}

/*  ARDUINO SETUP   */
void setup() {
  // put your setup code here, to run once:
  Serial.begin(SERIAL_SPEED);
  Serial.println();
  Serial.println(F("\t   AQUACONTROL"));
  Serial.print(F("\t  Version: "));
  Serial.println(F(VERSION));
  Serial.println(F(" Built on " __DATE__ " at " __TIME__));
  Serial.println();
  TempSensor.begin();
  Aquarium_Default();
  wifi_status = WiFiConfig_Setup();
  if(wifi_status != WL_CONNECTED) {
    wifi_status = WiFi_Setup();
  }
  OTA_Setup();
  if(wifi_status == WL_CONNECTED) {
    timeClient.begin();
    MQTT_Setup();
  }
  pubResetData();
  RTC_Setup();

  l_startCtlTime = millis();
}

/*  ARDUINO LOOP   */
void loop() {
  // put your main code here, to run repeatedly:
  ArduinoOTA.handle();
  mqtt_client.loop();

  l_elapsCtlTime  = millis() - l_startCtlTime;
  if (l_elapsCtlTime > (CTL_TIME*1000)) {
    Serial.println(F(" ---> Control loop <--- "));
    // Run some vefications
    wifi_status = WiFi.status();
    if(wifi_status != WL_CONNECTED) {
      Serial.println("---> WiFi Disconnected");
      wifi_status = WiFi_Setup();
      if(wifi_status == WL_CONNECTED) {
        timeClient.begin();
        MQTT_Setup();
      }
    }
    if (mqtt_client.state() != 0) {
      MQTT_Setup();
    }

    // Control Aquarium
    pubTemperature();
    lampLoopCtl();
    heatLoopCtl();
    l_startCtlTime = millis();
  }
}
