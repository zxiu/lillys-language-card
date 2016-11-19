package com.zxiu.lillyscard;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.util.Base64;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.ProfileTracker;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.zxiu.lillyscard.entities.Audio;
import com.zxiu.lillyscard.entities.CardGroup;
import com.zxiu.lillyscard.entities.CardItem;
import com.zxiu.lillyscard.entities.Image;
import com.zxiu.lillyscard.entities.Player;
import com.zxiu.lillyscard.entities.ScoreboardEntry;
import com.zxiu.lillyscard.entities.Video;
import com.zxiu.lillyscard.listeners.OnFirebaseLoadListener;
import com.zxiu.lillyscard.utils.LocaleHelper;
import com.zxiu.lillyscard.utils.SoundManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Xiu on 10/10/2016.
 */

public class App extends MultiDexApplication {
    public static String TAG = "LillyApp";
    public static App context;
    public static boolean DEBUG = false;

    FirebaseDatabase database;
    DatabaseReference groupsRef, itemsRef, initPlayerRef, userRef, playersRef, videoRef, audioRef, imageRef;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseAuth firebaseAuth;
    static List<CardGroup> cardGroups = new ArrayList<>();
    static List<CardItem> cardItems = new ArrayList<>();

    public static List<Video> videos = new ArrayList<>();
    public static List<Audio> audios = new ArrayList<>();
    public static List<Image> images = new ArrayList<>();

    public static List<Player> initPlayerList;
    public static List<Player> myPlayerList = new ArrayList<>();


    static Set<OnFirebaseLoadListener> onFirebaseLoadListeners = new HashSet<>();

    public static boolean isCardItemLoaded, isCardGroupLoaded, isVideoLoaded, isAudioLoaded, isImageLoaded;

