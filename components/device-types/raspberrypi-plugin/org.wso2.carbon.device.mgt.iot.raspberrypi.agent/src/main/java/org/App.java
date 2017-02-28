package org.wso2;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) {
        try{
            SidhdhiQuery query = new SidhdhiQuery();
            System.out.println("starting....");
            query.run();
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
