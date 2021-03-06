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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.codenvy.analytics.DateRangeUtils.getNumberOfUnitsBetweenDates;
import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Reshetnyak
 * @author Dmytro Nochevnov
 */
public class TestNumberOfUnitsBetweenDates {

    @Test(dataProvider = "testDataProvider")
    public void test(String fromDate, String toDate, Parameters.TimeUnit timeUnit, long rowsCount) throws Exception {
        Context.Builder builder = new Context.Builder();

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
        builder.put(Parameters.TIME_UNIT, timeUnit.toString());

        assertEquals(getNumberOfUnitsBetweenDates(timeUnit,
                                                  builder.getAsDate(Parameters.FROM_DATE),
                                                  builder.getAsDate(Parameters.TO_DATE)),
                     rowsCount);
    }

    @DataProvider(name = "provider")
    public Object[][] testDataProvider() {
        return new Object[][]{
                {"20130101", "20131229", Parameters.TimeUnit.DAY, 363},
                {"20140101", "20140509", Parameters.TimeUnit.DAY, 129},
                {"20140502", "20140509", Parameters.TimeUnit.DAY, 8},
                {"20140502", "20140502", Parameters.TimeUnit.DAY, 1},
                {"20130101", "20140509", Parameters.TimeUnit.DAY, 494},

                {"20130101", "20140509", Parameters.TimeUnit.WEEK, 71},
                {"20140101", "20140509", Parameters.TimeUnit.WEEK, 19},
                {"20140401", "20140509", Parameters.TimeUnit.WEEK, 6},
                {"20140503", "20140509", Parameters.TimeUnit.WEEK, 2},
                {"20140505", "20140509", Parameters.TimeUnit.WEEK, 1},
                {"20140101", "20250509", Parameters.TimeUnit.WEEK, 593},

                {"20130101", "20140509", Parameters.TimeUnit.MONTH, 17},
                {"20140101", "20140509", Parameters.TimeUnit.MONTH, 5},
                {"20140501", "20140509", Parameters.TimeUnit.MONTH, 1},
                {"20140501", "20500509", Parameters.TimeUnit.MONTH, 433},

                {"20140101", "20140509", Parameters.TimeUnit.LIFETIME, 1},
                {"20130101", "20140509", Parameters.TimeUnit.LIFETIME, 1}
        };
    }
}
