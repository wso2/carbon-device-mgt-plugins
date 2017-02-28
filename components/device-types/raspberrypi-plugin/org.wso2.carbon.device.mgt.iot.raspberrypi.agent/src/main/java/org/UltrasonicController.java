package org.wso2;

/**
 * Created by thilinida on 2/13/17.
 */

import com.pi4j.io.gpio.*;
import com.pi4j.wiringpi.Gpio;

public class UltrasonicController {
    private static UltrasonicController controller;
    private static int speed = 58000;

    private void init(){
        Gpio.pinMode (12, Gpio.INPUT) ; //pin 19
        Gpio.pinMode (13, Gpio.OUTPUT) ; //pin 21
    }

    public static UltrasonicController getController(){
        if (controller==null){
            controller = new UltrasonicController();
            controller.init();
            return controller;
        }

        return controller;
    }

    public long getData(){
        long start = 0;
        try{
            Gpio.digitalWrite(13,true);
            Thread.sleep(10);
            Gpio.digitalWrite(13,false);

            while(Gpio.digitalRead(12)==0){
                start = System.nanoTime();
            }

            while (Gpio.digitalRead(12)==1){}

            return (System.nanoTime() - start) / speed;
        }catch (Exception e){
            System.out.println("Error measuring distance");
            return -1;
        }
    }
}
