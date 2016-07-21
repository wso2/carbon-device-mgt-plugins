package org.wso2.carbon.device.mgt.iot.output.adapter.ui.service;

import org.wso2.carbon.device.mgt.iot.output.adapter.ui.authentication.Authenticator;
import org.wso2.carbon.device.mgt.iot.output.adapter.ui.authorization.Authorizer;

/**
 * This returns the configured authenticator and authorizer for websocket.
 */
public interface WebsocketValidationService {

    Authenticator getAuthenticator();

    Authorizer getAuthorizer();

}
