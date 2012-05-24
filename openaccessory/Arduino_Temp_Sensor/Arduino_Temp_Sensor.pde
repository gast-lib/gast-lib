#define TEMP_SENSOR A3 // the temperature sensor pin

/* The two essential methods for any Arduino sketch: 
setup() and loop(). Run both of them once to ensure a
clear and functional board. */
void setup();
void loop();
 
/* Now declare setup() for real. This methood will run 
once after the board has been powered on or reset. */
void setup()
{
    // start serial debugging
    Serial.begin(115200);
    Serial.println("\r\nADK has run setup().");
    Serial.println("Ready to start reading the temp...");
}

/* Now declare loop() for real. This method will continue
to loop until Arduino is powered down or reset. */
void loop() 
{
    // Read the voltage from the sensor
    uint16_t val; 
    val = analogRead(TEMP_SENSOR);
    Serial.print("val=");
    Serial.println(val,HEX);

    // Delay for 100 milliseconds. 
    delay(1000);
}
