/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.plugin.contribution.client.workflow.Context;
import com.codenvy.plugin.contribution.client.workflow.Step;
import com.codenvy.plugin.contribution.client.workflow.WorkflowExecutor;

/**
 * This step defines ability to create forks.
 *
 * @author Mihail Kuznyetsov
 */
public class DefineExecutionConfiguration implements Step {

    @Override
    public void execute(WorkflowExecutor executor, Context context) {
        context.setForkAvailable(!context.getOriginRepositoryOwner().equals(context.getHostUserLogin()));
        executor.done(this, context);
    }
}
