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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.metrics.sessions.factory.ProductUsageFactorySessionsList;
import com.codenvy.analytics.metrics.top.AbstractTopMetrics;
import com.codenvy.analytics.metrics.top.TopFactories;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TestFactoryUriComponentsDecode extends BaseTest {

    private static final String ENCODE_URI_COMPONENTS =
        "https%3A%2F%2Fcodenvy.com%2Ffactory%2F%3Fv%3D1" +
        ".0%26pname%3DSample-Angul%0AarJS%26wname%3Dcodenvy-factories%26vcs%3Dgit%26vcsurl%3Dhttp%3A%2F%2Fcodenvy" +
        ".com%2Fgit%2F04%2F0f%2F7f%2Fworkspacegcpv6cdxy1q34n1i%2FSample-AngularJS%26idcommit" +
        "%3D37a21ef422e7995cbab615431f0f63991a9b314a%26ptype%3DJavaScript%26welcome%3D%7B%0A%22authenticated%22" +
        "%3A%20%7B%0A%22title%22%3A%20%22TodoMVC%20-%20AngularJS%20Implementation%22%2C%0A%22iconurl%22%3A%20" +
        "%22https%3A%2F%2Fdl.dropboxusercontent" +
        ".com%2Fu%2F2187905%2FCodenvy%2FSampleCustomizedWelcomeMessage%2Fangularjs-icon" +
        ".png%22%2C%0A%22contenturl%22%3A%20%22https%3A%2F%2Fdl.dropboxusercontent" +
        ".com%2Fu%2F2187905%2FCodenvy%2FSampleCustomizedWelcomeMessage%2Fangular_welcome_message_authenticated" +
        ".html%22%0A%7D%2C%0A%22nonauthenticated%22%3A%20%7B%0A%22title%22%3A%20%22TodoMVC%20-%20AngularJS" +
        "%20Implementation%22%2C%0A%22iconurl%22%3A%20%22https%3A%2F%2Fdl.dropboxusercontent" +
        ".com%2Fu%2F2187905%2FCodenvy%2FSampleCustomizedWelcomeMessage%2Fangularjs-icon" +
        ".png%22%2C%0A%22contenturl%22%3A%20%22https%3A%2F%2Fdl.dropboxusercontent" +
        ".com%2Fu%2F2187905%2FCodenvy%2FSampleCustomizedWelcomeMessage%2Fangular_welcome_message_not" +
        "-authenticated.html%22%0A%7D";

    // 'ptype=' param is removed
    // 'factory/?v=' replaced with 'factory?v='
    private static final String DECODE_URI_COMPONENTS
        = "https://codenvy.com/factory?v=1.0&pname=Sample-Angul\n" +
          "arJS&wname=codenvy-factories&vcs=git&vcsurl=http://codenvy" +
          ".com/git/04/0f/7f/workspacegcpv6cdxy1q34n1i/Sample-AngularJS&idcommit=37a21ef422e7995cbab615431f0f63991a9b314a&welcome={\n" +
          "\"authenticated\": {\n" +
          "\"title\": \"TodoMVC - AngularJS Implementation\",\n" +
          "\"iconurl\": \"https://dl.dropboxusercontent" +
          ".com/u/2187905/Codenvy/SampleCustomizedWelcomeMessage/angularjs-icon" +
          ".png\",\n" +
          "\"contenturl\": \"https://dl.dropboxusercontent" +
          ".com/u/2187905/Codenvy/SampleCustomizedWelcomeMessage/angular_welcome_message_authenticated.html\"\n" +
          "},\n" +
          "\"nonauthenticated\": {\n" +
          "\"title\": \"TodoMVC - AngularJS Implementation\",\n" +
          "\"iconurl\": \"https://dl.dropboxusercontent" +
          ".com/u/2187905/Codenvy/SampleCustomizedWelcomeMessage/angularjs-icon" +
          ".png\",\n" +
          "\"contenturl\": \"https://dl.dropboxusercontent" +
          ".com/u/2187905/Codenvy/SampleCustomizedWelcomeMessage/angular_welcome_message_not-authenticated.html\"\n" +
          "}";

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();

        // broken event, factory url contains new line character
        events.add(Event.Builder.createWorkspaceCreatedEvent(TWID4, "tmp-4", "anonymoususer_2")
                                .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", ENCODE_URI_COMPONENTS, "referrer2", "org3", "affiliate2", "named", "acceptor")
                                .withDate("2013-02-10").withTime("11:00:00").build());

        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_2", "tmp-4", "id4", true)
                                .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_2", "tmp-4", "id4", true)
                                .withDate("2013-02-10").withTime("11:15:00").build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(
            scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST)
                          .getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
    }

    @Test
    public void testAbstractTopFactoriesWithDecodedURIComponents() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.PASSED_DAYS_COUNT, Parameters.PassedDaysCount.BY_LIFETIME.toString());

        AbstractTopMetrics metric = new TestTopFactories();

        ListValueData value = (ListValueData)metric.getValue(builder.build());

        assertEquals(value.size(), 1);
        checkTopFactoriesDataItem((MapValueData)value.getAll().get(0),
                                  DECODE_URI_COMPONENTS,
                                  "1",
                                  "0",
                                  "900000",
                                  "0.0",
                                  "0.0",
                                  "0.0",
                                  "100.0",
                                  "0.0",
                                  "100.0",
                                  "0.0"
        );
    }

    private void checkTopFactoriesDataItem(MapValueData item,
                                           String factory,
                                           String wsCreated,
                                           String userCreated,
                                           String time,
                                           String buildRate,
                                           String runRate,
                                           String deployRate,
                                           String anonymousFactorySessionRate,
                                           String authenticatedFactorySessionRate,
                                           String abandonFactorySessionRate,
                                           String convertedFactorySessionRate) {

        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.FACTORY).getAsString(), factory);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.WS_CREATED).getAsString(), wsCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.USER_CREATED).getAsString(), userCreated);
        assertEquals(item.getAll().get(ProductUsageFactorySessionsList.TIME).getAsString(), time);
        assertEquals(item.getAll().get(TopFactories.BUILD_RATE).getAsString(), buildRate);
        assertEquals(item.getAll().get(TopFactories.RUN_RATE).getAsString(), runRate);
        assertEquals(item.getAll().get(TopFactories.DEPLOY_RATE).getAsString(), deployRate);
        assertEquals(item.getAll().get(TopFactories.ANONYMOUS_FACTORY_SESSION_RATE).getAsString(),
                     anonymousFactorySessionRate);
        assertEquals(item.getAll().get(TopFactories.AUTHENTICATED_FACTORY_SESSION_RATE).getAsString(),
                     authenticatedFactorySessionRate);
        assertEquals(item.getAll().get(TopFactories.ABANDON_FACTORY_SESSION_RATE).getAsString(),
                     abandonFactorySessionRate);
        assertEquals(item.getAll().get(TopFactories.CONVERTED_FACTORY_SESSION_RATE).getAsString(),
                     convertedFactorySessionRate);
    }

    // ------------------------> Tested Metrics

    private class TestTopFactories extends TopFactories {
        @Override
        public String getDescription() {
            return null;
        }
    }
}
