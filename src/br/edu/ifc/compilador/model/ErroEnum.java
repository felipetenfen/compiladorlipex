package br.edu.ifc.compilador.model;

/**
 * @author Felipe Tenfen
 * 
 * Enum com os tipos de erros do compilador
 */
public enum ErroEnum
{
	ID_DECLARADO,
	ID_NAO_DECLARADO,
	TIPO_INCOMPATIVEL,
	CLASSE_INCOMPATIVEL,
	TOKEN_NAO_ESPERADO,
	FIM_NAO_ESPERADO,
	CARACTERE_INVALIDO,
	LEXEMA_NAO_ESPERADO,
	LEXEMA_NAO_IDENTIFICADO,
	PROGRAMA_NAO_INICIADO;
}