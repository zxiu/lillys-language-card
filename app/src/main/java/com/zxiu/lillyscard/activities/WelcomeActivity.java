package com.zxiu.lillyscard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.zxiu.lillyscard.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

/**
 * Created by Xiu on 10/10/206.
 */

public class WelcomeActivity extends AppCompatActivity {
    String TAG = this.getClass().toString();

    @BindView(R.id.pager)
    ViewPager pager;


    CallbackManager callbackManager;


    List<WelcomePageItem> welcomePageItems = new ArrayList<WelcomePageItem>() {
        {
            add(new WelcomePageItem(R.drawable.flag_uk, R.string.learn_english, null));
            add(new WelcomePageItem(R.drawable.flag_cn, R.string.learn_chinese, null));
            add(new WelcomePageItem(R.drawable.flag_de, R.string.learn_germany, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER,
                                            AuthUI.FACEBOOK_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                    finish();
                }
            }));
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        pager.setAdapter(new WelcomePageAdpater());
        pager.setOffscreenPageLimit(1);
        pager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                Log.i("tag", "page=" + page + " position=" + position);
            }
        });
        callbackManager = CallbackManager.Factory.create();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
//                startActivity(new Intent(this, WelcomeBackActivity.class));
                finish();
            } else {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or makeCardPlayerDialogBuilder a message
                finish();
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    class WelcomePageAdpater extends PagerAdapter {

        @Override
        public int getCount() {
            return welcomePageItems.size();
        }

        public WelcomePageItem getItem(int position) {
            return welcomePageItems.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeViewAt(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.page_welcome, null);
            ImageView image = (ImageView) view.findViewById(R.id.image);
            if (getItem(position).imageResId != 0) {
                image.setImageResource(welcomePageItems.get(position).imageResId);
            }
            TextView text = (TextView) view.findViewById(R.id.text);
            if (getItem(position).textResId != 0) {
                text.setText(welcomePageItems.get(position).textResId);
            }
            Button button = (Button) view.findViewById(R.id.button);
            button.setVisibility(getItem(position).onClickListener != null ? View.VISIBLE : View.GONE);
            button.setOnClickListener(getItem(position).onClickListener);


            LoginButton fbLoginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
            fbLoginButton.setReadPermissions("email");
            fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.i(TAG, "onSuccess=" + loginResult);
                }

                @Override
                public void onCancel() {
                    Log.i(TAG, "onCancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.i(TAG, "onError=" + error);
                }
            });


            container.addView(view, position);
            return view;
        }

    }

    class WelcomePageItem {
        int imageResId;
        int textResId;
        View.OnClickListener onClickListener;

        public WelcomePageItem(int imageResId, int textResId, View.OnClickListener clickListener) {
            this.imageResId = imageResId;
            this.textResId = textResId;
            this.onClickListener = clickListener;
        }
    }
}
