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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.Utils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.codenvy.analytics.Utils.getFilterAsString;

/**
 * Unmodifiable execution context.
 *
 * @author Anatoliy Bazko
 */
public class Context {

    public static final Context EMPTY = new Context(Collections.<String, Object>emptyMap());

    private Map<String, Object> params;

    private Context(Map<String, Object> params) {
        this.params = Collections.unmodifiableMap(params);
    }

    public static Context valueOf(Map<String, String> params) {
        Builder builder = new Builder();
        builder.putAll(params);
        return builder.build();
    }

    public Set<MetricFilter> getFilters() {
        Set<MetricFilter> result = new HashSet<>();

        for (MetricFilter filter : MetricFilter.values()) {
            if (exists(filter)) {
                result.add(filter);
            }
        }

        return result;
    }

    public Map<String, Object> getAll() {
        return new HashMap<>(params);
    }

    public Map<String, String> getAllAsString() {
        Map<String, String> result = new HashMap<>(params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString());
        }

        return result;
    }

    public Calendar getAsDate(Parameters key) throws ParseException {
        return Utils.parseDate(getAsString(key));
    }

    public long getAsLong(Parameters key) {
        return Long.valueOf(getAsString(key));
    }

    public Parameters.TimeUnit getTimeUnit() {
        return Parameters.TimeUnit.valueOf(getAsString(Parameters.TIME_UNIT).toUpperCase());
    }

    public Parameters.PassedDaysCount getPassedDaysCount() {
        return Parameters.PassedDaysCount.valueOf(getAsString(Parameters.PASSED_DAYS_COUNT).toUpperCase());
    }

    public boolean isDefaultValue(Parameters key) {
        return params.get(key.toString()).equals(key.getDefaultValue());
    }

    public String getAsString(Parameters key) {
        return getAsString(key.toString());
    }

    public String getAsString(MetricFilter key) {
        return getAsString(key.toString());
    }

    public String getAsString(String key) {
        Object object = params.get(key);
        if (object == null) {
            return null;
        } else if (object.getClass().isArray()) {
            return getFilterAsString(new HashSet<>(Arrays.asList((String[])object)));
        } else {
            return (String)object;
        }
    }

    public Object get(MetricFilter key) {
        return params.get(key.toString());
    }

    public boolean exists(Parameters key) {
        return params.containsKey(key.toString());
    }

    public boolean exists(MetricFilter key) {
        return params.containsKey(key.toString());
    }


    public Context cloneAndRemove(Parameters param) {
        Builder builder = new Builder(params);
        builder.remove(param);

        return builder.build();
    }

    public Context cloneAndRemove(MetricFilter metricFilter) {
        Builder builder = new Builder(params);
        builder.remove(metricFilter);

        return builder.build();
    }

    public Context cloneAndPut(Object param, Object value) {
        Builder builder = new Builder(params);
        builder.put(param, value);

        return builder.build();
    }

    public Context cloneAndPut(Parameters param, Calendar value) {
        return cloneAndPut(param, Utils.formatDate(value));
    }

    public Context cloneAndPut(Parameters param, long value) {
        return cloneAndPut(param, Long.toString(value));
    }

    public Context cloneAndPut(MetricFilter param, long value) {
        return cloneAndPut(param, Long.toString(value));
    }

    @Override
    public String toString() {
        Iterator<Map.Entry<String, Object>> i = params.entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Map.Entry<String, Object> e = i.next();
            String key = e.getKey();
            Object value = e.getValue();

            sb.append(key);
            sb.append('=');
            if (value.getClass().isArray()) {
                sb.append(Arrays.toString((Object[])value));
            } else {
                sb.append(value);
            }

            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

    public Metric getExpandedMetric() {
        if (!exists(Parameters.EXPANDED_METRIC_NAME)) {
            throw new IllegalArgumentException("There is no parameter " + Parameters.EXPANDED_METRIC_NAME + " in the context");
        }

        String value = getAsString(Parameters.EXPANDED_METRIC_NAME);
        MetricType expandedMetricType = MetricType.valueOf(value.toUpperCase());

        Metric expandedMetric = MetricFactory.getMetric(expandedMetricType);
        if (!(expandedMetric instanceof Expandable)) {
            return null;
        }

        return expandedMetric;
    }

    /**
     * Context builder.
     */
    public static class Builder {
        private Map<String, Object> params = new LinkedHashMap<>();

        public Builder() {
        }

        public Builder(MetricFilter param, Object value) {
            put(param, value);
        }

        public Builder(Context context) {
            this.params.putAll(context.params);
        }

        public Builder(Map<String, Object> params) {
            this.params.putAll(params);
        }

        public Builder putAll(Context context) {
            this.params.putAll(context.params);
            return this;
        }

        public Builder putAll(Map<String, String> params) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                this.params.put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder putDefaultValue(Parameters param) {
            params.put(param.toString(), param.getDefaultValue());
            return this;
        }

        public Builder put(Parameters param, String value) {
            params.put(param.toString(), value);
            return this;
        }

        public Builder put(MetricFilter param, String value) {
            params.put(param.toString(), value);
            return this;
        }

        public Builder put(MetricFilter param, Object value) {
            params.put(param.toString(), value);
            return this;
        }

        public Builder put(Parameters param, Calendar value) {
            params.put(param.toString(), Utils.formatDate(value));
            return this;
        }

        public Builder put(Object param, Object value) {
            params.put(param.toString(), value);
            return this;
        }

        public Builder put(MetricFilter param, long value) {
            return put(param, Long.toString(value));
        }

        public Builder put(Parameters param, long value) {
            return put(param, Long.toString(value));
        }

        public Builder put(Parameters.TimeUnit timeUnit) {
            return put(Parameters.TIME_UNIT, timeUnit.toString());
        }

        public boolean exists(Parameters param) {
            return params.containsKey(param.toString());
        }

        public boolean exists(MetricFilter param) {
            return params.containsKey(param.toString());
        }

        public Builder remove(Parameters param) {
            params.remove(param.toString());
            return this;
        }

        public Builder remove(MetricFilter param) {
            params.remove(param.toString());
            return this;
        }

        public String getAsString(MetricFilter param) {
            Object object = params.get(param.toString());
            return object == null ? null : (String)object;
        }

        public String getAsString(Parameters param) {
            Object object = params.get(param.toString());
            return object == null ? null : (String)object;
        }

        public Calendar getAsDate(Parameters key) throws ParseException {
            return Utils.parseDate(getAsString(key));
        }

        public Parameters.TimeUnit getTimeUnit() {
            return Parameters.TimeUnit.valueOf(getAsString(Parameters.TIME_UNIT).toUpperCase());
        }

        public Parameters.PassedDaysCount getPassedDaysCount() {
            return Parameters.PassedDaysCount.valueOf(getAsString(Parameters.PASSED_DAYS_COUNT).toUpperCase());
        }

        public Context build() {
            return new Context(params);
        }
    }

}
