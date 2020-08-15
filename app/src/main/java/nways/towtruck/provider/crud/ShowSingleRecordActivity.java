package nways.towtruck.provider.crud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import nways.towtruck.R;
import nways.towtruck.provider.ProviderScreen;
import nways.towtruck.server.HttpParse;

public class ShowSingleRecordActivity extends AppCompatActivity {

    HttpParse httpParse = new HttpParse();
    ProgressDialog pDialog;

    String HttpURL = "http://172.20.10.3/towtruckbackend/FilterProviderData.php";

    String ParseResult;
    HashMap<String,String> ResultHash = new HashMap<>();
    String FinalJSonObject;
    TextView NAME, PHONE_NUMBER, EMAIL;
    String NameHolder, NumberHolder, EmailHolder;
    Button UpdateButton;
    String TempItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_single_record);

        NAME = findViewById(R.id.textNameSee);
        PHONE_NUMBER = findViewById(R.id.textPhoneSee);
        EMAIL = findViewById(R.id.textEmailSee);

        UpdateButton = findViewById(R.id.buttonUpdate);

        Intent intent = getIntent();
        TempItem = intent.getStringExtra(ProviderScreen.finalEmail);

        HttpWebCall(TempItem);

        UpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowSingleRecordActivity.this, UpdateActivity.class);
                intent.putExtra("name", NameHolder);
                intent.putExtra("phone", NumberHolder);
                intent.putExtra("email", TempItem);
                startActivity(intent);
                finish();
            }
        });
    }

    public void HttpWebCall(final String PreviousListViewClickedItem) {

        class HttpWebCallFunction extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = ProgressDialog.show(ShowSingleRecordActivity.this,"Loading Data",null,true,true);
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {
                super.onPostExecute(httpResponseMsg);
                pDialog.dismiss();
                FinalJSonObject = httpResponseMsg;
                new GetHttpResponse(ShowSingleRecordActivity.this).execute();
            }

            @Override
            protected String doInBackground(String... params) {
                ResultHash.put("email", params[0]);
                ParseResult = httpParse.postRequest(ResultHash, HttpURL);
                return ParseResult;
            }
        }

        HttpWebCallFunction httpWebCallFunction = new HttpWebCallFunction();
        httpWebCallFunction.execute(PreviousListViewClickedItem);
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
                            EmailHolder = jsonObject.getString("email");
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
            NAME.setText(NameHolder);
            PHONE_NUMBER.setText(NumberHolder);
            EMAIL.setText(EmailHolder);
        }
    }

}
