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
package com.codenvy.analytics.metrics.tasks;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.Summaraziable;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsDouble;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static com.codenvy.analytics.metrics.MetricFactory.getMetric;
import static com.codenvy.analytics.pig.scripts.util.Event.Builder.createFactoryUrlAcceptedEvent;
import static java.lang.Math.round;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class TestTaskMetrics extends BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        prepareData();
        doIntegrity("20131020");
    }

    @Test
    public void testTasks() throws IOException {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 12);
    }

    @Test
    public void testTasksList() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LIST);

        ListValueData value = (ListValueData)(metric).getValue(Context.EMPTY);
        assertEquals(value.size(), 12);

        Map<String, Map<String, ValueData>> m = listToMap(value, "id");
        assertEquals(m.get("id1_b").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 10:00:00") + ", "
                                                + "user=user, "
                                                + "ws=temp-ws2, "
                                                + "project=project1, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/temp-ws2/project1, "
                                                + "persistent_ws=0, "
                                                + "id=id1_b, "
                                                + "task_type=builder, "
                                                + "memory=1536, "
                                                + "usage_time=120000, "
                                                + "start_time=" + dateToMillis("2013-10-20 10:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 10:02:00") + ", "
                                                + "gigabyte_ram_hours=0.0512, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=normal, "
                                                + "factory_id=factory2"
                                                + "}");

        assertEquals(m.get("id2_b").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project2, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project2, "
                                                + "persistent_ws=0, "
                                                + "id=id2_b, "
                                                + "task_type=builder, "
                                                + "memory=250, "
                                                + "usage_time=60000, "
                                                + "start_time=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 11:01:00") + ", "
                                                + "gigabyte_ram_hours=0.004166666666666667, "
                                                + "is_factory=1, "
                                                + "launch_type=always-on, "
                                                + "shutdown_type=normal"
                                                + "}");

        assertEquals(m.get("id3_b").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project3, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project3, "
                                                + "persistent_ws=0, "
                                                + "id=id3_b, "
                                                + "task_type=builder, "
                                                + "memory=1536, "
                                                + "usage_time=120000, "
                                                + "start_time=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 11:02:00") + ", "
                                                + "gigabyte_ram_hours=0.0512, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=timeout"
                                                + "}");

        assertEquals(m.get("id1_r").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 10:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project1, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project1, "
                                                + "persistent_ws=0, "
                                                + "id=id1_r, "
                                                + "task_type=runner, "
                                                + "memory=128, "
                                                + "usage_time=120000, "
                                                + "start_time=" + dateToMillis("2013-10-20 10:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 10:02:00") + ", "
                                                + "gigabyte_ram_hours=0.004266666666666667, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=user"
                                                + "}");

        assertEquals(m.get("id2_r").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project2, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project2, "
                                                + "persistent_ws=0, "
                                                + "id=id2_r, "
                                                + "task_type=runner, "
                                                + "memory=128, "
                                                + "usage_time=120000, "
                                                + "start_time=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 11:02:00") + ", "
                                                + "gigabyte_ram_hours=0.004266666666666667, "
                                                + "is_factory=1, "
                                                + "launch_type=always-on, "
                                                + "shutdown_type=user"
                                                + "}");

        assertEquals(m.get("id3_r").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project3, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project3, "
                                                + "persistent_ws=0, "
                                                + "id=id3_r, "
                                                + "task_type=runner, "
                                                + "memory=128, "
                                                + "usage_time=60000, "
                                                + "start_time=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 11:01:00") + ", "
                                                + "gigabyte_ram_hours=0.0021333333333333334, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=timeout"
                                                + "}");

        assertEquals(m.get("id4_r").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 12:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project1, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project1, "
                                                + "persistent_ws=0, "
                                                + "id=id4_r, "
                                                + "task_type=runner, "
                                                + "start_time=" + dateToMillis("2013-10-20 12:00:00") + ", "
                                                + "is_factory=1, "
                                                + "launch_type=timeout"
                                                + "}");

        assertEquals(m.get("id1_d").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 13:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project, "
                                                + "persistent_ws=0, "
                                                + "id=id1_d, "
                                                + "task_type=debugger, "
                                                + "memory=128, "
                                                + "usage_time=120000, "
                                                + "start_time=" + dateToMillis("2013-10-20 13:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 13:02:00") + ", "
                                                + "gigabyte_ram_hours=0.004266666666666667, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=user"
                                                + "}");

        assertEquals(m.get("id2_d").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 14:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project, "
                                                + "persistent_ws=0, "
                                                + "id=id2_d, "
                                                + "task_type=debugger, "
                                                + "memory=128, "
                                                + "usage_time=60000, "
                                                + "start_time=" + dateToMillis("2013-10-20 14:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 14:01:00") + ", "
                                                + "gigabyte_ram_hours=0.0021333333333333334, "
                                                + "is_factory=1, "
                                                + "launch_type=always-on, "
                                                + "shutdown_type=user"
                                                + "}");

        assertEquals(m.get("id3_d").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 15:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project, "
                                                + "persistent_ws=0, "
                                                + "id=id3_d, "
                                                + "task_type=debugger, "
                                                + "memory=128, "
                                                + "usage_time=120000, "
                                                + "start_time=" + dateToMillis("2013-10-20 15:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 15:02:00") + ", "
                                                + "gigabyte_ram_hours=0.004266666666666667, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=timeout"
                                                + "}");

        assertEquals(m.get("session2").toString(), "{"
                                                   + "date=" + dateToMillis("2013-10-20 16:20:00") + ", "
                                                   + "user=user1@gmail.com, "
                                                   + "ws=ws1, "
                                                   + "persistent_ws=0, "
                                                   + "id=session2, "
                                                   + "task_type=editor, "
                                                   + "memory=25, "
                                                   + "usage_time=180000, "
                                                   + "start_time=" + dateToMillis("2013-10-20 16:20:00") + ", "
                                                   + "stop_time=" + dateToMillis("2013-10-20 16:23:00") + ", "
                                                   + "gigabyte_ram_hours=0.00125, "
                                                   + "is_factory=0, "
                                                   + "launch_type=always-on, "
                                                   + "shutdown_type=normal"
                                                   + "}");

        assertEquals(m.get("session1").toString(), "{"
                                                   + "date=" + dateToMillis("2013-10-20 16:00:00") + ", "
                                                   + "user=anonymoususer_user11, "
                                                   + "ws=temp-ws1, "
                                                   + "persistent_ws=0, "
                                                   + "id=session1, "
                                                   + "task_type=editor, "
                                                   + "memory=25, "
                                                   + "usage_time=180000, "
                                                   + "start_time=" + dateToMillis("2013-10-20 16:00:00") + ", "
                                                   + "stop_time=" + dateToMillis("2013-10-20 16:03:00") + ", "
                                                   + "gigabyte_ram_hours=0.00125, "
                                                   + "is_factory=1, "
                                                   + "launch_type=always-on, "
                                                   + "shutdown_type=normal, "
                                                   + "factory_id=factory1"
                                                   + "}");
    }

    @Test
    public void testTaskListFilteredByRunsMetric() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.EXPANDED_METRIC_NAME, MetricType.RUNS.toString());

        Metric metric = getMetric(MetricType.TASKS_LIST);

        ListValueData value = getAsList(metric, builder.build());
        assertEquals(value.size(), 4);

        Map<String, Map<String, ValueData>> m = listToMap(value, "id");

        assertEquals(m.get("id1_r").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 10:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project1, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project1, "
                                                + "persistent_ws=0, "
                                                + "id=id1_r, "
                                                + "task_type=runner, "
                                                + "memory=128, "
                                                + "usage_time=120000, "
                                                + "start_time=" + dateToMillis("2013-10-20 10:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 10:02:00") + ", "
                                                + "gigabyte_ram_hours=0.004266666666666667, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=user"
                                                + "}");

        assertEquals(m.get("id2_r").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project2, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project2, "
                                                + "persistent_ws=0, "
                                                + "id=id2_r, "
                                                + "task_type=runner, "
                                                + "memory=128, "
                                                + "usage_time=120000, "
                                                + "start_time=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 11:02:00") + ", "
                                                + "gigabyte_ram_hours=0.004266666666666667, "
                                                + "is_factory=1, "
                                                + "launch_type=always-on, "
                                                + "shutdown_type=user"
                                                + "}");

        assertEquals(m.get("id3_r").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project3, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project3, "
                                                + "persistent_ws=0, "
                                                + "id=id3_r, "
                                                + "task_type=runner, "
                                                + "memory=128, "
                                                + "usage_time=60000, "
                                                + "start_time=" + dateToMillis("2013-10-20 11:00:00") + ", "
                                                + "stop_time=" + dateToMillis("2013-10-20 11:01:00") + ", "
                                                + "gigabyte_ram_hours=0.0021333333333333334, "
                                                + "is_factory=1, "
                                                + "launch_type=timeout, "
                                                + "shutdown_type=timeout"
                                                + "}");

        assertEquals(m.get("id4_r").toString(), "{"
                                                + "date=" + dateToMillis("2013-10-20 12:00:00") + ", "
                                                + "user=user, "
                                                + "ws=ws, "
                                                + "project=project1, "
                                                + "project_type=projecttype, "
                                                + "project_id=user/ws/project1, "
                                                + "persistent_ws=0, "
                                                + "id=id4_r, "
                                                + "task_type=runner, "
                                                + "start_time=" + dateToMillis("2013-10-20 12:00:00") + ", "
                                                + "is_factory=1, "
                                                + "launch_type=timeout"
                                                + "}");
    }

    @Test
    public void testTestSummaryOfProjectsStatisticsListMetric() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LIST);

        ListValueData summaryValue = (ListValueData)((Summaraziable)metric).getSummaryValue(Context.EMPTY);
        assertEquals(summaryValue.size(), 1);
        Map<String, ValueData> m = ((MapValueData)summaryValue.getAll().get(0)).getAll();
        assertEquals(m.toString(), "{usage_time=1260000, gigabyte_ram_hours=0.1304}");
    }

    @Test
    public void testTasksLaunched() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 12);
    }

    @Test
    public void testExpandedTasksLaunched() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.size(), 12);
        assertEquals(m.get("id1_b").get("id"), StringValueData.valueOf("id1_b"));
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("id4_r").get("id"), StringValueData.valueOf("id4_r"));
        assertEquals(m.get("id2_d").get("id"), StringValueData.valueOf("id2_d"));
        assertEquals(m.get("session2").get("id"), StringValueData.valueOf("session2"));
        assertEquals(m.get("id2_b").get("id"), StringValueData.valueOf("id2_b"));
        assertEquals(m.get("id1_d").get("id"), StringValueData.valueOf("id1_d"));
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("session1").get("id"), StringValueData.valueOf("session1"));
        assertEquals(m.get("id3_r").get("id"), StringValueData.valueOf("id3_r"));
        assertEquals(m.get("id1_r").get("id"), StringValueData.valueOf("id1_r"));
        assertEquals(m.get("id2_r").get("id"), StringValueData.valueOf("id2_r"));
    }

    @Test
    public void testTasksStopped() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 11);
    }

    @Test
    public void testExpandedTasksStopped() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);

        assertEquals(m.size(), 11);
        assertTrue(m.containsKey("id1_b"));
        assertEquals(m.get("id1_b").get("id").getAsString(), "id1_b");

        assertTrue(m.containsKey("id3_b"));
        assertEquals(m.get("id3_d").get("id").getAsString(), "id3_d");

        assertTrue(m.containsKey("id2_d"));
        assertEquals(m.get("id2_d").get("id").getAsString(), "id2_d");

        assertTrue(m.containsKey("session2"));
        assertEquals(m.get("session2").get("id").getAsString(), "session2");

        assertTrue(m.containsKey("id2_b"));
        assertEquals(m.get("id2_b").get("id").getAsString(), "id2_b");

        assertTrue(m.containsKey("id1_d"));
        assertEquals(m.get("id1_d").get("id").getAsString(), "id1_d");

        assertTrue(m.containsKey("id3_b"));
        assertEquals(m.get("id3_b").get("id").getAsString(), "id3_b");

        assertTrue(m.containsKey("session1"));
        assertEquals(m.get("session1").get("id").getAsString(), "session1");

        assertTrue(m.containsKey("id3_r"));
        assertEquals(m.get("id3_r").get("id").getAsString(), "id3_r");

        assertTrue(m.containsKey("id1_r"));
        assertEquals(m.get("id1_r").get("id").getAsString(), "id1_r");

        assertTrue(m.containsKey("id2_r"));
        assertEquals(m.get("id2_r").get("id").getAsString(), "id2_r");
    }

    @Test
    public void testTasksTime() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_TIME);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 1260000);
    }

    @Test
    public void testExpandedTasksTime() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_TIME);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.size(), 12);
        assertEquals(m.get("id1_b").get("id"), StringValueData.valueOf("id1_b"));
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("id4_r").get("id"), StringValueData.valueOf("id4_r"));
        assertEquals(m.get("id2_d").get("id"), StringValueData.valueOf("id2_d"));
        assertEquals(m.get("session2").get("id"), StringValueData.valueOf("session2"));
        assertEquals(m.get("id2_b").get("id"), StringValueData.valueOf("id2_b"));
        assertEquals(m.get("id1_d").get("id"), StringValueData.valueOf("id1_d"));
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("session1").get("id"), StringValueData.valueOf("session1"));
        assertEquals(m.get("id3_r").get("id"), StringValueData.valueOf("id3_r"));
        assertEquals(m.get("id1_r").get("id"), StringValueData.valueOf("id1_r"));
        assertEquals(m.get("id2_r").get("id"), StringValueData.valueOf("id2_r"));
    }

    @Test
    public void testTasksGigabyteRamHours() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_GIGABYTE_RAM_HOURS);
        DoubleValueData d = getAsDouble(metric, Context.EMPTY);
        assertEquals(round(d.getAsDouble() * 10000), 1304);
    }

    @Test
    public void testExpandedTasksGigabyteRamHours() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_GIGABYTE_RAM_HOURS);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.size(), 12);
        assertEquals(m.get("id1_b").get("id"), StringValueData.valueOf("id1_b"));
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("id4_r").get("id"), StringValueData.valueOf("id4_r"));
        assertEquals(m.get("id2_d").get("id"), StringValueData.valueOf("id2_d"));
        assertEquals(m.get("session2").get("id"), StringValueData.valueOf("session2"));
        assertEquals(m.get("id2_b").get("id"), StringValueData.valueOf("id2_b"));
        assertEquals(m.get("id1_d").get("id"), StringValueData.valueOf("id1_d"));
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("session1").get("id"), StringValueData.valueOf("session1"));
        assertEquals(m.get("id3_r").get("id"), StringValueData.valueOf("id3_r"));
        assertEquals(m.get("id1_r").get("id"), StringValueData.valueOf("id1_r"));
        assertEquals(m.get("id2_r").get("id"), StringValueData.valueOf("id2_r"));
    }

    @Test
    public void testTasksLaunchedWithTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_TIMEOUT);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 7);
    }

    @Test
    public void testExpandedTasksLaunchedWithTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_TIMEOUT);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.size(), 7);
        assertEquals(m.get("id1_b").get("id"), StringValueData.valueOf("id1_b"));
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("id1_d").get("id"), StringValueData.valueOf("id1_d"));
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("id3_r").get("id"), StringValueData.valueOf("id3_r"));
        assertEquals(m.get("id1_r").get("id"), StringValueData.valueOf("id1_r"));
        assertEquals(m.get("id4_r").get("id"), StringValueData.valueOf("id4_r"));
    }

    @Test
    public void testTasksLaunchedWithAlwaysOn() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_ALWAYS_ON);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 5);
    }

    @Test
    public void testExpandedTasksLaunchedWithAlwaysOn() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_LAUNCHED_WITH_ALWAYS_ON);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.size(), 5);
        assertEquals(m.get("session1").get("id"), StringValueData.valueOf("session1"));
        assertEquals(m.get("session2").get("id"), StringValueData.valueOf("session2"));
        assertEquals(m.get("id2_d").get("id"), StringValueData.valueOf("id2_d"));
        assertEquals(m.get("id2_b").get("id"), StringValueData.valueOf("id2_b"));
        assertEquals(m.get("id2_r").get("id"), StringValueData.valueOf("id2_r"));
    }

    @Test
    public void testTasksStoppedNormally() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_NORMALLY);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 8);
    }

    @Test
    public void testExpandedTasksStoppedNormally() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_NORMALLY);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.size(), 8);
        assertEquals(m.get("session1").get("id"), StringValueData.valueOf("session1"));
        assertEquals(m.get("session2").get("id"), StringValueData.valueOf("session2"));
        assertEquals(m.get("id2_d").get("id"), StringValueData.valueOf("id2_d"));
        assertEquals(m.get("id2_b").get("id"), StringValueData.valueOf("id2_b"));
        assertEquals(m.get("id2_r").get("id"), StringValueData.valueOf("id2_r"));
        assertEquals(m.get("id1_d").get("id"), StringValueData.valueOf("id1_d"));
        assertEquals(m.get("id1_b").get("id"), StringValueData.valueOf("id1_b"));
        assertEquals(m.get("id1_r").get("id"), StringValueData.valueOf("id1_r"));
    }

    @Test
    public void testTasksStoppedByTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_BY_TIMEOUT);

        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l.getAsLong(), 3);
    }

    @Test
    public void testExpandedStoppedByTimeout() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_STOPPED_BY_TIMEOUT);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.TASK_ID);
        assertEquals(m.size(), 3);
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("id3_b").get("id"), StringValueData.valueOf("id3_b"));
        assertEquals(m.get("id3_r").get("id"), StringValueData.valueOf("id3_r"));
    }

    @Test
    public void testUsersGbHoursList() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_GB_HOURS_LIST);

        ListValueData lvd = getAsList(metric, Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(lvd, AbstractMetric.USER);

        assertEquals(m.size(), 3);
        assertEquals(m.get("user1@gmail.com").get("user"), StringValueData.valueOf("user1@gmail.com"));
        assertEquals(m.get("user1@gmail.com").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.00125));
        assertEquals(m.get("anonymoususer_user11").get("user"), StringValueData.valueOf("anonymoususer_user11"));
        assertEquals(m.get("anonymoususer_user11").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.00125));
        assertEquals(m.get("user").get("user"), StringValueData.valueOf("user"));
        assertEquals(m.get("user").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.1279));
    }

    @Test
    public void testUsersGbHoursListWithFilterByUsers() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_GB_HOURS_LIST);

        Set<String> users = new LinkedHashSet<>();
        users.add("user1@gmail.com");
        users.add("user");

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER_ID, Utils.getFilterAsString(users));

        ListValueData lvd = getAsList(metric, builder.build());

        Map<String, Map<String, ValueData>> m = listToMap(lvd, AbstractMetric.USER);

        assertEquals(m.size(), 2);
        assertEquals(m.get("user1@gmail.com").get("user"), StringValueData.valueOf("user1@gmail.com"));
        assertEquals(m.get("user1@gmail.com").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.00125));
        assertEquals(m.get("user").get("user"), StringValueData.valueOf("user"));
        assertEquals(m.get("user").get("gigabyte_ram_hours"), DoubleValueData.valueOf(0.1279));
    }


    void prepareData() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20131020");
        builder.put(Parameters.TO_DATE, "20131020");
        builder.put(Parameters.LOG, initLogs().getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.TASKS, MetricType.TASKS).getParamsAsMap());
        pigServer.execute(ScriptType.TASKS, builder.build());
    }

    private File initLogs() throws Exception {
        List<Event> events = new ArrayList<>();

        /** FACTORY ACCEPTED EVENTS */
        events.add(createFactoryUrlAcceptedEvent("temp-ws1", "http://1.com?id=factory1", "", "", "", "named", "acceptor").withDate("2013-10-20").withTime("16:00:00").build());
        events.add(createFactoryUrlAcceptedEvent("temp-ws2", "http://1.com?id=factory2", "", "", "", "named", "acceptor").withDate("2013-10-20").withTime("09:00:00").build());

        /** BUILD EVENTS */
        // #1 2min, stopped normally
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "temp-ws2")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_b")
                                      .withParam("TIMEOUT", "600000")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:02:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "temp-ws2")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_b")
                                      .withParam("TIMEOUT", "600000")
                                      .build());

        // #2 1m, stopped normally
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_b")
                                      .withParam("TIMEOUT", "-1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_b")
                                      .withParam("MEMORY", "250")
                                      .withParam("TIMEOUT", "-1")
                                      .build());


        // #3 1m, stopped by timeout
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "build-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_b")
                                      .withParam("TIMEOUT", "50000")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:02:00")
                                      .withParam("EVENT", "build-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_b")
                                      .withParam("TIMEOUT", "50000")
                                      .build());

        /** RUN EVENTS */
        // #1 2min, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600000")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("10:02:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600000")
                                      .build());

        // #2 1m, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:02:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project2")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());


        // #3 1m, stopped by timeout
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "50000")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("11:01:00")
                                      .withParam("EVENT", "run-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project3")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "50000")
                                      .build());

        // #1 2min, non-finished run
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("12:00:00")
                                      .withParam("EVENT", "run-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project1")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id4_r")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600")
                                      .build());

        /** DEBUGS EVENTS */
        // #1 2min, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("13:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600000")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("13:02:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id1_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "600000")
                                      .build());

        // #2 1m, stopped by user
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("14:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("14:01:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id2_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "-1")
                                      .build());

        // #3 1m, stopped by timeout
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("15:00:00")
                                      .withParam("EVENT", "debug-started")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "50000")
                                      .build());
        events.add(new Event.Builder().withDate("2013-10-20")
                                      .withTime("15:02:00")
                                      .withParam("EVENT", "debug-finished")
                                      .withParam("WS", "ws")
                                      .withParam("USER", "user")
                                      .withParam("PROJECT", "project")
                                      .withParam("TYPE", "projectType")
                                      .withParam("ID", "id3_d")
                                      .withParam("MEMORY", "128")
                                      .withParam("LIFETIME", "50000")
                                      .build());

        /** EDIT EVENTS */
        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_user11", "temp-ws1", "session1", true)
                                .withDate("2013-10-20")
                                .withTime("16:00:00")
                                .build());
        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_user11", "temp-ws1", "session1", true)
                                .withDate("2013-10-20")
                                .withTime("16:03:00")
                                .build());

        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "ws1", "session2", false)
                                .withDate("2013-10-20")
                                .withTime("16:20:00")
                                .build());
        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "ws1", "session2", false)
                                .withDate("2013-10-20")
                                .withTime("16:23:00")
                                .build());

        return LogGenerator.generateLog(events);
    }
}
