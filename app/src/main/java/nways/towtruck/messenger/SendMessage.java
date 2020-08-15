package nways.towtruck.messenger;

import android.os.AsyncTask;
import java.util.HashMap;

import nways.towtruck.server.HttpParse;

public class SendMessage {
    String finalResult;
    HashMap<String,String> hashMap = new HashMap<>();
    HttpParse httpParseAdd = new HttpParse();
    String HttpURLadd = "http://172.20.10.3/towtruckbackend/AddMessage.php";

    public SendMessage(){
    }

    public void sendMessage(final String from_email, final String to_email, final String message){
        class SendMessageClass extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {
                super.onPostExecute(httpResponseMsg);
            }

            @Override
            protected String doInBackground(String... params) {
                hashMap.put("from_email", params[0]);
                hashMap.put("to_email", params[1]);
                hashMap.put("message", params[2]);
                finalResult = httpParseAdd.postRequest(hashMap, HttpURLadd);
                return finalResult;
            }
        }
        SendMessageClass sendMessageClass = new SendMessageClass();
        sendMessageClass.execute(from_email, to_email, message);
    }
}
