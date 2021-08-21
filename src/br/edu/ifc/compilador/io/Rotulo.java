package br.edu.ifc.compilador.io;
/**
 * @author Felipe Tenfen
 * 
 * Classe respons�vel por realizar o gereciamento de r�tulos do compilador
 */
public class Rotulo
{
	static int contador;

	public Rotulo()
	{
		contador = 0;
	}
	
	public void resetRotulo()
	{
		contador = 0;
	}
	
	public String novoRotulo()
	{
		return "R" + contador++;
	}
}