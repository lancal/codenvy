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
package com.codenvy.analytics.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ListValueData extends CollectionValueData {

    private List<ValueData> value;

    public static final ListValueData DEFAULT = new ListValueData(Collections.<ValueData>emptyList());

    public ListValueData() {
    }

    public ListValueData(List<ValueData> value) {
        this.value = new ArrayList<>(value);
    }

    public static ListValueData valueOf(List<ValueData> value) {
        return new ListValueData(value);
    }

    public List<ValueData> getAll() {
        return Collections.unmodifiableList(value);
    }

    public int size() {
        return value.size();
    }

    @Override
    protected ListValueData doAdd(ValueData valueData) {
        List<ValueData> result = new ArrayList<>(value);
        result.addAll(treatAsList(valueData));
        return new ListValueData(result);
    }


    @Override
    protected ValueData doSubtract(ValueData valueData) {
        List<ValueData> result = new ArrayList<>(value);
        result.removeAll(treatAsList(valueData));
        return new ListValueData(result);
    }

    @Override
    public String getAsString() {
        StringBuilder builder = new StringBuilder();

        for (ValueData valueData : value) {
            if (builder.length() != 0) {
                builder.append(',');
            }

            builder.append(' ');
            builder.append(valueData.getAsString());
        }

        if (builder.length() != 0) {
            builder.setCharAt(0, '[');
            builder.append(']');
        } else {
            builder.append("[]");
        }

        return builder.toString();
    }

    @Override
    public String getType() {
        return ValueDataTypes.LIST.toString();
    }

    @Override
    protected boolean doEquals(ValueData valueData) {
        return this.value.equals(((ListValueData)valueData).value);
    }

    @Override
    protected int doHashCode() {
        return value.hashCode();
    }
}
