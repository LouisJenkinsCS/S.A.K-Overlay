# S.A.K-Overlay

## What is it?

S.A.K-Overlay is the brand new, unique and original Window Manager for Android. A Window Manager, in this sense, is a container which allows the dynamic placement of Widgets to the user's specifications. While this definition may differ from the conventional meaning, it maintains the central philosophy in mind.

S.A.K-Overlay allows the user to place custom-made Widgets wherever they may please, and is persistent in that they are maintained across user-sessions. S.A.K-Overlay will soon allow the user to create their own custom Widgets via our Drag-and-Drop tools, which will allow the user to create a Widget Layout from scratch to be inflated later. This means you can create your own mini-apps within S.A.K-Overlay without any prior programming experience. Code can be similated through our Callback Generator, which will support operations which effect other user-aligned Widget Views (created inside the Widget Layout), or even effect Widgets outside of the Widget Layout (I.E, pre-made Widgets, or launching another app). This feature is far off from completion, but is steadily getting there,and will be one of the defining features of this application.

S.A.K-Overlay also will soon allow the user to place homescreen widgets as well, allowing S.A.K-Overlay to act as a second, more portable Homescreen.

To summarize, this will allow the user to align and place (or even Snap) Widgets, either pre-made, user-created, or homescreen, wherever they please. The ability to detach (Remove from central container, add to WindowManager) the Widget from the overlay will also be created soon.

### Window Manager

#### Widgets

Comes with already created and configured, convenient widgets.

##### Web Browser

* Minimal web browser
* Basic navigation of history

##### Sticky Note

* Write notes
* Persistent

##### Google Maps

* Shows current location and address

##### Screen Recorder

* Records the screen
* Can be controlled through a "floating" controller (Play/Stop button)

##### Custom

Create your own widgets (coming soon) with our Drag-And-Drop tool, which allows you, the user, to create your own Widget. As it is impossible to anticipate everyone's wants and needs, this feature allows you to make it yourself.

* Drag and Drop
    - Move and align components of your layout, yourself.
* Attribute Menu
    - Allows you to manually alter attributes
    - Allows you to create simple callback methods
        + Uses a limited, minimal but simplistic DSL (Domain Specific Language)
        + For example, on a button, you may want it to perform some action when pressed. With our simple Callback editor, it will allow simplistic actions based on predefined but user-aligned conditions

End Goal: In the end, I want to make it possible for anyone to import their own XML and .Java files themselves, and vice versa (exporting). To do so, I would transcribe my own DSL into a .Java file, compile it to .Dex, load it with a DexClassLoader, and call the callback methods necessary that way (I.E, onClick). This way, it can replicate the normal Android development way of using XML + .java.

#### Multitasking

* Launch multiple Widgets
* Align multiple Widgets however you want
* Snap Widgets to both sides and corners of the screen
* Persistent
* Application in background remain visible (I.E, overlay is mostly transparent), hence is less likely to be killed by Android.

#### Gaming

##### Philosophy

* Designed with gaming in mind
* Originally meant to mimic the Steam overlay, now it will be capable of much more (in the future)

### Pricing

* When finished, this application will be completely free (with donations appreciated) and open source.
