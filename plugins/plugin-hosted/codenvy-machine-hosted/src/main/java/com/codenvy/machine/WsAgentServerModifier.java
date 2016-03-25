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
package com.codenvy.machine;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Modifies wsagent machine server to proxy requests to it.
 *
 * @author Alexander Garagatyi
 */
public class WsAgentServerModifier extends BaseServerModifier {
    @Inject
    public WsAgentServerModifier(@Named("machine.https_wsagent_server_url_template") String serverUrlTemplate) {
        super(serverUrlTemplate);
    }
}
