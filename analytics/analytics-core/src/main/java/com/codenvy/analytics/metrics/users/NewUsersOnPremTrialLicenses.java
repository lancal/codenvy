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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.MetricType;

/** @author Anatoliy Bazko */
@InternalMetric
public class NewUsersOnPremTrialLicenses extends AbstractNewUsersAnalysis {
    public NewUsersOnPremTrialLicenses() {
        super(MetricType.NEW_USERS_ON_PREM_TRIAL_LICENSES, MetricType.IM_ON_PREM_ISSUED);
    }

    /** {@inheritDoc} */
    @Override
    protected void setSpecificFilter(Context.Builder builder) {
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of On-Prem trail licenses issued for new users";
    }
}


