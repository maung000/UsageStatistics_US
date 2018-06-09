package ca.mimic.usagestatistics.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.takwolf.android.lock9.Lock9View;

import ca.mimic.usagestatistics.R;


public class PasswordOldActivity extends AppCompatActivity {
    Lock9View lock9View;
    SharedPreference sharedPreference;
    Context context;
    Button forgetPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_password_old);
        sharedPreference = new SharedPreference();
        forgetPassword = (Button) findViewById(R.id.forgetPassword);
        lock9View = (Lock9View) findViewById(R.id.lock_9_view);

        lock9View.setCallBack(new Lock9View.CallBack() {
            @Override
            public void onFinish(String password) {
                if (password.matches(sharedPreference.getPassword(context))) {
                    Intent i = new Intent(PasswordOldActivity.this, PasswordChange.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "Mật khẩu cũ không đúng. Thử lại", Toast.LENGTH_SHORT).show();
                }
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PasswordOldActivity.this, PasswordRecoveryActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        GoogleAnalytics.getInstance(context).reportActivityStart(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        GoogleAnalytics.getInstance(context).reportActivityStop(this);
        super.onStop();
        super.onStop();
    }
}
