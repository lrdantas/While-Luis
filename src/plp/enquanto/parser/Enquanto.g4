grammar Enquanto;

programa : seqComando;     // sequência de comandos

seqComando: comando (';' comando)* ;

comando: ID ':=' func                                    # atribuicaoFuncao
	   | ID ':=' expressao                               # atribuicao
	   | parametros ':=' valores                         # atribuicaoParalela
       | 'skip'                                          # skip
       | 'se' booleano 'entao' comando ('senaose' booleano 'entao' comando)*? 'senao' comando   # se
       | 'escolha' expressao ('caso' expressao ':' comando)* ('outro' ':' comando)?	# switch
       | 'enquanto' booleano 'faca' comando              # enquanto
       | 'para' ID 'de' expressao 'ate' expressao ('passo' expressao)? 'faca' comando	# para
       | 'exiba' TEXTO                                   # exiba
       | 'escreva' expressao                             # escreva
       | '{' seqComando '}'                              # bloco
       ;

func: '(' parametros ')' '=>' expressao                  # funcao
;

parametros: ID (',' ID)*?;

valores: expressao (',' expressao)*;


expressao: INT                                           # inteiro
         | 'leia'                                        # leia
         | ID                                            # id
         | expressao '^' expressao                       # opBin
         | expressao ('*' | '/') expressao               # opBin
         | expressao ('+' | '-') expressao               # opBin
         | '(' expressao ')'                             # expPar
         ;

booleano: BOOLEANO                                       # bool
        | expressao '=' expressao                        # opRel
        | expressao '<' expressao                        # opRel
        | expressao '>' expressao                        # opRel
        | expressao '<>' expressao                       # opRel
        | expressao '<=' expressao                       # opRel
        | expressao '>=' expressao                       # opRel
        | 'nao' booleano                                 # naoLogico
        | booleano 'ou' booleano                         # ouLogico
        | booleano 'xor' booleano                        # xorLogico
        | booleano 'e' booleano                          # eLogico
        | '(' booleano ')'                               # boolPar
        ;


BOOLEANO: 'verdadeiro' | 'falso';
INT: ('0'..'9')+ ;
ID: ('a'..'z')+;
TEXTO: '"' .*? '"';

Comentario: '#' .*? '\n' -> skip;
Espaco: [ \t\n\r] -> skip;