    CallbackManager callbackManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "App onCreate");
        FirebaseApp.initializeApp(this);
        SoundManager.init(this);
        Fresco.initialize(this);
        FacebookSdk.sdkInitialize(this);

        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        printHashKey();
        context = this;
        DEBUG &= (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) && Build.FINGERPRINT.contains("generic");
        LocaleHelper.init(this);
        firebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.w(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    onFirebaseAuthenticated();
                } else {
                    Log.w(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        firebaseAuth.addAuthStateListener(mAuthListener);

        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        groupsRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/groups");
        itemsRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/items");
        initPlayerRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/players");
        videoRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/videos");
        audioRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/audios");
        imageRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/public/images");
//
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    GenericTypeIndicator<List<CardGroup>> groupListGenericTypeIndicator = new GenericTypeIndicator<List<CardGroup>>() {
                    };
                    cardGroups = dataSnapshot.getValue(groupListGenericTypeIndicator);
                    Log.i(TAG, "firebase cardGroups = " + cardGroups);
                    isCardGroupLoaded = true;
                    onCardLoaded();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<List<CardItem>> itemListGenericTypeIndicator = new GenericTypeIndicator<List<CardItem>>() {
                    };
                    cardItems = dataSnapshot.getValue(itemListGenericTypeIndicator);
                    Log.i(TAG, "firebase cardItems = " + cardItems);
                    isCardItemLoaded = true;
                    onCardLoaded();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        initPlayerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<List<Player>> cardPlayersGenericTypeIndicator = new GenericTypeIndicator<List<Player>>() {
                    };
                    initPlayerList = dataSnapshot.getValue(cardPlayersGenericTypeIndicator);
                }
                Log.i(TAG, "initPlayerList=" + initPlayerList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<List<Video>> videoListGenericTypeIndicator = new GenericTypeIndicator<List<Video>>() {
                    };
                    videos = dataSnapshot.getValue(videoListGenericTypeIndicator);
                    for (Video video : videos) {
                        video.fetchDashUrl();
                    }
                    Log.i(TAG, "firebase videos = " + videos);
                    isVideoLoaded = true;
                    onVideoLoaded();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        audioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<List<Audio>> videoListGenericTypeIndicator = new GenericTypeIndicator<List<Audio>>() {
                    };
                    audios = dataSnapshot.getValue(videoListGenericTypeIndicator);
                    Log.i(TAG, "firebase audios = " + audios);
                    isAudioLoaded = true;
                    onAudioLoaded();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GenericTypeIndicator<List<Image>> imageListGenericTypeIndicator = new GenericTypeIndicator<List<Image>>() {
                    };
                    images = dataSnapshot.getValue(imageListGenericTypeIndicator);
                    Log.i(TAG, "firebase images = " + images);
                    isImageLoaded = true;
                    onImageLoaded();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        callbackManager = CallbackManager.Factory.create();
        ProfileTracker tracker;
    }

    @Override
    public void onTerminate() {
        firebaseAuth.removeAuthStateListener(mAuthListener);
        super.onTerminate();

    }

    public void onFirebaseAuthenticated() {
        Log.i(TAG,"onFirebaseAuthenticated");
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userRef = database.getReferenceFromUrl("https://lillys-card.firebaseio.com/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
//            playersRef = userRef.child("players");
//            playersRef.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.getValue() != null) {
//                        GenericTypeIndicator<List<Player>> cardPlayersGenericTypeIndicator = new GenericTypeIndicator<List<Player>>() {
//                        };
//                        myPlayerList = dataSnapshot.getValue(cardPlayersGenericTypeIndicator);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
        }
    }

    public static boolean isCardLoaded() {
        return isCardItemLoaded && isCardGroupLoaded;
    }

    protected static void onCardLoaded() {
        if (isCardLoaded()) {
            for (OnFirebaseLoadListener onFirebaseLoadListener : onFirebaseLoadListeners) {
                onFirebaseLoadListener.onCardLoaded();
            }
        }
    }

    protected static void onVideoLoaded() {
        if (isVideoLoaded) {
            for (OnFirebaseLoadListener onFirebaseLoadListener : onFirebaseLoadListeners) {
                onFirebaseLoadListener.onVideoLoaded();
            }
        }
    }

    protected static void onAudioLoaded() {
        if (isAudioLoaded) {
            for (OnFirebaseLoadListener onFirebaseLoadListener : onFirebaseLoadListeners) {
                onFirebaseLoadListener.onAudioLoaded(audios);
            }
        }
    }

    protected static void onImageLoaded() {
        if (isImageLoaded) {
            for (OnFirebaseLoadListener onFirebaseLoadListener : onFirebaseLoadListeners) {
                onFirebaseLoadListener.onImageLoaded(images);
            }
        }
    }

    public static List<CardGroup> getCardGroups() {
        return cardGroups;
    }

    public static List<CardItem> getCardItems() {
        return cardItems;
    }


    public static List<CardItem> getCardItemsByGroupName(String group) {
        List<CardItem> clonedCardItems = new ArrayList<>();
        for (CardItem cardItem : getCardItems()) {
            if (cardItem.groups.contains(group)) {
                clonedCardItems.add(cardItem);
            }
        }
        return clonedCardItems;
    }

    public static List<CardItem> getCardItemsByGroupIndex(int groupIndex) {
        return getCardItemsByGroupName(App.getCardGroups().get(groupIndex).name);
    }


    public static void addOnFirebaseLoadListeners(OnFirebaseLoadListener onFirebaseLoadListener) {
        onFirebaseLoadListeners.add(onFirebaseLoadListener);
    }

    public static void removeOnCardLoadListeners(OnFirebaseLoadListener onFirebaseLoadListener) {
        onFirebaseLoadListeners.remove(onFirebaseLoadListener);
    }

    public static void fireAll() {

    }

    public void printHashKey() {
        try {

            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }


    @Exclude
    public static List<Player> getSamplePlayers(boolean autoSelect) {
        List<Player> playerList = new ArrayList<>();
        if (App.myPlayerList == null || App.myPlayerList.isEmpty()) {
            App.myPlayerList = App.initPlayerList;
        }
        if (App.myPlayerList == null || App.myPlayerList.isEmpty() || App.myPlayerList.size() < 2) {
            Player player0 = new Player();
            player0.name = "Angel";
            player0.imageUrl = "https://farm1.static.flickr.com/83/219282536_43f50257e0.jpg";
            Player player1 = new Player();
            player1.name = "Viki";
            player1.imageUrl = "http://flikie.s3.amazonaws.com/ImageStorage/46/4695764c523d4b94a09e19cb36a60826.jpg";
            playerList.add(player0);
            playerList.add(player1);
        } else {
            playerList.addAll(App.myPlayerList);
        }
        return playerList;
    }


    public void storeMyPlayers(List<Player> players) {
        playersRef.setValue(players);
    }

    /**
     * Facebooks
     */

    private int score = 0;
    private int bombs = 0;
    private int coins = 0;
    private int coinsCollected = 0;
    private int topScore = 0;

    boolean loggedIn = false;
    private JSONArray friends;
    private JSONObject currentFBUser;
    public static final String CURRENT_FB_USER_KEY = "current_fb_user";
    private ArrayList<ScoreboardEntry> scoreboardEntriesList = null;

    private String lastFriendSmashedID = null;

    private String lastFriendSmashedName = null;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        if (!loggedIn) {
            setScore(0);
            setCurrentFBUser(null);
            setFriends(null);
            setLastFriendSmashedID(null);
            setScoreboardEntriesList(null);
        }
    }


    public JSONObject getCurrentFBUser() {
        return currentFBUser;
    }

    public void setCurrentFBUser(JSONObject currentFBUser) {
        this.currentFBUser = currentFBUser;
    }

    public int getTopScore() {
        return topScore;
    }

    public void setTopScore(int topScore) {
        this.topScore = topScore;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getBombs() {
        return bombs;
    }

    public void setBombs(int bombs) {
        this.bombs = bombs;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getCoinsCollected() {
        return coinsCollected;
    }

    public void setCoinsCollected(int coinsCollected) {
        this.coinsCollected = coinsCollected;
    }

    public void setLastFriendSmashedID(String lastFriendSmashedID) {
        this.lastFriendSmashedID = lastFriendSmashedID;
    }

    public String getLastFriendSmashedName() {
        return lastFriendSmashedName;
    }

    public JSONArray getFriends() {
        return friends;
    }

    public ArrayList<String> getFriendsAsArrayListOfStrings() {
        ArrayList<String> friendsAsArrayListOfStrings = new ArrayList<String>();

        int numFriends = friends.length();
        for (int i = 0; i < numFriends; i++) {
            friendsAsArrayListOfStrings.add(getFriend(i).toString());
        }

        return friendsAsArrayListOfStrings;
    }

    public ArrayList<ScoreboardEntry> getScoreboardEntriesList() {
        return scoreboardEntriesList;
    }

    public void setScoreboardEntriesList(ArrayList<ScoreboardEntry> scoreboardEntriesList) {
        this.scoreboardEntriesList = scoreboardEntriesList;
    }

    public JSONObject getFriend(int index) {
        JSONObject friend = null;
        if (friends != null && friends.length() > index) {
            friend = friends.optJSONObject(index);
        }
        return friend;
    }

    public void setFriends(JSONArray friends) {
        this.friends = friends;
    }


    public String userAgent;
    boolean cache = true;

    public DataSource.Factory buildDataSourceFactory(final DefaultBandwidthMeter bandwidthMeter) {
        if (!cache) {
            return new DefaultDataSourceFactory(this, bandwidthMeter,
                    buildHttpDataSourceFactory(bandwidthMeter));
        }

        return new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024);
                SimpleCache simpleCache = new SimpleCache(new File(getCacheDir(), "media_cache"), evictor);


                return new CacheDataSource(simpleCache, buildCachedHttpDataSourceFactory(bandwidthMeter).createDataSource(),
                        new FileDataSource(), new CacheDataSink(simpleCache, 10 * 1024 * 1024),
                        CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
            }
        };
    }

    private DefaultDataSource.Factory buildCachedHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }
}
