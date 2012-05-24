// the USB Host libraries
#include <Max3421e.h>
#include <Usb.h>

// the AOA library
#include <AndroidAccessory.h>

#define TEMP_SENSOR A3 // the temperature sensor pin

// create an instance of the AndroidAccessory class
AndroidAccessory acc("Manufacturer name",
        "Model",
        "Description",
        "1.0",
        "http://www.example.com",
        "Serial number"); 
        
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
    
    // Power up the Android device.
    acc.powerOn();
}

/* Now declare loop() for real. This method will continue
to loop until Arduino is powered down or reset. */
void loop() 
{
        
    if (acc.isConnected())
    {
        Serial.println("acc is connected\r\n");
         
        // Read the voltage from the sensor
        uint16_t val; 
        val = analogRead(TEMP_SENSOR);
        Serial.print("val=");
        Serial.println(val,HEX);
        
        // Declare a message to be sent to the Android device
        byte msg[3];
        
        // default to 0 for the first sensor
        msg[0] = 0x0; 

        /* Repackage val into two bytes. (This is unpackaged 
        by the composeInt method in the Android code.)
    	>> is a right-shift operator, so >> 8 moves all the 
        bits in val to the right by 8 places. 
        For more information, look up bitwise operations in 
        the C programming language. */
        msg[1] = val >> 8;
        msg[2] = val & 0xff;
        
        // Finally, send the message to the Android device
        acc.write(msg, 3);
    }

    // Delay for 100 milliseconds. 
    delay(100);
}
