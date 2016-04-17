package org.wso2.carbon.iot.android.sense.util.dto;


import feign.RequestInterceptor;
import feign.RequestTemplate;

import static feign.Util.checkNotNull;

public class OAuthRequestInterceptor implements RequestInterceptor {

    private final String headerValue;

    /**
     * Creates an interceptor that authenticates all requests with the specified OAUTH token
     *
     * @param token the access token to use for authentication
     */
    public OAuthRequestInterceptor(String token) {
        checkNotNull(token, "access_token");
        headerValue = "Bearer " + token;
    }
    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", headerValue);
    }
}
