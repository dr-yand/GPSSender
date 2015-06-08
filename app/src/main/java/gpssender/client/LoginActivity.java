package gpssender.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import gpssender.client.model.ServerAnswer;
import gpssender.client.task.LoginTask;
import gpssender.client.util.PreferenceUtils;


public class LoginActivity extends ActionBarActivity implements View.OnClickListener, LoginTask.OnLoginListener{

    private ProgressDialog mProgressDialog;
    private EditText mLogin, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addNotification();

        if(PreferenceUtils.isSignin(this)){
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);

        initView();
        initDlg();
    }

    private void addNotification(){
        Intent intent = new Intent(this, LoginActivity.class);
        PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setAutoCancel(false);
        builder.setOngoing(true);
//        builder.setContentIntent(pintent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void initView(){
        mLogin = (EditText)findViewById(R.id.login);
        mPassword = (EditText)findViewById(R.id.password);
        ((Button)findViewById(R.id.signin)).setOnClickListener(this);
    }

    private void initDlg(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Пожалуйста подождите...");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.signin){
            String login = mLogin.getText().toString();
            String password = mPassword.getText().toString();
            if(login.trim().equals("")||password.trim().equals("")){
                Toast.makeText(this, "Введите логин и пароль",Toast.LENGTH_LONG).show();
            }
            else{
                mProgressDialog.show();
                new LoginTask(this, this, login, password).execute(new Void[]{});
            }
        }
    }

    @Override
    public void onResultLogin(ServerAnswer result) {
        mProgressDialog.dismiss();
        if(result==ServerAnswer.ALL_OK){
            startService(new Intent(this, LocationService.class));
            PreferenceUtils.saveSingin(this, true);
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(this, "Неверный логин или пароль",Toast.LENGTH_LONG).show();
        }
    }

}
