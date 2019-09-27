package com.aquacontrol.app;

public class Constants {

    public static final String MQTT_BROKER_URL = "tcp://farmer.cloudmqtt.com:17344";
    public static final String MQTT_CLIENT_UN = "rjobxxfd";
    public static final String MQTT_CLIENT_PW = "JdD4715E4gmh";
    public static final String CLIENT_ID = "app_startup_placeholder";

    public static final String MQTT_AQU_VERS        = "aquarium/version";
    public static final String MQTT_AQU_RST         = "aquarium/reset";
    public static final String MQTT_AQU_IP          = "aquarium/ip";
    public static final String MQTT_AQU_TIME        = "aquarium/timestamp";
    public static final String MQTT_AQU_LAMP_STA    = "aquarium/lamp/status";
    public static final String MQTT_AQU_AERA_STA    = "aquarium/aerator/status";
    public static final String MQTT_AQU_HEAT_STA    = "aquarium/heater/status";
    public static final String MQTT_AQU_FILT_STA    = "aquarium/filter/status";
    public static final String MQTT_AQU_TEMP        = "aquarium/temperature";

    public static final String MQTT_HEAT_CTL        = "aquarium/heater/control";
    public static final String MQTT_FILT_CTL        = "aquarium/filter/control";

    public static final String MQTT_LAMP_CTL       = "aquarium/lamp/control";
    public static final String MQTT_LAMP_CTL_AUTO  = "aquarium/lamp/automode";
    public static final String MQTT_LAMP_CTL_A0ACT = "aquarium/lamp/prog0active";
    public static final String MQTT_LAMP_CTL_A0ON  = "aquarium/lamp/prog0on";
    public static final String MQTT_LAMP_CTL_A0OFF = "aquarium/lamp/prog0off";
    public static final String MQTT_LAMP_CTL_A1ACT = "aquarium/lamp/prog1active";
    public static final String MQTT_LAMP_CTL_A1ON  = "aquarium/lamp/prog1on";
    public static final String MQTT_LAMP_CTL_A1OFF = "aquarium/lamp/prog1off";
    public static final String MQTT_LAMP_CTL_A2ACT = "aquarium/lamp/prog2active";
    public static final String MQTT_LAMP_CTL_A2ON  = "aquarium/lamp/prog2on";
    public static final String MQTT_LAMP_CTL_A2OFF = "aquarium/lamp/prog2off";

    public static final String MQTT_ON              = "on";
    public static final String MQTT_OFF             = "off";

    public static final String CONNECTED = "Connected";
    public static final String NOTCONNECTED = "Not Connected";
}

