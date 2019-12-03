# AQUACONTROL
Aquacontrol is a freshwater Aquarium controller to make it easy the interaction with the most common technical equipment used in aquariums.

You can find:
1. PCB Board project on [Circuit Maker](https://workspace.circuitmaker.com/Projects/Details/Pablo-Garcia/Aquacontrol)
1. Housing for 3D Printer on [Thingiverse](https://www.thingiverse.com/thing:3843515)

And here on GitHub the repository includes:
- apk. Android apk developed on [Android Studio](https://developer.android.com/studio).
- Aquacontrol. ESP8266 firmware developed on [Arduino IDE](https://www.arduino.cc/en/main/software).
- me. Case developed on [FreeCAD](https://www.freecadweb.org/).

## Setup
Aquacontrol consists on one PCB which controls the most common technics on the beginners aquarium:
- Heater, to heat the water usually around 25ºC.
- Filter, to keep the biological and mechanical filtering of the water running.
- Lamp, to illuminate the aquarium and improve plants growing.
- Aerator, to improve the exchange of O2.

To ease the setup and to avoid cutting the equipment cables, I have used a Simon set:
- Simon 27731-65. Surface mount pack for four sockets.
- Simon 27432-65. 4 Schuko Sockets

## Application
The APK includes a Main Activity to control the Aquarium and a Settings activity con configure MQTT Broker.

![APK Main](https://github.com/garroble/Aquacontrol/blob/master/docs/img/apk_main.png?raw=true)
![APK Settings](https://github.com/garroble/Aquacontrol/blob/master/docs/img/apk_settings.png?raw=true)

## License
This project is licensed under the GPL v3 license.

## Credits
This project relies on Arduino IDE to work and ESP8266 boards installed from board manager.

### Examples
These are some of the most usefull examples I have found while developing Aquacontrol
1. https://techtutorialsx.com/2017/04/09/esp8266-connecting-to-mqtt-broker/
1. https://randomnerdtutorials.com/esp32-ntp-client-date-time-arduino-ide/
1. http://www.martyncurrey.com/esp8266-and-the-arduino-ide-part-5-adding-wifimanager/
1. https://internetofhomethings.com/homethings/?p=1943

### Libraries
The project is powered by the following libraries, thanks to all the contributors:
ESP8266 on Arduino IDE
- [OneWire](https://www.pjrc.com/teensy/td_libs_OneWire.html)
- [DallasTemperature](https://github.com/milesburton/Arduino-Temperature-Control-Library)
- [DS3231M](https://github.com/SV-Zanshin/DS3231M)
- [ESP8266WiFi](https://arduino-esp8266.readthedocs.io/en/latest/) and other ESP8266 libraries.
- [WiFiManager](https://github.com/kentaylor/WiFiManager)
- [NTPClient](https://github.com/taranais/NTPClient)
- [PubSubClient](https://pubsubclient.knolleary.net/)

Android APK
- [eclipse paho](https://www.eclipse.org/paho/)
