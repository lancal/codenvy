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
package com.codenvy.analytics.metrics.workspaces;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsageTimeByWorkspacesList extends AbstractListValueResulted {

    public static final String SESSIONS = "sessions";

    public UsageTimeByWorkspacesList() {
        super(MetricType.USAGE_TIME_BY_WORKSPACES_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_SESSIONS);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{TIME, SESSIONS, WS};
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + WS);
        group.put(TIME, new BasicDBObject("$sum", "$" + TIME));
        group.put(SESSIONS, new BasicDBObject("$sum", 1));

        DBObject project = new BasicDBObject();
        project.put(WS, "$" + ID);
        project.put(TIME, "$" + TIME);
        project.put(SESSIONS, "$" + SESSIONS);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "How much time every user has spent in workspaces";
    }
}
