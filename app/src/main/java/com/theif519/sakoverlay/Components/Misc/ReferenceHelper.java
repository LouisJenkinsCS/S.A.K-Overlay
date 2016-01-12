package com.theif519.sakoverlay.Components.Misc;

import android.util.ArrayMap;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.theif519.sakoverlay.Components.Types.ReferenceType;

import java.util.Map;

/**
 * Created by theif519 on 1/4/2016.
 *
 * TODO: Need to have a POJO to contain the BaseComponent, and list of Conditionals and Actions rather than a simple map.
 * TODO: This way I can use an Observable easily to map each.
 */
public class ReferenceHelper {
    Map<String, ReferenceType<?>> mMappedReferences;
    private static final String[] STATEMENTS = { "IF", "ELSE IF", "ELSE"};

    private static final ReferenceHelper INSTANCE = new ReferenceHelper();

    public static ReferenceHelper getInstance(){
        return INSTANCE;
    }

    public ReferenceHelper() {
        mMappedReferences = new ArrayMap<>();
    }

    public ReferenceHelper add(ReferenceType<?>... referenceTypes){
        Stream.of(referenceTypes)
                .forEach(ref -> mMappedReferences.put(ref.getName(), ref));
        return this;
    }

    public Stream<ReferenceType<?>> getAllReferences(){
        return Stream.of(mMappedReferences.values());
    }

    public Stream<Map.Entry<String, ReferenceType<?>>> getMappedReferences(){
        return Stream.of(mMappedReferences);
    }

    @SuppressWarnings("unchecked")
    public <T> Stream<ReferenceType<T>> getAllReferencesOfType(Class<T> clazz){
        return Stream.of(mMappedReferences.values())
                .filter(ref -> clazz.isAssignableFrom(ref.getInstanceClass()))
                .map(ref -> (ReferenceType<T>) ref);
    }

    public boolean contains(ReferenceType<?> referenceType){
        return mMappedReferences.containsValue(referenceType);
    }

    public boolean contains(String referenceName){
        return mMappedReferences.containsKey(referenceName);
    }

    public Optional<ReferenceType<?>> get(String referenceName){
        return Optional.ofNullable(mMappedReferences.get(referenceName));
    }

    public String[] getStatements(){
        return STATEMENTS;
    }

}
