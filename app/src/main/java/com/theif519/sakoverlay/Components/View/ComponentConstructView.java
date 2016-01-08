package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Misc.ReferenceHelper;
import com.theif519.sakoverlay.Components.Misc.ReferenceType;

/**
 * Created by theif519 on 1/7/2016.
 */
public class ComponentConstructView extends TextView implements PopupMenu.OnMenuItemClickListener {

    public static final int COMPONENTS = 1;
    public static final int CONDITIONALS = 1 << 1;
    public static final int ACTIONS = 1 << 2;
    public static final int STATEMENTS = 1 << 3;
    public static final int STATEMENTS_IF = 1 << 4;
    public static final int STATEMENTS_ELSE = 1 << 5;

    private ReferenceHelper mHelper;
    private PopupMenu mMenu;
    private int mMask;

    public ComponentConstructView(Context context, ReferenceHelper helper) {
        super(context);
        mHelper = helper;
        mMenu = new PopupMenu(context, this);
        setText("Select...");
        setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
        setOnClickListener(v -> mMenu.show());
        mMenu.setOnMenuItemClickListener(this);
    }

    public ComponentConstructView setOptions(int mask) {
        Menu menu = mMenu.getMenu();
        mMask = mask;
        menu.removeItem(STATEMENTS);
        menu.removeItem(COMPONENTS);
        menu.removeItem(CONDITIONALS);
        menu.removeItem(ACTIONS);
        if (isSet(STATEMENTS)) {
            SubMenu statements = menu.addSubMenu(Menu.NONE, STATEMENTS, 1, "Statements");
            Stream.of(mHelper.getStatements())
                    .forEach(statement -> statements.add(Menu.NONE, Menu.NONE, Menu.NONE, statement));
        }
        if (isSet(COMPONENTS)) {
            SubMenu components = menu.addSubMenu(Menu.NONE, COMPONENTS, 2, "Components");
            Stream.of(mHelper.getAllReferences())
                    .map(ReferenceType::getId)
                    .forEach(id -> components.add(Menu.NONE, Menu.NONE, Menu.NONE, id));
        }
        return this;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // TODO: Make it so that only SubMenu clicks trigger setText(...)
        setText(item.getTitle());
        return true;
    }

    private boolean isSet(int bit) {
        return (mMask & bit) != 0;
    }
}
