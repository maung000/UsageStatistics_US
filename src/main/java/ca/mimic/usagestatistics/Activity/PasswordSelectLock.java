package ca.mimic.usagestatistics.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.hanks.passcodeview.PasscodeView;
import com.takwolf.android.lock9.Lock9View;

import java.util.List;

import ca.mimic.usagestatistics.Activity.Settings;
import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.Utils.SharedPreference;

public class PasswordSelectLock extends AppCompatActivity {
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
        setContentView(R.layout.activity_passcode2);
        PasscodeView passcodeView = (PasscodeView) findViewById(R.id.passcodeView);
        passcodeView.setListener(new PasscodeView.PasscodeViewListener(){
            @Override
            public void onFail() {
                Toast.makeText(getApplication(),"Wrong!!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String number) {
                sharedPreference.savePasswordApp(PasswordSelectLock.this, number);
                Toast.makeText(getApplication(),"Tạo mật khẩu thành công",Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        sharedPreference.savePasswordApp(this,true);
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

//package ca.mimic.usagestatistics.Activity;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.andrognito.patternlockview.PatternLockView;
//import com.andrognito.patternlockview.listener.PatternLockViewListener;
//import com.andrognito.patternlockview.utils.PatternLockUtils;
//import com.google.android.gms.analytics.GoogleAnalytics;
//
//import java.util.List;
//import java.util.Set;
//
//import ca.mimic.usagestatistics.R;
//import ca.mimic.usagestatistics.Utils.AppLockConstants;
//import ca.mimic.usagestatistics.Utils.SharedPreference;
//
//public class PasswordSelectLock extends AppCompatActivity {
//    SharedPreference sharedPreference;
//    Context context;
//    Button forgetPassword;
//    TextView tvDrawPassword;
//
//    PatternLockView patternLockView;
//    String password;
//    public static boolean check = true;
//
//    public boolean isCheck() {
//        return check;
//    }
//
//    public void setCheck(boolean check) {
//        this.check = check;
//    }
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        context = getApplicationContext();
//        setContentView(R.layout.activity_password_select_lock);
//        sharedPreference = new SharedPreference();
//        forgetPassword = (Button) findViewById(R.id.forgetPassword);
//        patternLockView = findViewById(R.id.patternView);
//        tvDrawPassword = findViewById(R.id.tvDrawPassword);
//        getSupportActionBar().hide();
//
//
//        check = false;
//        patternLockView.addPatternLockListener(new PatternLockViewListener() {
//            @Override
//            public void onStarted() {
//
//            }
//
//            @Override
//            public void onProgress(List<PatternLockView.Dot> progressPattern) {
//
//            }
//
//            @Override
//            public void onComplete(List<PatternLockView.Dot> pattern) {
//                password = PatternLockUtils.patternToString(patternLockView, pattern);
//                Intent intent =  new Intent(PasswordSelectLock.this,PasswordSelectLockAgain.class);
//                intent.putExtra(AppLockConstants.EXTRA_PASSWORD_APP,password);
//                startActivity(intent);
//                finish();
//            }
//
//            @Override
//            public void onCleared() {
//
//            }
//        });
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        setCheck(true);
//    }
//
//    @Override
//    protected void onStart() {
//        GoogleAnalytics.getInstance(context).reportActivityStart(this);
//        super.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        GoogleAnalytics.getInstance(context).reportActivityStop(this);
//        super.onStop();
//    }
//
//    @Override
//    public void onBackPressed() {
//        Intent intent =  new Intent(PasswordSelectLock.this, Settings.class);
//        startActivity(intent);
//        finish();
//    }
//}
