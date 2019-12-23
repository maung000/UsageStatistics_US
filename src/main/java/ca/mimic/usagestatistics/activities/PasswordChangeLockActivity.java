package ca.mimic.usagestatistics.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.hanks.passcodeview.PasscodeView;

import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.utils.SharedPreference;

public class PasswordChangeLockActivity extends AppCompatActivity {
    SharedPreference sharedPreference;
    Context context;
    public static boolean check = true;

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        sharedPreference = new SharedPreference();
        getSupportActionBar().hide();
        setContentView(R.layout.activity_change_password);
        PasscodeView passcodeView = (PasscodeView) findViewById(R.id.passcodeView);
        passcodeView.setListener(new PasscodeView.PasscodeViewListener() {
            @Override
            public void onFail() {
                Toast.makeText(getApplication(), getResources().getString(R.string.text_compare_input_password), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String number) {
                sharedPreference.savePasswordApp(PasswordChangeLockActivity.this, number);
                Toast.makeText(getApplication(), getResources().getString(R.string.text_complete_input_new_password), Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(PasswordChangeLockActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        setCheck(true);
    }

    @Override
    protected void onStart() {
//        GoogleAnalytics.getInstance(context).reportActivityStart(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
//        GoogleAnalytics.getInstance(context).reportActivityStop(this);
        super.onStop();
    }
}
