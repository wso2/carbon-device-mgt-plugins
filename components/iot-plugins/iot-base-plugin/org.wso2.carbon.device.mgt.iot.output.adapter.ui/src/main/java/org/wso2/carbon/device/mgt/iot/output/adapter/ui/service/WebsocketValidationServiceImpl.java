package org.wso2.carbon.device.mgt.iot.output.adapter.ui.service;

import org.wso2.carbon.device.mgt.iot.output.adapter.ui.authentication.Authenticator;
import org.wso2.carbon.device.mgt.iot.output.adapter.ui.authorization.Authorizer;

/**
 * This returns the configured authenticator and authorizer for websocket.
 */
public class WebsocketValidationServiceImpl implements WebsocketValidationService{
    private Authenticator authenticator;
    private Authorizer authorizer;

    @Override
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }
}
