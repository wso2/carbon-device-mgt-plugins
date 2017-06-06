/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.gpl.siddhi.extensions.geo.stream;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.gpl.siddhi.extensions.geo.internal.util.GeoOperation;
import org.wso2.gpl.siddhi.extensions.geo.internal.util.WithinOperation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.Attribute.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GeoCrossesStreamProcessor extends StreamProcessor {

    private GeoOperation geoOperation;
    private Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    /**
     * The init method of the StreamProcessor, this method will be called before other methods
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param executionPlanContext         the context of the execution plan
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        geoOperation = new WithinOperation();
        geoOperation.init(attributeExpressionExecutors, 1, attributeExpressionLength);
        List<Attribute> attributeList = new ArrayList<Attribute>(1);
        attributeList.add(new Attribute("crosses", Type.BOOL));
        return attributeList;
    }

    /**
     * This will be called only once, to acquire required resources
     * after initializing the system and before processing the events.
     */
    @Override
    public void start() {

    }

    /**
     * This will be called only once, to release the acquired resources
     * before shutting down the system.
     */
    @Override
    public void stop() {

    }

    /**
     * The serializable state of the element, that need to be
     * persisted for the reconstructing the element to the same state
     * on a different point of time
     *
     * @return stateful objects of the element as an array
     */
    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    /**
     * The serialized state of the element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param state the stateful objects of the element as an array on
     *              the same order provided by currentState().
     */
    @Override
    public void restoreState(Object[] state) {

    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        while (streamEventChunk.hasNext()) {
            ComplexEvent complexEvent = streamEventChunk.next();

            Object[] data = new Object[attributeExpressionLength - 1];
            for (int i = 1; i < attributeExpressionLength; i++) {
                data[i-1] = attributeExpressionExecutors[i].execute(complexEvent);
            }
            boolean within = (Boolean)geoOperation.process(data);

            String id = attributeExpressionExecutors[0].execute(complexEvent).toString();
            if (set.contains(id)) {
                if (!within) {
                    //alert out
                    complexEventPopulater.populateComplexEvent(complexEvent, new Object[]{within});
                    set.remove(id);
                } else {
                    streamEventChunk.remove();
                }
            } else {
                if (within) {
                    //alert in
                    complexEventPopulater.populateComplexEvent(complexEvent, new Object[]{within});
                    set.add(id);
                } else {
                    streamEventChunk.remove();
                }
            }
        }
        nextProcessor.process(streamEventChunk);
    }
}
