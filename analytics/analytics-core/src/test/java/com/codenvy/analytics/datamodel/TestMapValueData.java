/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.datamodel;

import com.codenvy.analytics.BaseTest;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestMapValueData extends BaseTest {

    private ValueData valueData;

    @BeforeClass
    public void init() {
        Map<String, ValueData> value = new HashMap<>();
        value.put("key1", new StringValueData("value"));
        value.put("key2", new DoubleValueData(10.1));
        value.put("key3", new LongValueData(10));

        valueData = new MapValueData(value);
    }

    @Test
    public void testEquals() {
        Map<String, ValueData> value = new HashMap<>();
        value.put("key3", new LongValueData(10));
        value.put("key1", new StringValueData("value"));
        value.put("key2", new DoubleValueData(10.1));

        MapValueData newValueData = new MapValueData(value);

        assertEquals(valueData, newValueData);
    }

    @Test
    public void testNotEquals() {
        Map<String, ValueData> value = new HashMap<>();
        value.put("key1", new StringValueData("value"));
        value.put("key2", new DoubleValueData(10.1));

        MapValueData newValueData = new MapValueData(value);

        assertNotEquals(valueData, newValueData);
    }

    @Test
    public void testGetAsString() {
        String asString = valueData.getAsString();
        assertTrue(asString.contains("\"key1\":\"value\""));
        assertTrue(asString.contains("\"key2\":\"10.1\""));
        assertTrue(asString.contains("\"key3\":\"10\""));
    }

    @Test
    public void testAdd() {
        Map<String, ValueData> value = new HashMap<>();
        value.put("key3", new LongValueData(10));
        value.put("key2", new DoubleValueData(10.1));

        MapValueData newValueData = new MapValueData(value);

        value = new HashMap<>();
        value.put("key3", new LongValueData(20));
        value.put("key1", new StringValueData("value"));
        value.put("key2", new DoubleValueData(20.2));

        MapValueData sumValueData = new MapValueData(value);

        assertEquals(sumValueData, valueData.add(newValueData));
    }

    @Test
    public void testSubtract() {
        Map<String, ValueData> value = new HashMap<>();
        value.put("key1", new StringValueData("value"));
        value.put("key2", new DoubleValueData(20));
        value.put("key3", new LongValueData(20));
        MapValueData valueData1 = new MapValueData(value);


        value = new HashMap<>();
        value.put("key1", new StringValueData("value"));
        value.put("key2", new DoubleValueData(10));
        value.put("key3", new LongValueData(5));
        MapValueData valueData2 = new MapValueData(value);

        value = new HashMap<>();
        value.put("key2", new DoubleValueData(10));
        value.put("key3", new LongValueData(15));
        MapValueData subtractValueData = new MapValueData(value);

        assertEquals(subtractValueData, valueData1.subtract(valueData2));
    }
}
