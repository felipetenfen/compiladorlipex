package br.edu.ifc.compilador.io;

import br.edu.ifc.compilador.model.ErroEnum;

/**
 * @author Felipe Tenfen
 * 
 * Classe onde é gerado as mensagens de erro que ocorrem durante o processo de compilação
 */
public abstract class Erro
{
	public void setErro( ErroEnum erro ) throws InterruptedException
	{
		String str = "";

		if ( erro == ErroEnum.ID_DECLARADO )
			str = "Identificador já declarado [" + getLexema() + "]";
		else if ( erro == ErroEnum.TIPO_INCOMPATIVEL )
			str = "Tipos incompatíveis";
		else if ( erro == ErroEnum.TOKEN_NAO_ESPERADO )
			str = "Token não esperado [" + getLexema() + "]";
		else if ( erro == ErroEnum.ID_NAO_DECLARADO )
			str = "Identificador não declarado [" + getLexema() + "]";
		else if ( erro == ErroEnum.FIM_NAO_ESPERADO )
			str = "Fim de Arquivo não esperado.";
		else if ( erro == ErroEnum.CARACTERE_INVALIDO)
			str = "Caractere inválido";
		else if ( erro == ErroEnum.LEXEMA_NAO_ESPERADO )
			str = "Lexema não esperado [" + getLexema() + "]";
		else if ( erro == ErroEnum.LEXEMA_NAO_IDENTIFICADO )
			str = "Lexema não identificado [" + getLexema() + "]";
		else if ( erro == ErroEnum.CLASSE_INCOMPATIVEL )
			str = "Classe de identificador incompatível [" + getLexema() + "]";
		else if ( erro == ErroEnum.PROGRAMA_NAO_INICIADO )
			str = "Programa não iniciado";

		System.err.println("Linha: " + getLinha() + " | Erro: " + str);

		Thread.sleep(500);

		System.out.println("Compilador finalizado!");
		System.exit(0);
	}

	public abstract int getLinha();

	public abstract String getLexema();
}