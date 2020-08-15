package nways.towtruck.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import nways.towtruck.R;
import nways.towtruck.server.HttpParse;

public class UserLogin extends AppCompatActivity implements View.OnClickListener {
    Button btnRegister, btnLogin;
    EditText etEmail, etPassword;

    String PasswordHolder, EmailHolder;
    String finalResult ;
    String HttpURL = "http://172.20.10.3/towtruckbackend/UserLogin.php";
    Boolean CheckEditText;
    ProgressDialog progressDialog;
    HashMap<String,String> hashMap = new HashMap<>();
    HttpParse httpParse = new HttpParse();

    public static final String UserEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setPaintFlags(btnRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btnRegister.setOnClickListener(this);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnRegister:
                intent = new Intent(this, UserRegister.class);
                startActivity(intent);
                break;
            case R.id.btnLogin:
                CheckEditTextIsEmptyOrNot();
                if(CheckEditText) UserLoginFunction(EmailHolder, PasswordHolder);
                else Toast.makeText(UserLogin.this, "Please fill all form fields.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void CheckEditTextIsEmptyOrNot(){
        EmailHolder = etEmail.getText().toString();
        PasswordHolder = etPassword.getText().toString();
        if(TextUtils.isEmpty(EmailHolder) || TextUtils.isEmpty(PasswordHolder)) CheckEditText = false;
        else CheckEditText = true;
    }

    public void UserLoginFunction(final String email, final String password){
        class UserLoginClass extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(UserLogin.this,"Loading Data",null,true,true);
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {
                super.onPostExecute(httpResponseMsg);
                progressDialog.dismiss();

                if(httpResponseMsg.equalsIgnoreCase("Data Matched")){
                    finish();

                    Intent intent = new Intent(UserLogin.this, UserActivity.class);
                    intent.putExtra(UserEmail, email);
                    startActivity(intent);
                    finish();
                }
                else Toast.makeText(UserLogin.this, httpResponseMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {
                hashMap.put("email", params[0]);
                hashMap.put("password", params[1]);
                finalResult = httpParse.postRequest(hashMap, HttpURL);
                return finalResult;
            }
        }
        UserLoginClass userLoginClass = new UserLoginClass();
        userLoginClass.execute(email, password);
    }
}
