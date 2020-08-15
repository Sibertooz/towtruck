package nways.towtruck.provider.crud;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import nways.towtruck.R;
import nways.towtruck.provider.LoginActivity;
import nways.towtruck.server.HttpParse;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etInputPhone, etInputEmail, etInputName, etInputPassword, etInputPasswordRepeat;
    Button btnDoRegister;

    String getName, getPhone, getEmail, getPassword;

    ProgressDialog progressDialog;
    String finalResult ;
    HashMap<String,String> hashMap = new HashMap<>();
    HttpParse httpParse = new HttpParse();

    String HttpURL = "http://172.20.10.3/towtruckbackend/ProviderRegistration.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etInputPhone = findViewById(R.id.etInputPhone);
        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots("+38 (0__) __ __ ___");
        FormatWatcher formatWatcher = new MaskFormatWatcher(MaskImpl.createTerminated(slots));
        formatWatcher.installOn(etInputPhone);

        btnDoRegister = findViewById(R.id.btnDoRegister);
        btnDoRegister.setOnClickListener(this);

        etInputEmail = findViewById(R.id.etInputEmail);
        etInputName = findViewById(R.id.etInputName);
        etInputPassword = findViewById(R.id.etInputPassword);
        etInputPasswordRepeat = findViewById(R.id.etInputPasswordRepeat);
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public void onClick(View v){
        String passFirst = etInputPassword.getText().toString();
        String passSecond = etInputPasswordRepeat.getText().toString();

        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.toast_layout_main,
                (ViewGroup) findViewById(R.id.toast_layout_root_main));

        TextView text = layout.findViewById(R.id.text_main);
        text.setText("Не все поля заполнены!");

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        if (!isValidEmail(etInputEmail.getText().toString())){ text.setText("Некорректный e-mail!"); toast.show();}
        else if (etInputName.getText().toString().equals("")) toast.show();
        else if (etInputPhone.getText().toString().equals("")) toast.show();
        else if (etInputPassword.getText().toString().equals("")) toast.show();
        else if (!(passFirst.equals(passSecond))){ text.setText("Пароли не совпадают!"); toast.show();}
        else if (passFirst.length() < 8){ text.setText("Короткий пароль!"); toast.show(); }
        else { getDataProvider(); ProviderRegistration(getName, getPhone, getEmail, getPassword); }
    }

    private void getDataProvider(){
        getName = etInputName.getText().toString();
        getPhone = etInputPhone.getText().toString();
        getEmail = etInputEmail.getText().toString();
        getPassword = etInputPassword.getText().toString();
    }

    private void afterRegister(){
        Intent intent;
        intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void ProviderRegistration(final String name, final String phone, final String email,
                                     final String password){
        class ProviderRegistrationClass extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(RegisterActivity.this,"Loading Data",
                        null,true,true);
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {
                super.onPostExecute(httpResponseMsg);
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, httpResponseMsg, Toast.LENGTH_LONG).show();
                if (!(httpResponseMsg.equals("Email Already Exist"))) afterRegister();
            }

            @Override
            protected String doInBackground(String... params) {
                hashMap.put("name", params[0]);
                hashMap.put("phone", params[1]);
                hashMap.put("email", params[2]);
                hashMap.put("password", params[3]);
                finalResult = httpParse.postRequest(hashMap, HttpURL);
                return finalResult;
            }
        }
        ProviderRegistrationClass providerRegistrationClass = new ProviderRegistrationClass();
        providerRegistrationClass.execute(name, phone, email, password);
    }
}
