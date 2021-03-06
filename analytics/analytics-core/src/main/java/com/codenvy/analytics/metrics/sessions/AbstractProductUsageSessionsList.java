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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Anatoliy Bazko */
public abstract class AbstractProductUsageSessionsList extends AbstractListValueResulted {

    public static final String EMPTY_SESSION_MESSAGE = "User Did Not Enter Workspace";

    public AbstractProductUsageSessionsList(MetricType metricType) {
        super(metricType);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
                            USER,
                            USER_COMPANY,
                            DOMAIN,
                            TIME,
                            SESSION_ID,
                            DATE,
                            END_TIME,
                            LOGOUT_INTERVAL};
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_SESSIONS);
    }

    @Override
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        List<ValueData> list2Return = new ArrayList<>();

        for (ValueData items : ((ListValueData)valueData).getAll()) {

            MapValueData prevItems = (MapValueData)items;
            Map<String, ValueData> items2Return = new HashMap<>(prevItems.getAll());

            long delta = ValueDataUtil.treatAsLong(items2Return.get(TIME));

            // replace empty session_id field on explanation message
            if (items2Return.get(SESSION_ID).getAsString().isEmpty() && delta == 0) {
                items2Return.put(SESSION_ID, StringValueData.valueOf(EMPTY_SESSION_MESSAGE));
            }

            list2Return.add(new MapValueData(items2Return));
        }

        return new ListValueData(list2Return);
    }

    @Override
    public String getDescription() {
        return "Users' sessions";
    }
}
