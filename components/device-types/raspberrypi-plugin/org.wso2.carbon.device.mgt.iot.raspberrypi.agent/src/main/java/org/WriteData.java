package org.wso2;

/**
 * Created by thilinida on 2/15/17.
 */

import java.io.BufferedWriter;
import java.io.FileWriter;

public class WriteData {
    public static void vacant(){
        try(BufferedWriter f= new BufferedWriter(new FileWriter("src/readings.txt"))){
            f.write("dVacant",1,6);
            f.flush();
            Thread.sleep(500);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void occupied(){
        try(BufferedWriter f= new BufferedWriter(new FileWriter("src/readings.txt"))){
            f.write("dOccupied",1,8);
            f.flush();
            Thread.sleep(500);
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
