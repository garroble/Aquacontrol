# AQUACONTROL
Aquacontrol is a Springwater Aquarium controller to make it easy the interaction with the most common technical equipment used in aquariums.

You can find:
1. PCB Board project on [Circuit Maker](https://workspace.circuitmaker.com/Projects/Details/Pablo-Garcia/Aquacontrol)
1. Housing for 3D Printer on [Thingiverse](https://www.thingiverse.com/thing:3843515)

## License
This project is licensed under the GPL v3 license.

## Credits
This project relies on Arduino IDE to work and ESP8266 boards installed from board manager.

### Examples
These are some of the most usefull examples I have found while developing Aquacontrol
1. https://techtutorialsx.com/2017/04/09/esp8266-connecting-to-mqtt-broker/
1. https://randomnerdtutorials.com/esp32-ntp-client-date-time-arduino-ide/
1. https://www.esp8266.com/viewtopic.php?f=160&t=17549

### Libraries
The project is powered by the following libraries, thanks to all the contributors:
OneWire.h            // https://www.pjrc.com/teensy/td_libs_OneWire.html
DallasTemperature.h  // https://github.com/milesburton/Arduino-Temperature-Control-Library
DS3231M.h            // https://github.com/SV-Zanshin/DS3231M
ESP8266WiFi.h        // https://arduino-esp8266.readthedocs.io/en/latest/
WiFiUdp.h            // https://arduino-esp8266.readthedocs.io/en/latest/
NTPClient.h          // https://github.com/taranais/NTPClient
PubSubClient.h       // https://pubsubclient.knolleary.net/
