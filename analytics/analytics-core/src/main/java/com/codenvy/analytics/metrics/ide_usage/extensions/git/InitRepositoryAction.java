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
package com.codenvy.analytics.metrics.ide_usage.extensions.git;

import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ide_usage.AbstractIdeUsage;

/**
 * @author Dmytro Nochevnov
 */
public class InitRepositoryAction extends AbstractIdeUsage {
    public static final String[] SOURCE = {
            "com.codenvy.ide.ext.git.client.action.InitRepositoryAction",
            "org.eclipse.che.ide.ext.git.client.action.InitRepositoryAction"
    };

    public InitRepositoryAction() {
        super(MetricType.INIT_REPOSITORY_ACTION, SOURCE);
    }
}
