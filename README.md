# S.A.K-Overlay
Swiss-Army-Knife Overlay

## Description

### What is it?

S.A.K-Overlay, short for Swiss-Army-Knife Overlay, is an all-in-one overlay for Android, designed specifically for Tablets but also with Phones in mind. It hosts a plethora of features, widgets and utilities for which may suit anyones need, all from one screen. 

It allows you to multitask, and also acts as sort of dynamic WindowManager, not to be confused with Android's WindowManager service, but in the conventional sense. S.A.K-Overlay allows you to host multiple windows, which are dynamically moveable, resizeable, and best of all, serializable/persistent.

#### Overlay

The overlay, like the meaning of the word, is another layer added on top of a current activity. It is fully transparent, hence acitivites behind it are visible and less likely to be killed, and can* seamlessly overlay on top of other programs.

* Whether or not the transition from the activity to the overlay is "seamless" depends on hwo the app is designed. For instance, there are few apps which release all resources in onPause rather than onStop, and hence it may end not be possible to transition back to the previous application. However, in my experience, most apps transition well from onPause (as the app is still visible) to onResume. 

### Why should I use it?

If you've ever wanted an all-in-one hub to access anything, from web browser, to your current geolocation (using Google Maps), to Post-It notes, to even screen recording, without having to hit the Home button, this is for you. If you do not like the current features offered, then there eventually will be AppWidgetHost support where you can host your own widgets. Then if that wasn't enough, eventually I will get around to implementing a way to create custom views yourself to have them be inflated dynamically at runtime.

Basically, this is your HomeScreen away from Home.

## Features

### Implemented

#### Web Browser

##### Summary

Browse the web without having to interrupt that task you're doing. 

#### Post-It Note

##### Summary

Remind yourself to do events later!

#### Maps

##### Summary

Find your current location or stores near you.

#### Screen Recorder

##### Summary

Record those valuable moments with just one press of a button!

### Transparent

Makes use of the Android Life Cycle based on visibility. Since the app being drawn over always remains visible, it is less likely to be destroyed.

### All-In-One

Every and anything you could need, all at one place, usable at any time. From a simple messager, email-client, web browser, or even sticky notes.

### Floating Widgets

Dynamic, resizable, moveable and persistent floating "widgets", basically fragments, which allow you to multitask easily.

## Costs

### Absolutely Free

TODO!

## FAQ

### TODO! (Think of questions)

## Screenshots

### TODO! (Take some)

