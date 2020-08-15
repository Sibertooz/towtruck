package nways.towtruck;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import nways.towtruck.provider.LoginActivity;
import nways.towtruck.user.UserActivity;
import nways.towtruck.user.UserLogin;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnProvider, btnUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnProvider = findViewById(R.id.btnProvider);
        btnProvider.setOnClickListener(this);

        btnUser = findViewById(R.id.btnUser);
        btnUser.setOnClickListener(this);
    }

    public void onClick(View v){
        Intent intent;
        switch(v.getId()){
            case R.id.btnProvider: intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btnUser: intent = new Intent(this, UserLogin.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
