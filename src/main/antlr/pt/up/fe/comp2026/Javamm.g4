grammar Javamm;

@header {
    package pt.up.fe.comp2026;
}

PACKAGE : 'package' ;
IMPORT : 'import' ;
CLASS : 'class' ;
EXTENDS : 'extends' ;
PUBLIC : 'public' ;
PRIVATE : 'private' ;
PROTECTED : 'protected' ;
STATIC : 'static' ;
RETURN : 'return' ;
INT : 'int' ;
BOOLEAN : 'boolean' ;
VOID : 'void' ;
IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
FOR : 'for' ;
DO : 'do' ;
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
    (varDecl | methodDecl)* 
    '}'
    ;

varDecl
    : typeNode=type name=ID ('=' expr)? ';'
    ;

param
    : typeNode=type name = ID
    ;

type    //Based on the Slides of the AST
    : name=(INT|BOOLEAN|VOID) #BaseType
    | type '[' ']' #ArrayType
    | name=ID #ClassType
    ;

methodDecl locals[boolean isStatic=false]
    : (visibility=(PUBLIC|PRIVATE|PROTECTED))? (STATIC {$isStatic=true;})?
      returnType=type name=ID
      '(' (param (',' param)*)? ')'
      '{' varDecl* stmt* '}'
    ;

stmt
    : var=ID '=' expr ';' #AssignStmt
    | RETURN (expr)? ';' #ReturnStmt
    | '{' stmt* '}' #Block
    | IF '(' expr ')' stmt (ELSE stmt)? #IfStmt
    | WHILE '(' expr ')' stmt #WhileStmt
    | FOR '(' (ID '=' expr)? ';' (expr)? ';' ((ID '=' expr) | (ID ('++'|'--')) | (('++'|'--') ID))? ')' stmt #ForStmt // stmt in the for has to be Assign
    | DO stmt WHILE '(' expr ')' ';' #DoWhileStmt // Does stmt have to be a block?
    | expr ';'  #ExprStmt
    | ID (arrayIndex)+ '=' expr ';' #AssignArrayElementStmt
    ;

// Need to improve extension
expr
    : '(' expr ')' #Parenthesis
    | expr (arrayIndex)+ #ArrayAccess
    | expr '.' LENGTH #LengthOperator
    | expr '.' func=ID '(' (expr (',' expr)* )? ')' #FunctionCall
    | expr '.' name=ID #FieldAccess
    | func=ID '(' (expr (',' expr)* )? ')' #ImplicitThisFunctionCall
    | NEW name=(INT|BOOLEAN|VOID) (arrayIndex)+ #ArrayAlloc
    | NEW name=(INT|BOOLEAN|VOID) (arrayDim)+ ('{' (expr (',' expr)*)? '}') #ArrayAllocInit
    | NEW classType=ID '(' (expr (',' expr)*)? ')' #Constructor
    | op=('!'|'+'|'-'|'++'|'--') expr #UnaryExpr
    | expr op=('*'|'/'|'%') expr #BinaryExpr
    | expr op=('+'|'-') expr #BinaryExpr
    | expr op=('<'|'>'|'<='|'>=') expr #BinaryExpr
    | expr op=('=='|'!=') expr #BinaryExpr
    | expr op='&&' expr #BinaryExpr
    | expr op='||' expr #BinaryExpr
    | value=INTEGER #IntegerLiteral
    | value=BOOL #BooleanLiteral
    | THIS #This
    | name=ID #VarRefExpr
    ;

arrayIndex
    : '[' expr ']'
    ;
arrayDim
    : '[' ']'
    ;