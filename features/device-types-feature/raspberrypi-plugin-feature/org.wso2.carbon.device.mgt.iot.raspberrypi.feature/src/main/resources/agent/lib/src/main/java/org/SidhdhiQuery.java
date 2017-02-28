package org.wso2;

/**
 * Created by thilinida on 2/1/17.
 */

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

public class SidhdhiQuery implements Runnable {

    String file;

    //Bam data push client
    private static SiddhiManager siddhiManager = new SiddhiManager();
    private StartExecutionPlan startExecutionPlan;

    public static SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public static void setSiddhiManager(SiddhiManager siddhiManager) {
        SidhdhiQuery.siddhiManager = siddhiManager;
    }

    public void run() {

        startExecutionPlan = new StartExecutionPlan().invoke();
        InputHandler inputHandler = startExecutionPlan.getInputHandler();

        while (true) {

            //Sending events to Siddhi
            try {
                int Reading = PIRController.getController().getData();
                // System.out.printf("reading = %d\n",Reading);
                inputHandler.send(new Object[]{"device_1",Reading});
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Re-Initialize SiddhiManager
     */
    private void restartSiddhi() {
        siddhiManager.shutdown();
        siddhiManager = new SiddhiManager();
    }


    /**
     * Initialize SiddhiExecution plan
     */
    private static class StartExecutionPlan {
        private InputHandler inputHandler;

        public InputHandler getInputHandler() {
            return inputHandler;
        }

        public StartExecutionPlan invoke() {
            String executionPlan;

            executionPlan = "define stream PIREventStream (deviceID string, val int);\n"+
                    "from PIREventStream#window.time(8 sec)\n"+
                    "select deviceID, max(val) as maxValue\n"+
                    "group by deviceID\n"+
                    "insert expired events into analyzeStream;\n"+
                    "from analyzeStream[maxValue >0]\n"+
                    "select maxValue\n"+
                    "insert into occupiedStream;\n"+
                    "from analyzeStream[maxValue < 1]"+
                    "select maxValue\n"+
                    "insert into vacantStream;";

            //Generating runtime
            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
            executionPlanRuntime.start();


            executionPlanRuntime.addCallback("vacantStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    if (events.length > 0) {
                        System.out.println("Siddhiuery.java:-vacant");
                        WriteData.vacant();
                    }
                }
            });

            executionPlanRuntime.addCallback("occupiedStream", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    if (events.length > 0) {
                        System.out.println("Siddhiuery.java:-occupied");
                        WriteData.occupied();
                    }
                }

            });

            //Retrieving InputHandler to push events into Siddhi
            inputHandler = executionPlanRuntime.getInputHandler("PIREventStream");

            //Starting event processing
            System.out.println("Execution Plan Started!");
            return this;
        }

    }
}
