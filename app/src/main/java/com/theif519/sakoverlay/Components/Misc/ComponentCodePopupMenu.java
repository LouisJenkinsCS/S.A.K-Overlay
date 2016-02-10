package com.theif519.sakoverlay.Components.Misc;

import android.content.Context;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.PopupMenu;

import com.annimon.stream.Optional;
import com.theif519.sakoverlay.Components.Types.ReferenceType;
import com.theif519.utils.Misc.BitmaskTools;

import java.util.Map;

import rx.Observable;
import rx.subjects.BehaviorSubject;

import static com.theif519.sakoverlay.Components.Types.QueryTypes.ACTIONS;
import static com.theif519.sakoverlay.Components.Types.QueryTypes.CONDITIONALS;
import static com.theif519.sakoverlay.Components.Types.QueryTypes.REFERENCES;
import static com.theif519.sakoverlay.Components.Types.QueryTypes.STATEMENTS;
import static com.theif519.sakoverlay.Components.Types.QueryTypes.STATEMENTS_ELSE;
import static com.theif519.sakoverlay.Components.Types.QueryTypes.STATEMENTS_ELSE_IF;
import static com.theif519.sakoverlay.Components.Types.QueryTypes.STATEMENTS_IF;

/**
 * Created by theif519 on 1/10/2016.
 */
public class ComponentCodePopupMenu extends PopupMenu implements PopupMenu.OnMenuItemClickListener {

    private static final int ALL_GROUP = 1;

    private final BehaviorSubject<Pair<Integer, String>> mSelected = BehaviorSubject.create();

    public ComponentCodePopupMenu(Context context, View anchor) {
        super(context, anchor, Gravity.BOTTOM);
        setOnMenuItemClickListener(this);
    }

    public ComponentCodePopupMenu setOptions(int mask, Optional<ReferenceType<?>> reference) {
        Menu menu = getMenu();
        menu.removeGroup(ALL_GROUP);
        if (BitmaskTools.isSet(mask, STATEMENTS)) {
            SubMenu statements = menu.addSubMenu(ALL_GROUP, STATEMENTS, 1, "Statements");
            statements.add(STATEMENTS, STATEMENTS_IF, 1, ConstructStatement.IF.toString());
            statements.add(STATEMENTS, STATEMENTS_ELSE_IF, 2, ConstructStatement.ELSE_IF.toString());
            statements.add(STATEMENTS, STATEMENTS_ELSE, 3, ConstructStatement.ELSE.toString());
        }
        if (BitmaskTools.isSet(mask, REFERENCES)) {
            SubMenu components = menu.addSubMenu(ALL_GROUP, REFERENCES, 2, "References");
            ReferenceHelper.getInstance().getMappedReferences()
                    .map(Map.Entry::getKey)
                    .forEach(methodName -> components.add(REFERENCES, Menu.NONE, Menu.NONE, methodName));
        } else { // Note we do not want to both obtain methods and the reference itself. That would be silly.
            if (BitmaskTools.isSet(mask, CONDITIONALS)) {
                ReferenceType<?> ref = reference.orElseThrow(() ->
                        new RuntimeException("An attempt was made to retrieve conditional methods from a null reference!"));
                SubMenu conditionals = menu.addSubMenu(Menu.NONE, CONDITIONALS, 2, "Conditionals");
                ref.getConditionals().getMappedMethods()
                        .map(Map.Entry::getKey)
                        .sorted()
                        .forEach(name -> conditionals.add(CONDITIONALS, Menu.NONE, Menu.NONE, name));
            }
            if (BitmaskTools.isSet(mask, ACTIONS)) {
                ReferenceType<?> ref = reference.orElseThrow(() ->
                        new RuntimeException("An attempt was made to retrieve action methods from a null reference!"));
                SubMenu conditionals = menu.addSubMenu(Menu.NONE, ACTIONS, 3, "Actions");
                ref.getActions().getMappedMethods()
                        .map(Map.Entry::getKey)
                        .sorted()
                        .forEach(name -> conditionals.add(ACTIONS, Menu.NONE, Menu.NONE, name));
            }
        }
        return this;
    }

    public Observable<Pair<Integer, String>> observeSelection(){
        return mSelected.asObservable();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (!item.hasSubMenu()) {
            mSelected.onNext(Pair.create(item.getGroupId(), item.getTitle().toString()));
            return true;
        }
        return false;
    }
}
