/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
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
package com.codenvy.subscription.service;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Subscription of factories
 *
 * @author Sergii Kabashniuk
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class FactorySubscriptionService extends SubscriptionService {
    private static final Logger LOG = LoggerFactory.getLogger(FactorySubscriptionService.class);
    private final AccountDao accountDao;

    @Inject
    public FactorySubscriptionService(AccountDao accountDao) {
        super("Factory", "Factory");
        this.accountDao = accountDao;
    }

    //fixme for now Factory supports only 1 active subscription per 1 account
    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException {
        final List<Subscription> allSubscriptions;
        try {
            allSubscriptions = accountDao.getSubscriptions(subscription.getAccountId());
            for (Subscription current : allSubscriptions) {
                if (getServiceId().equals(current.getServiceId())) {
                    throw new ConflictException("Factory subscription already exists");
                }
            }
        } catch (NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        //nothing to do
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) {
        //nothing to do
    }

    @Override
    public void onCheckSubscription(Subscription subscription) {
        //nothing to do
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription) {
        //nothing to do
    }
}