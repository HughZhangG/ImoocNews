package com.zgc.imoocnews.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zgc.imoocnews.R;
import com.zgc.imoocnews.entity.NewsBean;
import com.zgc.imoocnews.utils.ImageLoader;

import java.util.List;

/**
 * Created by Administrator on 2015/12/19 17:10.
 */
public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    private int mStart, mEnd;
    public static String[] URLS;
    private boolean mIsFirst;

    private List<NewsBean> mNewsBeanList;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;

    public NewsAdapter(Context context, List<NewsBean> newsBeanList ,ListView listView) {
        mInflater = LayoutInflater.from(context);
        mNewsBeanList = newsBeanList;
        mImageLoader = new ImageLoader(listView);
        URLS = new String[newsBeanList.size()];

        for (int i = 0; i < newsBeanList.size(); i++) {
            URLS[i] = newsBeanList.get(i).mIconUrl;
        }

        //设置监听事件
        listView.setOnScrollListener(this);
        mIsFirst = true;
    }

    @Override
    public int getCount() {
        return mNewsBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return mNewsBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        NewsBean newsBean = mNewsBeanList.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_list_news, null);
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvContent.setText(newsBean.mContent);
        viewHolder.tvTitle.setText(newsBean.mTitle);
        String url = newsBean.mIconUrl;
        viewHolder.ivIcon.setTag(url);
//        new ImageLoader().loadByThread(viewHolder.ivIcon,url);
        mImageLoader.loadImageByAsyncTask(viewHolder.ivIcon,url);
        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE){
            //加载图片
            mImageLoader.loadImages(mStart,mEnd);
        }else{
            //停止所有任务
            mImageLoader.cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem + visibleItemCount;
        //保证画出来了visibleItemCount>0
        //第一次 预加载
        if (mIsFirst && visibleItemCount>0){
            mImageLoader.loadImages(mStart,mEnd);
            mIsFirst = false;
        }
    }


    private class ViewHolder {
        TextView tvTitle, tvContent;
        ImageView ivIcon;
    }
}
