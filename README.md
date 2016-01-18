# S.A.K-Overlay

## What is it?

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
