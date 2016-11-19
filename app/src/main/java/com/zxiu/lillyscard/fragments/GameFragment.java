package com.zxiu.lillyscard.fragments;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zxiu.lillyscard.App;
import com.zxiu.lillyscard.R;
import com.zxiu.lillyscard.activities.MainActivity;
import com.zxiu.lillyscard.entities.CardGroup;
import com.zxiu.lillyscard.entities.CardItem;
import com.zxiu.lillyscard.entities.Player;
import com.zxiu.lillyscard.listeners.OnCardToggleListener;
import com.zxiu.lillyscard.listeners.OnFirebaseLoadListenerImp;
import com.zxiu.lillyscard.utils.SettingManager;
import com.zxiu.lillyscard.utils.SoundManager;
import com.zxiu.lillyscard.views.CardView;
import com.zxiu.lillyscard.views.CardsLayout;
import com.zxiu.lillyscard.views.PlayerBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.grantland.widget.AutofitTextView;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Xiu on 11/2/2016.
 */

public class GameFragment extends Fragment implements OnCardToggleListener {
    static final String TAG = "GameFragment";
    public static final String EXTRA_MODE = "MODE", EXTRA_GROUP_INDEX = "GROUP_INDEX", EXTRA_PLAYER_INDEX = "PLAYER_INDEX";
    public static final int MODE_COMBAT = 0, MODE_STUDY = 1;
    static final int REPEATS_COMBAT = 2, REPEATS_STUDY = 1;

    private Handler handler;
    private int mode;
    private View view;
    private View playerBarContainer;
    private PlayerBar playerBar0, playerBar1;
    private Player player0, player1;
    int COUNT_PLAYERS = 2;
    private CardsLayout cardsLayout;
    private LayoutTransition layoutTransition;

