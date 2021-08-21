package br.edu.ifc.compilador.model;

/**
 * @author Felipe Tenfen
 * 
 * Classe responsável por definir cada simbolo utilizado durante o processo de compilação.
 * Armazenando os seguintes atributos, token, endereço de memória, lexema, tipo e classe,
 */
public class SimboloBean
{
	private int token, endereco;
	private String lexema;
	private TipoEnum tipo;
	private ClasseEnum classe;

	public SimboloBean setToken(int token)
	{
		this.token = token;
		
		return this;
	}
	
	public int getToken()
	{
		return token;
	}
	
	public SimboloBean setLexema(String lexema)
	{
		this.lexema = lexema;

		return this;
	}
	
	public String getLexema()
	{
		return lexema == null ? "" : lexema;
	}
	
	public SimboloBean setEndereco(int endereco)
	{
		this.endereco = endereco;
		
		return this;
	}
	
	public int getEndereco()
	{
		return endereco;
	}

	public SimboloBean setClasse(ClasseEnum classe)
	{
		this.classe = classe;

		return this;
	}

	public ClasseEnum getClasse()
	{
		return classe;
	}

	public SimboloBean setTipo(TipoEnum tipo)
	{
		this.tipo = tipo;
		
		return this;
	}
	
	public TipoEnum getTipo()
	{
		return tipo;
	}

	public boolean isVariavel()
	{
		return getToken() == OpEnum.INT.getToken() ||
				getToken() == OpEnum.BOOL.getToken() || 
				getToken() == OpEnum.BYTE.getToken() || 
				getToken() == OpEnum.STR.getToken();
	}
	
	public boolean isComparacao()
	{
		return getToken() == OpEnum.MAIOR.getToken() || 
				getToken() == OpEnum.MENOR.getToken() || 
				getToken() == OpEnum.MAIOR_IGUAL.getToken() || 
				getToken() == OpEnum.MENOR_IGUAL.getToken() || 
				getToken() == OpEnum.IGUAL.getToken() || 
				getToken() == OpEnum.DIF.getToken();
	}
}