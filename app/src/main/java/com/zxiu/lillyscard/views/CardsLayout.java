package com.zxiu.lillyscard.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.entities.CardItem;
import com.zxiu.lillyscard.listeners.OnCardToggleListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * Created by Xiu on 10/12/2016.
 */

public class CardsLayout extends FrameLayout {
    String TAG = CardsLayout.class.getName();

    double ratio = 5.7 / 8.8;
    public int columns = 4;
    public int rows = 4;
    int repeats;
    boolean showText;
    OnCardToggleListener onCardToggleListener;
    int dividerSize = 10;
    int width, height;
    int cardWidth, cardHeight;
    int offsetX, offsetY;
    boolean newRound = false;

    public List<CardView> cardViews = new ArrayList<>();

    protected List<CardItem> sourceCardItems = new ArrayList<>();
    public List<CardItem> cardItems = new ArrayList<>();

    public CardsLayout(Context context) {
        this(context, null);
    }

    public CardsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CardsLayout, 0, 0);
        try {
            rows = ta.getInteger(R.styleable.CardsLayout_rows, rows);
            columns = ta.getInteger(R.styleable.CardsLayout_columns, columns);
        } finally {
            ta.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e(TAG, "onLayout changed=" + changed);
        if (changed || newRound) {
            initCardViews();
            newRound = false;
        }
    }

    public void setSourceCardItems(List<CardItem> sourceCardItems, int repeats, boolean showText, OnCardToggleListener onCardToggleListener) {
        this.sourceCardItems.clear();
        this.sourceCardItems.addAll(sourceCardItems);
        this.repeats = repeats;
        this.onCardToggleListener = onCardToggleListener;
        this.showText = showText;
        this.newRound = true;
        this.requestLayout();
    }

    public void initCardViews() {
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        if (width != 0 && height != 0) {
            cardHeight = (height - dividerSize * (rows - 1)) / rows;
            cardWidth = (int) (cardHeight * ratio);
            offsetX = (width - (cardWidth * columns + dividerSize * (columns - 1))) / 2;
            offsetY = 0;
            if (offsetX <= 0) {
                cardWidth = (width - dividerSize * (columns - 1)) / columns;
                cardHeight = (int) (cardWidth / ratio);
                offsetX = 0;
                offsetY = (height - (cardHeight * rows + dividerSize * (rows - 1))) / 2;
            }
                /*
                cardHeight*rows + dividerSize*(rows-1) <= height
                cardHeight*ratio*columns + dividerSize*(columns-1) <= width
                 */
        }

        removeAllViews();
        cardViews.clear();
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                CardView cardView = new CardView(getContext());
                cardView.setLayoutParams(new LayoutParams(cardWidth, cardHeight));
                addView(cardView);
                cardView.setX(offsetX + i * (dividerSize + cardWidth));
                cardView.setY(offsetY + j * (dividerSize + cardHeight));
                cardViews.add(cardView);
            }
        }
        initCardItems();
    }

    public void initCardItems() {
        if (sourceCardItems.size() == 0 || cardViews.size() == 0) {
            return;
        }
        cardItems.clear();
        List<CardItem> clonedCardItems = new ArrayList<>();
        Random random = new Random(System.nanoTime());
        while (cardItems.size() < cardViews.size()) {

            if (clonedCardItems.size() == 0) {
                clonedCardItems.addAll(sourceCardItems);
            }
            int index = random.nextInt(clonedCardItems.size());
            for (int i = 0; i < repeats; i++) {
                cardItems.add(clonedCardItems.get(index));
            }
            clonedCardItems.remove(index);
            Log.i(TAG, "cardItems " + cardItems.size());
        }
        Collections.shuffle(cardItems, random);
        Collections.shuffle(cardItems, random);
        for (int i = 0; i < cardViews.size(); i++) {
            CardView cardView = cardViews.get(i);
            cardView.setCardItem(cardItems.get(i), showText);
            cardView.setOnClickListener(onCardToggleListener);
            cardView.setOnCardToggleListener(onCardToggleListener);
        }
    }

    public int getCardCount(Boolean opened, Boolean locked) {
        return getCardViews(opened, locked).size();
    }

    public int getTurningCardCount() {
        return getCardViews(null, null, true).size();
    }

    public List<CardView> getCardViews(Boolean opened, Boolean locked) {
        return getCardViews(opened, locked, null);
    }

    public List<CardView> getCardViews(Boolean opened, Boolean locked, Boolean turning) {
        List<CardView> selectedCardViews = new ArrayList<>();
        for (CardView cardView : cardViews) {
            if ((opened == null || opened == cardView.isOpened())
                    && (locked == null || locked == cardView.isLocked())
                    && (turning == null || cardView.isTurning() == turning)) {
                selectedCardViews.add(cardView);
            }
        }
        return selectedCardViews;
    }


}
