package com.example.topza.piggy;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import java.util.Calendar;
import java.util.HashMap;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

/**
 * Created by nuuneoi on 11/16/2014.
 */
public class BalanceFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    HistoryTable history;
    DBHelper dbHelper;
    SliderLayout coinSlider;
    TextView textCountMoney;
    TextView bottomText;
    CountDownTimer countDownTimer;
    ImageView coin1;
    ImageView coin5;
    ImageView coin10;
    TextView textCurrency;
    TextView textAnimation;
    Button clearButton, connectButton;
    BluetoothSPP bt;

    double countMoney = 0.00;
    int tokenCountMoney = 5;
    boolean startTicker = false;
    int windowHeight, windowWidth;

    View.OnClickListener getButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doOnClick(v);
            }
        };
    }

    public void doOnClick(View v){
        switch (v.getId()){
            case R.id.ClearButton:
                countMoney = 0.00;
                ((HomeActivity) getActivity()).sendBluetoothText(Double.toString(countMoney));
                break;
            case R.id.ConnectButton:
//                bt.setupService();
//                bt.startService(BluetoothState.DEVICE_ANDROID);
                receiveUserBluetoothDialog((AppCompatActivity) getActivity(), "Bluetooth Connection",
                        "Please Input your Bluetooth connect device name.", "Connect");
                break;
        }
    }

    private AlertDialog receiveUserBluetoothDialog(final AppCompatActivity act, CharSequence title,
                                                   CharSequence message, CharSequence buttonYes){
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        final EditText bluetooth_name = new EditText(getContext());
        bluetooth_name.setInputType(InputType.TYPE_CLASS_TEXT);
        downloadDialog.setView(bluetooth_name);
        downloadDialog.setTitle(title).setMessage(message).setPositiveButton(buttonYes, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bt.autoConnect(bluetooth_name.getText().toString());
            }
        });
        return downloadDialog.show();
    }

    public BalanceFragment() {
        super();
    }

    public static BalanceFragment newInstance(Bundle sliderSet) {
        BalanceFragment fragment = new BalanceFragment();
        fragment.setArguments(sliderSet);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Read from Arguments from function getArguments().
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.balance_fragment, container, false);
        initInstances(rootView);
        return rootView;
    }

    private void initInstances(View rootView) {
        // Init 'View' instance(s) with rootView.findViewById here
        textCountMoney = (TextView) rootView.findViewById(R.id.TextCountMoneyFragment);
        textCurrency = (TextView) rootView.findViewById(R.id.TextCurrency);
        textAnimation = (TextView) rootView.findViewById(R.id.TextAnimation);
        bottomText = (TextView) rootView.findViewById(R.id.BottomTextFragment);
        coinSlider = (SliderLayout) rootView.findViewById(R.id.SliderFragment);
        clearButton = (Button) rootView.findViewById(R.id.ClearButton);
        connectButton = (Button) rootView.findViewById(R.id.ConnectButton);
        clearButton.setOnClickListener(getButtonOnClickListener());
        connectButton.setOnClickListener(getButtonOnClickListener());

        countDownTimer = new CountDownTimer(25000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (tokenCountMoney < 5) tokenCountMoney++;
            }

            @Override
            public void onFinish() {
                startTicker = false;
            }
        };

        dbHelper = new DBHelper(getContext());

        coin1 = (ImageView) rootView.findViewById(R.id.coin1Animation);
        coin5 = (ImageView) rootView.findViewById(R.id.coin5Animation);
        coin10 = (ImageView) rootView.findViewById(R.id.coin10Animation);

        initSlider();
    }

    private void initSlider() {

        HashMap<String, Integer> file_map = new HashMap<String, Integer>();
        if (getArguments().getBoolean("Check1"))
            file_map.put("1.00", R.drawable.coin1);
        if (getArguments().getBoolean("Check100"))
            file_map.put("100.00", R.drawable.coin1);
        if (getArguments().getBoolean("Check10"))
            file_map.put("10.00", R.drawable.coin10);
        if (getArguments().getBoolean("Check5"))
            file_map.put("5.00", R.drawable.coin5);
        if (getArguments().getBoolean("Check20"))
            file_map.put("20.00", R.drawable.coin1);

        for (String name : file_map.keySet()) {
            DefaultSliderView defaultSliderView = new DefaultSliderView(getContext());
            defaultSliderView
                    .image(file_map.get(name))
                    .setScaleType(BaseSliderView.ScaleType.CenterInside)
                    .setOnSliderClickListener(this);

            //add your extra information
            defaultSliderView.bundle(new Bundle());
            defaultSliderView.getBundle()
                    .putString("extra", name);

            coinSlider.addSlider(defaultSliderView);
        }

        coinSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        coinSlider.addOnPageChangeListener(this);
        coinSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
        coinSlider.stopAutoCycle();

        BaseSliderView current = coinSlider.getCurrentSlider();
        String i = current.getBundle().getString("extra");
        textCountMoney.setText(i);

        String s = "Click to send THB " + i + " to Piggy";
        bottomText.setText(s);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
     * Save Instance State Here
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Instance State here
        outState.putDouble("CountMoney", countMoney);
    }

    /*
     * Restore Instance State Here
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore Instance State here
            countMoney = savedInstanceState.getDouble("CountMoney");
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        BaseSliderView current = coinSlider.getCurrentSlider();

        String i = current.getBundle().getString("extra");
        textCountMoney.setText(i);

        String s = "Click to send THB " + i + " to Piggy";
        bottomText.setText(s);

        //Toast.makeText(getContext(), position + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        String addMoney = slider.getBundle().getString("extra");
        countMoney = countMoney + Double.parseDouble(addMoney);

        String s = Double.toString(countMoney);
        Calendar rightNow = Calendar.getInstance();

        history = new HistoryTable();
        history.setHistoryApp("Piggy");
        history.setHistoryMoney(addMoney);
        history.setHistoryTime(rightNow.get(Calendar.HOUR_OF_DAY) + ":" + rightNow.get(Calendar.MINUTE));
        history.setHistoryIO("Output");

        dbHelper.addHistory(history);

        ((HomeActivity) getActivity()).sendBluetoothText(s);

        textAnimation.setText("THB " + addMoney + " to Piggy");

        windowWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        windowHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();

        if (addMoney == "5.00") {
//            coin5.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) coin5.getLayoutParams();
//                    switch(event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            break;
//                        case MotionEvent.ACTION_MOVE:
//                            int x_cord = (int)event.getRawX();
//                            int y_cord = (int)event.getRawY();
//
//                            if(x_cord > windowWidth){x_cord = windowWidth;}
//                            if(y_cord > windowHeight){y_cord = windowHeight;}
//
//                            layoutParams.leftMargin = x_cord;
//                            layoutParams.topMargin = y_cord;
//
//                            coin5.setLayoutParams(layoutParams);
//                            if(layoutParams.topMargin == 100){
//                                Animation textFadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
//                                textAnimation.startAnimation(textFadeInAnimation);
//                                Toast.makeText(getContext(), "" + coinSlider.getCurrentPosition(), Toast.LENGTH_SHORT).show();
//                            }
//                            break;
//                        default:
//                            break;
//                    }
//                    return true;
//                }
//            });
            coinAnimation(coin5);
        } else coinAnimation(coin1);

        //Toast.makeText(getContext(), "Add " + addMoney + " To Piggy Your Money are " + countMoney, Toast.LENGTH_SHORT).show();
    }

    private void coinAnimation(final ImageView coin) {
        final MediaPlayer coin_sound = MediaPlayer.create(getContext(), R.raw.coin_drop_sound);
        Animation coinMoveAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_animation);
        coin_sound.setVolume(80, 80);
        coinMoveAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                setAnimationVisibility("VISIBLE");
                textAnimation.setVisibility(View.INVISIBLE);
                coin.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
                setAnimationVisibility("INVISIBLE");
                textAnimation.setVisibility(View.VISIBLE);
                coin_sound.start();
            }
        });
        coin.setVisibility(View.VISIBLE);
        coin.startAnimation(coinMoveAnimation);
    }

    private void setAnimationVisibility(String status) {
        int setting;

        switch (status) {
            case "INVISIBLE":
                setting = View.INVISIBLE;
                break;
            case "VISIBLE":
                setting = View.VISIBLE;
                break;
            default:
                setting = View.INVISIBLE;
                break;
        }

        bottomText.setVisibility(setting);
        coinSlider.setVisibility(setting);
        textCurrency.setVisibility(setting);
        textCountMoney.setVisibility(setting);
    }

    public double getCountMoney() {
        return countMoney;
    }

    public void setCountMoney(double cm) {
        countMoney = cm;
    }

}

