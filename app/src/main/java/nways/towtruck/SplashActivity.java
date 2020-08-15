package nways.towtruck;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    TextView towtruck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        towtruck = findViewById(R.id.towtruck);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.combo);
        towtruck.setAnimation(anim);

        Thread logoTimer = new Thread() {
            public void run() {
                try {
                    int logoTimer = 0;
                    while(logoTimer < 5000) {
                        sleep(100);
                        logoTimer = logoTimer + 100;
                    }
                    startActivity(new Intent("com.tutorial.CLEARSCREEN"));
                }
                catch (InterruptedException e) { e.printStackTrace(); }
                finally {
                    finish();
                }
            }
        };
        logoTimer.start();
    }
}
