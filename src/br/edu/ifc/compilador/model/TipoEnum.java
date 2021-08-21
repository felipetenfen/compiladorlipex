package br.edu.ifc.compilador.model;

/**
 * @author Felipe Tenfen
 * 
 * Enum para definir os tipos de dados identificados
 */
public enum TipoEnum
{
	STRING,
	INTEIRO,
	LOGICO,
	BYTE;

	public static boolean isString(TipoEnum tipo)
	{
		return tipo != null && tipo == STRING;
	}
	
	public static boolean isInteiro(TipoEnum tipo)
	{
		return tipo != null && tipo == INTEIRO;
	}
	
	public static boolean isLogico(TipoEnum tipo)
	{
		return tipo != null && tipo == LOGICO;
	}
	
	public static boolean isByte(TipoEnum tipo)
	{
		return tipo != null && tipo == BYTE;
	}
}