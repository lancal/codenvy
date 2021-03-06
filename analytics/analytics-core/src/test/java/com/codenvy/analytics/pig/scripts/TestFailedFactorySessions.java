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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** @author Alexander Reshetnyak */
public class TestFailedFactorySessions extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-02-10").withTime("10:00:00,000").build());

        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "tmp-1", "id1", true)
                                .withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "tmp-1", "id1", true)
                                .withDate("2013-02-10").withTime("10:00:00").build());

        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "tmp-2", "id2", true)
                                .withDate("2013-02-10").withTime("10:20:00").build());
        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "tmp-2", "id2", true)
                                .withDate("2013-02-10").withTime("10:20:00").build());

        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_1", "tmp-3", "id3", true)
                                .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_1", "tmp-3", "id3", true)
                                .withDate("2013-02-10").withTime("11:10:00").build());

        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_1", "tmp-4", "id4", true)
                                .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent("anonymoususer_1", "tmp-4", "id4", true)
                                .withDate("2013-02-10").withTime("11:10:00").build());

        events.add(Event.Builder.createFactoryProjectImportedEvent("user1@gmail.com", "tmp-1", "project", "type")
                                .withDate("2013-02-10").withTime("10:05:00").build());

        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-1", "factoryUrl0", "referrer1", "org1", "affiliate1", "named", "acceptor")
                             .withDate("2013-02-10").withTime("11:00:00").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-2", "factoryUrl1", "referrer2", "org2", "affiliate1", "named", "acceptor")
                             .withDate("2013-02-10").withTime("11:00:01").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-3", "factoryUrl1", "referrer2", "org3", "affiliate2", "named", "acceptor")
                             .withDate("2013-02-10").withTime("11:00:02").build());
        events.add(
                Event.Builder.createFactoryUrlAcceptedEvent("tmp-4", "factoryUrl0", "referrer3", "org4", "affiliate2", "named", "acceptor")
                             .withDate("2013-02-10").withTime("11:00:03").build());


        events.add(Event.Builder.createWorkspaceCreatedEvent(TWID1, "tmp-1", "user1@gmail.com")
                                .withDate("2013-02-10").withTime("12:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(TWID2, "tmp-2", "user1@gmail.com")
                                .withDate("2013-02-10").withTime("12:01:00").build());

        // run event for session #1
        events.add(Event.Builder.createRunStartedEvent("user1@gmail.com", "tmp-1", "project", "type", "id", "60000", "128")
                                .withDate("2013-02-10").withTime("10:03:00").build());

        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "tmp-1", "project", "type")
                                .withDate("2013-02-10")
                                .withTime("10:04:00")
                                .build());


        // create user
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "website")
                                .withDate("2013-02-10").build());

        events.add(Event.Builder.createUserChangedNameEvent("anonymoususer_1", "user4@gmail.com").withDate("2013-02-10")
                                .build());

        events.add(Event.Builder.createUserCreatedEvent("user-id2", "user4@gmail.com", "user4@gmail.com").withDate("2013-02-10").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());

        builder.putAll(
                scriptsManager.getScript(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.PRODUCT_USAGE_FACTORY_SESSIONS, builder.build());
    }
}
