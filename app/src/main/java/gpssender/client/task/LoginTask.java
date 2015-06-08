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
import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import gpssender.client.model.ServerAnswer;
import gpssender.client.util.Config;

//Класс авторизации
public class LoginTask extends AsyncTask<Void, String, Boolean>{
	private String mLogin, mPassword;
	private String mHost = Config.LOGIN_URL;

	public interface OnLoginListener {
		public void onResultLogin(ServerAnswer result);
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
    protected Boolean doInBackground(Void... par) {
        AuthInfo authInfo = getAuthInfo(mHost, Config.SITE_LOGIN, Config.SITE_PASSWORD);

        return signin(mHost,mLogin,mPassword, Config.SITE_LOGIN, Config.SITE_PASSWORD,authInfo.session, authInfo.token);
    }

    private class AuthInfo{
        public String session, token;
    }

    private AuthInfo getAuthInfo(String host, String login, String password){
        AuthInfo result = new AuthInfo();
        String responseString = "";
        try {
            URL url = new URL (host);

            String encoding = Base64.encodeToString(new String(login.trim()+":"+password.trim()).getBytes(), Base64.DEFAULT);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Authorization", "Basic " + encoding.trim());

            Map<String, List<String>> headerFields = connection.getHeaderFields();

            Set<String> headerFieldsSet = headerFields.keySet();
            for(String key:headerFieldsSet){
                Log.i(">"+key, headerFields.get(key).toString());
            }
            String session = "";
            String[] cookies = headerFields.get("Set-Cookie").toString().split(";");
            for (String cookie : cookies) {
                cookie=cookie.replaceAll("\\[","").replaceAll("\\[","");
                if(cookie.split("=")[0].equals("_igooods_session")){
                    session = cookie;
                }
            }

            InputStream content = null;//(InputStream)connection.getInputStream();
            int status = connection.getResponseCode();

            if(status >= HttpStatus.SC_BAD_REQUEST)
                content = connection.getErrorStream();
            else
                content = connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader (content));

            String line="";
            while ((line = in.readLine()) != null) {
                responseString+=line;
            }
            org.jsoup.nodes.Document doc = Jsoup.parse(responseString);
            org.jsoup.nodes.Element el = doc.select("input[name*=" + "authenticity_token").first();
            String token = el.attr("value");
            result.token = token;
            result.session = session;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean signin(String host, String login, String password,
                           String siteLogin, String sitePassword, String session, String token){
        boolean result = false;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(host);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
        nameValuePairs.add(new BasicNameValuePair("user[login]", login));
        nameValuePairs.add(new BasicNameValuePair("user[password]", password));
        nameValuePairs.add(new BasicNameValuePair("authenticity_token", token));
        String encoding = Base64.encodeToString(new String(siteLogin.trim()+":"+sitePassword.trim()).getBytes(), Base64.NO_WRAP);
        httppost.setHeader("Authorization", "Basic "+encoding);
        httppost.addHeader("Cookie", session);

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
                org.jsoup.nodes.Document doc = Jsoup.parse(responseString);
                org.jsoup.nodes.Element el = doc.select("input[name*=" + "authenticity_token").first();
//                String tokenValue = el.attr("value");
                if(el==null)
                    result = true;
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
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        ServerAnswer response = ServerAnswer.ERROR;
        if(result)
            response = ServerAnswer.ALL_OK;
        listener.onResultLogin(response);
    }
}