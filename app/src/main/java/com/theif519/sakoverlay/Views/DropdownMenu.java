package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.theif519.sakoverlay.Adapters.MenuOptionsAdapter;
import com.theif519.sakoverlay.R;

/**
 * Created by theif519 on 12/15/2015.
 */
public class DropdownMenu extends LinearLayout {

    private ListView mListView;

    public DropdownMenu(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dropdown_menu, this);
    }

    public DropdownMenu setMenuOptionsAdapter(MenuOptionsAdapter adapter){
        mListView.setAdapter(adapter);
        return this;
    }

    public DropdownMenu setOnItemClickListener(AdapterView.OnItemClickListener listener){
        mListView.setOnItemClickListener(listener);
        return this;
    }
}
