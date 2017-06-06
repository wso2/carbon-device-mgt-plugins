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

package org.wso2.gpl.siddhi.extensions.geo.internal.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

public abstract class GeoOperation {
    public boolean point = false;
    protected Object data;
    public PreparedGeometry geometry = null;

    public void init(ExpressionExecutor[] attributeExpressionExecutors, int start, int end) {
        int position = start;
        if (attributeExpressionExecutors[position].getReturnType() == Attribute.Type.DOUBLE) {
            point = true;
            if (attributeExpressionExecutors[position + 1].getReturnType() != Attribute.Type.DOUBLE) {
                throw new ExecutionPlanCreationException("Longitude and Latitude must be provided as double values");
            }
            ++position;
        } else if (attributeExpressionExecutors[position].getReturnType() == Attribute.Type.STRING) {
            point = false;
        } else {
            throw new ExecutionPlanCreationException((position + 1) +
                    " parameter should be a string for a geometry or a double for a latitude");
        }
        ++position;
        if (position >= end) {
            return;
        }
        if (attributeExpressionExecutors[position].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanCreationException((position + 1) + " parameter should be a GeoJSON geometry string");
        }
        if (attributeExpressionExecutors[position] instanceof ConstantExpressionExecutor) {
            String strGeometry = attributeExpressionExecutors[position].execute(null).toString();
            geometry = GeometryUtils.preparedGeometryFromJSON(strGeometry);
        }
    }

    public Object process(Object[] data) {
        Geometry currentGeometry;
        if (point) {
            double longitude = (Double) data[0];
            double latitude = (Double) data[1];
            currentGeometry = GeometryUtils.createPoint(longitude, latitude);
        } else {
            currentGeometry = GeometryUtils.geometryFromJSON(data[0].toString());
        }
        if (geometry != null) {
            return operation(currentGeometry, geometry, data);
        } else {
            return operation(currentGeometry, GeometryUtils.geometryFromJSON(data[point ? 2 : 1].toString()),
                    data);
        }
    }

    public Geometry getCurrentGeometry(Object[] data) {
        if (point) {
            double longitude = (Double) data[0];
            double latitude = (Double) data[1];
            return GeometryUtils.createPoint(longitude, latitude);
        } else {
            return GeometryUtils.createGeometry(data[0]);
        }
    }

    public abstract Object operation(Geometry a, Geometry b, Object[] data);

    public abstract Object operation(Geometry a, PreparedGeometry b, Object[] data);

    public abstract Attribute.Type getReturnType();
}
