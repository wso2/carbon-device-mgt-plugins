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

package org.wso2.carbon.mdm.services.android.omadm.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * MD5 Hash class
 */
public class MD5 {

    private static SecureRandom random = null;
    private static MessageDigest md = null;

    public static byte[] getNextNonce()
    {
        byte[] nextNonce = new byte[16];
        random.nextBytes(nextNonce);
        for (int j = 0; j < nextNonce.length; j++)
        {
            int i = nextNonce[j] & 0xFF;
            if ((i < 32) || (i > 128)) {
                nextNonce[j] = ((byte)(32 + i % 64));
            }
        }
        return nextNonce;
    }

    public static byte[] digest(byte[] data)
    {
        md.reset();
        return md.digest(data);
    }

    private static void randomGeneratorInit()
            throws NoSuchAlgorithmException
    {
        random = SecureRandom.getInstance("SHA1PRNG");
    }

    static
    {
        try
        {
            randomGeneratorInit();
            md = MessageDigest.getInstance("MD5");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
