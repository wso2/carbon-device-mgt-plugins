package org.wso2;

/**
 * Created by thilinida on 2/1/17.
 */

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.wiringpi.Gpio;

public class PIRController {
    private static PIRController controller;
    final GpioController gpio = GpioFactory.getInstance();

    private void init(){
        Gpio.pinMode (3, Gpio.INPUT) ;
    }//pin 15
    public static PIRController getController(){
        if (controller==null){
            controller = new PIRController();
            controller.init();
            return controller;
        }

        return controller;
    }

    public int getData(){
        if (Gpio.digitalRead(3) == 0) return 0;

        return 1;
    }
}
