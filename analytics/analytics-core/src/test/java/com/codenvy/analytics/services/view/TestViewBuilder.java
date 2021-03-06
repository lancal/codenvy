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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class TestViewBuilder extends BaseTest {

    private static final String           RESOURCE_DIR = BASE_DIR + "/test-classes/" + TestViewBuilder.class.getSimpleName();
    private static final String           VIEW_CONF    = RESOURCE_DIR + "/view.xml";
    private static final String           PASSED_DAYS_VIEW_CONF    = RESOURCE_DIR + "/passed_days_view.xml";
    private static final String           EMPTY_DESCRIPTION_VIEW_CONF = RESOURCE_DIR + "/empty_description_view.xml";
    private static final SimpleDateFormat DIR_FORMAT                  =
        new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");

    private ViewBuilder viewBuilder;

    @BeforeMethod
    public void prepare() throws Exception {
        XmlConfigurationManager configurationManager = mock(XmlConfigurationManager.class);
        when(configurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                XmlConfigurationManager manager = new XmlConfigurationManager();
                return manager.loadConfiguration(DisplayConfiguration.class, VIEW_CONF);
            }
        });

        Configurator configurator = spy(Injector.getInstance(Configurator.class));
        doReturn(new String[]{VIEW_CONF}).when(configurator).getArray(anyString());

        viewBuilder = spy(new ViewBuilder(Injector.getInstance(JdbcDataPersisterFactory.class),
                                          Injector.getInstance(CSVReportPersister.class),
                                          configurationManager,
                                          configurator));
    }

    @Test
    public void testIfShippedConfigurationCorrect() throws Exception {
        ViewBuilder viewBuilder = Injector.getInstance(ViewBuilder.class);

        Context.Builder builder = new Context.Builder();
        builder.putDefaultValue(Parameters.TO_DATE);
        builder.put(Parameters.FROM_DATE, builder.getAsString(Parameters.TO_DATE));

        viewBuilder.doExecute(builder.build());
    }

    @Test
    public void testLastDayPeriod() throws Exception {
        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ViewData> viewData = ArgumentCaptor.forClass(ViewData.class);
        ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);

        viewBuilder.computeDisplayData(Utils.initializeContext(Parameters.TimeUnit.DAY));
        verify(viewBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        ViewData actualData = viewData.getAllValues().get(0);
        for (int i = 1; i < 4; i++) {
            if (viewData.getAllValues().get(i).containsKey("workspaces_day")) {
                actualData = viewData.getAllValues().get(i);
                break;
            }
        }

        assertEquals(actualData.size(), 1);
        assertLastDayData(actualData.values().iterator().next());

        Calendar calendar = Utils.initializeContext(Parameters.TimeUnit.DAY).getAsDate(Parameters.TO_DATE);

        File csvReport = new File("./target/reports/" + DIR_FORMAT.format(calendar.getTime()) + "/view_day.csv");
        assertTrue(csvReport.exists());

        csvReport = new File("./target/reports/" + DIR_FORMAT.format(calendar.getTime()) + "/view_week.csv");
        assertTrue(csvReport.exists());

        csvReport = new File("./target/reports/" + DIR_FORMAT.format(calendar.getTime()) + "/view_month.csv");
        assertTrue(csvReport.exists());

        csvReport = new File("./target/reports/" + DIR_FORMAT.format(calendar.getTime()) + "/view_lifetime.csv");
        assertTrue(csvReport.exists());

        CSVReportPersister csvReportPersister = Injector.getInstance(CSVReportPersister.class);
        csvReportPersister.restoreBackup();
    }

    @Test
    public void testSpecificDayPeriod() throws Exception {
        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ViewData> viewData = ArgumentCaptor.forClass(ViewData.class);
        ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20130930");
        builder.put(Parameters.FROM_DATE, "20130930");

        viewBuilder.computeDisplayData(builder.build());
        verify(viewBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        ViewData actualData = viewData.getAllValues().get(0);
        for (int i = 1; i < 4; i++) {
            if (viewData.getAllValues().get(i).containsKey("workspaces_day")) {
                actualData = viewData.getAllValues().get(i);
            }
        }

        assertEquals(actualData.size(), 1);
        assertSpecificDayData(actualData.values().iterator().next());

        File csvReport = new File("./target/reports/2013/09/30/view_day.csv");
        assertTrue(csvReport.exists());

        new File("./target/reports/2013/09/30/view_week.csv");
        assertTrue(csvReport.exists());

        new File("./target/reports/2013/09/30/view_month.csv");
        assertTrue(csvReport.exists());

        new File("./target/reports/2013/09/30/view_lifetime.csv");
        assertTrue(csvReport.exists());
    }

    @Test
    public void testSpecificPassedDaysCount() throws Exception {
        XmlConfigurationManager configurationManager = mock(XmlConfigurationManager.class);
        when(configurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                XmlConfigurationManager manager = new XmlConfigurationManager();
                return manager.loadConfiguration(DisplayConfiguration.class, PASSED_DAYS_VIEW_CONF);
            }
        });

        Configurator configurator = spy(Injector.getInstance(Configurator.class));
        doReturn(new String[]{PASSED_DAYS_VIEW_CONF}).when(configurator).getArray(anyString());

        viewBuilder = spy(new ViewBuilder(Injector.getInstance(JdbcDataPersisterFactory.class),
                                          Injector.getInstance(CSVReportPersister.class),
                                          configurationManager,
                                          configurator));

        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ViewData> viewData = ArgumentCaptor.forClass(ViewData.class);
        ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TO_DATE, "20130930");
        builder.put(Parameters.FROM_DATE, "20130930");

        viewBuilder.computeDisplayData(builder.build());
        verify(viewBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        File csvReport = new File("./target/reports/2013/09/30/passed_days_view_by_1_day.csv");
        assertTrue(csvReport.exists());

        new File("./target/reports/2013/09/30/passed_days_view_by_lifetime.csv");
        assertTrue(csvReport.exists());
    }
    
    @Test
    public void testQueryViewData() throws Exception {
        Context context = Utils.initializeContext(Parameters.TimeUnit.DAY);
        viewBuilder.computeDisplayData(context);

        ViewData actualData = viewBuilder.getViewData("view", context);

        assertEquals(actualData.size(), 1);
        assertLastDayData(actualData.values().iterator().next());
    }

    @Test
    public void testCustomDateRangeWithDayUnit() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());
        builder.put(Parameters.FROM_DATE, "20130928");
        builder.put(Parameters.TO_DATE, "20130930");
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");

        viewBuilder.computeDisplayData(builder.build());

        ViewData actualData = viewBuilder.getViewData("view", builder.build());

        assertEquals(actualData.size(), 1);

        List<List<ValueData>> data = actualData.values().iterator().next();

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(4, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));
        assertEquals("30 Sep", dateRow.get(1).getAsString());
        assertEquals("29 Sep", dateRow.get(2).getAsString());
        assertEquals("28 Sep", dateRow.get(3).getAsString());

        List<ValueData> metricRow = data.get(1);
        assertEquals(4, metricRow.size());
        assertEquals(new StringValueData("Created Workspaces"), metricRow.get(0));
        assertEquals(new StringValueData("5"), metricRow.get(1));
        assertEquals(new StringValueData("5"), metricRow.get(2));
        assertEquals(new StringValueData("5"), metricRow.get(3));

        List<ValueData> emptyRow = data.get(2);
        assertEquals(4, emptyRow.size());
        assertEquals(StringValueData.DEFAULT, emptyRow.get(0));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(1));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(2));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(3));
    }

    @Test
    public void testSameWeekCustomDateRangeWithWeekUnit() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.FROM_DATE, "20131230");
        builder.put(Parameters.TO_DATE, "20140102");      // same week
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");

        viewBuilder.computeDisplayData(builder.build());

        ViewData actualData = viewBuilder.getViewData("view", builder.build());

        assertEquals(actualData.size(), 1);

        List<List<ValueData>> data = actualData.values().iterator().next();

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(2, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));
        assertEquals("02 Jan", dateRow.get(1).getAsString());

        List<ValueData> metricRow = data.get(1);
        assertEquals(2, metricRow.size());
        assertEquals(new StringValueData("Created Workspaces"), metricRow.get(0));
        assertEquals(new StringValueData("5"), metricRow.get(1));

        List<ValueData> emptyRow = data.get(2);
        assertEquals(2, emptyRow.size());
        assertEquals(StringValueData.DEFAULT, emptyRow.get(0));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(1));
    }

    @Test
    public void testDifferentWeekCustomDateRangeWithWeekUnit() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.WEEK.toString());
        builder.put(Parameters.FROM_DATE, "20131227");
        builder.put(Parameters.TO_DATE, "20140102");    // different week
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");

        viewBuilder.computeDisplayData(builder.build());

        ViewData actualData = viewBuilder.getViewData("view", builder.build());

        assertEquals(1, actualData.size());

        List<List<ValueData>> data = actualData.values().iterator().next();

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(3, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));
        assertEquals("02 Jan", dateRow.get(1).getAsString());
        assertEquals("28 Dec", dateRow.get(2).getAsString());

        List<ValueData> metricRow = data.get(1);
        assertEquals(3, metricRow.size());
        assertEquals(new StringValueData("Created Workspaces"), metricRow.get(0));
        assertEquals(new StringValueData("5"), metricRow.get(1));
        assertEquals(new StringValueData("5"), metricRow.get(2));

        List<ValueData> emptyRow = data.get(2);
        assertEquals(3, emptyRow.size());
        assertEquals(StringValueData.DEFAULT, emptyRow.get(0));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(1));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(2));
    }

    @Test
    public void testSameMonthCustomDateRangeWithMonthUnit() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());
        builder.put(Parameters.FROM_DATE, "20140106");
        builder.put(Parameters.TO_DATE, "20140126");   // same month
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");

        viewBuilder.computeDisplayData(builder.build());

        ViewData actualData = viewBuilder.getViewData("view", builder.build());

        assertEquals(1, actualData.size());

        List<List<ValueData>> data = actualData.values().iterator().next();

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(2, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));
        assertEquals("Jan 2014", dateRow.get(1).getAsString());

        List<ValueData> metricRow = data.get(1);
        assertEquals(2, metricRow.size());
        assertEquals(new StringValueData("Created Workspaces"), metricRow.get(0));
        assertEquals(new StringValueData("5"), metricRow.get(1));

        List<ValueData> emptyRow = data.get(2);
        assertEquals(2, emptyRow.size());
        assertEquals(StringValueData.DEFAULT, emptyRow.get(0));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(1));
    }

    @Test
    public void testDifferentMonthCustomDateRangeWithMonthUnit() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.MONTH.toString());
        builder.put(Parameters.FROM_DATE, "20131227");
        builder.put(Parameters.TO_DATE, "20140102");    // different month
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");

        viewBuilder.computeDisplayData(builder.build());

        ViewData actualData = viewBuilder.getViewData("view", builder.build());

        assertEquals(actualData.size(), 1);

        List<List<ValueData>> data = actualData.values().iterator().next();

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(3, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));
        assertEquals("Jan 2014", dateRow.get(1).getAsString());
        assertEquals("Dec 2013", dateRow.get(2).getAsString());

        List<ValueData> metricRow = data.get(1);
        assertEquals(3, metricRow.size());
        assertEquals(new StringValueData("Created Workspaces"), metricRow.get(0));
        assertEquals(new StringValueData("5"), metricRow.get(1));
        assertEquals(new StringValueData("5"), metricRow.get(2));

        List<ValueData> emptyRow = data.get(2);
        assertEquals(3, emptyRow.size());
        assertEquals(StringValueData.DEFAULT, emptyRow.get(0));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(1));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(2));
    }

    @Test
    public void testMaxRowsCountConstraint() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());
        builder.put(Parameters.FROM_DATE, "20101227");
        builder.put(Parameters.TO_DATE, "20140102");    // more then ViewBuilder.MAX_ROWS
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");

        viewBuilder.computeDisplayData(builder.build());

        ViewData actualData = viewBuilder.getViewData("view", builder.build());

        assertEquals(1, actualData.size());

        List<List<ValueData>> data = actualData.values().iterator().next();

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(ViewBuilder.MAX_ROWS, dateRow.size());
    }

    @Test
    public void testViewWithEmptyDescription() throws Exception {
        XmlConfigurationManager configurationManager = mock(XmlConfigurationManager.class);
        when(configurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                XmlConfigurationManager manager = new XmlConfigurationManager();
                return manager.loadConfiguration(DisplayConfiguration.class, EMPTY_DESCRIPTION_VIEW_CONF);
            }
        });

        Configurator configurator = spy(Injector.getInstance(Configurator.class));
        doReturn(new String[]{EMPTY_DESCRIPTION_VIEW_CONF}).when(configurator).getArray(anyString());

        viewBuilder = spy(new ViewBuilder(Injector.getInstance(JdbcDataPersisterFactory.class),
                                          Injector.getInstance(CSVReportPersister.class),
                                          configurationManager,
                                          configurator));

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.TIME_UNIT, Parameters.TimeUnit.DAY.toString());
        builder.put(Parameters.FROM_DATE, "20140120");
        builder.put(Parameters.TO_DATE, "20140120");
        builder.put(Parameters.IS_CUSTOM_DATE_RANGE, "");

        viewBuilder.computeDisplayData(builder.build());
        ViewData actualData = viewBuilder.getViewData("key_feature_usage_view", builder.build());

        assertEquals(actualData.size(), 1);

        List<List<ValueData>> data = actualData.values().iterator().next();

        assertEquals(2, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(2, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));
        assertEquals("20 Jan", dateRow.get(1).getAsString());

        List<ValueData> metricRow = data.get(1);
        assertEquals(2, metricRow.size());
        assertEquals(new StringValueData("Metric Description"), metricRow.get(0));
        assertEquals(new StringValueData("5"), metricRow.get(1));
    }

    private void assertSpecificDayData(List<List<ValueData>> data) {
        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(3, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));
        assertTrue(dateRow.get(1).getAsString().contains("30"));
        assertTrue(dateRow.get(2).getAsString().contains("29"));

        List<ValueData> metricRow = data.get(1);
        assertEquals(3, metricRow.size());
        assertEquals(new StringValueData("Created Workspaces"), metricRow.get(0));
        assertEquals(new StringValueData("5"), metricRow.get(1));
        assertEquals(new StringValueData("5"), metricRow.get(2));

        List<ValueData> emptyRow = data.get(2);
        assertEquals(3, emptyRow.size());
        assertEquals(StringValueData.DEFAULT, emptyRow.get(0));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(1));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(2));
    }

    private void assertLastDayData(List<List<ValueData>> data) {
        Calendar day1 = Calendar.getInstance();
        day1.add(Calendar.DAY_OF_MONTH, -1);

        Calendar day2 = Calendar.getInstance();
        day2.add(Calendar.DAY_OF_MONTH, -2);

        assertEquals(3, data.size());

        List<ValueData> dateRow = data.get(0);
        assertEquals(3, dateRow.size());
        assertEquals(new StringValueData("desc"), dateRow.get(0));

        assertTrue(dateRow.get(1).getAsString().contains("" + day1.get(Calendar.DAY_OF_MONTH)));
        assertTrue(dateRow.get(2).getAsString().contains("" + day2.get(Calendar.DAY_OF_MONTH)));

        List<ValueData> metricRow = data.get(1);
        assertEquals(3, metricRow.size());
        assertEquals(new StringValueData("Created Workspaces"), metricRow.get(0));
        assertEquals(new StringValueData("10"), metricRow.get(1));
        assertEquals(new StringValueData("5"), metricRow.get(2));

        List<ValueData> emptyRow = data.get(2);
        assertEquals(3, emptyRow.size());
        assertEquals(StringValueData.DEFAULT, emptyRow.get(0));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(1));
        assertEquals(StringValueData.DEFAULT, emptyRow.get(2));
    }

    // -------------------> Tested Metrics

    public static class TestedMetricRow extends MetricRow {

        public TestedMetricRow(Map<String, String> parameters) {
            super(parameters);
        }

        @Override
        protected ValueData getMetricValue(Context context) throws IOException {
            if (context.getAsString(Parameters.TO_DATE).equals(Parameters.TO_DATE.getDefaultValue())) {
                return new StringValueData("10");
            } else {
                return new StringValueData("5");
            }
        }

        @Override protected String getMetricDescription() {
            return "Metric Description";
        }
    }

}
