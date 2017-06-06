package com.urgenthelper.adapter.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.urgenthelper.listeners.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/6.
 */

public abstract class RecyclerBaseAdapter<D,V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V>{
    protected final List<D> mDataList = new ArrayList<>();//RecyclerView中的数据集
    private OnItemClickListener<D> mItemClickListener;//点击事件回调处理
    protected Context mContext;

    public void setItemClickListener(OnItemClickListener<D> listener){
        mItemClickListener = listener;
    }

    public RecyclerBaseAdapter(Context context){
        mContext = context;
    }

    @Override
    public int getItemCount(){
        return mDataList.size();
    }

    @Override
    public void onBindViewHolder(V holder,int position){
        final D item = getItem(position);
        bindDataToItemView(holder,item);
        setupItemViewClickListener(holder,item);
    }

    protected  D getItem(int position){
        return mDataList.get(position);
    }

    protected abstract void bindDataToItemView(V viewHolder,D item);

    protected void setupItemViewClickListener(V holder,final D item){
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(mItemClickListener!=null){
                    mItemClickListener.onClick(item);
                }
            }
        });
    }

    public void addItems(List<D> items){
        items.removeAll(mDataList);//移除已经存在的数据
        mDataList.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(D item){
        mDataList.add(item);
        notifyDataSetChanged();
    }

    public void update(){
        notifyDataSetChanged();
    }

    public void clear(){
        mDataList.clear();
        notifyDataSetChanged();
    }

    protected View inflateItemView(ViewGroup viewGroup,int layoutId){
        return LayoutInflater.from(viewGroup.getContext()).inflate(layoutId,viewGroup,false);
    }
}
