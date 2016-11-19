package com.zxiu.lillyscard.listeners;

import android.view.View;

import com.zxiu.lillyscard.views.CardView;

/**
 * Created by Xiu on 10/10/2016.
 */

public interface OnCardToggleListener extends View.OnClickListener {
    public void onToggleStart(CardView cardView, boolean open);

    public void onToggleEnd(CardView cardView, boolean open);
}
