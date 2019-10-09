package com.aquacontrol.app;

public class Constants {

    public static final String CLIENT_ID            = "app_startup_placeholder";
    public static final String PREFERENCES          = "com.aquacontrol.app.PREFERENCES";
    public static final String PREF_BROKER_URL      = "broker_url";
    public static final String PREF_BROKER_USER     = "broker_user";
    public static final String PREF_BROKER_PASS     = "broker_pass";

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

    public static final String MQTT_AQU_TEMP_TMAX   = "aquarium/temperature/tempmax";
    public static final String MQTT_AQU_TEMP_TMIN   = "aquarium/temperature/tempmin";
    public static final int    TEMP_MAX_MAX         = 30;
    public static final int    TEMP_MAX_MIN         = 20;
    public static final int    TEMP_MIN_MAX         = 20;
    public static final int    TEMP_MIN_MIN         = 10;

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

    public static final String MQTT_AERA_CTL       = "aquarium/aerator/control";
    public static final String MQTT_AERA_CTL_AUTO  = "aquarium/aerator/automode";
    public static final String MQTT_AERA_CTL_A0ACT = "aquarium/aerator/prog0active";
    public static final String MQTT_AERA_CTL_A0ON  = "aquarium/aerator/prog0on";
    public static final String MQTT_AERA_CTL_A0OFF = "aquarium/aerator/prog0off";
    public static final String MQTT_AERA_CTL_A1ACT = "aquarium/aerator/prog1active";
    public static final String MQTT_AERA_CTL_A1ON  = "aquarium/aerator/prog1on";
    public static final String MQTT_AERA_CTL_A1OFF = "aquarium/aerator/prog1off";
    public static final String MQTT_AERA_CTL_A2ACT = "aquarium/aerator/prog2active";
    public static final String MQTT_AERA_CTL_A2ON  = "aquarium/aerator/prog2on";
    public static final String MQTT_AERA_CTL_A2OFF = "aquarium/aerator/prog2off";

    public static final String MQTT_ON              = "on";
    public static final String MQTT_OFF             = "off";

    public static final String CONNECTED = "Connected";
    public static final String NOTCONNECTED = "Not Connected";
}

