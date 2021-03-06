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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.persistent.CollectionsManagement;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.codenvy.analytics.pig.scripts.EventsHolder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClientURI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.StoreFunc;
import org.apache.pig.data.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static com.codenvy.analytics.Utils.fetchEncodedPairs;
import static com.codenvy.analytics.Utils.isAnonymousUser;
import static com.codenvy.analytics.Utils.isTemporaryWorkspace;
import static com.codenvy.analytics.Utils.toArray;
import static java.net.URLDecoder.decode;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoStorage extends StoreFunc {

    private static final Logger             LOG                 = LoggerFactory.getLogger(MongoStorage.class);
    private static final String             SERVER_URL_PARAM    = "server.url";
    private static final String             ALIASES_PARAM       = "aliases";
    private static final String             ROLES_PARAM         = "roles";
    private static final String             PARAMETERS_PARAM    = "parameters";
    private static final String             ERROR_MESSAGE_PARAM = "error-message";
    private static final Collection<String> ENCODED_PARAMETERS  = Arrays.asList(ERROR_MESSAGE_PARAM);

    private RecordWriter writer;

    public MongoStorage() {
    }

    @Override
    public OutputFormat getOutputFormat() throws IOException {
        return new MongoOutputFormat();
    }

    @Override
    public void setStoreLocation(String location, Job job) throws IOException {
        job.getConfiguration().set(SERVER_URL_PARAM, location);
    }

    @Override
    public void prepareToWrite(RecordWriter writer) throws IOException {
        this.writer = writer;
    }

    @Override
    public void putNext(Tuple t) throws IOException {
        try {
            writer.write(null, t);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     *
     */
    public static class MongoWriter extends RecordWriter<WritableComparable, Tuple> {

        /** Collection to write data in. */
        private final DBCollection dbCollection;
        private final EventsHolder eventsHolder;

        public MongoWriter(Configuration configuration) throws IOException {
            MongoClientURI uri = new MongoClientURI(configuration.get(SERVER_URL_PARAM));

            MongoDataStorage mongoDataStorage = Injector.getInstance(MongoDataStorage.class);
            CollectionsManagement collectionsManagement = Injector.getInstance(CollectionsManagement.class);

            String name = uri.getCollection();
            if (!name.startsWith("test") && !collectionsManagement.exists(name)) {
                String msg = "Collection " + name + " doesn't exist in configuration";
                LOG.error(msg);
                throw new IOException(msg);
            }

            this.dbCollection = mongoDataStorage.getDb().getCollection(name);
            this.eventsHolder = Injector.getInstance(EventsHolder.class);
        }

        @Override
        public void write(WritableComparable writableComparable, Tuple value) throws IOException, InterruptedException {
            BasicDBObject dbObject = new BasicDBObject();
            try {
                for (int i = 1; i < value.size(); i++) {
                    Tuple tuple = (Tuple)value.get(i);

                    String key = tuple.get(0).toString();
                    Object data = tuple.get(1);

                    if (data != null) {
                        if (data instanceof String) {
                            String str = (String)data;

                            if (isParameters(key)) {
                                putKeyValuePairs(dbObject, str);

                            } else if (isAliases(key) || isRoles(key)) { //TODO
                                put(dbObject, key, toArray(str));

                            } else if ("params-only-remove-event".equals(key)) {
                                putMessageParameters(dbObject, str);
                                dbObject.remove("event");

                            } else {
                                put(dbObject, key, data);
                                if (isMessage(key)) {
                                    putMessageParameters(dbObject, str);
                                }
                            }
                        } else {
                            put(dbObject, key, data);
                        }
                    }
                }

                if (!dbObject.isEmpty()) {
                    addRegisteredUserTag(dbObject);
                    addPersistentWsTag(dbObject);

                    dbCollection.update(new BasicDBObject("_id", value.get(0)),
                                        new BasicDBObject("$set", dbObject),
                                        true,
                                        false);

                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

        private void addRegisteredUserTag(BasicDBObject dbObject) {
            Object user = dbObject.get(ReadBasedMetric.USER);
            if (user != null) {
                dbObject.put(MetricFilter.REGISTERED_USER.toString().toLowerCase(), isAnonymousUser(user) ? 0 : 1);
            }
        }

        private void addPersistentWsTag(BasicDBObject dbObject) {
            Object ws = dbObject.get(ReadBasedMetric.WS);
            if (ws != null) {
                dbObject.put(MetricFilter.PERSISTENT_WS.toString().toLowerCase(), isTemporaryWorkspace(ws) ? 0 : 1);
            }
        }

        /**
         * The parameter 'message' contains raw log which means that every possible parameters have to be extracted out
         * of message and stored.
         *
         * @return true if key equals to 'message' and false otherwise
         */
        private boolean isMessage(String key) {
            return key.equalsIgnoreCase("message");
        }

        /**
         * The parameter 'parameters' contains other key-value pairs which must be stored separately.
         *
         * @return true if key equals to 'parameters' and false otherwise
         */
        private boolean isParameters(String key) {
            return key.equalsIgnoreCase(PARAMETERS_PARAM);
        }

        /**
         * The parameter which contains URL encoded value.
         *
         * @return true if key belongs to list of encoded parameters
         */
        private boolean isEncodedParameter(String key) {
            return ENCODED_PARAMETERS.contains(key.toLowerCase());
        }

        /**
         * The parameter 'aliases' contains arrays of strings.
         *
         * @return true if key equals to 'aliases' and false otherwise
         */
        private boolean isAliases(String key) {
            return key.equalsIgnoreCase(ALIASES_PARAM);
        }

        /**
         * The parameter 'roles' contains arrays of strings.
         *
         * @return true if key equals to 'roles' and false otherwise
         */
        private boolean isRoles(String key) {
            return key.equalsIgnoreCase(ROLES_PARAM);
        }

        /**
         * Extracts and puts all parameters out of the message.
         */
        private void putMessageParameters(DBObject dbObject, String message) throws UnsupportedEncodingException {
            String event = (String)dbObject.get("event");
            Map<String, Object> values = eventsHolder.getParametersValues(event, message);

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String key = entry.getKey();
                Object data = entry.getValue();

                if (isParameters(key)) {
                    putKeyValuePairs(dbObject, String.valueOf(data));
                } else {
                    if (isEncodedParameter(key)) {
                        data = decode(String.valueOf(data), "UTF-8");
                    }

                    put(dbObject, key, data);
                }
            }
        }

        /**
         * Puts key-value encoded pairs separated by ",".
         */
        private void putKeyValuePairs(DBObject dbObject, String data) throws UnsupportedEncodingException {
            for (Map.Entry<String, String> e : fetchEncodedPairs(data, true).entrySet()) {
                put(dbObject, e.getKey(), e.getValue());
            }
        }

        private void put(DBObject object, String key, Object value) {
            object.put(key.toLowerCase().replace("-", "_"), value);
        }

        @Override
        public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        }
    }

    /** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
    public static class MongoOutputFormat extends OutputFormat<WritableComparable, Tuple> {

        @Override
        public RecordWriter<WritableComparable, Tuple> getRecordWriter(TaskAttemptContext context) throws IOException,
                                                                                                          InterruptedException {
            return new MongoWriter(context.getConfiguration());
        }

        @Override
        public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
        }

        @Override
        public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
            return new MongoCommitter();
        }
    }

    /** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
    public static class MongoCommitter extends OutputCommitter {

        @Override
        public void setupJob(JobContext jobContext) throws IOException {
        }

        @Override
        public void cleanupJob(JobContext jobContext) throws IOException {
        }

        @Override
        public void setupTask(TaskAttemptContext taskContext) throws IOException {
        }

        @Override
        public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException {
            return false;
        }

        @Override
        public void commitTask(TaskAttemptContext taskContext) throws IOException {
        }

        @Override
        public void abortTask(TaskAttemptContext taskContext) throws IOException {
        }
    }
}

