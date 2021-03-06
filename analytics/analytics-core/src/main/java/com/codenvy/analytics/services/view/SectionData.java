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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.datamodel.ValueData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** @author Anatoliy Bazko */
public class SectionData extends ArrayList<List<ValueData>> {

    public SectionData(int initialCapacity) {
        super(initialCapacity);
    }

    public SectionData() {
    }

    public SectionData(Collection<? extends List<ValueData>> c) {
        super(c);
    }
}
