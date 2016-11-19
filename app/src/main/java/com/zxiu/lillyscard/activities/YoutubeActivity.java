package com.zxiu.lillyscard.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.zxiu.lillyscard.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Xiu on 10/30/2016.
 */

public class YoutubeActivity extends YouTubeBaseActivity {
    String YOUTUBE_API_KEY = "AIzaSyDgdiG3lejozn3gE6N1hoe8UytlVBA9ENY";
    @BindView(R.id.youtube_player_view)
    YouTubePlayerView youTubePlayerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        ButterKnife.bind(this);

        youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                Log.i("Youtube", "onInitializationSuccess");
                youTubePlayer.setFullscreen(true);
                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                youTubePlayer.loadVideo("OiinNlX1OSU");
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e("Youtube", "onInitializationFailure youTubeInitializationResult="+youTubeInitializationResult);
            }
        });
    }
}
