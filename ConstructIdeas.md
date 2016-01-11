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