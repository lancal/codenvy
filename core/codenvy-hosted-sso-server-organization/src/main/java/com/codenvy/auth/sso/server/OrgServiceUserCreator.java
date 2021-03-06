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
package com.codenvy.auth.sso.server;


import com.codenvy.auth.sso.server.organization.UserCreator;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.Constants;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.commons.user.UserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sergii Kabashniuk
 */
public class OrgServiceUserCreator implements UserCreator {
    private static final Logger LOG = LoggerFactory.getLogger(OrgServiceUserCreator.class);

    @Inject
    private UserDao userDao;

    @Inject
    private UserProfileDao profileDao;

    @Inject
    private PreferenceDao preferenceDao;

    @Inject
    @Named("user.self.creation.allowed")
    private boolean userSelfCreationAllowed;

    @Override
    public User createUser(String email, String userName, String firstName, String lastName) throws IOException {
        //TODO check this method should only call if user is not exists.
        try {
            org.eclipse.che.api.user.server.dao.User user = userDao.getByAlias(email);
            return new UserImpl(user.getName(), user.getId(), null, Collections.<String>emptyList(), false);
        } catch (NotFoundException e) {
            if (!userSelfCreationAllowed) {
                throw new IOException("Currently only admins can create accounts. Please contact our Admin Team for further info.");
            }

            String id = NameGenerator.generate(User.class.getSimpleName().toLowerCase(), Constants.ID_LENGTH);

            final Map<String, String> attributes = new HashMap<>();
            attributes.put("firstName", firstName);
            attributes.put("lastName", lastName);
            attributes.put("email", email);

            Profile profile = new Profile()
                    .withId(id)
                    .withUserId(id)
                    .withAttributes(attributes);
            String password = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

            try {
                userDao.create(new org.eclipse.che.api.user.server.dao.User().withId(id)
                                                                             .withName(userName)
                                                                             .withEmail(email)
                                                                             .withPassword(password));
                profileDao.create(profile);

                final Map<String, String> preferences = new HashMap<>();
                preferences.put("codenvy:created", Long.toString(System.currentTimeMillis()));
                preferences.put("resetPassword", "true");
                preferenceDao.setPreferences(id, preferences);

                return new UserImpl(userName, id, null, Collections.<String>emptyList(), false);
            } catch (ConflictException | ServerException | NotFoundException e1) {
                throw new IOException(e1.getLocalizedMessage(), e1);
            }
        } catch (ServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }

    }

    @Override
    public User createTemporary() throws IOException {

        String id = NameGenerator.generate(User.class.getSimpleName(), Constants.ID_LENGTH);
        try {
            String testName = null;
            while (true) {
                testName = NameGenerator.generate("AnonymousUser_", 6);
                try {
                    userDao.getByName(testName);
                } catch (NotFoundException e) {
                    break;
                } catch (ApiException e) {
                    throw new IOException(e.getLocalizedMessage(), e);
                }

            }


            final String anonymousUser = testName;
            // generate password and delete all "-" symbols which are generated by randomUUID()
            String password = UUID.randomUUID().toString().replace("-", "").substring(0, 12);


            userDao.create(new org.eclipse.che.api.user.server.dao.User().withId(id).withName(anonymousUser)
                                                                     .withPassword(password));

            profileDao.create(new Profile()
                                      .withId(id)
                                      .withUserId(id));

            final Map<String, String> preferences = new HashMap<>();
            preferences.put("temporary", String.valueOf(true));
            preferences.put("codenvy:created", Long.toString(System.currentTimeMillis()));
            preferenceDao.setPreferences(id, preferences);

            LOG.info("Temporary user {} created", anonymousUser);
            return new UserImpl(anonymousUser, id, null, Collections.<String>emptyList(), true);
        } catch (ApiException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }
}
