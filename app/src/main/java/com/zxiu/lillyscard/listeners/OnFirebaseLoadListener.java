package com.zxiu.lillyscard.listeners;

import com.zxiu.lillyscard.entities.Audio;
import com.zxiu.lillyscard.entities.Image;

import java.util.List;

/**
 * Created by Xiu on 10/10/2016.
 */

public interface OnFirebaseLoadListener {
    public void onCardLoaded();
    public void onVideoLoaded();
    public void onAudioLoaded(List<Audio> audios);
    public void onImageLoaded(List<Image> images);
}
