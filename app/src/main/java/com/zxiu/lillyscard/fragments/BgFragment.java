package com.zxiu.lillyscard.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zxiu.lillyscard.App;
import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.entities.Audio;
import com.zxiu.lillyscard.entities.Image;
import com.zxiu.lillyscard.listeners.OnFirebaseLoadListenerImp;
import com.zxiu.lillyscard.utils.MediaManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;

import static com.zxiu.lillyscard.App.audios;

/**
 * Created by Xiu on 11/7/2016.
 */

public class BgFragment extends Fragment {
    final int BACKGROUND_IMAGE_DURATION = 1000 * 60;

    @BindViews({R.id.image0, R.id.image1})
    SimpleDraweeView[] imageViews;
    List<Image> images = new ArrayList<>();
    int currentIndex = 0;
    boolean running = true;
    MediaManager mediaManager;
    List<OnBgChangedListener> onBgChangedListenerList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bg, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (App.isImageLoaded) {
            images = App.images;
            startShowImage();
        } else {
            App.addOnFirebaseLoadListeners(new OnFirebaseLoadListenerImp() {
                @Override
                public void onImageLoaded(List<Image> images) {
                    BgFragment.this.images = images;
                    startShowImage();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mediaManager != null) {
            mediaManager.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaManager != null) {
            mediaManager.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        running = false;
    }

    protected void startShowImage() {
        new Thread() {
            @Override
            public void run() {
                while (running) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showNextImage();
                        }
                    });
                    try {
                        Thread.sleep(BACKGROUND_IMAGE_DURATION);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    protected void showNextImage() {
        if (images.size() > 0) {
            Animation animIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
            animIn.setFillBefore(true);
            animIn.setDuration(2000);
            animIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            imageViews[currentIndex % imageViews.length].setVisibility(View.VISIBLE);
            imageViews[currentIndex % imageViews.length].setImageURI(images.get(currentIndex % images.size()).url);
            imageViews[currentIndex % imageViews.length].startAnimation(animIn);
            currentIndex++;
            Animation animOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            animOut.setFillAfter(true);
            animOut.setDuration(2000);
            animOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageViews[currentIndex % imageViews.length].setVisibility(View.INVISIBLE);
                    imageViews[currentIndex % imageViews.length].setImageURI(images.get(currentIndex % images.size()).url);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            imageViews[currentIndex % imageViews.length].startAnimation(animOut);
        }
    }

    protected void startPlayMusic(List<Audio> audios) {
//        mediaManager = new MediaManager(audios);
//        mediaManager.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    public void addBgChangedListener(OnBgChangedListener onBgChangedListener) {
        onBgChangedListenerList.add(onBgChangedListener);
    }

    public interface OnBgChangedListener {
        public void onImageChanged(Palette palette);

        public void onMusicChanged();
    }

    public class OnBgChangedListenerImp implements OnBgChangedListener {

        @Override
        public void onImageChanged(Palette palette) {

        }

        @Override
        public void onMusicChanged() {

        }
    }

}
