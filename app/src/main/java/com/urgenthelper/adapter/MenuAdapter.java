package com.urgenthelper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.urgenthelper.ItemEntry.MenuEntry;
import com.urgenthelper.R;
import com.urgenthelper.adapter.MenuAdapter.MenuViewHolder;
import com.urgenthelper.adapter.base.RecyclerBaseAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/6.
 */

public class MenuAdapter extends RecyclerBaseAdapter<MenuEntry, MenuViewHolder> {

    public MenuAdapter(Context context) {
        super(context);
    }

    @Override
    protected void bindDataToItemView(MenuViewHolder viewHolder, MenuEntry item) {
        viewHolder.menuImg.setImageResource(item.mIconResId);
        viewHolder.menuTxt.setText(item.mTextStr);
    }

    @Override
    public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflateItemView(parent, R.layout.item_menu);
        return new MenuViewHolder(itemView);
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.menu_img)
        ImageView menuImg;
        @BindView(R.id.menu_txt)
        TextView menuTxt;

        public MenuViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
