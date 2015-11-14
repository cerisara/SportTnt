package fr.xtof54.detsport;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by xtof2 on 14/11/15.
 */
public class Connection {
    private String url, res;
    private Handler handler;

    static interface Handler {
        public void gotResult(String res);
    }

    public Connection(String httpurl, final Handler hdl) {
        url = httpurl;
        handler = hdl;
    }

    public void connect() {
        res="ERROR";
        class MyRunnable implements Runnable {
            @Override
            public void run() {
                HttpParams httpparms = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpparms, 6000);
                HttpConnectionParams.setSoTimeout(httpparms, 6000);
                HttpClient httpclient = new DefaultHttpClient(httpparms);
                try {
                    HttpGet httpget = new HttpGet(url);
                    HttpResponse response = httpclient.execute(httpget);
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream instream = entity.getContent();
                        StringBuilder sb = new StringBuilder();
                        BufferedReader fin = new BufferedReader(new InputStreamReader(instream, Charset.forName("UTF-8")));
                        for (; ; ) {
                            String s = fin.readLine();
                            if (s == null) break;
                            sb.append(s);
                        }
                        fin.close();
                        res = sb.toString();
                        handler.gotResult(res);
                    }
                    httpclient.getConnectionManager().shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        MyRunnable r = new MyRunnable();
        Thread connectThread = new Thread(r);
        connectThread.start();
    }
}
