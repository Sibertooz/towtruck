package nways.towtruck.provider.crud;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import nways.towtruck.R;
import nways.towtruck.server.HttpParse;

public class UpdateActivity extends AppCompatActivity {

    String HttpURL = "http://172.20.10.3/towtruckbackend/UpdateProvider.php";
    ProgressDialog progressDialog;
    String finalResult ;
    HashMap<String,String> hashMap = new HashMap<>();
    HttpParse httpParse = new HttpParse();
    EditText etName, etPhoneNumber, etEmail;
    Button btnUpdate;
    String ProviderNameHolder, ProviderPhoneHolder, ProviderEmailHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);


        etName = findViewById(R.id.editName);
        etPhoneNumber = findViewById(R.id.editPhoneNumber);
        etEmail = findViewById(R.id.editEmail);

        btnUpdate = findViewById(R.id.UpdateButton);

        // Receive Student ID, Name , Phone Number , Class Send by previous ShowSingleRecordActivity.
        ProviderNameHolder = getIntent().getStringExtra("name");
        ProviderPhoneHolder = getIntent().getStringExtra("phone");
        ProviderEmailHolder = getIntent().getStringExtra("email");

        // Setting Received Student Name, Phone Number, Class into EditText.
        etName.setText(ProviderNameHolder);
        etPhoneNumber.setText(ProviderPhoneHolder);
        etEmail.setText(ProviderEmailHolder);

        // Adding click listener to update button .
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Getting data from EditText after button click.
                GetDataFromEditText();

                // Sending Student Name, Phone Number, Class to method to update on server.
                StudentRecordUpdate(ProviderNameHolder, ProviderPhoneHolder, ProviderEmailHolder);

            }
        });
    }

    // Method to get existing data from EditText.
    public void GetDataFromEditText(){

        ProviderNameHolder = etName.getText().toString();
        ProviderPhoneHolder = etPhoneNumber.getText().toString();
        ProviderEmailHolder = etEmail.getText().toString();

    }

    // Method to Update Student Record.
    public void StudentRecordUpdate(final String S_Name, final String S_Phone, final String S_Email){

        class StudentRecordUpdateClass extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = ProgressDialog.show(UpdateActivity.this,"Loading Data",null,true,true);
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {

                super.onPostExecute(httpResponseMsg);

                progressDialog.dismiss();

                Toast.makeText(UpdateActivity.this, httpResponseMsg, Toast.LENGTH_LONG).show();

            }

            @Override
            protected String doInBackground(String... params) {

                hashMap.put("name", params[0]);

                hashMap.put("phone", params[1]);

                hashMap.put("email", params[2]);

                //hashMap.put("StudentClass", params[3]);

                finalResult = httpParse.postRequest(hashMap, HttpURL);

                return finalResult;
            }
        }

        StudentRecordUpdateClass studentRecordUpdateClass = new StudentRecordUpdateClass();

        studentRecordUpdateClass.execute(S_Name, S_Phone, S_Email);
    }
}
