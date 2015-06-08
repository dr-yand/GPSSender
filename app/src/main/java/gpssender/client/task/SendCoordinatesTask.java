package gpssender.client.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gpssender.client.model.ServerAnswer;
import gpssender.client.util.Config;

//Класс авторизации
public class SendCoordinatesTask extends AsyncTask<Void, String, Boolean>{
	private String mLat, mLon;
	private String mHost = Config.COORDINATES_URL;


	private Context mContext;

	public SendCoordinatesTask(Context context, String lat, String lon){
		this.mContext=context;
        this.mLat = lat;
        this.mLon = lon;
	}


    @Override
    protected Boolean doInBackground(Void... par) {
        return send(mHost, mLat, mLon);
    }


    private boolean send(String host, String lat, String lon){
        boolean result = false;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(host);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
        nameValuePairs.add(new BasicNameValuePair("latitude", lat));
        nameValuePairs.add(new BasicNameValuePair("longitude", lon));

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
    }
}