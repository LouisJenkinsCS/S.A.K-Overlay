package com.theif519.sakoverlay.Components.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Misc.MethodWrapper;
import com.theif519.sakoverlay.Components.Misc.ReferenceHelper;
import com.theif519.sakoverlay.Components.Misc.ReferenceType;
import com.theif519.sakoverlay.R;

import java.util.List;

/**
 * Created by theif519 on 1/7/2016.
 */
public class ComponentConstructIf extends LinearLayout {
    private ReferenceHelper mHelper;

    private Spinner mReference, mConditional;

    public ComponentConstructIf(Context context, ReferenceHelper helper) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.component_constructs_if, this);
        mReference = (Spinner) findViewById(R.id.component_constructs_if_reference);
        mConditional = (Spinner) findViewById(R.id.component_constructs_if_conditional);
        List<String> options = Stream.of(helper.getAllReferences())
                .map(ReferenceType::getId)
                .collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mReference.setAdapter(adapter);
        mReference.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<String> list =
                Stream.of(helper.getAllReferences())
                        .filter(ref -> ref.getId().equals(parent.getItemAtPosition(position)))
                        .flatMap(ref -> Stream.of(ref.getConditionals().getAllMethods()))
                        .map(MethodWrapper::getMethodName)
                        .collect(Collectors.toList());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, list);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mConditional.setAdapter(adapter);
                mConditional.setVisibility(VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mConditional.setVisibility(INVISIBLE);
            }
        });
    }
}
