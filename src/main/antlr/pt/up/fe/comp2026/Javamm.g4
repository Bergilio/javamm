grammar Javamm;

@header {
    package pt.up.fe.comp2026;
}

PACKAGE : 'package' ;
IMPORT : 'import' ;
CLASS : 'class' ;
EXTENDS : 'extends' ;
PUBLIC : 'public' ;
// PRIVATE : 'private' ;
// PROTECTED : 'protected' ;
STATIC : 'static' ;
RETURN : 'return' ;
INT : 'int' ;
BOOLEAN : 'boolean' ;
VOID : 'void' ;
IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
LENGTH : 'length' ;
NEW : 'new' ;
THIS : 'this' ;

INTEGER : '0' | [1-9][0-9]* ;
BOOL : 'true' | 'false';
ID : ([a-zA-Z] | '$' | '_')([a-zA-Z] | [0-9] | '$' | '_')* ;

WS : [ \t\n\r\f]+ -> skip ;

SINGLE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

program
    : packageDecl importDecl* classNode=classDecl EOF
    ;

packageDecl
    : PACKAGE path += ID ('.' path += ID)* ';'
    ;

importDecl
    : IMPORT path += ID ('.' path += ID)+ ';'
    ;

classDecl
    : CLASS name=ID (EXTENDS extension=ID)?
    '{' 
    varDecl*
    methodDecl* 
    '}'
    ;

varDecl
    : typeNode=type name=ID ';'
    ;

param
    : typeNode=type name = ID
    ;

type
    : name=(INT|BOOLEAN|VOID|ID) ('[' ']')?
    ;

methodDecl locals[boolean isStatic=false]
    : (visibility=PUBLIC)? (STATIC {$isStatic=true;})?
      returnType=type name=ID
      '(' (params += param (',' params += param)*)? ')'
      '{' varDecl* stmt* '}'
    ;

stmt
    : var=ID '=' expr ';' #AssignStmt
    | RETURN (expr)? ';' #ReturnStmt
    | '{' stmt* '}' #Block
    | IF '(' expr ')' stmt ELSE stmt #IfStmt
    | WHILE '(' expr ')' stmt #WhileStmt
    | expr ';'  #ExprStmt
    | ID '[' expr ']' '=' expr ';' #AssignArrayElementStmt
    ;

expr
    : '(' expr ')' #Parenthesis
    | expr '[' expr ']' #ArrayAcess
    | expr '.' LENGTH #LengthOperator
    | expr '.' func=ID '(' (expr (',' expr)* )? ')' #FunctionCall
    | NEW INT '[' expr ']' #ArrayAlloc
    | NEW class=ID '(' ')' #ClassCreation
    | op='!' expr #UnaryExpr
    | expr op=('*'|'/') expr #BinaryExpr
    | expr op=('+'|'-') expr #BinaryExpr
    | expr op='<' expr #BinaryExpr
    | expr op='&&' expr #BinaryExpr
    | expr op='||' expr #BinaryExpr
    | value=INTEGER #IntegerLiteral
    | value=BOOL #BooleanLiteral
    | name=ID #VarRefExpr
    | THIS #CurrentObject
    ;