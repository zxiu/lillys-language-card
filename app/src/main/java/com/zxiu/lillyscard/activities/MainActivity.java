package com.zxiu.lillyscard.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.zxiu.lillyscard.App;
import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.entities.Audio;
import com.zxiu.lillyscard.fb.FacebookLogin;
import com.zxiu.lillyscard.fb.FacebookLoginCallback;
import com.zxiu.lillyscard.fb.FacebookLoginPermission;
import com.zxiu.lillyscard.fragments.ArtFragment;
import com.zxiu.lillyscard.fragments.BgFragment;
import com.zxiu.lillyscard.fragments.GameFragment;
import com.zxiu.lillyscard.fragments.HomeFragment;
import com.zxiu.lillyscard.fragments.SettingFragment;
import com.zxiu.lillyscard.listeners.OnFirebaseLoadListenerImp;
import com.zxiu.lillyscard.services.MyService;
import com.zxiu.lillyscard.utils.ExceptionHandler;
import com.zxiu.lillyscard.utils.MediaManager;

import org.json.JSONObject;

import java.util.List;

import static com.zxiu.lillyscard.App.audios;
import static com.zxiu.lillyscard.R.id.content_container;

/**
 * Created by Xiu on 10/10/2016.
 */

public class MainActivity extends AppCompatActivity implements FacebookLoginCallback {
    static String TAG = MainActivity.class.getSimpleName();

    FacebookLogin facebookLogin;
    LoginButton loginButton;
    MediaManager mediaManager;
    protected static int REQUEST_CODE_SETTING = 4096;
    protected static long lastAdTime = 0;
    Handler handler;
    FrameLayout bgContainer, contentContainer;
    AdView adView;
    HomeFragment homeFragment;
    GameFragment gameFragment;
    SettingFragment settingFragment;
    BgFragment bgFragment;
    InterstitialAd mInterstitialAd;
    boolean isInterstitialAdShowing;

    ProfilePictureView profilePictureView;


    String s0 = "http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0";
    String s1 = "https://manifest.googlevideo.com/api/manifest/dash/initcwndbps/642500/source/youtube/pl/47/nh/IgpwZjAxLmZyYTE1KhYyMDAxOjQ4NjA6MToxOjA6MTowOjE1/requiressl/yes/mm/31/mn/sn-4g57knky/ipbits/0/mt/1478211272/mv/m/ms/au/signature/D5EF3A1CD375E7A1EE75ACD73F22779B3DF60817.65080D7E9985317AA921CF190BB9CD94D6DB21D4/as/fmp4_audio_clear,webm_audio_clear,webm2_audio_clear,fmp4_sd_hd_clear,webm2_sd_hd_clear/expire/1478233056/key/yt6/ip/2003:75:ce17:3dac:5922:560c:6972:6722/itag/0/id/o-ANqRDKdz_rp3fUPudMLpir7YCt7ERe2bPodsLO57h4Pi/upn/fPdzlemSPd8/playback_host/r8---sn-4g57knky.googlevideo.com/sparams/as,hfr,id,initcwndbps,ip,ipbits,itag,mm,mn,ms,mv,nh,pl,playback_host,requiressl,source,expire/hfr/1";
    String s2 = "https://manifest.googlevideo.com/api/manifest/dash/as/fmp4_audio_clear,webm_audio_clear,webm2_audio_clear,fmp4_sd_hd_clear,webm2_sd_hd_clear/initcwndbps/285000/expire/1478209268/key/yt6/itag/0/signature/2A52ED5E368670EAA2FBB106F2F792A0D65AC1BF.64BD93B9E5F4B3884C9135F008F5D14FBE238F6C/source/youtube/nh/IgpwcjAyLmZyYTE2KgkxMjcuMC4wLjE/upn/SIVFXaN0HZY/hfr/1/ipbits/0/playback_host/r1---sn-4g57kn7z.googlevideo.com/mn/sn-4g57kn7z/mm/31/ip/2003:75:ce17:3dbd:5922:560c:6972:6722/sparams/as,hfr,id,initcwndbps,ip,ipbits,itag,mm,mn,ms,mv,nh,pl,playback_host,requiressl,source,expire/pl/47/mv/m/mt/1478187372/ms/au/requiressl/yes/id/o-AAT6eWszbE2hcZkCUy89Xs3L6blqSvG_RxDH1tPu4dZk";
    String s3 = "https://manifest.googlevideo.com/api/manifest/dash/upn/2ZUA1u1Ssxk/as/fmp4_audio_clear,webm_audio_clear,webm2_audio_clear,fmp4_sd_hd_clear,webm2_sd_hd_clear/itag/0/playback_host/r1---sn-4g5edn7y.googlevideo.com/mt/1478189236/pl/47/mv/m/id/o-APGSz27W4EmaYD944FgATaRFoUd4jsVH55vvzk2eEGyB/ms/au/ip/2003:75:ce17:3dbd:5922:560c:6972:6722/mm/31/source/youtube/mn/sn-4g5edn7y/sparams/as,hfr,id,initcwndbps,ip,ipbits,itag,mm,mn,ms,mv,nh,pl,playback_host,requiressl,source,expire/requiressl/yes/key/yt6/initcwndbps/677500/expire/1478210961/signature/3DA2454B8C00EE9E3022A7F603BF2794940A6E46.D3A2E3127823D83CB34B4505304BBE005FA2A1BE/ipbits/0/nh/IgpwZjAyLmZyYTE1KhgyMDAxOjQ4NjA6MToxOjA6Y2Y4OjA6MTM/hfr/1";
    String s4 = "https://manifest.googlevideo.com/api/manifest/dash/sparams/as,hfr,id,initcwndbps,ip,ipbits,itag,mm,mn,ms,mv,nh,pl,playback_host,requiressl,source,expire/ip/2003:75:ce17:3dac:5922:560c:6972:6722/hfr/1/id/o-AOtz_M7Mb4LybMT881Eu8SbavIYzDLqxsKauCk651foY/initcwndbps/548750/ipbits/0/playback_host/r8---sn-4g57knky.googlevideo.com/key/yt6/source/youtube/signature/0C205115925BCD21504C4D7BE9DE9F6FA14AFEC9.3607A16A308F75064F6E4A5B7D66E6C262378098/nh/IgpwcjAyLmZyYTE1KgkxMjcuMC4wLjE/requiressl/yes/mm/31/mn/sn-4g57knky/expire/1478225852/as/fmp4_audio_clear,webm_audio_clear,webm2_audio_clear,fmp4_sd_hd_clear,webm2_sd_hd_clear/upn/TX1_6tuYo7Y/ms/au/itag/0/mt/1478203950/pl/47/mv/m";
    String[] ss = new String[]{s1, s0};
    String[] es = new String[]{"mpd", "mpd"};

