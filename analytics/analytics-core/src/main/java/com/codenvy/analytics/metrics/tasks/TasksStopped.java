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
package com.codenvy.analytics.metrics.tasks;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.BasicDBObject;

import java.io.IOException;

/** @author Dmytro Nochevnov */
public class TasksStopped extends Tasks {
    public TasksStopped() {
        this(MetricType.TASKS_STOPPED);
    }

    public TasksStopped(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritDoc} */
    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(super.applySpecificFilter(context));
        builder.put(MetricFilter.STOP_TIME, new BasicDBObject("$exists", true));
        return builder.build();
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of times when user launched task on project";
    }
}
