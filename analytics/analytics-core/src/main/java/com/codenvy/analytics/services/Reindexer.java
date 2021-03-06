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

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.persistent.CollectionsManagement;
import com.google.inject.Singleton;

import javax.inject.Inject;

/** @author Anatoliy Bazko */
@Singleton
public class Reindexer extends Feature {

    private final CollectionsManagement collectionsManagement;

    @Inject
    public Reindexer(CollectionsManagement collectionsManagement) {
        this.collectionsManagement = collectionsManagement;
    }

    @Override
    protected void doExecute(Context context) throws Exception {
        collectionsManagement.crateIndexes();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
