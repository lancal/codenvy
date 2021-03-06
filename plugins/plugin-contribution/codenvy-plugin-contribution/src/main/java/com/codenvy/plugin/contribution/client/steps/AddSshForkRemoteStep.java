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
import com.google.inject.Singleton;

import javax.inject.Inject;

/**
 * Add SSH fork remote URL to repository.
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class AddSshForkRemoteStep implements Step {
    private final AddForkRemoteStepFactory addForkRemoteStepFactory;

    @Inject
    public AddSshForkRemoteStep(AddForkRemoteStepFactory addForkRemoteStepFactory) {
        this.addForkRemoteStepFactory = addForkRemoteStepFactory;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        String remoteUrl = context.getVcsHostingService().makeSSHRemoteUrl(context.getHostUserLogin(), context.getForkedRepositoryName());
        addForkRemoteStepFactory.create(this, remoteUrl)
                                .execute(executor, context);
    }
}
