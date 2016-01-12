Modes
- Statement
    + Masks
        * Reference
- Reference
    + Masks
        * Conditional
        * Action
        * Setter
        * Getter
    + Determines by the current mode, as it does not change.
        * Need some boolean to determine if it currently has a reference.
- Conditionals
    + Masks
        * None
    + Resets back to Statement|Reference
- Action
    + Masks
        * None.
    + Resets back to Statement|Reference

Meaning...

I only need a preset of 4 adapters for an Expandable List View.

Adapters
- Default Mode
    + Reference
    + Statement
- Statement Mode
    + Reference
- Reference Mode
    + Conditional
    + Action
    + Getter
    + Setter
    + Influenced by older mode.


PopupMenu
- Handles...
    + Querying from ReferenceHelper to retreive set selection options
    + Handling button clicks and notifying parent (Code)
Code
- Handles...
    + User Input
        * Switch between TextView + PopupMenu and EditText with certain InputType
    + Notifying parent (LineOfCode) of selection change.
LineOfCode
- Handles...
    + Spawning Code
        * In response to Code children's selection
    + Deleting excess Code
        * In reponse to Code's children selection (If midway change)
    + Keep track of it's type
        * STATEMENT_IF, STATEMENT_ELSE, etc.
    + Notifying parent when all child Code has finished based on type.
LineWrapper
- Handles...
    + Adds buttons for user input based on completion
        * Delete, Add New, Copy, etc.
    + Adds Horizontal Scrollview
    + Notify parent BlockOfCode when a button is pressed.
BlockOfCode
- Handles...
    + Spawning LinesOfCode
        * In response to User Input for LineWrapper
    + Deleting LinesOfCode
        * Either in response to User Input from LineWrapper, or if a parent LineOfCode (I.E, IF for IF...ELSE statement) gets deleted.

Redesign Decisions
- Attribute Menu
    + Have PopupMenu attached to text at top
        * Have it automatically select current selected components
        * But also have it list other components as well to query their attributes
    + Have either a tab layout for different types of attribute options or PopupMenu appear from a TextView
        * Position & Size
        * Text
        * Callbacks
        * etc.
    + Have each category fill the entire Drawer, within a vertical and horizontal scrollview.
    + Redesign DynamicComponent to allow it to easily add (and remove) options to (and from) the attribute menu.
- LayoutCreator
    + Improve overall design
    + Handle interaction between dynamic components
        * Create a class to handle collision and snapping
        * I.E, make it easier to align two different components up
            - Or even align with the sides or center, etc.
        * Could probably be done by having them emit invisible lines which can be checked with for collision.
- Callbacks
    + Move to BaseComponent
        * View handles all onTouch and onClick events
    + Remove any parenthesis and stick with indentation
        * Indentation is easier to understand for new programmers
            - I.E, Python
    + Make more elegant and aesthetically pleasing
        * Maybe not now, but adding animations should make it more desirable.
    + 
