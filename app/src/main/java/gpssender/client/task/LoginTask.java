package gpssender.client.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import gpssender.client.model.ServerAnswer;
import gpssender.client.util.Config;

//Класс авторизации
public class LoginTask extends AsyncTask<Void, String, String>{
	private String mLogin, mPassword;
	private String mHost = Config.LOGIN_URL;

	public interface OnLoginListener {
		public void onResultLogin(ServerAnswer result, String userId);
	}


	private OnLoginListener listener;
	private Context mContext;
	
	public LoginTask(OnLoginListener listener, Context context, String login, String password){
		this.listener=listener;
		this.mContext=context;
        this.mLogin = login;
        this.mPassword = password;
	}


    @Override
    protected String doInBackground(Void... par) {
        return signin(mHost,mLogin,mPassword);
    }


    private String signin(String host, String login, String password){
        String result = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(host);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
        nameValuePairs.add(new BasicNameValuePair("user[login]", login));
        nameValuePairs.add(new BasicNameValuePair("user[password]", password));
        HttpResponse response = null;
        String responseString = null;
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            Log.i("UrlEncodedFormEntity", new UrlEncodedFormEntity(nameValuePairs, "UTF-8").toString());
            response = httpclient.execute(httppost);

            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();

                JSONObject jsonObject = new JSONObject(responseString);
                result = jsonObject.getString("user_id");

            } else{
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        ServerAnswer response = ServerAnswer.ERROR;
        if(!result.equals(""))
            response = ServerAnswer.ALL_OK;
        listener.onResultLogin(response, result);
    }
}