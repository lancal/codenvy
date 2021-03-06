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
package com.codenvy.analytics.services;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Reshetnyak
 */
public abstract class AbstractUsersActivityTest extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        computeStatistics("20131101");
        computeStatistics("20131102");
    }

    protected void computeStatistics(String date) throws Exception {
        executeScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST, date);
        executeScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST, date);
        executeScript(ScriptType.USERS_ACCOUNTS, MetricType.USERS_ACCOUNTS_LIST, date);
        executeScript(ScriptType.EVENTS, MetricType.CREDIT_CARD_ADDED, date);
        executeScript(ScriptType.EVENTS, MetricType.CREDIT_CARD_REMOVED, date);
        executeScript(ScriptType.EVENTS, MetricType.ACCOUNT_LOCKED, date);
        executeScript(ScriptType.EVENTS, MetricType.ACCOUNT_UNLOCKED, date);
        executeScript(ScriptType.EVENTS, MetricType.SUBSCRIPTION_ADDED, date);
        executeScript(ScriptType.EVENTS, MetricType.SUBSCRIPTION_REMOVED, date);
        executeScript(ScriptType.PRODUCT_USAGE_SESSIONS, MetricType.PRODUCT_USAGE_SESSIONS_LIST, date);
        executeScript(ScriptType.USERS_STATISTICS, MetricType.USERS_STATISTICS_LIST, date);
        executeScript(ScriptType.USERS_ACTIVITY, MetricType.USERS_ACTIVITY_LIST, date);
        executeScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_USERS_SET, date);
        executeScript(ScriptType.ACTIVE_ENTITIES, MetricType.ACTIVE_WORKSPACES_SET, date);
        executeScript(ScriptType.EVENTS_BY_TYPE, MetricType.USERS_LOGGED_IN_TYPES, date);
        executeScript(ScriptType.TASKS, MetricType.TASKS_LIST, date);
        executeScript(ScriptType.CREATED_USERS, MetricType.CREATED_USERS_SET, date);
        doIntegrity(date);
    }

    private void executeScript(ScriptType scriptType, MetricType metricType, String date) throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.putAll(scriptsManager.getScript(scriptType, metricType).getParamsAsMap());
        builder.put(Parameters.FROM_DATE, date);
        builder.put(Parameters.TO_DATE, date);
        builder.put(Parameters.LOG, prepareLog().getAbsolutePath());
        pigServer.execute(scriptType, builder.build());
    }

    protected abstract Map<String, String> getHeaders();

    protected Map<String, Map<String, String>> read(File jobFile) throws IOException {
        Map<String, Map<String, String>> results = new HashMap<>();

        List<String> columns = new ArrayList<>();
        for (String header : getHeaders().values()) {
            columns.add(header);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(jobFile))) {
            String line;
            boolean isHeaderRead = false;
            while ((line = reader.readLine()) != null) {
                String[] userDataArray;
                if (!isHeaderRead) {
                    line = line.replace("\"", "");  // remove all '"'
                    userDataArray = line.split(",");
                    isHeaderRead = true;
                } else {
                    userDataArray = line.split(",");

                    for (int i=0; i<userDataArray.length; i++) {
                        userDataArray[i] = userDataArray[i].replace("\"", "");  // remove all '"'
                    }
                }

                // put line values into map
                Map<String, String> userDataMap = new HashMap<>();
                for (int i = 0; i < userDataArray.length; i++) {
                    userDataMap.put(columns.get(i), userDataArray[i]);
                }

                if (userDataArray[0].equals(getHeaders().get(AbstractMetric.ID))) {
                    results.put("_HEAD", userDataMap);
                } else {
                    results.put(userDataArray[0], userDataMap);
                }

            }
        }

        return results;
    }

    private File prepareLog() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("id1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("id2", "user2@gmail.com", "user2@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("id3", "user3@gmail.com", "user3@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("id5", "user5@gmail.com", "user5@gmail.com")
                                .withDate("2013-11-01").withTime("10:00:00,000").build());

        events.add(Event.Builder.createAccountAddMemberEvent("acid1", "id1", Arrays.asList("account/owner"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid1", "id1", Arrays.asList("account/member"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid2", "id2", Arrays.asList("account/owner"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid2", "id2", Arrays.asList("account/member"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid3", "id3", Arrays.asList("account/owner"))
                                .withDate("2013-11-01", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid3", "id3", Arrays.asList("account/member"))
                                .withDate("2013-11-01", "10:00:00").build());

        events.add(Event.Builder.createCreditCardAddedEvent("id2", "acid2")
                                .withDate("2013-11-01", "11:00:00").build());
        events.add(Event.Builder.createCreditCardAddedEvent("id3", "acid3")
                                .withDate("2013-11-01", "11:00:00").build());

        events.add(Event.Builder.createSubscriptionAddedEvent("acid2", "OnPremises", "opm-com-25u-y")
                                .withDate("2013-11-01", "11:15:01").build());
        events.add(Event.Builder.createSubscriptionAddedEvent("acid3", "OnPremises", "opm-com-25u-y")
                                .withDate("2013-11-01", "11:15:03").build());

        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                                .withDate("2013-11-01").withTime("10:10:20").build());
        events.add(Event.Builder.createUserSSOLoggedInEvent("user2@gmail.com", "google")
                                .withDate("2013-11-01").withTime("10:10:30").build());

        events.add(Event.Builder.createUserUpdateProfile("id1", "user1@gmail.com", "user1@gmail.com", "f", "l", "company", "phone", "jobtitle")
                                .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("id2", "user2@gmail.com", "user2@gmail.com", "", "", "", "", "")
                                .withDate("2013-11-01").build());
        events.add(Event.Builder.createUserUpdateProfile("id3", "user3@gmail.com", "user3@gmail.com", "", "", "", "", "")
                                .withDate("2013-11-01").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "ws1", "user1@gmail.com")
                                .withDate("2013-11-01").withTime("08:59:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid2", "ws2", "user2@gmail.com")
                                .withDate("2013-11-01").withTime("08:59:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid3", "ws3", "user3@gmail.com")
                                .withDate("2013-11-01").withTime("08:59:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid4", "ws2___", "user3@gmail.com")
                                .withDate("2013-11-01").withTime("08:59:00").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid5", "ws5", "user5@gmail.com")
                                .withDate("2013-11-01").withTime("09:00:00").build());
        events.add(Event.Builder.createProjectCreatedEvent("user5@gmail.com", "ws5", "project1", "type1")
                             .withDate("2013-11-01").withTime("10:05:00").build());

        // active users [user1, user2, user3]
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID1, "ws1", "user1@gmail.com").withTime("09:00:00").withDate("2013-11-01")
                                .build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID1, "ws2", "user2@gmail.com").withTime("09:00:00").withDate("2013-11-01")
                                .build());
        events.add(Event.Builder.createWorkspaceCreatedEvent(WID1, "ws3", "user3@gmail.com").withTime("09:00:00").withDate("2013-11-01")
                                .build());

        // projects created
        events.add(
                Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project1", "type1").withDate("2013-11-01")
                             .withTime("10:00:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user1@gmail.com", "ws1", "project2", "type1").withDate("2013-11-01")
                             .withTime("10:05:00").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("user2@gmail.com", "ws2", "project1", "type1").withDate("2013-11-01")
                             .withTime("10:03:00").build());

        // projects built
        events.add(Event.Builder.createBuildStartedEvent("user2@gmail.com", "ws1", "project1", "type1", "", "60000").withTime("10:06:00")
                                .withDate("2013-11-01").build());


        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project1", "type1", "paas1")
                                .withTime("10:10:00,000")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user3@gmail.com", "ws2", "project1", "type1", "paas2")
                                .withTime("10:00:00")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project2", "type1", "paas1")
                                .withTime("10:11:00,100")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project3", "type1", "paas1")
                                .withTime("10:12:00,200")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project4", "type1", "paas1")
                                .withTime("10:13:00,300")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project5", "type1", "paas1")
                                .withTime("10:14:00,400")
                                .withDate("2013-11-02").build());

        events.add(Event.Builder.createApplicationCreatedEvent("user2@gmail.com", "ws2", "project1", "type1", "paas1")
                                .withTime("10:15:00,500")
                                .withDate("2013-11-02").build());


        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "ws1", "1", false).withDate("2013-11-02").withTime("19:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent("user1@gmail.com", "ws1", "1", false).withDate("2013-11-02").withTime("19:05:00").build());
        events.add(Event.Builder.createSessionUsageEvent("user2@gmail.com", "ws1", "2", false).withDate("2013-11-02").withTime("20:00:00").build());
        events.add(Event.Builder.createSessionUsageEvent("user2@gmail.com", "ws1", "2", false).withDate("2013-11-02").withTime("20:10:00").build());

        events.add(Event.Builder.createAccountAddMemberEvent("acid2", "id1", Arrays.asList("account/member"))
                                .withDate("2013-11-02", "10:00:00").build());
        events.add(Event.Builder.createAccountAddMemberEvent("acid1", "id2", Arrays.asList("account/member"))
                                .withDate("2013-11-02", "10:00:00").build());

        events.add(Event.Builder.createLockedAccountEvent("acid2")
                                .withDate("2013-11-02", "11:00:00").build());

        events.add(Event.Builder.createSubscriptionRemovedEvent("acid2", "OnPremises", "pay-as-you-go")
                                .withDate("2013-11-02", "11:15:07").build());

        events.add(Event.Builder.createUserCreatedEvent("id6", "user5@gmail.com", "user5@gmail.com")
                                .withDate("2013-11-02").withTime("10:00:00,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid6", "ws6", "user5@gmail.com")
                                .withDate("2013-11-02").withTime("09:00:00").build());
        events.add(Event.Builder.createProjectCreatedEvent("user5@gmail.com", "ws6", "project1", "type1")
                                .withDate("2013-11-02").withTime("10:55:00").build());

        events.add(Event.Builder.createFactoryCreatedEvent("user1@gmail.com", "ws1", "", "", "", "", "", "")
                                .withDate("2013-11-01")
                                .withTime("20:03:00").build());

        events.add(Event.Builder.createDebugStartedEvent("user2@gmail.com", "ws1", "", "", "id1", "60000", "128")
                                .withDate("2013-11-01")
                                .withTime("20:06:00").build());

        events.add(Event.Builder.createUserInviteEvent("user1@gmail.com", "ws2", "email")
                                .withDate("2013-11-01").build());

        events.add(Event.Builder.createRunStartedEvent("user2@gmail.com", "ws2", "project", "type", "id1","60000", "128")
                                .withDate("2013-11-01").withTime("20:59:00").build());
        events.add(Event.Builder.createRunFinishedEvent("user2@gmail.com", "ws2", "project", "type", "id1","60000", "128")
                                .withDate("2013-11-01").withTime("21:01:00").build());

        events.add(Event.Builder.createBuildStartedEvent("user1@gmail.com", "ws1", "project", "type", "id2", "60000")
                                .withDate("2013-11-01").withTime("21:12:00").build());
        events.add(Event.Builder.createBuildFinishedEvent("user1@gmail.com", "ws1", "project", "type", "id2", "60000")
                                .withDate("2013-11-01").withTime("21:14:00").build());

        // projects deployed
        events.add(Event.Builder.createApplicationCreatedEvent("user3@gmail.com", "ws2___", "project1", "type1", "paas2")
                                .withTime("10:00:01")
                                .withDate("2013-11-03").build());

        events.add(Event.Builder.createAccountRemoveMemberEvent("acid2", "uid1")
                                .withDate("2013-01-03", "10:00:00").build());

        events.add(Event.Builder.createUserCreatedEvent("id4", "user4@gmail.com", "user4@gmail.com")
                                .withDate("2013-11-03").withTime("10:00:00,000").build());

        events.add(Event.Builder.createProjectCreatedEvent("user5@gmail.com", "ws6", "project2", "type1")
                                .withDate("2013-11-03").withTime("11:00:00").build());

        return LogGenerator.generateLog(events);
    }
}
