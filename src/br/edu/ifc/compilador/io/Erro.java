package br.edu.ifc.compilador.io;

import br.edu.ifc.compilador.model.ErroEnum;

/**
 * @author Felipe Tenfen
 * 
 * Classe onde � gerado as mensagens de erro que ocorrem durante o processo de compila��o
 */
public abstract class Erro
{
	public void setErro( ErroEnum erro ) throws InterruptedException
	{
		String str = "";

		if ( erro == ErroEnum.ID_DECLARADO )
			str = "Identificador j� declarado [" + getLexema() + "]";
		else if ( erro == ErroEnum.TIPO_INCOMPATIVEL )
			str = "Tipos incompat�veis";
		else if ( erro == ErroEnum.TOKEN_NAO_ESPERADO )
			str = "Token n�o esperado [" + getLexema() + "]";
		else if ( erro == ErroEnum.ID_NAO_DECLARADO )
			str = "Identificador n�o declarado [" + getLexema() + "]";
		else if ( erro == ErroEnum.FIM_NAO_ESPERADO )
			str = "Fim de Arquivo n�o esperado.";
		else if ( erro == ErroEnum.CARACTERE_INVALIDO)
			str = "Caractere inv�lido";
		else if ( erro == ErroEnum.LEXEMA_NAO_ESPERADO )
			str = "Lexema n�o esperado [" + getLexema() + "]";
		else if ( erro == ErroEnum.LEXEMA_NAO_IDENTIFICADO )
			str = "Lexema n�o identificado [" + getLexema() + "]";
		else if ( erro == ErroEnum.CLASSE_INCOMPATIVEL )
			str = "Classe de identificador incompat�vel [" + getLexema() + "]";
		else if ( erro == ErroEnum.PROGRAMA_NAO_INICIADO )
			str = "Programa n�o iniciado";

		System.err.println("Linha: " + getLinha() + " | Erro: " + str);

		Thread.sleep(500);

		System.out.println("Compilador finalizado!");
		System.exit(0);
	}

	public abstract int getLinha();

	public abstract String getLexema();
}