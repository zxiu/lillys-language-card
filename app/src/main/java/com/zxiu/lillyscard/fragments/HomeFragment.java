package com.zxiu.lillyscard.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.widget.LikeView;
import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.activities.MainActivity;
import com.zxiu.lillyscard.fb.FacebookLogin;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Xiu on 10/31/2016.
 */

public class HomeFragment extends Fragment implements View.OnClickListener {
    static final String TAG = "HomeFragment";
    FacebookLogin facebookLogin;

    @BindView(R.id.app_name)
    TextView appName;
    @BindView(R.id.btn_combat)
    Button buttonCombat;
    @BindView(R.id.btn_study)
    Button buttonStudy;
    @BindView(R.id.btn_setting)
    Button buttonSetting;
    @BindView(R.id.btn_art)
    Button buttonAbout;

    //Facebook views
    @BindView(R.id.fb_login_button)
    LoginButton loginButton;
    @BindView(R.id.fb_like_view)
    LikeView likeView;
    @BindView(R.id.fb_profile_picture)
    ProfilePictureView profilePicture;

    View.OnClickListener onClickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "fonts/CookieMonster.ttf");
        appName.setTypeface(type);
        ((MainActivity) getActivity()).setLoginButton(loginButton);
        ((MainActivity) getActivity()).setProfilePictureView(profilePicture);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        likeView.setObjectIdAndType("https://lillys-card.firebaseapp.com/", LikeView.ObjectType.OPEN_GRAPH);
    }

    @OnClick({R.id.btn_combat, R.id.btn_study, R.id.btn_setting, R.id.btn_art})
    @Override
    public void onClick(View view) {
        if (onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setAppNameColor(int color) {
        appName.setTextColor(color);
    }
}