    public MyService.MyBinder myBinder;
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myBinder = (MyService.MyBinder) iBinder;
            myBinder.startDownload();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService();
        handler = new Handler();
        facebookLogin = FacebookLogin.getInstance().addLoginCallback(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        setContentView(R.layout.activity_main);
        bgContainer = (FrameLayout) findViewById(R.id.background_container);
        contentContainer = (FrameLayout) findViewById(content_container);

        adView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        bgFragment = new BgFragment();
        Bundle arguments = new Bundle();
//        arguments.putString(VideoFragment.EXTENSION_EXTRA, "mpd");
//        arguments.putString("URI", s0);
//        arguments.putString("URI", s1);
//        arguments.putString("URI", s2);
//        arguments.putString("URI", s3);
//        arguments.putString("ACTION", VideoFragment.ACTION_VIEW);
//        arguments.putString("ACTION", VideoFragment.ACTION_VIEW_LIST);
//        arguments.putStringArray(VideoFragment.URI_LIST_EXTRA, ss);
//        arguments.putStringArray(VideoFragment.EXTENSION_LIST_EXTRA, es);

        bgFragment.setArguments(arguments);
        bgFragment.addBgChangedListener(new BgFragment.OnBgChangedListener() {
            @Override
            public void onImageChanged(Palette palette) {
                homeFragment.setAppNameColor(Color.CYAN);
            }

            @Override
            public void onMusicChanged() {

            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.background_container, bgFragment).commit();

        homeFragment = new HomeFragment();
        homeFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = null;
                Bundle arguments = new Bundle();
                switch (view.getId()) {
                    case R.id.btn_combat:
                        gameFragment = new GameFragment();
                        fragment = gameFragment;
                        arguments.putInt(GameFragment.EXTRA_MODE, GameFragment.MODE_COMBAT);
                        fragment.setArguments(arguments);
                        break;
                    case R.id.btn_study:
                        gameFragment = new GameFragment();
                        fragment = gameFragment;
                        arguments.putInt(GameFragment.EXTRA_MODE, GameFragment.MODE_STUDY);
                        fragment.setArguments(arguments);
                        break;
                    case R.id.btn_setting:
                        settingFragment = new SettingFragment();
                        fragment = settingFragment;
                        break;
                    case R.id.btn_art:
                        fragment = new ArtFragment();
                        break;
                }
                if (fragment != null) {
                    goTo(fragment);
                }
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.content_container, homeFragment).commit();
        initInterstitialAd();
        onLoginStateChanged(null, AccessToken.getCurrentAccessToken());

        if (App.isAudioLoaded) {
            startPlayMusic(audios);
        } else {
            App.addOnFirebaseLoadListeners(new OnFirebaseLoadListenerImp() {
                @Override
                public void onAudioLoaded(List<Audio> audios) {
                    startPlayMusic(audios);
                }
            });
        }
    }

    protected void startService() {
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    protected void startPlayMusic(List<Audio> audios) {
        mediaManager = MediaManager.getInstance().setSource(audios, true).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaManager != null && !isInterstitialAdShowing) {
            mediaManager.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mediaManager != null) {
            mediaManager.resume();
        }
    }

    private void initInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                onInterstitialClosed();
            }
        });
        requestNewInterstitial();
    }

    public void showInterstitial() {
        if (mInterstitialAd.isLoaded() && System.currentTimeMillis() - lastAdTime > 1000 * 60 * 5) {
            isInterstitialAdShowing = true;
            mInterstitialAd.show();
            lastAdTime = System.currentTimeMillis();
        } else {
            onInterstitialClosed();
        }
    }

    public void onInterstitialClosed() {
        isInterstitialAdShowing = false;
        newRound();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mInterstitialAd.loadAd(adRequest);
    }


    protected void goTo(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.content_container, fragment)
                .addToBackStack(fragment.getClass().toString())
                .commit();
    }

    public void newRound() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .detach(gameFragment)
                .attach(gameFragment)
                .commit();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.w(TAG, "handleFacebookAccessToken:" + token + " FirebaseAuth.getInstance().getCurrentUser()=" + FirebaseAuth.getInstance().getCurrentUser());
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.w(TAG, "handleFacebookAccessToken signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "signInWithCredential failed.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "signInWithCredential is Successful.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookLogin.getCallbackManager().onActivityResult(requestCode, resultCode, data);
    }

    public void setProfilePictureView(ProfilePictureView profilePictureView) {
        if (profilePictureView != null) {
            this.profilePictureView = profilePictureView;
            profilePictureView.setProfileId(fbId);
            profilePictureView.setVisibility(View.VISIBLE);
        }
    }

    public void setLoginButton(LoginButton loginButton) {
        if (loginButton != null) {
            this.loginButton = loginButton;
            facebookLogin.setLoginButton(loginButton);
        }
    }

    String fbId;

    @Override
    public void onProfileLoadListener(JSONObject user, String id, String name, String photoUrl) {
        Log.i(TAG, "onProfileLoadListener =" + user + " \n" + profilePictureView);
        if (profilePictureView != null) {
            profilePictureView.setProfileId(id);
            profilePictureView.setVisibility(View.VISIBLE);
        }
        fbId = id;
        ((App) getApplication()).setCurrentFBUser(user);
    }

    @Override
    public void onLoginStateChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
        Log.i(TAG, "onLoginStateChanged oldAccessToken=" + oldAccessToken + " currentAccessToken=" + currentAccessToken);
        boolean isLoggedIn = ((App) getApplication()).isLoggedIn();
        if (FacebookLogin.isAccessTokenValid() && !isLoggedIn) {
//            fetchUserInformationAndLogin();
            handleFacebookAccessToken(currentAccessToken);
            ((App) getApplication()).setLoggedIn(true);
            facebookLogin.loadProfile(AccessToken.getCurrentAccessToken());
        } else if (!FacebookLogin.isAccessTokenValid() && isLoggedIn) {
            ((App) getApplication()).setLoggedIn(false);
            logOut();
//            showFragment(FB_LOGGED_OUT_HOME);
        } else if (FacebookLogin.testTokenHasPermission(currentAccessToken, FacebookLoginPermission.USER_FRIENDS) &&
                !FacebookLogin.testTokenHasPermission(oldAccessToken, FacebookLoginPermission.USER_FRIENDS)) {
//            ((HomeFragment)fragments[HOME]).onUserFriendsGranted();
        } else if (FacebookLogin.testTokenHasPermission(currentAccessToken, FacebookLoginPermission.PUBLISH_ACTIONS) &&
                !FacebookLogin.testTokenHasPermission(oldAccessToken, FacebookLoginPermission.PUBLISH_ACTIONS)) {
//            ((HomeFragment)fragments[HOME]).onPublishActionsGranted();
        }
    }

    private void logOut() {
        if (profilePictureView != null) {
            profilePictureView.setProfileId(null);
            profilePictureView.setVisibility(View.GONE);
        }
        LoginManager.getInstance().logOut();
        recreate();
    }
}
