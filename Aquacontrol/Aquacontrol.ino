#include "Aquacontrol.h"
#include <OneWire.h>    // https://www.pjrc.com/teensy/td_libs_OneWire.html
#include <DS3231M.h>    // https://github.com/SV-Zanshin/DS3231M

#define   RELAY_LAMP  (4)
#define   RELAY_AERA  (5)
#define   RELAY_HEAT  (12)
#define   RELAY_FILT  (13)
#define   SENS_THERM  (16)
#define   SERIAL_SPEED  (115200)

DS3231M_Class DS3231M;    // Instance DS3231M

void setup() {
  // put your setup code here, to run once:
  DS3231M.begin();
}

void loop() {
  // put your main code here, to run repeatedly:

}
