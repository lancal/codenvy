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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@OmitFilters({MetricFilter.USER_ID, MetricFilter.REGISTERED_USER})
public class FactoriesAcceptedList extends AbstractListValueResulted {

    public FactoriesAcceptedList() {
        super(MetricType.FACTORIES_ACCEPTED_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.FACTORIES_ACCEPTED);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
                            FACTORY,
                            FACTORY_ID,
                            ORG_ID,
                            AFFILIATE_ID,
                            REFERRER};
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The list of accepted factories";
    }
}
