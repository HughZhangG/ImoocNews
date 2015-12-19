package com.zgc.imoocnews.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import com.zgc.imoocnews.R;
import com.zgc.imoocnews.adapter.NewsAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2015/12/19 17:26.
 */
public class ImageLoader {

    private ImageView mIvIcon;
    private String mUrl = null;
    private LruCache<String, Bitmap> mLruCache;
    private ListView mListView;
    private Set<ImageAsyncTask> mTasks;

    public ImageLoader(ListView listView) {
        mListView = listView;
        mTasks = new HashSet<>();

        int maxSize = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxSize / 4;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //返回图片大小
                return value.getByteCount();
            }
        };
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            // mIvIcon.getTag().equals(mUrl)避免因为缓存出现的图片重复加载问题
            if (mIvIcon.getTag().equals(mUrl)) {
                mIvIcon.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    public void addBitmapToCache(String url, Bitmap bitmap) {
        if (getBitmapFromCache(url) == null) {
            mLruCache.put(url, bitmap);
        }
    }

    public Bitmap getBitmapFromCache(String url) {
        return mLruCache.get(url);
    }


    /**
     * 实现停止滑动之后加载可见条目
     *
     * @param start
     * @param end
     */
    public void loadImages(int start, int end) {
        for (int i = start; i < end; i++) {
            String url = NewsAdapter.URLS[i];
            //查看缓存
            Bitmap bitmap = getBitmapFromCache(url);
            if (bitmap == null) {
                ImageAsyncTask task = new ImageAsyncTask(url);
                task.execute(url);
                mTasks.add(task);
            } else {
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * 停止所有正在进行的任务
     */
    public void cancelAllTasks() {
        if (mTasks != null) {
            for (ImageAsyncTask task : mTasks) {
                task.cancel(false);
            }
        }
    }


    /**
     * 通过多线程方式访问网络数据。未实现LruCache缓存
     *
     * @param ivIcon
     * @param url
     */
    public void loadByThread(ImageView ivIcon, final String url) {
        mIvIcon = ivIcon;
        mUrl = url;
        new Thread() {
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromUrl(mUrl);
                Message message = Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);
            }
        }.start();
    }


    private Bitmap getBitmapFromUrl(String urlString) {
        InputStream inputStream = null;
        HttpURLConnection connection;
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    /**
     * 通过异步任务方式访问网络数据  图片资源  实现LruCache缓存
     *
     * @param url
     */
    public void loadImageByAsyncTask(ImageView imageView,String url) {
        Bitmap bitmap = getBitmapFromCache(url);
        if (imageView == null)
            return;
        if (bitmap == null ) {
//              new ImageAsyncTask(url).execute(url);
            //为了将加载启动 移动 到 滚动事件
            imageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }



    class ImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView mIvIcon;
        private String mUrl;

        public ImageAsyncTask(String url) {
            this.mIvIcon = (ImageView) mListView.findViewWithTag(url);
            this.mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            //从网络请求资源
            Bitmap bitmap = getBitmapFromUrl(url);
            if (bitmap != null) {
                addBitmapToCache(url,bitmap);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //通过url作为图片标记去除图片重复加载的BUG
            if (mIvIcon.getTag().equals(mUrl) && bitmap != null)
                mIvIcon.setImageBitmap(bitmap);
            mTasks.remove(this);
        }
    }
}
