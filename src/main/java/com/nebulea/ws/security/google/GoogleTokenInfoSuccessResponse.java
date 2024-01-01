package com.nebulea.ws.security.google;

import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.jwt.util.DateUtils;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.SuccessResponse;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import net.minidev.json.JSONObject;

import java.util.Date;
import java.util.List;

public class GoogleTokenInfoSuccessResponse extends GoogleTokenInfoResponse implements SuccessResponse {
    private final JSONObject params;
    private String clientId;

    public GoogleTokenInfoSuccessResponse(JSONObject params) {
        if (!(params.get("aud") instanceof String)) {
            throw new IllegalArgumentException("Missing / invalid boolean active parameter");
        } else {
            this.params = params;
        }
    }

    public boolean isActive(String clientId) {
        try {
            if (JSONObjectUtils.getString(this.params, "aud", "empty")
                    .equals(clientId)) {
                this.clientId = clientId;
                return true;
            }
            return false;
        } catch (ParseException var2) {
            return false;
        }
    }

    public static GoogleTokenInfoSuccessResponse parse(HTTPResponse httpResponse) throws ParseException {
        httpResponse.ensureStatusCode(200);
        JSONObject jsonObject = httpResponse.getContentAsJSONObject();
        return parse(jsonObject);
    }

    public static GoogleTokenInfoSuccessResponse parse(JSONObject jsonObject) throws ParseException {
        try {
            return new GoogleTokenInfoSuccessResponse(jsonObject);
        } catch (IllegalArgumentException var2) {
            throw new ParseException(var2.getMessage(), var2);
        }
    }

    @Override
    public boolean indicatesSuccess() {
        return true;
    }

    @Override
    public HTTPResponse toHTTPResponse() {
        HTTPResponse httpResponse = new HTTPResponse(200);
        httpResponse.setEntityContentType(ContentType.APPLICATION_JSON);
        httpResponse.setContent(this.params.toJSONString());
        return httpResponse;
    }

    public JSONObject toJSONObject() {
        return new JSONObject(this.params);
    }

    public List<Audience> getAudience() {
        try {
            return Audience.create(JSONObjectUtils.getStringList(this.params, "aud"));
        } catch (ParseException var4) {
            try {
                return (new Audience(JSONObjectUtils.getString(this.params, "aud"))).toSingleAudienceList();
            } catch (ParseException var3) {
                return null;
            }
        }
    }

    public Issuer getIssuer() {
        try {
            return new Issuer(JSONObjectUtils.getString(this.params, "iss"));
        } catch (ParseException var2) {
            return null;
        }
    }

    public ClientID getClientID() {
        return new ClientID(this.clientId);
    }

    public String getUsername() {
        try {
            return JSONObjectUtils.getString(this.params, "username", (String)null);
        } catch (ParseException var2) {
            return null;
        }
    }

    public Subject getSubject() {
        try {
            return new Subject(JSONObjectUtils.getString(this.params, "sub"));
        } catch (ParseException var2) {
            return null;
        }
    }

    public Date getExpirationTime() {
        try {
            return DateUtils.fromSecondsSinceEpoch(JSONObjectUtils.getLong(this.params, "exp"));
        } catch (ParseException var2) {
            return null;
        }
    }

    public Date getIssueTime() {
        try {
            return DateUtils.fromSecondsSinceEpoch(JSONObjectUtils.getLong(this.params, "iat"));
        } catch (ParseException var2) {
            return null;
        }
    }
}