    private List<Player> allPlayers;
    List<Player> selectedPlayers;
    private PlayersAdapter adapter;
    Dialog progressDialog, selectPlayerDialog, bigCardDialog, newRoundDialog;
    int groupIndex;
    int currentPlayerIndex;
    TextToSpeech textToSpeech;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        handler = new Handler();
        parseArguments();
    }

    void parseArguments() {
        mode = getArguments().getInt(EXTRA_MODE);
        groupIndex = getArguments().getInt(EXTRA_GROUP_INDEX);
        currentPlayerIndex = getArguments().getInt(EXTRA_PLAYER_INDEX, 0);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        view = inflater.inflate(R.layout.fragment_game, container, false);
        playerBarContainer = view.findViewById(R.id.player_bar_container);
        playerBarContainer.setVisibility(isCombat() ? View.VISIBLE : View.GONE);

        playerBar0 = (PlayerBar) view.findViewById(R.id.player_bar_0);
        playerBar1 = (PlayerBar) view.findViewById(R.id.player_bar_1);
        playerBar0.setOnClickListener(this);
        playerBar1.setOnClickListener(this);

        cardsLayout = (CardsLayout) view.findViewById(R.id.cards_layout);
        cardsLayout.rows = getRows();
        cardsLayout.columns = getColumns();

//        initTransition();
        initTTS();
        showProgress();
        return view;
    }

    void initTTS() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            }
        });
    }

    protected void speakCard(CardItem cardItem, long delay) {
        if (textToSpeech != null && !textToSpeech.isSpeaking() && (Boolean) SettingManager.getValue(getString(R.string.key_speech), true)) {
            final String toSpeak = cardItem.getDisplayName();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }, delay);
        }
    }

    void initTransition() {
        layoutTransition = new LayoutTransition();
        layoutTransition.setAnimator(LayoutTransition.APPEARING, layoutTransition.getAnimator(LayoutTransition.APPEARING));
        layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, ObjectAnimator.ofFloat(this, "alpha", 1, 0).setDuration(500));
        cardsLayout.setLayoutTransition(layoutTransition);
    }

    void showProgress() {
        if (App.isCardLoaded()) {
            OnDataLoaded();
        } else {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("loading resources");
            App.addOnFirebaseLoadListeners(new OnFirebaseLoadListenerImp() {
                public void onCardLoaded() {
                    progressDialog.cancel();
                    OnDataLoaded();
                }
            });
            progressDialog.show();
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    protected void OnDataLoaded() {
        Log.i(TAG, "OnDataLoaded()");
        cardsLayout.rows = getRows();
        cardsLayout.columns = getColumns();
        groupIndex = groupIndex % App.getCardGroups().size();
        Log.i(TAG, "groupIndex=" + groupIndex);
        cardsLayout.setSourceCardItems(App.getCardItemsByGroupIndex(groupIndex), getRepeats(), showText(), this);
        initPlayers();
    }


    protected int getRows() {
        if (isCombat()) {
            return App.DEBUG ? 2 : (int) SettingManager.getValue("COMBAT_ROW", 3);
        } else {
            return App.DEBUG ? 2 : (int) SettingManager.getValue("STUDY_ROW", 3);
        }

    }

    protected int getColumns() {
        if (isCombat()) {
            return App.DEBUG ? 2 : (int) SettingManager.getValue("COMBAT_COLUMN", 8);
        } else {
            return App.DEBUG ? 2 : (int) SettingManager.getValue("STUDY_COMULN", 6);
        }
    }

    private int getRepeats() {
        return isCombat() ? REPEATS_COMBAT : REPEATS_STUDY;
    }

    private boolean showText() {
        return isCombat() ? false : false;
    }

    protected void initPlayers() {
        Log.i(TAG, "initPlayers()");
        allPlayers = App.getSamplePlayers(true);
        if (getSelectedPlayers().size() != COUNT_PLAYERS) {
            selectedPlayers = allPlayers.subList(0, COUNT_PLAYERS);
        } else {
            selectedPlayers = getSelectedPlayers();
        }
        for (Player player : allPlayers) {
            player.setPoint(0);
            if (!selectedPlayers.contains(player)) {
                player.setPoint(0);
            } else {
                player.selected = true;
            }
        }
        player0 = selectedPlayers.get(0);
        player1 = selectedPlayers.get(1);
        playerBar0.setPlayer(player0);
        playerBar0.invalidate();
        playerBar1.setPlayer(player1);
        playerBar1.invalidate();
        updatePlayerBarUI();
    }

    protected void updatePlayerBarUI() {
        playerBar0.setCurrent(currentPlayerIndex == 0);
        playerBar1.setCurrent(currentPlayerIndex == 1);
    }

    protected void newRound() {
//        showInterstitial();
//        finish();
//        Intent intent = new Intent(this, this.getClass());
//        intent.putExtra(EXTRA_PLAYER_INDEX, currentPlayerIndex);
//        intent.putExtra(EXTRA_GROUP_INDEX, groupIndex + 1);
//        startActivity(intent);
        if (isCombat()) {
            ((MainActivity) getActivity()).showInterstitial();
        } else {
            ((MainActivity) getActivity()).newRound();
        }

    }


    private boolean isCombat() {
        return mode == MODE_COMBAT;
    }

    @Override
    public void onClick(View view) {
        if (view instanceof CardView) {
            CardView cardView = (CardView) view;
            if (!cardView.isOpened() && cardsLayout.getCardCount(true, false) < getRepeats()) {
                cardView.open();
            } else if (!isCombat() && cardView.isOpened()) {
                showBigCard(cardView.getCardItem());
            }
        } else if (view instanceof PlayerBar) {
            showChoosePlayerDialog();
        }
    }

    private void showChoosePlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_player).setCancelable(false);
        adapter = new PlayersAdapter(allPlayers, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btn_ok) {
                    if (selectPlayerDialog != null) {
                        if (getSelectedPlayers().size() == COUNT_PLAYERS) {
                            if (selectedPlayers.containsAll(getSelectedPlayers())) {

                            } else {
                                newRound();
                            }
                            ((App) getActivity().getApplication()).storeMyPlayers(allPlayers);
                            selectPlayerDialog.dismiss();
                        } else {
                            Toast.makeText(view.getContext(), getResources().getQuantityString(R.plurals.need_select_x_players, COUNT_PLAYERS, COUNT_PLAYERS), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        });
        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (adapter.getItem(which).selected && getSelectedPlayers().size() <= COUNT_PLAYERS
                        || !adapter.getItem(which).selected && getSelectedPlayers().size() < COUNT_PLAYERS
                        || adapter.getItem(which).selected) {
                    adapter.getItem(which).toggleSelected();
                }
                adapter.notifyDataSetChanged();
            }
        });
        selectPlayerDialog = builder.show();
    }

    @Override
    public void onToggleStart(CardView cardView, boolean open) {
        //do nothing
    }

    @Override
    public void onToggleEnd(CardView cardView, boolean open) {
        if (cardsLayout.getCardCount(true, false) == getRepeats() && cardsLayout.getTurningCardCount() == 0) {
            if (isAllEqual(cardsLayout.getCardViews(true, false))) {
                onReachRepeatsAllEqual();
            } else {
                onReachRepeatsNotEqual();
            }
        } else {
        }
    }


    protected void onReachRepeatsAllEqual() {
        SoundManager.playEffect(SoundManager.SOUND_TYPE.CORRECT);
        Log.i(TAG, "isCombat=" + isCombat());
        if (isCombat()) {
            for (final CardView cardView : cardsLayout.getCardViews(true, false)) {
                Animation fadeOutAnim;
                fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                fadeOutAnim.setFillAfter(true);
                fadeOutAnim.setStartOffset(500);
                fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        cardView.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                cardView.setLocked(true);
                cardView.startAnimation(fadeOutAnim);
            }
            getCurrentPlayer().addPoint(1);
            if (cardsLayout.getCardViews(true, true).size() == cardsLayout.cardViews.size()) {
                onAllOpened();
            }
        } else {
            for (CardView cardView : cardsLayout.getCardViews(true, false)) {
                cardView.setLocked(true);
                showBigCard(cardView.getCardItem());
            }
        }
    }

    void showBigCard(final CardItem cardItem) {
        if (bigCardDialog != null && bigCardDialog.isShowing()) {
            bigCardDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Light_DialogWhenLarge_NoActionBar);
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_card, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakCard(cardItem, 0);
            }
        });
        SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.image);
        AutofitTextView textView = (AutofitTextView) view.findViewById(R.id.text);
        imageView.setImageURI(cardItem.imageUrl);
        textView.setText(cardItem.getDisplayName());
        builder.setView(view);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (cardsLayout.getCardViews(true, true).size() == cardsLayout.cardViews.size()) {
                    onAllOpened();
                }
            }
        });
        bigCardDialog = builder.create();
        bigCardDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                view.performClick();
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bigCardDialog.show();
            }
        }, 500);
    }

    protected Player getCurrentPlayer() {
        return selectedPlayers.get(currentPlayerIndex);
    }

    protected void onReachRepeatsNotEqual() {
        Log.i(TAG, "onReachRepeatsNotEqual");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (CardView cardView : cardsLayout.getCardViews(true, false)) {
                    cardView.setLocked(false);
                    cardView.close();
                }
                nextCurrentPlayer();
            }
        }, 500);
        SoundManager.playEffect(SoundManager.SOUND_TYPE.ERROR);
    }

    protected void nextCurrentPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % selectedPlayers.size();
        Log.i(TAG, "nextCurrentPlayer=" + currentPlayerIndex);
        updatePlayerBarUI();
    }

    private void onAllOpened() {
        if (isCombat()) {
            if (selectedPlayers.get(0).getPoint() == selectedPlayers.get(1).getPoint()) {
                showWinnerDialog(2);
            } else {
                showWinnerDialog(selectedPlayers.get(0).point > selectedPlayers.get(1).point ? 0 : 1);
            }
        } else {
            showChooseGroupDialog();
        }

    }


    private void showWinnerDialog(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.round_end).setCancelable(false);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_winner, null);
        SimpleDraweeView imageView0 = (SimpleDraweeView) view.findViewById(R.id.image0);
        SimpleDraweeView imageView1 = (SimpleDraweeView) view.findViewById(R.id.image1);
        imageView0.setImageURI(selectedPlayers.get(0).imageUrl);
        imageView1.setImageURI(selectedPlayers.get(1).imageUrl);

        AutofitTextView textView = (AutofitTextView) view.findViewById(R.id.text);
        if (index == 2) {
            textView.setText(getString(R.string.it_is_a_draw));
        } else {
//            textView.setText(getString(R.string.winner_is, selectedPlayers.get(index).name));
            if (index == 0) {
                imageView0.setAspectRatio(1);
                imageView0.setVisibility(View.VISIBLE);
            } else if (index == 1) {
                imageView1.setAspectRatio(1);
                imageView1.setVisibility(View.VISIBLE);
            }
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newRoundDialog != null && newRoundDialog.isShowing()) {
                    newRoundDialog.dismiss();
                }
                groupIndex++;
                newRound();
            }
        });
        builder.setView(view);
        newRoundDialog = builder.create();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                newRoundDialog.show();
            }
        }, 1000);
    }

    private void showChooseGroupDialog() {
        if (newRoundDialog != null && newRoundDialog.isShowing()) {
            newRoundDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_choose_group, null);
        final ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(new ItemGroupAdapter());
        pager.setCurrentItem(groupIndex + 1 + App.getCardGroups().size() * 2000);
        Button buttonOK = (Button) view.findViewById(R.id.btn_ok);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick");
                newRoundDialog.dismiss();
                groupIndex = pager.getCurrentItem() % App.getCardGroups().size();
                newRound();

            }
        });
        builder.setView(view);
        newRoundDialog = builder.create();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                newRoundDialog.show();
            }
        }, 1000);
    }

    class ItemGroupAdapter extends PagerAdapter {

        public CardGroup getItem(int position) {
            return App.getCardGroups().get(position % App.getCardGroups().size());
        }

        @Override
        public int getCount() {
            return 100000;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getItem(position).getDisplayName();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            CardGroup group = getItem(position);
            ImageView imageView = new ImageView(getActivity());
            Glide.with(getActivity()).load(group.imageUrl).into(imageView);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    protected boolean isAllEqual(List<?> objects) {
        boolean equal = true;
        for (int i = 0; i < objects.size() - 1; i++) {
            if (!objects.get(i).equals(objects.get(i + 1))) {
                equal = false;
                break;
            }
        }
        return equal;
    }


    class PlayersAdapter extends BaseAdapter {
        List<Player> players;
        View.OnClickListener onButtonClickListener;

        public PlayersAdapter(List<Player> players, View.OnClickListener onButtonClickListener) {
            this.players = players;
            this.onButtonClickListener = onButtonClickListener;
        }

        class ViewHolderPlayer {
            SimpleDraweeView playerImage;
            CheckedTextView playerName;
        }

        class ViewHolderButton {
            Button buttonOK;
        }

        @Override
        public int getCount() {
            return players.size() + 1;
        }


        @Override
        public int getItemViewType(int position) {
            return position < getCount() - 1 ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public Player getItem(int position) {
            return players.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getItemViewType(position) == 0) {
                ViewHolderPlayer viewHolderPlayer;
                if (convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
                    viewHolderPlayer = new ViewHolderPlayer();
                    viewHolderPlayer.playerImage = (SimpleDraweeView) convertView.findViewById(R.id.player_image);
                    viewHolderPlayer.playerName = (CheckedTextView) convertView.findViewById(R.id.player_name);
                    convertView.setTag(viewHolderPlayer);
                } else {
                    viewHolderPlayer = (ViewHolderPlayer) convertView.getTag();
                }
                viewHolderPlayer.playerName.setText(getItem(position).name);
                viewHolderPlayer.playerName.setChecked(getItem(position).selected);
                viewHolderPlayer.playerImage.setImageURI(getItem(position).imageUrl);
            } else if (getItemViewType(position) == 1) {
                ViewHolderButton viewHolderButton;
                if (convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_button_ok, parent, false);
                    viewHolderButton = new ViewHolderButton();
                    viewHolderButton.buttonOK = (Button) convertView.findViewById(R.id.btn_ok);
                    convertView.setTag(viewHolderButton);
                } else {
                    viewHolderButton = (ViewHolderButton) convertView.getTag();
                }
                viewHolderButton.buttonOK.setOnClickListener(onButtonClickListener);
                viewHolderButton.buttonOK.setEnabled(getSelectedPlayers().size() == COUNT_PLAYERS);
            }
            return convertView;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((MainActivity) getActivity()).showInterstitial();
    }

    public List<Player> getSelectedPlayers() {
        List<Player> players = new ArrayList<>();
        for (Player player : allPlayers) {
            if (player.selected) {
                players.add(player);
            }
        }
        return players;
    }
}
