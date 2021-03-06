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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;

/**
 * @author Anatoliy Bazko
 */
public abstract class AbstractCount extends ReadBasedMetric implements ReadBasedExpandable {

    private final ReadBasedMetric basedMetric;
    private final String          expandingField;

    public AbstractCount(MetricType metricType, MetricType basedMetric, String expandingField) {
        super(metricType);
        this.basedMetric = (ReadBasedMetric)MetricFactory.getMetric(basedMetric);
        this.expandingField = expandingField;
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return basedMetric.getStorageCollectionName();
    }

    /** {@inheritDoc} */
    @Override
    public final String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    /** {@inheritDoc} */
    @Override
    public Context applySpecificFilter(Context clauses) throws IOException {
        return basedMetric.applySpecificFilter(clauses);
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getExpandedField() {
        return expandingField;
    }
}
