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
package com.codenvy.analytics.metrics.users;


import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getExpandedValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author Anatoliy Bazko */
public class TestNewUsersBuilds extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent(UID1, "user1@gmail.com", "user1@gmail.com").withDate("2014-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent(UID2, "user2@gmail.com", "user2@gmail.com").withDate("2014-01-02").build());

        events.add(Event.Builder.createBuildStartedEvent(UID1, "ws", "p", "t", "id1", "60000").withDate("2014-01-01").withTime("10:00:00").build());
        events.add(Event.Builder.createBuildFinishedEvent(UID1, "ws", "p", "t", "id1", "60000").withDate("2014-01-01").withTime("10:01:00").build());

        events.add(Event.Builder.createBuildStartedEvent(UID1, "ws", "p", "t", "id2", "60000").withDate("2014-01-02").withTime("10:00:00").build());
        events.add(Event.Builder.createBuildFinishedEvent(UID1, "ws", "p", "t", "id2", "60000").withDate("2014-01-02").withTime("10:01:00").build());

        events.add(Event.Builder.createBuildStartedEvent(UID2, "ws", "p", "t", "id3", "60000").withDate("2014-01-02").withTime("10:00:00").build());
        events.add(Event.Builder.createBuildFinishedEvent(UID2, "ws", "p", "t", "id3", "60000").withDate("2014-01-02").withTime("10:01:00").build());

        events.add(Event.Builder.createRunStartedEvent(UID1, "ws", "p", "t", "id4", "60000", "128").withDate("2014-01-01").withTime("10:00:00").build());
        events.add(Event.Builder.createRunFinishedEvent(UID1, "ws", "p", "t", "id4", "60000", "128").withDate("2014-01-01").withTime("10:01:00").build());

        File log = LogGenerator.generateLog(events);

        executeScripts(log, "20140101");
        doIntegrity("20140101");

        executeScripts(log, "20140102");
        doIntegrity("20140102");
    }

    private void executeScripts(File log, String date) throws IOException, ParseException {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.CREATED_USERS, MetricType.CREATED_USERS).getParamsAsMap());
        pigServer.execute(ScriptType.CREATED_USERS, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());
    }

    @Test
    public void testGetValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.NEW_USERS_BUILDS);

        // user1
        Context context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140101").build();
        LongValueData l = getAsLong(metric, context);
        assertEquals(l.getAsLong(), 1L);

        // user2
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140102").put(Parameters.TO_DATE, "20140102").build();
        l = getAsLong(metric, context);
        assertEquals(l.getAsLong(), 1L);

        // user1
        // user2
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140102").build();
        l = getAsLong(metric, context);
        assertEquals(l.getAsLong(), 2L);
    }

    @Test
    public void testGetExpandedValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.NEW_USERS_BUILDS);

        // user1
        Context context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140101").build();
        ListValueData l = getExpandedValue(metric, context);
        assertEquals(l.size(), 1);

        // user2 70min
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140102").put(Parameters.TO_DATE, "20140102").build();
        l = getExpandedValue(metric, context);
        assertEquals(l.size(), 1);

        Map<String, Map<String, ValueData>> m = listToMap(l, "user");
        assertTrue(m.containsKey(UID2));

        // user1
        // user2
        context = new Context.Builder().put(Parameters.FROM_DATE, "20140101").put(Parameters.TO_DATE, "20140102").build();
        l = getExpandedValue(metric, context);
        assertEquals(l.size(), 2);

        m = listToMap(l, "user");
        assertTrue(m.containsKey(UID1));
        assertTrue(m.containsKey(UID2));
    }
}


