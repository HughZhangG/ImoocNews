package com.zgc.imoocnews.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.zgc.imoocnews.R;
import com.zgc.imoocnews.adapter.NewsAdapter;
import com.zgc.imoocnews.entity.NewsBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private String NEWS_URL = "http://www.imooc.com/api/teacher?type=4&num=30";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.lv_news);

        new NewsAsyncTask().execute(NEWS_URL);

    }

    class NewsAsyncTask extends AsyncTask<String, Void, List<NewsBean>> {


        @Override
        protected List<NewsBean> doInBackground(String... params) {
            //下载路径
            String url = params[0];
            return getJsonData(url);
        }

        @Override
        protected void onPostExecute(List<NewsBean> newsBeanList) {
            super.onPostExecute(newsBeanList);
            mListView.setAdapter(new NewsAdapter(MainActivity.this,newsBeanList,mListView));
        }
    }

    /**
     * 从服务器获取Json对象转换为List集合
     *
     * @param urlString
     * @return
     */
    private List<NewsBean> getJsonData(String urlString) {
        List<NewsBean> newsBeenList = new ArrayList<>();
        JSONObject jsonObject;
        NewsBean newsBean;
        try {
            String jsonString = readStream(new URL(urlString).openStream());
            jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                newsBean = new NewsBean();
                newsBean.mIconUrl = jsonObject.getString("picSmall");
                newsBean.mContent = jsonObject.getString("description");
                newsBean.mTitle = jsonObject.getString("name");
                newsBeenList.add(newsBean);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsBeenList;
    }

    private String readStream(InputStream inputStream) {
        StringBuffer sb = new StringBuffer("");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
