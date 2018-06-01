package ca.mimic.usagestatistics.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.takwolf.android.lock9.Lock9View;

import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.Settings;


public class PasswordChange extends AppCompatActivity {

    Button confirmButton, retryButton;
    TextView textView;
    boolean isEnteringFirstTime = true;
    boolean isEnteringSecondTime = false;
    String enteredPassword;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Lock9View lock9View;
    SharedPreference sharedPreference;
    Context context;
    Button forgetPassword;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getApplicationContext();
            setContentView(R.layout.activity_password_set);

            sharedPreference = new SharedPreference();
            forgetPassword = (Button) findViewById(R.id.forgetPassword);


            lock9View = (Lock9View) findViewById(R.id.lock_9_view);
            confirmButton = (Button) findViewById(R.id.confirmButton);
            retryButton = (Button) findViewById(R.id.retryButton);
            textView = (TextView) findViewById(R.id.textView);
            confirmButton.setEnabled(false);
            retryButton.setEnabled(false);

            sharedPreferences = getSharedPreferences(AppLockConstants.MyPREFERENCES, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.putString(AppLockConstants.PASSWORD, enteredPassword);
                    editor.commit();
                    editor.putBoolean(AppLockConstants.IS_PASSWORD_SET, true);
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "Thay đổi mật khẩu thành công.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(PasswordChange.this, Settings.class);
                    startActivity(i);
                    finish();

                }
            });
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isEnteringFirstTime = true;
                    isEnteringSecondTime = false;
                    textView.setText("Vẽ mật khẩu");
                    confirmButton.setEnabled(false);
                    retryButton.setEnabled(false);
                }
            });

            lock9View.setCallBack(new Lock9View.CallBack() {
                @Override
                public void onFinish(String password) {
                    retryButton.setEnabled(true);
                    if (isEnteringFirstTime) {
                        enteredPassword = password;
                        isEnteringFirstTime = false;
                        isEnteringSecondTime = true;
                        textView.setText("Vẽ lại mật khẩu");
                    } else if (isEnteringSecondTime) {
                        if (enteredPassword.matches(password)) {
                            confirmButton.setEnabled(true);
                        } else {
                            Toast.makeText(getApplicationContext(), "Hai mật khẩu không giống nhau. Thử lại", Toast.LENGTH_SHORT).show();
                            isEnteringFirstTime = true;
                            isEnteringSecondTime = false;
                            textView.setText("Vẽ mật khẩu");
                            retryButton.setEnabled(false);
                        }
                    }
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
