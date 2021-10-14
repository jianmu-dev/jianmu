grammar JianMuEl;

@header {
package dev.jianmu.el.antlr4;
}

equation : expression EOF;

literal
    : INT_LITERAL
    | FLOAT_LITERAL
    | STRING_LITERAL
    | BOOL_LITERAL
    | NULL_LITERAL
    ;

expression
    : primary
    | prefix=NOT expression
    | expression bop=(TIMES|DIV|MODULO) expression
    | expression bop=(PLUS|MINUS) expression
    | expression bop=(LE | GE | GT | LT) expression
    | expression bop=(EQ | NE) expression
    | expression bop=AND expression
    | expression bop=OR expression
    ;

primary
    : '(' expression ')'
    | literal
    | VARNAME
    | TEMPLATE
    ;

// Literals

INT_LITERAL: DIGIT+;
FLOAT_LITERAL: DIGIT+ '.' DIGIT*;
BOOL_LITERAL:       'true'
            |       'false'
            ;
STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';
NULL_LITERAL:       'null';
TEMPLATE:           '`' (~[\\\r\n] | EscapeSequence)* '`';

// Operators

LBRACK :  '[';
RBRACK :  ']';
LPAREN :  '(';
RPAREN :  ')';
PLUS   :  '+';
MINUS  :  '-';
TIMES  :  '*';
DIV    :  '/';
MODULO :  '%';

DOT    :  '.';

GT     :  '>';
GE     :  '>=';
LT     :  '<';
LE     :  '<=';

EQ     :  '==';
NE     :  '!=';

NOT    :  '!';
AND    :  '&&';
OR     :  '||';

DollarString : '$';
OpenCurlyBracket:   '{';
CloseCurlyBracket:  '}';
REVERSE_QUOTE_SYMB: '`';

WS  : [ \t\r\n]+ -> skip ;

// VarName
VARNAME: DollarString OpenCurlyBracket Letter LetterOrDigitOrDot* CloseCurlyBracket;

// Fragment rules
fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;
fragment HexDigit
    : [0-9a-fA-F]
    ;
fragment DIGIT
   : [0-9]
   ;
fragment LetterOrDigitOrDot
    : Letter
    | DOT
    | [0-9]
    ;
fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;