/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zxiu.lillyscard.fragments;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelections;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.zxiu.lillyscard.App;
import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.utils.EventLogger;
import com.zxiu.lillyscard.utils.TrackSelectionHelper;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An activity that plays media using {@link SimpleExoPlayer}.
 */
public class VideoFragment extends Fragment implements OnClickListener, ExoPlayer.EventListener,
        TrackSelector.EventListener<MappedTrackInfo>, PlaybackControlView.VisibilityListener {
    static final String TAG = "VideoFragment";
    public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
    public static final String DRM_LICENSE_URL = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    public static final String ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW";
    public static final String EXTENSION_EXTRA = "extension";

    public static final String ACTION_VIEW_LIST =
            "com.google.android.exoplayer.demo.action.VIEW_LIST";
    public static final String URI_LIST_EXTRA = "uri_list";
    public static final String EXTENSION_LIST_EXTRA = "extension_list";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Handler mainHandler;
    private Timeline.Window window;
    private EventLogger eventLogger;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SurfaceView surfaceView;
    private LinearLayout debugRootView;
    private TextView debugTextView;
    private Button retryButton;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private MappingTrackSelector trackSelector;
    private TrackSelectionHelper trackSelectionHelper;
    private DebugTextViewHelper debugViewHelper;
    private boolean playerNeedsSource;

    private boolean shouldAutoPlay;
    private boolean isTimelineStatic;
    private int playerWindow;
    private long playerPosition;

    private Bundle arguments;
    private boolean showControl = false;
    // Activity lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldAutoPlay = true;
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        window = new Timeline.Window();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        arguments = getArguments();
    }

    View contentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_video, container, false);
        View rootView = contentView.findViewById(R.id.root);
        rootView.setOnClickListener(this);


        debugRootView = (LinearLayout) contentView.findViewById(R.id.controls_root);
        debugTextView = (TextView) contentView.findViewById(R.id.debug_text_view);
        retryButton = (Button) contentView.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(this);

        simpleExoPlayerView = (SimpleExoPlayerView) contentView.findViewById(R.id.player_view);
        simpleExoPlayerView.setControllerVisibilityListener(this);
        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.setVisibility(showControl ? View.VISIBLE : View.GONE);

        surfaceView = (SurfaceView) contentView.findViewById(R.id.surface_view);
        surfaceView.setVisibility(showControl ? View.GONE : View.VISIBLE);

        return contentView;
    }

//    @Override
//    public void onNewIntent(Intent intent) {
//        releasePlayer();
//        isTimelineStatic = false;
//        setIntent(intent);
//    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer();
        } else {
            showToast(R.string.storage_permission_denied);
//            finish();
        }
    }

    // OnClickListener methods
    @Override
    public void onClick(View view) {
        if (view == retryButton) {
            initializePlayer();
        } else if (view.getParent() == debugRootView) {
            trackSelectionHelper.showSelectionDialog(getActivity(), ((Button) view).getText(),
                    trackSelector.getCurrentSelections().info, (int) view.getTag());
        }
    }

    // PlaybackControlView.VisibilityListener implementation

    @Override
    public void onVisibilityChange(int visibility) {
        debugRootView.setVisibility(visibility);
    }

    // Internal methods

    private void initializePlayer() {
//        Intent intent = getIntent();


        if (player == null) {
            boolean preferExtensionDecoders = arguments.getBoolean(PREFER_EXTENSION_DECODERS, false);
            UUID drmSchemeUuid = arguments.getString(DRM_SCHEME_UUID_EXTRA) != null
                    ? UUID.fromString(arguments.getString(DRM_SCHEME_UUID_EXTRA)) : null;
            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            if (drmSchemeUuid != null) {
                String drmLicenseUrl = arguments.getString(DRM_LICENSE_URL);
                String[] keyRequestPropertiesArray = arguments.getStringArray(DRM_KEY_REQUEST_PROPERTIES);
                Map<String, String> keyRequestProperties;
                if (keyRequestPropertiesArray == null || keyRequestPropertiesArray.length < 2) {
                    keyRequestProperties = null;
                } else {
                    keyRequestProperties = new HashMap<>();
                    for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                        keyRequestProperties.put(keyRequestPropertiesArray[i],
                                keyRequestPropertiesArray[i + 1]);
                    }
                }
                try {
                    drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl,
                            keyRequestProperties);
                } catch (UnsupportedDrmException e) {
                    int errorStringId = Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                            : (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                            ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
                    showToast(errorStringId);
                    return;
                }
            }

            eventLogger = new EventLogger();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
            trackSelector.addListener(this);
            trackSelector.addListener(eventLogger);
            trackSelectionHelper = new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, new DefaultLoadControl(),
                    drmSessionManager, preferExtensionDecoders);

            player.addListener(this);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setId3Output(eventLogger);

            if (showControl) {
                simpleExoPlayerView.setPlayer(player);
            } else {
                player.setVideoSurfaceView(surfaceView);
            }


            if (isTimelineStatic) {
                if (playerPosition == C.TIME_UNSET) {
                    player.seekToDefaultPosition(playerWindow);
                } else {
                    player.seekTo(playerWindow, playerPosition);
                }
            }
            player.setPlayWhenReady(shouldAutoPlay);
            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();
            playerNeedsSource = true;
        }
        if (playerNeedsSource) {
            String action = arguments.getString("ACTION");
            Uri[] uris;
            String[] extensions;
            if (ACTION_VIEW.equals(action)) {
                Log.i("PlayActivity", "ACTION_VIEW");
                uris = new Uri[]{Uri.parse(arguments.getString("URI"))};
                extensions = new String[]{arguments.getString(EXTENSION_EXTRA)};
            } else if (ACTION_VIEW_LIST.equals(action)) {
                Log.i("PlayActivity", "ACTION_VIEW_LIST");
                String[] uriStrings = arguments.getStringArray(URI_LIST_EXTRA);
                uris = new Uri[uriStrings.length];
                for (int i = 0; i < uriStrings.length; i++) {
                    uris[i] = Uri.parse(uriStrings[i]);
                }
                extensions = arguments.getStringArray(EXTENSION_LIST_EXTRA);
                if (extensions == null) {
                    extensions = new String[uriStrings.length];
                }

            } else {
                showToast(getString(R.string.unexpected_intent_action, action));
                return;
            }
            if (Util.maybeRequestReadExternalStoragePermission(getActivity(), uris)) {
                // The player will be reinitialized if the permission is granted.
                return;
            }
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            player.prepare(mediaSource, !isTimelineStatic, !isTimelineStatic);
            playerNeedsSource = false;
            updateButtonVisibilities();
        }

        final String userAgent = ((App) getActivity().getApplication()).userAgent;
        final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        final Allocator allocator = new DefaultAllocator(true, BUFFER_SEGMENT_SIZE);

