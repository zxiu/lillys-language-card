package com.zxiu.lillyscard.fb;

import com.facebook.AccessToken;

import org.json.JSONObject;

/**
 * Created by Xiu on 10/20/2016.
 */

public interface FacebookLoginCallback {
    public void onLoginStateChanged(AccessToken oldAccessToken, AccessToken currentAccessToken);

    public void onProfileLoadListener(JSONObject user, String id, String name, String photoUrl);
}
