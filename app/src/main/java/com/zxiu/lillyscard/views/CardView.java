package com.zxiu.lillyscard.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.anim.Rotate3dAnimation;
import com.zxiu.lillyscard.entities.CardItem;
import com.zxiu.lillyscard.listeners.OnCardToggleListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Xiu on 10/9/2016.
 */

public class CardView extends FrameLayout {
    String TAG = CardView.class.getName();


    @BindView(R.id.view_front)
    public View viewFront;
    @BindView(R.id.view_back)
    public View viewBack;
    @BindView(R.id.name_text)
    public TextView nameText;
    @BindView(R.id.image_front)
    public ImageView imageFront;
    @BindView(R.id.image_back)
    public ImageView imageBack;

    private CardItem cardItem;

    private OnCardToggleListener onCardToggleListener;

    public CardView(Context context) {
        this(context, null);
    }

    public CardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CardView, 0, 0);
        try {
            opened = ta.getBoolean(R.styleable.CardView_open, false);
        } finally {
            ta.recycle();
        }
        inflate(context, opened ? R.layout.view_card_open : R.layout.view_card_close, this);
        ButterKnife.bind(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            centerX = (int) (getX() + this.getWidth() / 2);
            centerY = (int) (getY() + this.getHeight() / 2);
            initOpenAnim();
            initCloseAnim();
            initFlyAnim();
        }

    }

    public void setCardItem(CardItem cardItem, boolean showText) {
        if (cardItem == null) {
            return;
        }
        this.cardItem = cardItem;
        this.nameText.setText(cardItem.getDisplayName());
        this.nameText.setVisibility(showText ? VISIBLE : GONE);
        Glide.with(getContext()).load(cardItem.imageUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageFront);
    }

    public CardItem getCardItem() {
        return cardItem;
    }


    private boolean opened = false;
    private boolean locked = false;
    private boolean turning = false;

    public boolean isOpened() {
        return opened;
    }


    public void open() {
        if (!opened) {
            toggle();
        }
    }

    public void close() {
        if (opened) {
            toggle();
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setTurning(boolean turning) {
        this.turning = turning;
    }

    public boolean isTurning() {
        return turning;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void toggle() {
        if (isLocked() || isTurning()) {
            return;
        }
        if (closeAnimation.hasStarted() && !closeAnimation.hasEnded()) {
            return;
        }
        this.startAnimation(opened ? closeAnimation : openAnimation);
        setTurning(true);
        opened = !opened;

    }

    private int centerX;
    private int centerY;
    private int depthZ = 200;
    private int duration = 400;
    private Rotate3dAnimation openAnimation;
    private Rotate3dAnimation closeAnimation;
    private Animation flyAnimation;


    /**
     * 卡牌文本介绍打开效果：注意旋转角度
     */
    private void initOpenAnim() {
//        Log.i(TAG, "centerX=" + centerX + " centerY=" + centerY);
        //从0到90度，顺时针旋转视图，此时reverse参数为true，达到90度时动画结束时视图变得不可见，
        openAnimation = new Rotate3dAnimation(0, 90, centerX, centerY, depthZ, true);
        openAnimation.setDuration(duration);
        openAnimation.setFillAfter(true);
        openAnimation.setInterpolator(new AccelerateInterpolator());
        openAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                if (onCardToggleListener != null) {
                    onCardToggleListener.onToggleStart(CardView.this, opened);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewBack.setVisibility(View.GONE);
                viewFront.setVisibility(View.VISIBLE);

                //从270到360度，顺时针旋转视图，此时reverse参数为false，达到360度动画结束时视图变得可见
                Rotate3dAnimation rotateAnimation = new Rotate3dAnimation(270, 360, centerX, centerY, depthZ, false);
                rotateAnimation.setDuration(duration);
                rotateAnimation.setFillAfter(true);
                rotateAnimation.setInterpolator(new DecelerateInterpolator());
                rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        setTurning(false);
                        if (onCardToggleListener != null) {
                            onCardToggleListener.onToggleEnd(CardView.this, opened);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                CardView.this.startAnimation(rotateAnimation);

            }
        });
    }


    /**
     * 卡牌文本介绍关闭效果：旋转角度与打开时逆行即可
     */
    private void initCloseAnim() {
        closeAnimation = new Rotate3dAnimation(360, 270, centerX, centerY, depthZ, true);
        closeAnimation.setDuration(duration);
        closeAnimation.setFillAfter(true);
        closeAnimation.setInterpolator(new AccelerateInterpolator());
        closeAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                if (onCardToggleListener != null) {
                    onCardToggleListener.onToggleStart(CardView.this, opened);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewBack.setVisibility(View.VISIBLE);
                viewFront.setVisibility(View.GONE);

                Rotate3dAnimation rotateAnimation = new Rotate3dAnimation(90, 0, centerX, centerY, depthZ, false);
                rotateAnimation.setDuration(duration);
                rotateAnimation.setFillAfter(true);
                rotateAnimation.setInterpolator(new DecelerateInterpolator());
                rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        setTurning(false);
                        if (onCardToggleListener != null) {
                            onCardToggleListener.onToggleEnd(CardView.this, opened);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                CardView.this.startAnimation(rotateAnimation);
            }
        });
    }

    public void fly(){
        this.startAnimation(flyAnimation);
    }

    private void initFlyAnim() {
        flyAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        flyAnimation.setFillAfter(true);
        flyAnimation.setInterpolator(new AccelerateInterpolator());
        flyAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                CardView.this.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void setOnCardToggleListener(OnCardToggleListener onCardToggleListener) {
        this.onCardToggleListener = onCardToggleListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardView cardView = (CardView) o;

        return cardItem != null ? cardItem.equals(cardView.cardItem) : cardView.cardItem == null;

    }

    @Override
    public int hashCode() {
        return cardItem != null ? cardItem.hashCode() : 0;
    }
}
