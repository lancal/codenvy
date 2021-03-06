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
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.BaseTest;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class LoadTestMongoIndexes extends BaseTest {

    private static final int COUNT = 2000000;

    private DB db;

    @BeforeClass
    public void prepare() throws Exception {
        db = mongoDataStorage.getDb();
    }

    @Test
    public void loadTestPreExistCompoundIndex() throws Exception {
        String collectionName = "test-pre-exist-compound-index";
        String indexName = collectionName;

        DBCollection dbCollection = db.getCollection(collectionName);

        LOG.info(String.format("INFO(%s) start creating index at %s \n", collectionName, new Date().toString()));
        addCompoundIndex(dbCollection, indexName);
        LOG.info(String.format("INFO(%s) finish creating index at %s \n", collectionName, new Date().toString()));

        LOG.info(String.format("INFO(%s) start adding data at %s \n", collectionName, new Date().toString()));
        for (int i = 0; i < COUNT; i++) {
            BasicDBObject dbObject = getDBObjectWithTestData(COUNT);

            dbCollection.save(dbObject);

            if ((i % 10000) == 0) {
                LOG.info(String.format("INFO(%s) adding document number %d at %s \n", collectionName, i,
                                       new Date().toString()));
            }
        }
        LOG.info(String.format("INFO(%s) finish adding data at %s \n", collectionName, new Date().toString()));
    }

    @Test
    public void loadTestPostExistCompoundIndex() throws Exception {
        String collectionName = "test-post-exist-compound-index";
        String indexName = collectionName;

        DBCollection dbCollection = db.getCollection(collectionName);

        LOG.info(String.format("INFO(%s) start adding data at %s \n", collectionName, new Date().toString()));
        for (int i = 0; i < COUNT; i++) {
            BasicDBObject dbObject = getDBObjectWithTestData(COUNT);

            dbCollection.save(dbObject);

            if ((i % 10000) == 0) {
                LOG.info(String.format("INFO(%s) adding document number %d at %s \n", collectionName, i,
                                       new Date().toString()));
            }
        }
        LOG.info(String.format("INFO(%s) finish adding data at %s \n", collectionName, new Date().toString()));

        LOG.info(String.format("INFO(%s) start creating index at %s \n", collectionName, new Date().toString()));
        addCompoundIndex(dbCollection, indexName);
        LOG.info(String.format("INFO(%s) finish creating index at %s \n", collectionName, new Date().toString()));
    }

    @Test
    public void loadTestPostExistClearThenPostExistCompoundIndex() throws Exception {
        String collectionName = "test-post-create-clear-post-create-compound-index";
        String indexName = collectionName;

        DBCollection dbCollection = db.getCollection(collectionName);

        LOG.info(String.format("INFO(%s) start adding data at %s \n", collectionName, new Date().toString()));
        for (int i = 0; i < COUNT / 2; i++) {
            BasicDBObject dbObject = getDBObjectWithTestData(COUNT);

            dbCollection.save(dbObject);

            if ((i % 100000) == 0) {
                LOG.info(String.format("INFO(%s) adding document number %d at %s \n", collectionName, i,
                                       new Date().toString()));
            }
        }
        LOG.info(String.format("INFO(%s) finish adding data at %s \n", collectionName, new Date().toString()));

        LOG.info(String.format("INFO(%s) start creating index at %s \n", collectionName, new Date().toString()));
        addCompoundIndex(dbCollection, indexName);
        LOG.info(String.format("INFO(%s) finish creating index at %s \n", collectionName, new Date().toString()));


        LOG.info(String.format("INFO(%s) start clearing index at %s \n", collectionName, new Date().toString()));
        dbCollection.dropIndex(indexName);
        LOG.info(String.format("INFO(%s) finish clearing index at %s \n", collectionName, new Date().toString()));


        LOG.info(String.format("INFO(%s) start adding data at %s \n", collectionName, new Date().toString()));
        for (int i = COUNT / 2; i < COUNT; i++) {
            BasicDBObject dbObject = getDBObjectWithTestData(COUNT);

            dbCollection.save(dbObject);

            if ((i % 100000) == 0) {
                LOG.info(String.format("INFO(%s) adding document number %d at %s \n", collectionName, i,
                                       new Date().toString()));
            }
        }
        LOG.info(String.format("INFO(%s) finish adding data at %s \n", collectionName, new Date().toString()));

        LOG.info(String.format("INFO(%s) start creating index at %s \n", collectionName, new Date().toString()));
        addCompoundIndex(dbCollection, indexName);
        LOG.info(String.format("INFO(%s) finish creating index at %s \n", collectionName, new Date().toString()));

    }

    /** Create compound index db.test.ensureIndex( { "user": 1,  "ws": 1, "domain": 1} ) */
    private void addCompoundIndex(DBCollection dbCollection, String indexName) {
        BasicDBObject indexes = new BasicDBObject();
        indexes.put("user", 1);
        indexes.put("ws", 1);
        indexes.put("domain", 1);
        dbCollection.createIndex(indexes, indexName);
    }

    private BasicDBObject getDBObjectWithTestData(int count) {
        BasicDBObject dbObject = new BasicDBObject();

        String user = "user" + (int)(Math.random() * count);
        dbObject.put("user", user);

        String ws = "ws" + (int)(Math.random() * count);
        dbObject.put("ws", ws);

        String domain = "domain" + (int)(Math.random() * count);
        dbObject.put("domain", domain);

        return dbObject;
    }
}
