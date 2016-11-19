/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 * <p>
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 * <p>
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.zxiu.lillyscard.fb;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.zxiu.lillyscard.App;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class handles Facebook Login using LoginButton.
 * For more information on Facebook Login for Android see
 * https://developers.facebook.com/docs/facebook-login/android/
 */
public class FacebookLogin {
    static String TAG = App.TAG;
    Set<FacebookLoginCallback> loginCallbacks = new HashSet<>();
    static FacebookLogin facebookLogin;
    /**
     * SampleActivity is the activity handling Facebook Login in the app. Needed here to send
     * the signal back when user successfully logged in.
     */

    /**
     * CallbackManager is a Facebook SDK class managing the callbacks into the FacebookSdk from
     * an Activity's or Fragment's onActivityResult() method.
     * For more information see
     * https://developers.facebook.com/docs/reference/android/current/interface/CallbackManager/
     */
    private CallbackManager callbackManager;

    /**
     * CallbackManager is exposed here to so that onActivityResult() can be called from Activities
     * and Fragments when required. This is necessary so that the login result is passed to the
     * LoginManager
     */
    public CallbackManager getCallbackManager() {
        if (callbackManager == null) {
            callbackManager = CallbackManager.Factory.create();
        }
        return callbackManager;
    }

    /**
     * AccessTokenTracker allows for tracking whenever the access token changes - whenever user logs
     * in, logs out etc abstract method onCurrentAccessTokenChanged is called.
     */
    private AccessTokenTracker accessTokenTracker;

    private LoginManager loginManager;

    public static FacebookLogin getInstance() {
        if (facebookLogin == null) {
            facebookLogin = new FacebookLogin();
        }
        return facebookLogin;
    }

    protected FacebookLogin() {
        getLoginManager().registerCallback(getCallbackManager(), new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Log.i(TAG, "FacebookLogin onSuccess accessToken=" + accessToken);
            }

            @Override
            public void onCancel() {
                Log.w(TAG, "FacebookLogin onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "FacebookLogin onError=" + error.getMessage());
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                for (FacebookLoginCallback callback : loginCallbacks) {
                    callback.onLoginStateChanged(oldAccessToken, currentAccessToken);
                }
            }
        };
    }

    public FacebookLogin addLoginCallback(FacebookLoginCallback callback) {
        if (callback != null) {
            loginCallbacks.add(callback);
        }
        return this;
    }

    public void removeLoginCallback(FacebookLoginCallback callback) {
        loginCallbacks.remove(callback);
    }

    public LoginManager getLoginManager() {
        if (loginManager == null) {
            loginManager = LoginManager.getInstance();
        }
        return loginManager;
    }

    /**
     * 104      * 获取登录信息
     * 105      * @param accessToken
     * 106
     */
    public void loadProfile(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                if (object != null) {
                    String id = object.optString("id");   //比如:1565455221565
                    String name = object.optString("name");  //比如：Zhang San
                    String gender = object.optString("gender");  //性别：比如 male （男）  female （女）
                    String emali = object.optString("email");  //邮箱：比如：56236545@qq.com

                    //获取用户头像
                    JSONObject object_pic = object.optJSONObject("picture");
                    JSONObject object_data = object_pic.optJSONObject("data");
                    String photoUrl = object_data.optString("url");

                    //获取地域信息
                    String locale = object.optString("locale");   //zh_CN 代表中文简体
                    Log.i(TAG, photoUrl);
                    for (FacebookLoginCallback callback : loginCallbacks) {
                        callback.onProfileLoadListener(object, id, name, photoUrl);
                    }
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,gender,birthday,email,picture,locale,updated_time,timezone,age_range,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * Called when SampleActivity resumes. Ensures accessTokenTracker tracks token changes
     */
    public void activate() {
        accessTokenTracker.startTracking();
    }

    /**
     * Called when SampleActivity is paused. Ensures accessTokenTracker stops tracking
     */
    public void deactivate() {
        accessTokenTracker.stopTracking();
    }

    /**
     * LoginButton can be used to trigger the login dialog asking for any permission so it is
     * important to specify which permissions you want to request from a user. In Friend Smash case
     * only user_friends is required to enable access to friends, so that the game can makeCardPlayerDialogBuilder friends'
     * profile picture to make the experience more personal and engaging.
     * For more info on permissions see
     * https://developers.facebook.com/docs/facebook-login/android/permissions
     * This method is called from onCreateView() of a Fragment displayed when user is logged out of
     * Facebook.
     */
    public void setLoginButton(LoginButton button) {
        button.setReadPermissions(FacebookLoginPermission.USER_FRIENDS.toString());
        button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.w(TAG, "onSuccess " + loginResult.getAccessToken());
                for (FacebookLoginCallback callback : loginCallbacks) {
                    callback.onLoginStateChanged(null, loginResult.getAccessToken());
                }
            }

            @Override
            public void onCancel() {
                Log.w("DEBUG", "on Login Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(App.TAG, error.toString());
            }
        });
    }

    /**
     * Uses LoginManager to request additional permissions when needed, e.g. when user has finished
     * the game and is trying to post score the game would call this method to request publish_actions
     * See https://developers.facebook.com/docs/facebook-login/android/permissions for more info on
     * Login permissions
     */
    public void requestPermission(FacebookLoginPermission permission) {
        if (!isPermissionGranted(permission)) {
            Collection<String> permissions = new ArrayList<String>(1);
            permissions.add(permission.toString());
            if (permission.isRead()) {
//                LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
            } else {
//                LoginManager.getInstance().logInWithPublishPermissions(activity, permissions);
            }
        }
    }

    /**
     * Helper function checking if user is logged in and access token hasn't expired.
     */
    public static boolean isAccessTokenValid() {
        return testAccessTokenValid(AccessToken.getCurrentAccessToken());
    }

    /**
     * Helper function checking if user has granted particular permission to the app
     * For more info on permissions see
     * https://developers.facebook.com/docs/facebook-login/android/permissions
     */
    public static boolean isPermissionGranted(FacebookLoginPermission permission) {
        return testTokenHasPermission(AccessToken.getCurrentAccessToken(), permission);
    }

    /**
     * Helper function checking if the given access token is valid
     */
    public static boolean testAccessTokenValid(AccessToken token) {
        return token != null && !token.isExpired();
    }

    /**
     * Helper function checking if the given access token includes specified login permission
     */
    public static boolean testTokenHasPermission(AccessToken token, FacebookLoginPermission permission) {
        return testAccessTokenValid(token) && token.getPermissions().contains(permission.toString());
    }

}
