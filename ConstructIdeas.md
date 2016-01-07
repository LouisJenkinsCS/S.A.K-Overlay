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
