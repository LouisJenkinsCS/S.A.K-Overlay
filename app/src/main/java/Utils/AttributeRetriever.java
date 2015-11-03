package Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import com.example.theif519.saklauncher.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by theif519 on 10/11/2015.
 *
 * Author: Louis Jenkins
 *
 * This is a great utility class I spent about a day writing up, because I got tired of having to manually
 * retrieve attributes for each new layout-class pair I create. As I love to learn new things, this
 * was a wonderful way for me to experiment with reflection.
 *
 * AttributeRetriever does what it's name describes, it automates the retrieval of attributes declared
 * with <declare-styleable>. It retrieves the attributes through reflection by obtaining the class's
 * simple name, then appending a "_#source". By using annotations it makes it extremely simple to
 * parse out the setter methods to call. It uses setter methods as it allows it to work on either
 * public, protected, or private variables so long as the setter is declared.
 *
 * Annotations are as easy as adding the following about a setter method...
 *
 * @Utils.AttributeRetriever.AttributeHelper(Source = #source)
 *
 * or if you have Utils.AttributeRetriever imported...
 *
 * @AttributeHelper(Source = #source)
 *
 * That's literally all that's needed. You can define your default variables by setting them beforehand,
 * as it won't call the methods if the current instance does not have the attribute declared.
 *
 * As of yet, it only supports Float/float, Integer/int, and String, but only because I can't find
 * a way to truly generically get any type of attribute. Plus, this way it's amortized O(N) and doesn't
 * slow everything down too much.
 *
 * It can be called like this...
 *
 * Utils.AttributeRetreiver.fillAttributes(this.getClass(), this, context, attrs);
 */
public final class AttributeRetriever {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AttributeHelper {
        String source();
    }

    private AttributeRetriever() {

    }

    private static final String TAG = "AttributeRetriever";

    /**
     * A helper-method, made public for if I need to use it outside of this class. It simply
     * obtains the integer index associated with any given R.* class. I.E in this class, I used
     * it to generically retrieve the R.styleable attributes. (Will make more generic later)
     * @param rClass Class for any R.* class
     * @param name Name of the R.* class's field. MUST be a int[].
     * @return The int[] associated.
     */
    public static int[] getStyleableArr(Class rClass, String name){
        try {
            return (int[]) rClass.getField(name).get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Fills the attributes out for any annotated methods, if it can retrieve the attributes from the
     * R.styleable.* for the passed Class clazz. Best called from the constructor, as that is the only
     * time you will have both the AttributeSet and Context at the same time.
     * @param clazz The class of the object to fill the attributes for. I.E this.getClass()
     * @param self A pointer to the instance of clazz. I.E this.
     * @param context Context.
     * @param attrs Set of Attributes.
     */
    public static void fillAttributes(Class clazz, Object self, Context context, AttributeSet attrs){
        TypedArray arr = context.obtainStyledAttributes(attrs, getStyleableArr(R.styleable.class, clazz.getSimpleName()));
        /*
            By getting the declared methods, you are able to retrieve any and all public methods, in
            particular the annotated methods needed. It will skip any non-annotated methods entirely,
            or if the parameter length if greater than 1, as it can only pass the value parsed from the
            AttributeSet.
         */
        Method[] methods = clazz.getDeclaredMethods();
        for(Method m: methods){
            AttributeHelper helper = m.getAnnotation(AttributeHelper.class);
            if(helper == null) continue;
            if(m.getParameterTypes().length != 1) continue;
            Class type = m.getParameterTypes()[0];
            Log.v(TAG, "Parsed: " + helper.source());
            try {
                /*
                    Should be noted that all of R.styleable.* fields first contain the name of the
                    class followed by an underscore and it's attribute, so I take advantage of this fact.

                    Also by utilizing the fact that every single object has a toString() method, it makes
                    it possible to obtain the string representation for any primitive and wrapper reference type.
                    I don't know if this will work for other things, but I'm assuming that .toString() is called
                 */
                Field f = R.styleable.class.getField(clazz.getSimpleName() + "_" + helper.source());
                if(arr.getString(f.getInt(null)) == null) continue;
                Log.v(TAG, "Method: " + m.getName());
                Log.v(TAG, "Field: " + f.getName());
                if(type == float.class || type == Float.class){
                    m.invoke(self, arr.getFloat(f.getInt(null), 0));
                } else if(type == Integer.class || type == int.class){
                    m.invoke(self, arr.getInt(f.getInt(null), 0));
                } else if(type == Boolean.class || type == boolean.class){
                    m.invoke(self, arr.getBoolean(f.getInt(null), false));
                } else if(type == String.class){
                    m.invoke(self, arr.getString(f.getInt(null)));
                }
            } catch (InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
                if (e.getMessage() != null) Log.v(TAG, e.getMessage());
            }
        }
        arr.recycle();
    }
}
