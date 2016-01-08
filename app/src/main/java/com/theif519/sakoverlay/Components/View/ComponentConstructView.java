package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Misc.ConstructStatement;
import com.theif519.sakoverlay.Components.Misc.MethodWrapper;
import com.theif519.sakoverlay.Components.Misc.ReferenceHelper;
import com.theif519.sakoverlay.Components.Misc.ReferenceType;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by theif519 on 1/7/2016.
 */
public class ComponentConstructView extends TextView implements PopupMenu.OnMenuItemClickListener {

    public static final int COMPONENTS = 1;
    public static final int CONDITIONALS = 1 << 1;
    public static final int ACTIONS = 1 << 2;
    public static final int STATEMENTS = 1 << 3;
    public static final int STATEMENTS_IF = 1 << 4;
    public static final int STATEMENTS_ELSE_IF = 1 << 5;
    public static final int STATEMENTS_ELSE = 1 << 6;

    private ReferenceHelper mHelper;
    private PopupMenu mMenu;
    private int mMask;
    private BehaviorSubject<String> mSelection = BehaviorSubject.create();

    public ComponentConstructView(Context context, ReferenceHelper helper) {
        super(context);
        mHelper = helper;
        mMenu = new PopupMenu(context, this);
        setText("Select...");
        setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOnClickListener(v -> mMenu.show());
        mMenu.setOnMenuItemClickListener(this);
    }

    // TODO: Move most of the work to another thread!
    public ComponentConstructView setOptions(int mask, Optional<ReferenceType<?>> reference) {
        Menu menu = mMenu.getMenu();
        mMask = mask;
        menu.removeItem(STATEMENTS);
        menu.removeItem(COMPONENTS);
        menu.removeItem(CONDITIONALS);
        menu.removeItem(ACTIONS);
        if (isSet(STATEMENTS)) {
            SubMenu statements = menu.addSubMenu(Menu.NONE, STATEMENTS, 1, "Statements");
            statements.add(STATEMENTS, STATEMENTS_IF, 1, ConstructStatement.IF.toString());
            statements.add(STATEMENTS, STATEMENTS_ELSE_IF, 2, ConstructStatement.ELSE_IF.toString());
            statements.add(STATEMENTS, STATEMENTS_ELSE, 3, ConstructStatement.ELSE.toString());
        }
        if (isSet(COMPONENTS)) {
            SubMenu components = menu.addSubMenu(Menu.NONE, COMPONENTS, 2, "Components");
            Stream.of(mHelper.getAllReferences())
                    .map(ReferenceType::getId)
                    .forEach(id -> components.add(COMPONENTS, Menu.NONE, Menu.NONE, id));
        } else {
            if (isSet(CONDITIONALS)) { // Note we do not want to both obtain conditionals and the reference itself. That would be silly.
                if (!reference.isPresent()) {
                    throw new RuntimeException("An attempt was made to retrieve conditional methods from a null reference!");
                }
                SubMenu conditionals = menu.addSubMenu(Menu.NONE, CONDITIONALS, 2, "Conditionals");
                Stream.of(reference.get().getConditionals().getAllMethods())
                        .map(MethodWrapper::getMethodName)
                        .filter(name -> name != null) // Since such things are processed in the background, it CAN return null if called too early.
                        .forEach(name -> conditionals.add(CONDITIONALS, Menu.NONE, Menu.NONE, name));
            }
            if (isSet(ACTIONS)) { // Note we do not want to both obtain conditionals and the reference itself. That would be silly.
                if (!reference.isPresent()) {
                    throw new RuntimeException("An attempt was made to retrieve action methods from a null reference!");
                }
                SubMenu conditionals = menu.addSubMenu(Menu.NONE, ACTIONS, 3, "Actions");
                Stream.of(reference.get().getActions().getAllMethods())
                        .map(MethodWrapper::getMethodName)
                        .filter(name -> name != null) // Since such things are processed in the background, it CAN return null if called too early.
                        .forEach(name -> conditionals.add(ACTIONS, Menu.NONE, Menu.NONE, name));
            }
        }
        return this;
    }

    public Observable<String> observeSelectionChanges() {
        return mSelection.asObservable();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (!item.hasSubMenu()) {
            setText(item.getTitle().toString());
            String extras = "";
            switch(item.getGroupId()){
                case CONDITIONALS:
                    extras = "()";
                    break;
                case ACTIONS:
                    extras = "(...)";
                    break;
                case COMPONENTS:
                    extras = ".";
                    break;
            }
            if(!extras.isEmpty()){
                setText(getText() + extras);
            }
            mSelection.onNext(item.getTitle().toString());
            return true;
        }
        return false;
    }

    private boolean isSet(int bit) {
        return (mMask & bit) != 0;
    }
}
