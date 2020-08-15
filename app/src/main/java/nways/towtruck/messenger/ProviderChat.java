package nways.towtruck.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import nways.towtruck.R;
import nways.towtruck.provider.ProviderScreen;
import nways.towtruck.provider.ProviderScreenMain;
import nways.towtruck.server.HttpParse;

public class ProviderChat extends AppCompatActivity implements View.OnClickListener {
    Button btnClose, btnCall, btnSend, btnLocation;
    TextView tvMsgLeft, tvMsgRight, tvName;
    EditText etInputMsg;
    LinearLayout llChat;
    String getEtMsg;
    LinearLayout.LayoutParams lp;
    ScrollView scroll;

    String from_email, message;

    BroadcastReceiver fReceiver;

    SendMessage sendMessage;

    String userEmail, myEmail;

    HttpParse httpParse = new HttpParse();

    String HttpURL = "http://172.20.10.3/towtruckbackend/FilterProviderData.php";

    String ParseResult;
    HashMap<String,String> ResultHash = new HashMap<>();
    String FinalJSonObject;
    String NameHolder, NumberHolder, EmailHolder;
    double  LatHolder, LngHolder;

    final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_chat);

        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);

        btnCall = findViewById(R.id.btnCall);
        btnCall.setOnClickListener(this);

        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        btnLocation = findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(this);

        etInputMsg = findViewById(R.id.etInputMsg);
        llChat = findViewById(R.id.llChat);
        scroll = findViewById(R.id.scroll);
        tvName = findViewById(R.id.tvName);

        userEmail = getIntent().getStringExtra("from_email");
        myEmail = getIntent().getStringExtra("to_email");

        HttpWebCall(userEmail);

        sendMessage = new SendMessage();

        startService(new Intent(this, ProviderChatServiceMain.class)
                .putExtra("from_email", myEmail).putExtra("to_email", userEmail));

        IntentFilter intentFilter = new IntentFilter("android.intent.action.UPDATE");
        fReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                from_email = intent.getStringExtra("from_email");
                message = intent.getStringExtra("message");

                if (from_email != null && message != null) {
                    if (from_email.equals(myEmail)) createMessageRight(message);
                    else createMessageLeft(message);
                }
            }
        };
        registerReceiver(fReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(fReceiver);
        stopService(new Intent(this, ProviderChatServiceMain.class));
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
                sendMessage.sendMessage(myEmail, userEmail, getEtMsg);
                break;

            case R.id.btnCall: startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + NumberHolder)));
                break;

            case R.id.btnLocation: finish();
                startActivity(new Intent(ProviderChat.this, ProviderScreenMain.class)
                    .putExtra("latUser", LatHolder)
                    .putExtra("lngUser", LngHolder)
                    .putExtra("nameUser", NameHolder)
                    .putExtra("emailUser", EmailHolder)
                    .putExtra("myEmail", myEmail));
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

    public void HttpWebCall(final String email){
        class HttpWebCallFunction extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {
                super.onPostExecute(httpResponseMsg);
                FinalJSonObject = httpResponseMsg;
                new GetHttpResponse(ProviderChat.this).execute();
            }

            @Override
            protected String doInBackground(String... params) {
                ResultHash.put("email", params[0]);
                ParseResult = httpParse.postRequest(ResultHash, HttpURL);
                return ParseResult;
            }
        }
        HttpWebCallFunction httpWebCallFunction = new HttpWebCallFunction();
        httpWebCallFunction.execute(email);
    }

    private class GetHttpResponse extends AsyncTask<Void, Void, Void> {
        public Context context;

        public GetHttpResponse(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                if(FinalJSonObject != null) {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(FinalJSonObject);
                        JSONObject jsonObject;

                        for(int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            NameHolder = jsonObject.getString("name");
                            NumberHolder = jsonObject.getString("phone");
                            LatHolder = jsonObject.getDouble("lat");
                            LngHolder = jsonObject.getDouble("lng");
                            EmailHolder = jsonObject.getString("email");
                        }
                    }
                    catch (JSONException e) { e.printStackTrace(); }
                }
            }
            catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            tvName.setText(NameHolder);
        }
    }
}
