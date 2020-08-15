package nways.towtruck.user;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import nways.towtruck.R;

public class UserActivity extends AppCompatActivity implements View.OnClickListener{
    Button btnTowTruck, btnMchs, btnPolice, btnAmb, btnGas;
    String getEmail;
    public static String finalEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        btnTowTruck = findViewById(R.id.btnTowTruck);
        btnTowTruck.setOnClickListener(this);

        btnMchs = findViewById(R.id.btnMchs);
        btnMchs.setOnClickListener(this);

        btnPolice = findViewById(R.id.btnPolice);
        btnPolice.setOnClickListener(this);

        btnAmb = findViewById(R.id.btnAmb);
        btnAmb.setOnClickListener(this);

        btnGas = findViewById(R.id.btnGas);
        btnGas.setOnClickListener(this);

        Intent intent = getIntent();
        getEmail = intent.getStringExtra(UserLogin.UserEmail);
    }

    public void onClick(View v){
        Intent intent;

        switch(v.getId()){
            case R.id.btnTowTruck:
                Intent intentEmail = getIntent();
                String email = intentEmail.getStringExtra(UserLogin.UserEmail);

                intent = new Intent(this, UserScreen.class);
                intent.putExtra(finalEmail, email);
                startActivity(intent);
                finish();
                break;
            case R.id.btnMchs:
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:101"));
                startActivity(intent);
                break;
            case R.id.btnPolice:
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:102"));
                startActivity(intent);
                break;
            case R.id.btnAmb:
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:103"));
                startActivity(intent);
                break;
            case R.id.btnGas:
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:104"));
                startActivity(intent);
                break;
        }
    }
}
