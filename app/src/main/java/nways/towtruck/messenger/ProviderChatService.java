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

public class ProviderChatService extends Service {
    HttpParse httpParseGet = new HttpParse();

    String HttpURLget = "http://172.20.10.3/towtruckbackend/OneRecord.php";

    String ParseResult;
    HashMap<String, String> ResultHash = new HashMap<>();
    String FinalJSonObject;

    String from_email, to_email, message;
    long data;

    Runnable mRunnable;
    Handler mHandler = new Handler();

    final String LOG_TAG = "myLogs";

    String myEmail;

    ArrayList<Message> messages;

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
        myEmail = intent.getStringExtra("myEmail");

        Log.d(LOG_TAG, "ProviderService started");

        mRunnable = new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(mRunnable,10*1000);
                getRecord(myEmail);
            }
        };
        mHandler.postDelayed(mRunnable,2*1000);
    }

    public void getRecord(final String to_email) {
        class getRecord extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {
                super.onPostExecute(httpResponseMsg);
                FinalJSonObject = httpResponseMsg;
                new GetHttpResponseForRecord(ProviderChatService.this).execute();
            }

            @Override
            protected String doInBackground(String... params) {
                ResultHash.put("to_email", params[0]);

                ParseResult = httpParseGet.postRequest(ResultHash, HttpURLget);

                return ParseResult;
            }
        }
        getRecord httpWebCallFunction = new getRecord();
        httpWebCallFunction.execute(to_email);
    }

    private class GetHttpResponseForRecord extends AsyncTask<Void, Void, Void> {
        public Context context;

        public GetHttpResponseForRecord (Context context) {
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
                            to_email = jsonObject.getString("to_email");
                            from_email = jsonObject.getString("from_email");
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
                    intent = new Intent("android.intent.action.UPDATE")
                            .putExtra("to_email", myEmail)
                            .putExtra("from_email", from_email);
                    sendBroadcast(intent);
                }
                messages.clear();
            }
        }
    }
}
