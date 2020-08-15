package nways.towtruck.messenger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import nways.towtruck.R;

public class UserChat extends AppCompatActivity implements View.OnClickListener {
    Button btnClose, btnCall, btnSend;
    TextView tvMsgLeft, tvMsgRight, tvName;
    EditText etInputMsg;
    LinearLayout llChat;
    String getEtMsg;
    LinearLayout.LayoutParams lp;
    ScrollView scroll;

    String providerPhone, providerEmail, providerName, myLatUser, myLngUser, myEmailUser;

    String from_email, message;

    final String LOG_TAG = "myLogs";

    BroadcastReceiver mReceiver;

    SendMessage sendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);

        btnCall = findViewById(R.id.btnCall);
        btnCall.setOnClickListener(this);

        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        etInputMsg = findViewById(R.id.etInputMsg);
        llChat = findViewById(R.id.llChat);
        scroll = findViewById(R.id.scroll);
        tvName = findViewById(R.id.tvName);

        providerName = getIntent().getStringExtra("name");
        providerPhone = getIntent().getStringExtra("phone");
        providerEmail = getIntent().getStringExtra("email");
        myLatUser = getIntent().getStringExtra("myLat");
        myLngUser = getIntent().getStringExtra("myLng");
        myEmailUser = getIntent().getStringExtra("myEmail");

        tvName.setText(providerName);

        sendMessage = new SendMessage();

        startService(new Intent(this, UserChatService.class)
                .putExtra("myEmail", myEmailUser)
                .putExtra("providerEmail", providerEmail));

        IntentFilter intentFilter = new IntentFilter("android.intent.action.UPDATE");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                from_email = intent.getStringExtra("from_email");
                message = intent.getStringExtra("message");

                if (from_email.equals(myEmailUser)) createMessageRight(message);
                else createMessageLeft(message);

                Log.i(LOG_TAG, message);
            }
        };
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        stopService(new Intent(this, UserChatService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "ON STOP");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "ON PAUSE");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "ON RESTART");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void onClick(View v){
        switch(v.getId()) {
            case R.id.btnClose: finish(); break;

            case R.id.btnSend: if (etInputMsg.getText().toString().equals("")) return;
                else {
                    getEtMsg = etInputMsg.getText().toString();
                    etInputMsg.setText("");
                }
                sendMessage.sendMessage(myEmailUser, providerEmail, getEtMsg);
                break;

            case R.id.btnCall: startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + providerPhone)));
                break;
        }
    }

    public void createMessage(String msg, TextView tv, LinearLayout.LayoutParams lp){
        tv.setPadding(13, 13, 13, 13);
        tv.setTextSize(15);
        tv.setMaxWidth(350);
        tv.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        tv.setText(msg);

        llChat.addView(tv, lp);
        scroll.post(new Runnable() { public void run() { scroll.fullScroll(scroll.FOCUS_DOWN); } });
    }

    public void createMessageLeft(String msg){
        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 6;
        lp.bottomMargin = 6;
        lp.leftMargin = 12;
        lp.gravity = Gravity.LEFT;
        tvMsgLeft = new TextView(this);
        tvMsgLeft.setTextColor(getResources().getColor(R.color.chatLeftText));
        tvMsgLeft.setBackground(getResources().getDrawable(R.drawable.chatleft));
        createMessage(msg, tvMsgLeft, lp);
    }

    public void createMessageRight(String msg){
        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 6;
        lp.bottomMargin = 6;
        lp.rightMargin = 12;
        lp.gravity = Gravity.RIGHT;
        tvMsgRight = new TextView(this);
        tvMsgRight.setTextColor(getResources().getColor(R.color.chatRightText));
        tvMsgRight.setBackground(getResources().getDrawable(R.drawable.chatright));
        createMessage(msg, tvMsgRight, lp);
    }
}
