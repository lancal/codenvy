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

import com.codenvy.analytics.metrics.MetricType;

/** @author Anatoliy Bazko */
public class ProductUsageConditionBelow120Min extends AbstractProductUsageCondition {

    public ProductUsageConditionBelow120Min() {
        super(MetricType.PRODUCT_USAGE_CONDITION_BELOW_120_MIN,
              0,
              120 * 60 * 1000,
              true,
              false,
              "$or",
              0,
              5,
              true,
              false);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of users who have the number of sessions less than 5 and usage time less than 120 minutes";
    }
}
