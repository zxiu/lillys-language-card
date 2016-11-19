package com.zxiu.lillyscard.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.zxiu.lillyscard.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xiu on 10/15/2016.
 */

public class SoundManager {
    static SoundPool soundPool;
    static MediaPlayer mediaPlayer;
    static boolean loadComplete;
    static Map<SOUND_TYPE, Integer> soundMap = new HashMap<>();

    public enum SOUND_TYPE {
        CORRECT, ERROR
    }

    public static void init(Context context) {
        soundPool = new SoundPool(3, AudioManager.STREAM_VOICE_CALL, 0);
        soundMap.put(SOUND_TYPE.CORRECT, soundPool.load(context, R.raw.sound_correct_0, 1));
        soundMap.put(SOUND_TYPE.ERROR, soundPool.load(context, R.raw.sound_error_0, 1));
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loadComplete = true;
            }
        });

        mediaPlayer = new MediaPlayer();
    }

    public static void playEffect(SOUND_TYPE soundType) {
        if (loadComplete) {
            soundPool.play(soundMap.get(soundType), 1, 1, 0, 0, 1.0f);
        }
    }
}
