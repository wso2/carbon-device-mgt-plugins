/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.parsers;

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMNodeException;

/**
 * This class handles the URI parsing of Management Nodes
 */
public class URIParser {

    /******
     * A valid URI for OMADM is :
     *    uri        = node_uri[ property | list ]
     *    property   = "?prop=" prop_name
     *    list       = "?list=" attribute
     *    node_uri   = "." | [ "./" ] path
     *    path       = segment *( "/" segment )
     *    segment    = *( pchar | "." ) pchar
     *    pchar      = unreserved | escaped | ":" | "@" | "&" | "=" | "+" | "$" | ","
     *    unreserved = alphanum | mark
     *    mark       = "-" | "_" | "!" | "~" | "*" | "'" | "(" | ")"
     *    escaped    = "%" hex hex
     *    hex        = digit | "A" | "B" | "C" | "D" | "E" | "F" |
     *                         "a" | "b" | "c" | "d" | "e" | "f"
     *    alphanum   = alpha | digit
     *    alpha      = lowalpha | upalpha
     *    lowalpha   = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" |
     *                 "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" |
     *                 "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
     *    upalpha    = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" |
     *                 "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" |
     *                 "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
     *    digit      = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
     *                 "8" | "9"
     *
     *****/

    private static String NODE_URI_DELIMETER = "/";

    public static void validateUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            throw new DMNodeException("URI cannot be null or empty");
        }

        if (!uri.startsWith(".") || uri.endsWith(".")) {
            throw new DMNodeException("URI cannot be null or empty");
        }
        //TODO: More validation scenarios should be added
    }

    public static String[] nodeNames(String uri) {
        validateUri(uri);
        String[] nodeNames = uri.split(NODE_URI_DELIMETER);
        return nodeNames;
    }

    public static String getParentPath(String uri) {
        String parentPath = uri.substring(0, (uri.lastIndexOf(NODE_URI_DELIMETER) - 1));
        return parentPath;
    }

    public static String getNodeName(String uri) {
        String[] nodeNames = uri.split(NODE_URI_DELIMETER);
        return nodeNames[(nodeNames.length - 1)];
    }
}
