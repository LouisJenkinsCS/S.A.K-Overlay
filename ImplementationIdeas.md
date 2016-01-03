Layout Creator - Scripted/Conditional Constructs

IF Statement
- Implicitly set to "this" instance
    + Can explicitly be set using the "With" operator
- Obtains the possible conditionals for the set object (defined by the instance of the class).
    + Displays all conditionals in a dropdown spinner
AND Statement
- Used to join two or more conditionals into one.
    + Can only be used with the current reference
OR Statement
- Used to join two or more conditionals into one.
    + Also can only be used with the current reference.
THEN Statement
- Proceeds the IF statement
    + With the conditional, if it is true, this statement will be executed
    + Lists the next possible statements
WITH Statement
- Obtains a list of references/components currently available in a spinner
    + User may select one of these which will allow them to get the next possible statements
DO Statement
- Perform an action on the currently scoped reference
    + List of actions defined by the reference.
ELSE_IF Statement
- Executes this chain if the preceeding IF and ELSE_IF statement is false.
ELSE Statement
- Executes this chain if all preceeding IF and ELSE_IF statements are false
FOR Statement
- Executes the proceeding statement for a set amount of time, may be infinite or finite.

Types of Constructs
- Statements
    + Control-flow statements
        * IF, ELSE, etc.
- Conditionals
    + Scope-based and Context-based conditionals
- Action
    + Actions performed on references and variables in the current scope.
- Reference
    + Refers to a Component currently in the layout.
        * Defines it's own actions, conditionals, etc.
- Local Variable
    + Variables used to store information locally.
- Global Variable
    + Variables used to store information globally.

Script-Helper Menu
- Brings up possible options based on scope and context.
    + I.E, will show the currently usable Statements, Conditionals, Actions, etc.
- Should be smart enough to parse possible options as quickly as possible.


Pseudocode Example
[] -> Conditional
() -> Action
{} -> Reference
(::) -> Setter Action
? -> User input
: -> Function call, comma separated
"" -> Compile-time Defined

When Button1 is clicked, it will execute the user defined instructions. Anything shown in any brackets will be obtained through the script-helper menu. Hence, the user has to only type the source String. Should only take about 15 seconds in reality if they copy paste.

Compiled as...

Button1.onClick
    IF {ImageView1}[isVisible]
        {ImageView1}(setVisible::false)
    ELSE
        {ImageView1}(setSource:"https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")

Seen as...

    IF ImageView1.isVisible()
        ImageView1.setVisible(false)
    ELSE
        ImageView1.setSource("https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")

When Button2 is clicked, it will execute a simple for loop. GlobalsFuncs and LocalVars global in reference, but will display to the user the appropriate information. Also, when the for loop is done and we leave the scope of the loop, "X" will be removed from LocalVars, unless defined as a global variable. The AS operator will be optional, and is used to store each index in the variable X.

Compiled as...

Button2.onClick
    FOR "1" to "99" AS "X"
        {GlobalFuncs}(println: "Index: " + {LocalVars}(get::"X"))

Seen as...

    FOR 1 to 99 AS X
        println("Index: " + X)