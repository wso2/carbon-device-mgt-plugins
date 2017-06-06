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

package org.wso2.gpl.siddhi.extensions.geo.stream.function;

import com.vividsolutions.jts.geom.Coordinate;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.gpl.siddhi.extensions.geo.internal.util.ClosestOperation;
import org.wso2.gpl.siddhi.extensions.geo.internal.util.GeoOperation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

public class GeoClosestPointsStreamFunctionProcessor extends StreamFunctionProcessor {

    GeoOperation geoOperation;

    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.geoOperation = new ClosestOperation();
        this.geoOperation.init(attributeExpressionExecutors, 0, attributeExpressionExecutors.length);
        List<Attribute> attributeList = new ArrayList<Attribute>(4);
        attributeList.add(new Attribute("closestPointOf1From2Latitude", Attribute.Type.DOUBLE));
        attributeList.add(new Attribute("closestPointOf1From2Longitude", Attribute.Type.DOUBLE));
        attributeList.add(new Attribute("closestPointOf2From1Latitude", Attribute.Type.DOUBLE));
        attributeList.add(new Attribute("closestPointOf2From1Longitude", Attribute.Type.DOUBLE));
        return attributeList;
    }

    @Override
    protected Object[] process(Object[] data) {
        Coordinate[] coordinates = (Coordinate[]) geoOperation.process(data);

        return new Object[]{coordinates[0].x, coordinates[0].y, coordinates[1].x, coordinates[1].y};
    }

    @Override
    protected Object[] process(Object o) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] objects) {

    }
}
