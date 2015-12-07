package com.theif519.sakoverlay.Rx;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by theif519 on 11/25/2015.
 *
 * This is a simple EventBus implementation utilizing RxJava. The basic idea of it was taken from here...
 *
 * http://nerds.weddingpartyapp.com/tech/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/
 *
 * To summarize, this class utilizes Rx to the fullest to allow a completely decoupled broadcsating system,
 * without the hassle of setting up Intents, adding intent extras (and lord knows if you need to add like
 * 10 things to an Intent, you need to do it sequentially (I.E, no fluent interface) so 10 things would take a
 * minimum of 10 lines. And since BroadcastReceiver was made for general purpose, to send an object, even
 * locally, it must implement either Serializable or Parcelable, even though there is no need for IPC.
 *
 * This little event bus allows you to send whatever you want, wherever you want, to whomever you want without
 * knowing who (or even where) it's sent to. Also, it allows you to send an object directly, without needing
 * to marshall it into an Intent or even a Message. It is as simple as publishing the object. Also, the user
 * can subscribe/register for certain events, which returns a Observable which can be subscribed to. It works
 * by simply filtering out events which are not of the specified type.
 *
 * For example usage, see this extremely simple way of sending a String as an event, without the complexity
 * of a normal broadcast receiver. Here is how it would normally look...
 *
 * LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this); // Need context
 * Intent intent = new Intent(...); // Now, pick whether you want to send the String as a Action, or as a normal StringExtra
 * intent.putExtra("Key", "Value"); // What a waste of a key. Now we have to make another global constant just for this type.
 * manager.sendBroadcast(intent);
 *
 * But wait wait wait, what if we do not have a context to give it? That's what getInstance() takes, a context.
 * What if we wish to broadcast something from inside of another thread, or even better yet, what if you want
 * to use a context within an custom listener (I.E View.OnTouchListener)? Okay, no biggie, you can pass in another
 * context. What if it's something which is final and cannot be overridden? Okay, also not too big of a deal, you
 * can follow the AppContext getInstance() singleton pattern/workaround (probably an anti-pattern) which actually
 * goes against Android's best practices, but everyone has to settle with it because it's the only way to do it.
 *
 * Not to mention other complications, like "How do I know if the message has been received? How do I receive
 * a response to know it has been received if I go that route? What if I have an error, how should I return that?"
 *
 * These were all things which plagued me just a couple weeks ago before I started using RxJava. Now, lets demonstrate
 * how to use this RxBus, and how simple it is.
 *
 * RxBus.publish("Hello World");
 *
 * Done. No need for serializing. If the object itself sent cannot be serialized/parceled, you're suddenly
 * not screwed any longer. Of course this is restricted to local events, but so is LocalBroadcastManager. It can be
 * called from any class, any thread, anywhere (that is within this process).
 *
 * Now on that note, I am not bashing Android's LocalBroadcastManager, especially not the normal BroadcastManager, as
 * IPC is VERY complicated and is not easy to do. However, this event bus is an easy to use implementation and fits this
 * project's purposes perfectly.
 */
public class RxBus {
    private static final Subject<Object, Object> INSTANCE = new SerializedSubject<>(PublishSubject.create());

    /**
     * Publishes an event to any and all subscribers. The object does not need to be serializable, nor does it
     * need to be wrapped in an interface such as Parcelable.
     * @param event Event to publish.
     */
    public static void publish(Object event){
        INSTANCE.onNext(event);
    }

    /**
     * Returns an observable filtered to only emit events for the specified type.
     * @param eventType Specified class of event to filter. I.E, Event.class
     * @param <T> Type of event. I.E, Event
     * @return Filtered observable.
     */
    public static <T> Observable<T> subscribe(final Class<T> eventType){
        return INSTANCE.filter(new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return eventType.isInstance(o);
            }
        }).cast(eventType);
    }
}
