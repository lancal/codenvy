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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TopFactorySessions extends AbstractTopMetrics {
    public TopFactorySessions() {
        super(MetricType.TOP_FACTORY_SESSIONS);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{TIME,
                            SESSION_ID,
                            FACTORY,
                            REFERRER,
                            AUTHENTICATED_SESSION,
                            CONVERTED_SESSION};
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject[] dbOperations = new DBObject[4];

        dbOperations[0] = new BasicDBObject("$match", new BasicDBObject(FACTORY, new BasicDBObject("$nin", new Object[]{"", null})));
        dbOperations[1] = new BasicDBObject("$sort", new BasicDBObject(TIME, -1));
        dbOperations[2] = new BasicDBObject("$limit", MAX_DOCUMENT_COUNT);
        dbOperations[3] = new BasicDBObject("$project", new BasicDBObject(TIME, 1)
                .append(SESSION_ID, 1)
                .append(USER_CREATED, 1)
                .append(FACTORY, 1)
                .append(REFERRER, 1)
                .append(AUTHENTICATED_SESSION, "$" + REGISTERED_USER)
                .append(CONVERTED_SESSION, 1));

        return dbOperations;
    }


    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS);
    }


    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The top factory sessions sorted by duration of session in period of time during last days";
    }
}
