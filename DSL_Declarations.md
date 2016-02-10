#Intro

The abstract starts off like this. The user can choose between what type of flow they wish to go with; assignments (create variable on stack), statements (conditional and iterative loops), and expression (general actions, where anything goes). In EBNF notation, they are defined as such below. It is a rough draft.

##Default

<default> := <assignment>|<statements>|<expression>

##Assignment

Assignments will create variables allocated on some stack, to help with lexical scoping and ensuring no leakage occurs. That is up to the RunTime to decide how it is handled.

The variable name MUST start with a chracter, and may only contain characters, digits and/or an underscore thereafter, like many other currently existing programming languages. They also must not be in a list of reserved keywords, up to the implementor to check.

The variable type and value are up to the implementor to handle.

###Declaration

<assignment> := <var-type>, " ", <var-name>, " = ", <value>

<var-name> := char, ?(char|digit|"_")*

<var-type> := ???

<value> := ???

###Example

int num_3 = 3

where...

<var-type> = int; <var-name> = num_3; <value> = 3;

##Statement

Where each basic control flow can be defined as such below.

Also below are modifiers used to help when it comes to making more dynamic statements, both on left and right sides.

Also below is a conditional declaration, which is meant to represent a boolean value or expression.

##Declaration

<statement> := <if-then> | <else-if-then> | <else> | <for> | <while>

<l-modifier> := "!" | "("

<r-modifier> := <logical-op> | ")"

<conditional> := boolean|(<value>, <comparison-op>, <value>)

<chained-conditional> = (?<l-modifier>, <conditional>, ?<r-modifier>)*

###If-Then

<if-then> := <chained-conditional>

####Example

if !ImageView.isShowing() && ImageView.isEnabled()

where...

<l-modifier> = !; <conditional> = ImageView.isShowing() & ImageView.isEnabled(); <r-modifier> = "&&"

###Else-If-Then

<else-if-then> := <if-then>

####Example

else if EditText.getText()  == "Hello World"

where...

<conditional> = EditText.getText() == "Hello World"

in which conditional is the <value>, <comparison-op>, <value>

where...

<value> = EditText.getText(); <comparison-op> = "=="; <value> = "Hello World"

###Else

<else> := ;

####Example

else

As else is always on a line of it's own, it is defined as a termination.

###For

<for> := <assignment>, ";", <chained-conditional>, ";", <expression>

####Example

for int i = 0; i < 100; i++

###While

<while> := <chained-conditional>

####Example

while(buffer.next() != EOF)

##Expression

Where actions are performed. Basically the generic line of code where things get done, normally used after <statement>'s. This is also left to the implementor, as there are numerous things that can be done that is context specific.

####Example

WebView.setSrc("www.google.com")
