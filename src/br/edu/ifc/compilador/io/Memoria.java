package br.edu.ifc.compilador.io;
/**
 * @author Felipe Tenfen
 * 
 * Classe responsável por realizar o gereciamento de memória do compilador
 */
public class Memoria
{
	private int contador, contTemp;

	public Memoria()
	{
		contador = 0;
		contTemp = 0;
	}
	
	public void restetTemp()
	{
		contTemp = 0;
	}
	
	public int alocarTemp()
	{
		int tmp = contador;
		contador += 16384;
		
		return tmp;
	}
	
	public int alocarByte()
	{
		int tmp = contador;
		contador++;
		
		return tmp;
	}
	
	public int alocarLogico()
	{
		int tmp = contador;
		contador++;
		
		return tmp;
	}
	
	public int alocarInteiro()
	{
		int tmp = contador;
		contador += 2;
		
		return tmp;
	}
	
	public int alocarString()
	{
		int tmp = contador;
		contador += 256;
		
		return tmp;
	}

	public int alocarString(int tam)
	{
		int tmp = contador;
		contador += tam;
		
		return tmp;
	}
	
	public int novoTemp()
	{
		return contTemp;
	}
	
	public int alocarTempByte()
	{
		int tmp = contTemp;
		contTemp++;
		
		return tmp;
	}
	
	public int alocarTempLogico()
	{
		int tmp = contTemp;
		contTemp++;
		
		return tmp;
	}
	
	public int alocarTempInteiro()
	{
		int tmp = contTemp;
		contTemp += 2;
		
		return tmp;
	}
	
	public int alocarTempString()
	{
		int tmp = contTemp;
		contTemp += 256;
		
		return tmp;
	}

	public int getContador()
	{
		return contador;
	}

	public void addContTemp(int valor)
	{
		contTemp += valor;
	}
	
	public int getContTemp()
	{
		return contTemp;
	}
}