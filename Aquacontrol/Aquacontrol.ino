#include "Aquacontrol.h"
#include <OneWire.h>            // https://www.pjrc.com/teensy/td_libs_OneWire.html
#include <DallasTemperature.h>  // https://github.com/milesburton/Arduino-Temperature-Control-Library
#include <DS3231M.h>            // https://github.com/SV-Zanshin/DS3231M
#include <ESP8266WiFi.h>        // https://arduino-esp8266.readthedocs.io/en/latest/
#include <WiFiUdp.h>
#include <NTPClient.h>          // https://github.com/taranais/NTPClient
#include <PubSubClient.h>       // https://pubsubclient.knolleary.net/

// Examples: https://techtutorialsx.com/2017/04/09/esp8266-connecting-to-mqtt-broker/
//           https://randomnerdtutorials.com/esp32-ntp-client-date-time-arduino-ide/
//           https://www.esp8266.com/viewtopic.php?f=160&t=17549

OneWire             oneWire(SENS_THERM);    // Instance OneWire bus for Temp Sensor
DallasTemperature   TempSensor(&oneWire);   // Instance Temp Sensor
DS3231M_Class       RTClock;                // Instance DS3231M RTC Clock
WiFiClient          espClient;
WiFiUDP             ntpUDP;                 // UDP Link for NTP
NTPClient           timeClient(ntpUDP, "pool.ntp.org", GMTp1_OFF);
PubSubClient        mqtt_client(espClient);


/* FUNCTIONS DECLARATION   */

void Relay_Default() {
  pinMode(RELAY_LAMP, OUTPUT);
  pinMode(RELAY_AERA, OUTPUT);
  pinMode(RELAY_HEAT, OUTPUT);
  pinMode(RELAY_FILT, OUTPUT);
  digitalWrite(RELAY_LAMP, LOW);
  digitalWrite(RELAY_AERA, LOW);
  digitalWrite(RELAY_HEAT, LOW);
  digitalWrite(RELAY_FILT, LOW);
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

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

// MQTT_callback
// Handles incoming messages for the topics subscribed
void mqtt_callback(char* topic, byte* payload, unsigned int length) {
  #ifdef AQU_DEBUG
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);
 
  Serial.print("Message:");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
 
  Serial.println();
  Serial.println("-----------------------");
  #endif
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
}

void setResetTime() {
  String  timeString;
  int     i_datetime = 0;
  // Send reset time
  timeClient.update();
  timeString = timeClient.getFormattedDate();
  i_datetime = timeString.indexOf("T");
  Serial.print("DATE: ");
  Serial.print(timeString.substring(0,i_datetime));
  Serial.print(" | TIME (GMT+1): ");
  Serial.println(timeString.substring(i_datetime+1, timeString.length()-1));
  mqtt_client.publish("aquarium/reset", timeString.c_str());
}

/*  ARDUINO SETUP   */
void setup() {
  // put your setup code here, to run once:
  Relay_Default();
  Serial.begin(SERIAL_SPEED);
  RTClock.begin();
  TempSensor.begin();
  WiFi_Setup();
  timeClient.begin();
  MQTT_Setup();
  setResetTime();
}

/*  ARDUINO LOOP   */
void loop() {
  // put your main code here, to run repeatedly:

}
