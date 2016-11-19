package com.zxiu.lillyscard.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.entities.Audio;
import com.zxiu.lillyscard.utils.MediaManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.facebook.login.widget.ProfilePictureView.TAG;

/**
 * Created by Xiu on 11/15/2016.
 */

public class ArtFragment extends Fragment implements View.OnClickListener {
    MediaManager mediaManager;
    Integer[] colors = new Integer[]{Color.MAGENTA, Color.BLUE, Color.rgb(0x33, 0x66, 0x66), Color.rgb(0x28, 0x00, 0x4d), Color.rgb(0x64, 0x21, 0x00), Color.rgb(0x00, 0x60, 0x30), Color.rgb(0x6c, 0x33, 0x65), Color.RED, Color.DKGRAY};
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    @BindView(R.id.audio)
    TextView currentTitle;
    @BindView(R.id.play)
    ImageView playButton;
    @BindView(R.id.pause)
    ImageView pauseButton;
    @BindView(R.id.forward)
    ImageView forwardButton;

    AudioViewAdapter adapter;

    MediaManager.MediaManagerListener audioChangeListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaManager = MediaManager.getInstance();
        adapter = new AudioViewAdapter();
        audioChangeListener = new MediaManager.MediaManagerListener() {
            @Override
            public void onStart(int index, Audio audio) {
                Log.i(TAG, "onStart");
                currentTitle.setText(audio.title + " - " + audio.artist);
                recyclerView.smoothScrollToPosition(index);
                adapter.notifyDataSetChanged();
                refreshControllerUI();
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_art, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        List<Integer> colorList = new ArrayList<>(Arrays.asList(colors));
        Collections.shuffle(colorList);
        colors = colorList.toArray(colors);
        mediaManager.addOnAudioChangeListener(audioChangeListener);
        refreshControllerUI();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @OnClick({R.id.play, R.id.pause, R.id.forward, R.id.audio})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.audio:
                recyclerView.smoothScrollToPosition(mediaManager.getCurrentMusicIndex());
                break;
            case R.id.play:
                mediaManager.resume();
                refreshControllerUI();
                break;
            case R.id.pause:
                mediaManager.pause();
                refreshControllerUI();
                break;
            case R.id.forward:
                mediaManager.next();
                refreshControllerUI();
                break;
        }
    }

    public void refreshControllerUI() {
        playButton.setVisibility(mediaManager.isPlaying() ? View.GONE : View.VISIBLE);
        pauseButton.setVisibility(mediaManager.isPlaying() ? View.VISIBLE : View.GONE);
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView title, artist, licence;
        int position;

        public AudioViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.title = (TextView) itemView.findViewById(R.id.title);
            this.artist = (TextView) itemView.findViewById(R.id.artist);
            this.licence = (TextView) itemView.findViewById(R.id.licence);
        }
    }

    class AudioViewAdapter extends RecyclerView.Adapter<AudioViewHolder> {

        @Override
        public AudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_audio, parent, false);
            return new AudioViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AudioViewHolder holder, final int position) {
            final Audio audio = mediaManager.getAudioList().get(position);
            holder.itemView.setBackgroundColor(colors[position % colors.length]);
            holder.itemView.setAlpha(mediaManager.getCurrentMusicIndex() == position ? 1.0f : 0.8f);
            int textColor = mediaManager.getCurrentMusicIndex() == position ? Color.argb(255, 255, 255, 255) : Color.argb(200, 255, 255, 255);
            holder.title.setText(audio.title);
            holder.title.setTextColor(textColor);
            holder.artist.setText("by: " + audio.artist);
            holder.artist.setTextColor(textColor);
            holder.licence.setText(audio.licence);
            holder.licence.setTextColor(textColor);
            holder.position = position;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Toast.makeText(getActivity(), "Long Click to Change", Toast.LENGTH_SHORT).show();
                    if (position != mediaManager.getCurrentMusicIndex()) {
                        mediaManager.changeTo(position);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (position != mediaManager.getCurrentMusicIndex()) {
                        mediaManager.changeTo(position);
                    }
                    return true;
                }
            });

        }

        @Override
        public int getItemCount() {
            return mediaManager.getAudioList().size();
        }
    }

    @Override
    public void onDestroy() {
        mediaManager.removeOnAudioChangeListener(audioChangeListener);
        super.onDestroy();
    }

    class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

        private Drawable mDivider;

        private int mOrientation;

        public DividerItemDecoration(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }

        }


        public void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                android.support.v7.widget.RecyclerView v = new android.support.v7.widget.RecyclerView(parent.getContext());
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }
    }
}
