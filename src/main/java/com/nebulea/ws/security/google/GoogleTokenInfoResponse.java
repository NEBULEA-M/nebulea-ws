package com.nebulea.ws.security.google;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Response;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;

public abstract class GoogleTokenInfoResponse implements Response {
    public GoogleTokenInfoResponse() {
    }

    public GoogleTokenInfoSuccessResponse toSuccessResponse() {
        return (GoogleTokenInfoSuccessResponse) this;
    }

    public GoogleTokenInfoErrorResponse toErrorResponse() {
        return (GoogleTokenInfoErrorResponse) this;
    }

    public static GoogleTokenInfoResponse parse(HTTPResponse httpResponse) throws ParseException {
        return httpResponse.getStatusCode() == 200 ?
                GoogleTokenInfoSuccessResponse.parse(httpResponse) :
                GoogleTokenInfoErrorResponse.parse(httpResponse);
    }
}
