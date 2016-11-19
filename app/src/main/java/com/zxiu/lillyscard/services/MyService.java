package com.zxiu.lillyscard.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.zxiu.lillyscard.entities.Audio;
import com.zxiu.lillyscard.entities.CardGroup;
import com.zxiu.lillyscard.entities.CardItem;
import com.zxiu.lillyscard.entities.Image;
import com.zxiu.lillyscard.entities.Video;
import com.zxiu.lillyscard.firebases.FirebaseRef;

import java.util.ArrayList;
import java.util.List;

import static com.zxiu.lillyscard.App.isAudioLoaded;

/**
 * Created by Xiu on 11/16/2016.
 */

public class MyService extends Service {
    String TAG = "MyService";

    static String HOST = "https://lillys-card.firebaseio.com";
    static String PUBLIC = "public";
    static String INITIATE = "initiate";

//    public enum FIREBASE_REF {
//        REF_GROUPS(PUBLIC, "groups"), REF_ITEMS(PUBLIC, "items");
//        String key;
//        DatabaseReference ref;
//
//        FIREBASE_REF(String... keys) {
//            StringBuilder $ = new StringBuilder(HOST);
//            for (String key : keys) {
//                $.append("/").append(key);
//            }
//            this.key = $.toString();
//            this.ref = FirebaseDatabase.getInstance().getReferenceFromUrl(key);
//        }
//
//        @Override
//        public String toString() {
//            return key;
//        }
//    }

    public static final String REF_GROUP = "https://lillys-card.firebaseio.com/public/groups";


    MyBinder mBinder = new MyBinder();
    FirebaseDatabase database;
    DatabaseReference groupsRef, itemsRef, initPlayerRef, userRef, playersRef, videoRef, audioRef, imageRef;
    List<CardGroup> cardGroups = new ArrayList<>();
    List<CardItem> cardItems = new ArrayList<>();
    List<Video> videos = new ArrayList<>();
    List<Audio> audios = new ArrayList<>();
    List<Image> images = new ArrayList<>();
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
        Log.i(TAG, " " + new FirebaseRef<CardGroup>(FirebaseRef.PATH.PUBLIC, FirebaseRef.PATH.GROUPS).ref);
//        firebaseAuth = FirebaseAuth.getInstance();
//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    Log.w(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//                    onFirebaseAuthenticated();
//                } else {
//                    Log.w(TAG, "onAuthStateChanged:signed_out");
//                }
//            }
//        };
//
//        firebaseAuth.addAuthStateListener(mAuthListener);


        groupsRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/groups");
        itemsRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/items");
        initPlayerRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/players");
        videoRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/videos");
        audioRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/audios");
        imageRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/images");

//        groupsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot != null) {
//                    cardGroups = dataSnapshot.getValue(new GenericTypeIndicator<List<CardGroup>>() {
//                    });
////                    Log.i(TAG, "firebase cardGroups = " + dataSnapshot.getValue(false).getClass().getName());
////                    onCardLoaded();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        itemsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() != null) {
//                    cardItems = dataSnapshot.getValue(new GenericTypeIndicator<List<CardItem>>() {
//                    });
////                    Log.i(TAG, "firebase cardItems = " + cardItems);
//                    isCardItemLoaded = true;
////                    onCardLoaded();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


//        initPlayerRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() != null) {
//                    initPlayerList = dataSnapshot.getValue(new GenericTypeIndicator<List<Player>>() {
//                    });
//                }
//                Log.i(TAG, "initPlayerList=" + initPlayerList);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() != null) {
//                    GenericTypeIndicator<List<Video>> videoListGenericTypeIndicator = new GenericTypeIndicator<List<Video>>() {
//                    };
//                    videos = dataSnapshot.getValue(videoListGenericTypeIndicator);
//                    for (Video video : videos) {
//                        video.fetchDashUrl();
//                    }
//                    Log.i(TAG, "firebase videos = " + videos);
//                    isVideoLoaded = true;
//                    onVideoLoaded();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
        audioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<List<Audio>> videoListGenericTypeIndicator = new GenericTypeIndicator<List<Audio>>() {
                    };
                    audios = dataSnapshot.getValue(videoListGenericTypeIndicator);
                    Log.i(TAG, "firebase audios = " + audios);
                    isAudioLoaded = true;
//                    onAudioLoaded();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() != null) {
//                    GenericTypeIndicator<List<Image>> imageListGenericTypeIndicator = new GenericTypeIndicator<List<Image>>() {
//                    };
//                    images = dataSnapshot.getValue(imageListGenericTypeIndicator);
//                    Log.i(TAG, "firebase images = " + images);
//                    isImageLoaded = true;
//                    onImageLoaded();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 开始执行后台任务
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    public class MyBinder extends Binder {

        public void startDownload() {
            Log.i(TAG, "startDownload");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 执行具体的下载任务
                }
            }).start();
        }


    }
}
