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
package com.codenvy.analytics.pig.scripts;


import com.codenvy.analytics.metrics.Parameters;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Anatoliy Bazko
 */
public enum ScriptType {
    EVENTS {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            return params;
        }
    },
    EVENTS_BY_TYPE {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            params.add(Parameters.PARAM);
            return params;
        }
    },
    IDE_USAGE_EVENTS,
    DEPLOYMENTS_BY_TYPES,
    CREATED_USERS_FROM_FACTORY,
    PRODUCT_USAGE_SESSIONS {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            params.add(Parameters.STORAGE_TABLE_USERS_STATISTICS);
            params.add(Parameters.STORAGE_TABLE_PRODUCT_USAGE_SESSIONS_FAILS);
            return params;
        }
    },
    PRODUCT_USAGE_SESSIONS_OLD {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.STORAGE_TABLE_USERS_STATISTICS);
            return params;
        }
    },
    ACTIVE_ENTITIES {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.PARAM);
            return params;
        }
    },
    TIME_SPENT_IN_ACTION {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            return params;
        }
    },
    USED_TIME {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            params.add(Parameters.PARAM);
            return params;
        }
    },
    CREATED_FACTORIES,
    ACCEPTED_FACTORIES,
    PRODUCT_USAGE_FACTORY_SESSIONS {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.STORAGE_TABLE_PRODUCT_USAGE_SESSIONS);
            params.add(Parameters.STORAGE_TABLE_USERS_STATISTICS);
            params.add(Parameters.STORAGE_TABLE_ACCEPTED_FACTORIES);
            params.add(Parameters.STORAGE_TABLE_PRODUCT_USAGE_SESSIONS_FAILS);
            return params;
        }
    },
    CREATED_TEMPORARY_WORKSPACES,
    USERS_PROFILES,
    WORKSPACES_PROFILES,
    USERS_STATISTICS,
    USERS_ACTIVITY,
    USERS_ACCOUNTS,
    PROJECTS_STATISTICS,
    USERS_EVENTS {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.STORAGE_TABLE_PRODUCT_USAGE_SESSIONS);
            params.add(Parameters.STORAGE_TABLE_USERS_STATISTICS);
            return params;
        }
    },
    LOG_CHECKER {
        @Override
        public Set<Parameters> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new Parameters[]{Parameters.TO_DATE,
                                                   Parameters.FROM_DATE}));
        }
    },

    TASKS,

    /** Script for testing purpose. */
    TEST_MONGO_LOADER {
        public Set<Parameters> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new Parameters[]{Parameters.STORAGE_URL,
                                                   Parameters.STORAGE_TABLE}));
        }
    },
    TEST_EXTRACT_WS,
    TEST_EXTRACT_USER,
    TEST_EXTRACT_ORG_AND_AFFILIATE_ID,
    TEST_COMBINE_CLOSEST_EVENTS,
    TEST_CALCULATE_TIME,
    TEST_LOAD_RESOURCES,
    TEST_FIX_FACTORY_URL,
    SEND_VERIFICATION_EMAIL,
    CREATED_USERS;


    /** @return list of mandatory parameters required to be passed into the script */
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new Parameters[]{Parameters.WS,
                                               Parameters.USER,
                                               Parameters.TO_DATE,
                                               Parameters.FROM_DATE,
                                               Parameters.STORAGE_URL,
                                               Parameters.STORAGE_TABLE}));
    }
}
