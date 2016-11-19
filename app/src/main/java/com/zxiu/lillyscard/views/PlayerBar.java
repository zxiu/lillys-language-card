package com.zxiu.lillyscard.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.zxiu.lillyscard.databinding.ViewPlayerBarBinding;
import com.zxiu.lillyscard.entities.Player;

import butterknife.ButterKnife;

/**
 * Created by Xiu on10/15/2016.
 */

public class PlayerBar extends FrameLayout {
    ViewPlayerBarBinding binding;

    public PlayerBar(Context context) {
        this(context, null);
    }

    public PlayerBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        binding = ViewPlayerBarBinding.inflate(LayoutInflater.from(context), this, true);
        ButterKnife.bind(this);
    }

    public void setPlayer(Player player) {
        Toast.makeText(getContext(), player.toString(), Toast.LENGTH_SHORT).show();
        binding.setPlayer(player);
        binding.notifyChange();
    }

    public void setCurrent(boolean current) {
        this.setAlpha(current ? 1.0f : 0.3f);
    }
}
