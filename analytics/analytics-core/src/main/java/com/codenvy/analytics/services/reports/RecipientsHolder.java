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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.services.configuration.ParameterConfiguration;
import com.codenvy.analytics.services.configuration.ParametersConfiguration;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Singleton
public class RecipientsHolder {

    private static final String CONFIGURATION = "analytics.reports.recipients";

    private final RecipientsHolderConfiguration configuration;

    @Inject
    public RecipientsHolder(Configurator configurator, XmlConfigurationManager confManager) throws IOException {
        configuration = confManager.loadConfiguration(RecipientsHolderConfiguration.class,
                                                      configurator.getString(CONFIGURATION));
    }

    public Set<String> getEmails(String groupName, Context context) throws IOException {
        return doGetEmails(groupName, context);
    }

    protected Set<String> doGetEmails(String groupName, Context context) throws IOException {
        try {
            for (GroupConfiguration groupConf : configuration.getGroups()) {
                if (groupConf.getName().equals(groupName)) {
                    InitializerConfiguration initializer = groupConf.getInitializer();

                    ParametersConfiguration paramsConf = initializer.getParametersConfiguration();
                    List<ParameterConfiguration> parameters = paramsConf.getParameters();

                    String clazzName = initializer.getClazz();
                    Class<?> clazz = Class.forName(clazzName);
                    Constructor<?> constructor = clazz.getConstructor(List.class);

                    RecipientGroup recipientGroup = (RecipientGroup)constructor.newInstance(parameters);
                    return recipientGroup.getEmails(context);
                }
            }

            return Collections.emptySet();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException
                | IllegalAccessException | NoSuchMethodException e) {
            throw new IOException(e);
        }
    }
}