//        Cache cache = new SimpleCache(getActivity().getCacheDir(), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 10));
//        DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
//        CacheDataSource cacheDataSource = new CacheDataSource(cache, dataSource, false, false);
//
//        ExtractorMediaSource sampleSource = new ExtractorMediaSource(uri
//                , cacheDataSource
//                , allocator
//                , BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE
//                , new Mp4Extractor());

    }

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 160;


    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                : uri.getLastPathSegment());
        Log.i("PlayActivity", "Uri=" + uri);
        Log.i("PlayActivity", "overrideExtension=" + overrideExtension + " type=" + type);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(true),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
                                                                           String licenseUrl, Map<String, String> keyRequestProperties) throws UnsupportedDrmException {
        if (Util.SDK_INT < 18) {
            return null;
        }
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(false), keyRequestProperties);
        return new StreamingDrmSessionManager<>(uuid,
                FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
    }

    private void releasePlayer() {
        if (player != null) {
            debugViewHelper.stop();
            debugViewHelper = null;
            shouldAutoPlay = player.getPlayWhenReady();
            playerWindow = player.getCurrentWindowIndex();
            playerPosition = C.TIME_UNSET;
            Timeline timeline = player.getCurrentTimeline();
            if (timeline != null && timeline.getWindow(playerWindow, window).isSeekable) {
                playerPosition = player.getCurrentPosition();
            }
            player.release();
            player = null;
            trackSelector = null;
            trackSelectionHelper = null;
            eventLogger = null;
        }
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((App) getActivity().getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return ((App) getActivity().getApplication())
                .buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // ExoPlayer.EventListener implementation

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
            player.seekToDefaultPosition(0);
        }
        updateButtonVisibilities();
    }

    @Override
    public void onPositionDiscontinuity() {
        // Do nothing.
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        isTimelineStatic = timeline != null && timeline.getWindowCount() > 0
                && !timeline.getWindow(timeline.getWindowCount() - 1, window).isDynamic;
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof DecoderInitializationException) {
                // Special case for decoder initialization failures.
                DecoderInitializationException decoderInitializationException =
                        (DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString);
        }
        playerNeedsSource = true;
        updateButtonVisibilities();
        showControls();
    }

    // MappingTrackSelector.EventListener implementation

    @Override
    public void onTrackSelectionsChanged(TrackSelections<? extends MappedTrackInfo> trackSelections) {
        updateButtonVisibilities();
        MappedTrackInfo trackInfo = trackSelections.info;
        if (trackInfo.hasOnlyUnplayableTracks(C.TRACK_TYPE_VIDEO)) {
            showToast(R.string.error_unsupported_video);
        }
        if (trackInfo.hasOnlyUnplayableTracks(C.TRACK_TYPE_AUDIO)) {
            showToast(R.string.error_unsupported_audio);
        }
    }

    // User controls

    private void updateButtonVisibilities() {
        debugRootView.removeAllViews();

        retryButton.setVisibility(playerNeedsSource ? View.VISIBLE : View.GONE);
        debugRootView.addView(retryButton);

        if (player == null) {
            return;
        }

        TrackSelections<MappedTrackInfo> trackSelections = trackSelector.getCurrentSelections();
        if (trackSelections == null) {
            return;
        }

        int rendererCount = trackSelections.length;
        for (int i = 0; i < rendererCount; i++) {
            TrackGroupArray trackGroups = trackSelections.info.getTrackGroups(i);
            if (trackGroups.length != 0) {
                Button button = new Button(getActivity());
                int label;
                switch (player.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        label = R.string.audio;
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = R.string.video;
                        break;
                    case C.TRACK_TYPE_TEXT:
                        label = R.string.text;
                        break;
                    default:
                        continue;
                }
                button.setText(label);
                button.setTag(i);
                button.setOnClickListener(this);
                debugRootView.addView(button, debugRootView.getChildCount() - 1);
            }
        }
    }

    private void showControls() {
        debugRootView.setVisibility(View.VISIBLE);
    }

    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

}
