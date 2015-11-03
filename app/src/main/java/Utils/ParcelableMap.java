package Utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.ArrayMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by theif519 on 10/26/2015.
 * <p/>
 * ParcelableMap will always contain a String key, as every object contains a toString() method, which by
 * default can be used to obtain it's hashcode.
 */
public class ParcelableMap<T extends Parcelable> implements Map<String, T>, Parcelable {

    private ArrayMap<String, T> mMap;

    private ClassLoader mLoader;

    private static final Parcelable.Creator<ParcelableMap<? extends Parcelable>> CREATOR = new Creator<ParcelableMap<? extends Parcelable>>() {
        @Override
        public ParcelableMap<? extends Parcelable> createFromParcel(Parcel source) {
            return new ParcelableMap<>(source);
        }

        @Override
        public ParcelableMap<? extends Parcelable>[] newArray(int size) {
            return new ParcelableMap<?>[size];
        }
    };

    public ParcelableMap(ClassLoader classLoader){
        mMap = new ArrayMap<>();
        mLoader = classLoader;
    }

    @SuppressWarnings("unchecked")
    private ParcelableMap(Parcel in) {
        mMap = new ArrayMap<>();
        for (int i = 0; i < in.readInt(); i++) {
            mMap.put(in.readString(), (T) in.readParcelable(mLoader));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMap.size());
        for (Map.Entry<String, T> entry : mMap.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
    }

    /*
        Everything below are delegated methods going straight to the  array mMap above!
     */

    @Override
    public void clear() {
        mMap.clear();
    }

    public void ensureCapacity(int minimumCapacity) {
        mMap.ensureCapacity(minimumCapacity);
    }

    @Override
    public boolean containsKey(Object key) {
        return mMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mMap.containsValue(value);
    }

    @Override
    public T get(Object key) {
        return mMap.get(key);
    }

    public String keyAt(int index) {
        return mMap.keyAt(index);
    }

    public T valueAt(int index) {
        return mMap.valueAt(index);
    }

    public T setValueAt(int index, T value) {
        return mMap.setValueAt(index, value);
    }

    @Override
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    @Override
    public T put(String key, T value) {
        return mMap.put(key, value);
    }

    public void putAll(ArrayMap<? extends String, ? extends T> array) {
        mMap.putAll(array);
    }

    @Override
    public T remove(Object key) {
        return mMap.remove(key);
    }

    public T removeAt(int index) {
        return mMap.removeAt(index);
    }

    @Override
    public int size() {
        return mMap.size();
    }

    @Override
    public boolean equals(Object object) {
        return mMap.equals(object);
    }

    @Override
    public int hashCode() {
        return mMap.hashCode();
    }

    @Override
    public String toString() {
        return mMap.toString();
    }

    public boolean containsAll(Collection<?> collection) {
        return mMap.containsAll(collection);
    }

    @Override
    public void putAll(Map<? extends String, ? extends T> map) {
        this.mMap.putAll(map);
    }

    public boolean removeAll(Collection<?> collection) {
        return mMap.removeAll(collection);
    }

    public boolean retainAll(Collection<?> collection) {
        return mMap.retainAll(collection);
    }

    @NonNull
    @Override
    public Set<Entry<String, T>> entrySet() {
        return mMap.entrySet();
    }

    @NonNull
    @Override
    public Set<String> keySet() {
        return mMap.keySet();
    }

    @NonNull
    @Override
    public Collection<T> values() {
        return mMap.values();
    }
}
