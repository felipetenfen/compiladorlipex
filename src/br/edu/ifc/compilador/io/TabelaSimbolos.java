package br.edu.ifc.compilador.io;
import java.util.HashMap;

import br.edu.ifc.compilador.model.OpEnum;
import br.edu.ifc.compilador.model.SimboloBean;
import br.edu.ifc.compilador.model.TipoEnum;

/**
 * @author Felipe Tenfen
 * 
 * Classe responsável por armazenar e gerenciar a tabela de simbolos
 */
public class TabelaSimbolos
{
	public static int endereco = -1;

	private HashMap<String, SimboloBean> tabela = new HashMap<>();
	
	public TabelaSimbolos()
	{
		for (OpEnum op : OpEnum.getOperadores())
		{
			tabela.put(op.getLexema(), 
						new SimboloBean()
							.setToken( op.getToken() )
							.setLexema( op.getLexema() )
							.setEndereco( ++endereco ) );
		}
	}

	public HashMap<String, SimboloBean> getTabela()
	{
		return tabela;
	}

	public SimboloBean inserirID(String lexema)
	{
		lexema = lexema.toLowerCase();

		SimboloBean beanSimb = 
			new SimboloBean()
				.setToken( OpEnum.ID.getToken() )
				.setLexema(lexema)
				.setEndereco(++endereco);

		tabela.put(lexema, beanSimb);

		return beanSimb;
	}
	
	public SimboloBean inserirConst(String lexema, TipoEnum tipo)
	{
		lexema = lexema.toLowerCase();
		
		SimboloBean beanSimb =
			new SimboloBean()
				.setToken( OpEnum.CONST.getToken() )
				.setLexema(lexema)
				.setTipo(tipo)
				.setEndereco(++endereco);

		tabela.put(lexema, beanSimb);

		return beanSimb;
	}
	
	public SimboloBean buscaSimbolo(String lexema)
	{
		return tabela.get( lexema.toLowerCase() );
	}

	public int pesquisa(String lexema)
	{
		return buscaSimbolo(lexema.toLowerCase()).getEndereco();
	}
}
