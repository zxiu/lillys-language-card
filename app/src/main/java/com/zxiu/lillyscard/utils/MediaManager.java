package com.zxiu.lillyscard.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.zxiu.lillyscard.App;
import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.entities.Audio;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Xiu on 10/30/2016.
 */

public class MediaManager implements MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {


    private MediaPlayer mediaPlayer;
    private int currentMusicIndex = 0;
    private List<Audio> audioList = new ArrayList<>();

    private static MediaManager mediaManager;
    private List<MediaManagerListener> mediaManagerListenerList = new ArrayList<>();


    public static MediaManager getInstance() {
        if (mediaManager == null) {
            mediaManager = new MediaManager();
        }
        return mediaManager;
    }

    public MediaManager setSource(List<Audio> audioList, boolean random) {
        this.audioList = audioList;
        currentMusicIndex = random ? new Random().nextInt(audioList.size()) : 0;
        return this;
    }

    private MediaManager() {
        mediaPlayer = new MediaPlayer();
    }

    void initMediaPlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setVolume(1f, 1f);
        mediaPlayer.setLooping(false);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();
    }


    public MediaManager start() {
        new AudioStreamWorkerTask(App.context, new AudioStreamWorkerTask.OnCacheCallback() {
            @Override
            public void onSuccess(FileInputStream fileInputStream) {
                initMediaPlayer();
                if (fileInputStream != null) {
                    // reset media player here if necessary
                    try {
                        mediaPlayer.setDataSource(fileInputStream.getFD());
                        mediaPlayer.prepare();
                        fileInputStream.close();
                    } catch (IOException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(getClass().getSimpleName() + ".MediaPlayer", "fileDescriptor is not valid");
                }
            }

            @Override
            public void onError() {
                Log.e(getClass().getSimpleName() + ".MediaPlayer", "Can't play audio file");
            }
        }).execute(audioList.get(currentMusicIndex).url);
        return this;
    }

    private void fireAllChangeListener() {
        for (MediaManagerListener listener : mediaManagerListenerList) {
            listener.onStart(currentMusicIndex, audioList.get(currentMusicIndex));
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (allowToPlay()){
            mediaPlayer.start();
        }
        fireAllChangeListener();
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()&& allowToPlay()) {
            mediaPlayer.start();
        }
    }

    public void next() {
        if (audioList != null && audioList.size() > 0) {
            pause();
            currentMusicIndex = (currentMusicIndex + 1) % audioList.size();
            start();
        }
    }

    public List<Audio> getAudioList() {
        return audioList;
    }

    public int getCurrentMusicIndex() {
        return currentMusicIndex;
    }

    public void addOnAudioChangeListener(MediaManagerListener mediaManagerListener) {
        mediaManagerListenerList.add(mediaManagerListener);
        if (mediaPlayer.isPlaying()) {
            fireAllChangeListener();
        }
    }

    public void removeOnAudioChangeListener(MediaManagerListener mediaManagerListener) {
        mediaManagerListenerList.remove(mediaManagerListener);
    }

    public interface MediaManagerListener {
        public void onStart(int index, Audio audio);
    }

    public void changeTo(int index) {
        if (index < audioList.size()) {
            pause();
            currentMusicIndex = index;
            start();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    private boolean allowToPlay() {
        return (boolean) SettingManager.getValue(App.context.getString(R.string.key_music), true);
    }
}
