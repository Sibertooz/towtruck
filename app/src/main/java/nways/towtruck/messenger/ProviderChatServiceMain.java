package nways.towtruck.messenger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import nways.towtruck.server.HttpParse;

public class ProviderChatServiceMain extends Service {
    HttpParse httpParseGet = new HttpParse();

    String HttpURLget = "http://172.20.10.3/towtruckbackend/GetMessages.php";

    String ParseResult;
    HashMap<String, String> ResultHash = new HashMap<>();
    String FinalJSonObject;

    String from_email, to_email, message;
    ArrayList<Message> messages;

    Runnable mRunnable;
    Handler mHandler = new Handler();

    long data;

    final String LOG_TAG = "myLogs";

    String myEmail, userEmail;

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        myEmail = intent.getStringExtra("from_email");
        userEmail = intent.getStringExtra("to_email");

        Log.d(LOG_TAG, "FirstService started");

        mRunnable = new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(mRunnable,10*1000);
                getMessages(myEmail, userEmail, String.valueOf(data));
            }
        };
        mHandler.postDelayed(mRunnable,2*1000);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    private void getMessages(final String from_email, final String to_email, final String data) {
        class getMessages extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {
                super.onPostExecute(httpResponseMsg);
                FinalJSonObject = httpResponseMsg;
                new GetHttpResponseForMessages(ProviderChatServiceMain.this).execute();
            }

            @Override
            protected String doInBackground(String... params) {
                ResultHash.put("from_email", params[0]);
                ResultHash.put("to_email", params[1]);
                ResultHash.put("data", params[2]);

                ParseResult = httpParseGet.postRequest(ResultHash, HttpURLget);

                return ParseResult;
            }
        }
        getMessages httpWebCallFunction = new getMessages();
        httpWebCallFunction.execute(from_email, to_email, data);
    }

    private class GetHttpResponseForMessages extends AsyncTask<Void, Void, Void> {
        public Context context;

        public GetHttpResponseForMessages (Context context) {
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

                        Message messageClass;
                        messages = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            from_email = jsonObject.getString("from_email");
                            to_email = jsonObject.getString("to_email");
                            message = jsonObject.getString("message");
                            data = jsonObject.getLong("data");

                            messageClass = new Message(from_email, to_email, message, data);
                            messages.add(messageClass);
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent intent;
            if (messages != null && !messages.isEmpty()) {
                for (int i = 0; i < messages.size(); i++) {
                    String from_email = messages.get(i).getFrom_email();
                    String message = messages.get(i).getMessage();
                    if (from_email.equals(myEmail))
                        intent = new Intent("android.intent.action.UPDATE")
                                .putExtra("from_email", myEmail)
                                .putExtra("message", message);
                    else
                        intent = new Intent("android.intent.action.UPDATE")
                                .putExtra("from_email", userEmail)
                                .putExtra("message", message);

                    sendBroadcast(intent);
                }
                messages.clear();
            }
        }
    }
}
