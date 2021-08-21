package br.edu.ifc.compilador;
import java.io.BufferedReader;

import br.edu.ifc.compilador.io.Erro;
import br.edu.ifc.compilador.io.TabelaSimbolos;
import br.edu.ifc.compilador.model.ErroEnum;
import br.edu.ifc.compilador.model.SimboloBean;
import br.edu.ifc.compilador.model.TipoEnum;

/**
 * @author Felipe Tenfen
 * 
 * Classe responsável por realizar o processo de analise léxica do compilador
 */
public class AnalisadorLexico
{
	private int linha;
	private char c;
	private String lexema;
	private boolean eof, dev;
	private SimboloBean beanSimbolo;
	private TabelaSimbolos simbolos;
	private Erro erro;

	public AnalisadorLexico()
	{
		linha = 1;
		lexema = "";
		simbolos = new TabelaSimbolos();
		erro = new Erro()
		{
			@Override
			public int getLinha()
			{
				return linha;
			}
			
			@Override
			public String getLexema()
			{
				return lexema;
			}
		};
	}

	/*
	 * Método realiza a analise do arquivo .lipex, identifica o lexema, gera um bean com os 
	 * parametros do lexema identificado e retorna o bean gerado.
	 */
	public SimboloBean analisar(BufferedReader arquivo) throws Exception
	{
		int posInicial = 0;
		int posFinal = 14;
		boolean devolucao = dev;

		lexema = "";

		while (posInicial != posFinal)
		{
			switch (posInicial)
			{
				case 0:
					if(devolucao == false)
						c = (char)arquivo.read();

					devolucao = false;

					if (c == '\n' || c == 11)
						linha++;
					else if (c == '+' || c == '-' || c == '*' || c == '^' || c == '%' || c == '(' || c == ')' || c == ';' || c == ',' || c == '{' || c == '}' )
					{
						lexema += c;
						posInicial = posFinal;
						dev = false;
					}
					else if (c == 32 || c == 11 || c == 8 || c == 13 || c == 9)
						posInicial = 0;
					else if (c == '/')
					{
						lexema += c;
						posInicial = 1;
					}
					else if (c == '<' || c == '>' || c == '=')
					{
						lexema += c;
						posInicial = 4;
					}
					else if (c == '!')
					{
						lexema += c;
						posInicial = 5;
					}
					else if (c == '&')
					{
						lexema += c;
						posInicial = 18;
					}
					else if (c == '|')
					{
						lexema += c;
						posInicial = 19;
					}
					else if (letra(c) || c == '_')
					{
						lexema += c;
						posInicial = 6;
					}
					else if (digito(c))
					{
						if (c == '0')
						{
							lexema += c;
							posInicial = 7;
						}
						else
						{
							lexema += c;
							posInicial = 8;
						}
					}
					else if (c == '"')
					{
						lexema += c;
						posInicial = 17;
					}
					else if (c == 65535)
					{
	                    posInicial = posFinal;
	                    lexema += c;
	                    eof = true;
	                    dev = false;
	                    arquivo.close();
	                }
					else
						erro.setErro( ErroEnum.CARACTERE_INVALIDO );

					break;
			
				case 1:
					c = (char)arquivo.read();

					if (c == '*')
					{
						lexema += c;
						posInicial = 2;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						this.dev = true;
					}

					break;
				
				case 2:
					c = (char)arquivo.read();
					
					if (c == '*')
					{
						posInicial = 3;
					}
					else if (c == 13)
					{
						posInicial = 2;
						linha ++;
					}
					else if (c == -1 || c == 65535)
					{
						eof = true;
						erro.setErro( ErroEnum.FIM_NAO_ESPERADO );
					}
					else
						posInicial = 2;
	
					break;

				case 3:
					c = (char)arquivo.read();
					
					if (c == '/')
					{
						posInicial = 0;
						lexema = "";
					}
					else if (c == '*')
					{
						posInicial = 3;
					}
					else if (c == -1 || c == 65535)
					{
						eof = true;
						erro.setErro( ErroEnum.FIM_NAO_ESPERADO );
					}
					else
						posInicial = 2;
	
					break;

				case 4:
					c = (char)arquivo.read();
				
					if (c == '=')
					{
						lexema += c;
						posInicial = posFinal;
						dev = false;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;
			
				case 5:
					c = (char)arquivo.read();
					
					if (c == '=')
					{
						lexema += c;
						posInicial = posFinal;
						dev = false;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;
			
				case 6:
					c = (char)arquivo.read();
				
					if (digito(c) || letra(c) || c == '_')
					{
						lexema += c;
						posInicial = 6;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;
				
				case 7:
					c = (char)arquivo.read();
				
					if(c == 'x')
					{
						lexema += c;
						posInicial = 10;
					}
					else if (digito(c))
					{
						lexema += c;
						posInicial = 9;
					}
					else if (c == 'h')
					{
						lexema += c;
						posInicial = posFinal;
						dev = false;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;
			
				case 8:
					c = (char)arquivo.read();
				
					if (digito(c))
					{
						lexema += c;
						posInicial = 9;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;
				
				case 9:
					c = (char)arquivo.read();
					
					if ( digito(c) )
					{
						lexema += c;
						posInicial = 15;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;
			
				case 10:
					c = (char)arquivo.read();
				
					if (digito(c))
					{
						lexema += c;
						posInicial = 11;
					}
					else if (letra(c))
					{
						lexema += c;
						posInicial = 12;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;
			
				case 11:
					c = (char)arquivo.read();
				
					if (digito(c))
					{
						lexema += c;
						posInicial = 13;
					}
					else if (letra(c))
					{
						lexema += c;
						posInicial = posFinal;
						dev = false;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;
				
				case 12:
					c = (char)arquivo.read();
					
					if (digito(c))
					{
						lexema += c;
						posInicial = 13;
					}
					else if (letra(c))
					{
						lexema += c;
						posInicial = posFinal;
						dev = false;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
	
					break;

				case 13:
					c = (char)arquivo.read();
				
					if (! digito(c))
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					else
						erro.setErro( ErroEnum.CARACTERE_INVALIDO );
					
					break;

				case 15:
					c = (char)arquivo.read();
				
					if (digito(c))
					{
						lexema += c;
						posInicial = 16;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;

				case 16:
					c = (char)arquivo.read();
				
					if (digito(c))
					{
						lexema += c;
						posInicial = 13;
					}
					else
					{
						posInicial = posFinal;
						devolucao = true;
						dev = true;
					}
					
					break;

				case 17:
					c = (char)arquivo.read();

					if (c == '"')
					{
						lexema += c;
						posInicial = posFinal;
						dev = false;
					}
					else if (c == -1 || c == 65535)
					{
						eof = true;
						erro.setErro( ErroEnum.FIM_NAO_ESPERADO );
					}
					else if (c == 13 || c == 10 || c == 11 || c == 8 && ! digito(c) && ! letra(c) && 
							 c != '+' && c != '-' && c != '*' && c != '(' && c != ')' && c != ';' && 
							 c != ',' && c != '/' && c != '>' && c != '<' && c != '=' && c != '"')
					{
						erro.setErro( ErroEnum.CARACTERE_INVALIDO );
					}
					else
					{
						lexema += c;
						posInicial = 17;
					}

					break;
					
				case 18:
					c = (char)arquivo.read();

					if (c == '&')
					{
						lexema += c;
						posInicial = posFinal;
						dev = false;
					}
					else
						erro.setErro( ErroEnum.CARACTERE_INVALIDO );

					break;
					
				case 19:
					c = (char)arquivo.read();

					if (c == '|')
					{
						lexema += c;
						posInicial = posFinal;
						dev = false;
					}
					else
						erro.setErro( ErroEnum.CARACTERE_INVALIDO );
					
					break;
			}
		}

		if (! eof)
		{
			//Verifica se lexema identificado já não está armazenado na tabela de simbolos
			if (simbolos.getTabela().get(lexema.toLowerCase()) != null)
			{
				beanSimbolo = simbolos.getTabela().get(lexema.toLowerCase());
			}
			else
			{
				/*
				 * Caso lexema não exista na tabela de simbolos, é contruido o bean
				 * com os parametros referentes ao lexama identificado
				 */
				if ( lexema.toLowerCase().equals("true") || 
						lexema.toLowerCase().equals("false") )
					beanSimbolo = simbolos.inserirConst(lexema, TipoEnum.LOGICO);
				else if (lexema == "FFh")
					beanSimbolo = simbolos.inserirConst(lexema, TipoEnum.LOGICO);
				else if (letra(lexema.charAt(0)) || lexema.charAt(0) == '_')
					beanSimbolo = simbolos.inserirID(lexema);
				else if (digito(lexema.charAt(0)) || lexema.charAt(0) == '"')
				{
					if (digito(lexema.charAt(0)))
					{
						if (lexema.charAt(0) == '0')
						{
							/*
							 * Começa com 0 e tamanho maior que um, pode ser INTEIRO, BYTE ou LOGICO
							 */
							if(lexema.length() > 1)
							{
								//0h é tipo LOGICO
								if(lexema.charAt(1) == 'h')
								{
									beanSimbolo = simbolos.inserirConst(lexema, TipoEnum.LOGICO);
								}
								//0x garante ser hexadecimal
								else if (lexema.charAt(1) == 'x')
								{
									//hexadecimal tem que ser tamanho 4
									if (lexema.length() == 4)
									{
										for (int i = 2; i < lexema.length(); i++)
										{
											if ( (lexema.charAt(i) < 'A' || lexema.charAt(i) > 'F') && ! digito(lexema.charAt(i)) )
												erro.setErro( ErroEnum.LEXEMA_NAO_ESPERADO );
										}

										beanSimbolo = simbolos.inserirConst(lexema, TipoEnum.BYTE);
									}
									//0x tamanho menor que 4 ERRO
									else if(lexema.length() < 4)
										erro.setErro( ErroEnum.LEXEMA_NAO_ESPERADO );
								}
								else if (lexema.length() <= 5)
								{
									//Verifica se o lexema possui somente numeros
									for (char l : lexema.toCharArray())
									{
										if (! digito(l))
											erro.setErro( ErroEnum.LEXEMA_NAO_ESPERADO );
									}

									beanSimbolo = simbolos.inserirConst(lexema, TipoEnum.INTEIRO);
								}
							}
							//começa com 0 e tem um digito só, é byte
							else if(lexema.length() == 1)
								beanSimbolo = simbolos.inserirConst(lexema, TipoEnum.BYTE);
						}
						else
						{
							if(lexema.length() <= 5)
							{
								//Verifica se o lexema possui somente numeros
								for (char l : lexema.toCharArray())
								{
									if (! digito(l))
										erro.setErro( ErroEnum.CARACTERE_INVALIDO );
								}

								int lex = Integer.parseInt(lexema);

								//se for entre 0 e 255 é byte
								if (lex >= 0 && lex <= 255)
									beanSimbolo = simbolos.inserirConst(lexema, TipoEnum.BYTE);
								else
									beanSimbolo = simbolos.inserirConst(lexema, TipoEnum.INTEIRO);
							}
							//mais de 5 digitos é ERRO
							else
								erro.setErro( ErroEnum.LEXEMA_NAO_IDENTIFICADO );
						}
					}
					else if(lexema.charAt(0) == '"' && lexema.charAt(lexema.length() - 1) == '"')
					{
						beanSimbolo = simbolos.inserirConst(lexema, TipoEnum.STRING);
					}
					else
						erro.setErro( ErroEnum.LEXEMA_NAO_IDENTIFICADO );
				}
				else
					erro.setErro( ErroEnum.LEXEMA_NAO_IDENTIFICADO );
			}
		}

		return beanSimbolo;
	}

	private boolean letra(char c)
	{
		return Character.isLetter(c);
	}

	private boolean digito(char c)
	{
		return Character.isDigit(c);
	}

	public int getLinha()
	{
		return linha;
	}
	
	public boolean isEof()
	{
		return eof;
	}
}