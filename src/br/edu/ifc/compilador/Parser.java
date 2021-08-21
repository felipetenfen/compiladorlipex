package br.edu.ifc.compilador;
import java.io.BufferedReader;
import java.io.IOException;

import br.edu.ifc.compilador.io.Buffer;
import br.edu.ifc.compilador.io.Erro;
import br.edu.ifc.compilador.io.Memoria;
import br.edu.ifc.compilador.io.Rotulo;
import br.edu.ifc.compilador.model.ClasseEnum;
import br.edu.ifc.compilador.model.ErroEnum;
import br.edu.ifc.compilador.model.OpEnum;
import br.edu.ifc.compilador.model.SimboloBean;
import br.edu.ifc.compilador.model.TipoEnum;

/**
 * @author Felipe Tenfen
 * 
 * Classe responsável por realizar todo o processo de análise sintatica e semântica da liguagem, 
 * além de fazer parte da geração de código e armazena-lo na classe Buffer.
 */
public class Parser
{
	int endereco, enderecoToken, enderecoToken2, enderecoExp, enderecoExpSub;
	Erro erro;
	Rotulo rotulo;
	Buffer buffer;
	SimboloBean s;
	Memoria memoria;
	BufferedReader arquivo;
	AnalisadorLexico lexico;

	public Parser(BufferedReader arquivo)
	{
		try
		{
			this.arquivo = arquivo;
			endereco = enderecoToken2 = enderecoToken = enderecoExp = enderecoExpSub = 0;

			lexico = new AnalisadorLexico();
			memoria = new Memoria();
			rotulo = new Rotulo();
			buffer = new Buffer();
			erro = new Erro()
			{
				@Override
				public int getLinha()
				{
					return lexico.getLinha();
				}
				
				@Override
				public String getLexema()
				{
					return s.getLexema();
				}
			};
		}
		catch(Exception e)
		{
			System.out.print(e.getMessage());
		}
	}
	
	private void confirmaToken(int token) throws InterruptedException
	{
		if (s == null)
			return;

		try
		{
			/*
			 * Se token que está sendo analisado (armazenado no bean simbolo) esta de acordo com token 
			 * esperado (token passado como parametro do método), o analisador léxico realiza a analise do
			 * arquivo e retorna o próximo simbolo identificado, caso contrário gera um erro
			 */
			if ( s.getToken() == token )
				s = lexico.analisar( arquivo );
			else if ( lexico.isEof() )
				erro.setErro( ErroEnum.FIM_NAO_ESPERADO );
			else if (OpEnum.PROGRAMA.getToken() == token)
				erro.setErro( ErroEnum.PROGRAMA_NAO_INICIADO );
			else
				erro.setErro( ErroEnum.TOKEN_NAO_ESPERADO );
		}
		catch (IOException e)
		{
			erro.setErro( ErroEnum.FIM_NAO_ESPERADO );
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println(e.toString());
			System.exit(0);
		}
	}

	public void parse()
	{
		try
		{
			s = lexico.analisar( arquivo );

			if ( lexico.isEof() )
				erro.setErro( ErroEnum.FIM_NAO_ESPERADO );

			if (s == null)
				return;

			buffer.add("sseg SEGMENT STACK ;início seg. pilha");
			buffer.add("byte 4000h DUP(?) ;dimensiona pilha");
			buffer.add("sseg ENDS ;fim seg. pilha");
			buffer.add("dseg SEGMENT PUBLIC ;início seg. dados");
			buffer.add("byte 4000h DUP(?) ;temporários");

			endereco = memoria.alocarTemp();

			while ( s.isVariavel() )
				dados();

	 		buffer.add("dseg ENDS ;fim seg. dados");
	 		buffer.add("cseg SEGMENT PUBLIC ;início seg. código");
	 		buffer.add("ASSUME CS:cseg, DS:dseg");
	 		buffer.add("strt:");
	 		buffer.add("mov ax, dseg");
	 		buffer.add("mov ds, ax");

	 		iniciaPrograma();

			buffer.add("mov ah, 4Ch");
			buffer.add("int 21h");
			buffer.add("cseg ENDS ;fim seg. código");
			buffer.add("END strt ;fim programa");

			buffer.otimizar();
			buffer.criarArquivo();

			if (! lexico.isEof())
				erro.setErro( ErroEnum.TOKEN_NAO_ESPERADO );
		 }
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println(e.toString());
		}
	}
	
	public void dados() throws Exception
	{
		TipoEnum tipoToken;
		SimboloBean beanSimbTemp = s;
		boolean subtracao = false;

		if ( s.isVariavel() )
		{
			tipoToken = getTipoToken();
			beanSimbTemp = s;

			if ( s.getClasse() != null )
				erro.setErro( ErroEnum.ID_DECLARADO );

			/* Ação semântica */
			s.setClasse( ClasseEnum.VAR );
			s.setTipo( tipoToken );

			confirmaToken( OpEnum.ID.getToken() );

			if ( s.getToken() == OpEnum.ATRIB.getToken() )
			{
				if ( beanSimbTemp.getClasse() != null && beanSimbTemp.getClasse() == ClasseEnum.CONST )
					erro.setErro( ErroEnum.CLASSE_INCOMPATIVEL );
				
				confirmaToken( OpEnum.ATRIB.getToken() );
				
				if ( s.getToken() == OpEnum.SUB.getToken() )
				{
					subtracao = true;
					confirmaToken( OpEnum.SUB.getToken() );

					if (! TipoEnum.isInteiro( s.getTipo() ))
						erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
				}

				/* Acao Semantica */
				if (! beanSimbTemp.getTipo().equals(s.getTipo()) && 
					! ( TipoEnum.isInteiro(beanSimbTemp.getTipo()) && TipoEnum.isByte(s.getTipo())))
					erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

				String lexTemp = s.getLexema();

				if ( s.getLexema().toLowerCase().equals("true") )
					lexTemp = "0FFh";
				else if ( s.getLexema().toLowerCase().equals("false") )
					lexTemp = "0h";

				if (subtracao)
				{
					endereco = memoria.alocarInteiro();
					buffer.add("sword -" + lexTemp + " ; valor negativo " + beanSimbTemp.getLexema());

					beanSimbTemp.setTipo( TipoEnum.INTEIRO );
				}
				else if ( TipoEnum.isByte(beanSimbTemp.getTipo()) )
				{
					buffer.add("byte " + lexTemp + " ; byte " + beanSimbTemp.getLexema());
					endereco = memoria.alocarByte();
				}
				else if ( TipoEnum.isLogico(beanSimbTemp.getTipo()) )
				{
					buffer.add("byte " + lexTemp + " ; boolean " + beanSimbTemp.getLexema());
					endereco = memoria.alocarLogico();
				}
				else if ( TipoEnum.isInteiro(beanSimbTemp.getTipo()) )
				{
					buffer.add("sword " + lexTemp + " ; byte " + beanSimbTemp.getLexema());
					endereco = memoria.alocarInteiro();
				}
				else if ( TipoEnum.isString(beanSimbTemp.getTipo()) )
				{
					endereco = memoria.alocarString(s.getLexema().length() - 1);
					buffer.add("byte " + s.getLexema().substring(0, s.getLexema().length() - 1) + "$" + 
										 s.getLexema().charAt(s.getLexema().length() - 1) + "; string " + 
										 beanSimbTemp.getLexema() + " em " + endereco);
				}

				beanSimbTemp.setEndereco(endereco);

				confirmaToken( OpEnum.CONST.getToken() );
			}
			else
			{
				if ( TipoEnum.isByte(beanSimbTemp.getTipo()) )
				{
					endereco = memoria.alocarByte();
					buffer.add("byte ? ;byte " + beanSimbTemp.getLexema());
				}
				else if ( TipoEnum.isLogico(beanSimbTemp.getTipo()) )
				{
					endereco = memoria.alocarLogico();
					buffer.add("byte ? ;logico " + beanSimbTemp.getLexema());
				}
				else if ( TipoEnum.isInteiro(beanSimbTemp.getTipo()) )
				{
					endereco = memoria.alocarInteiro();
					buffer.add("sword ? ;inteiro " + beanSimbTemp.getLexema());
				}
				else if ( TipoEnum.isString(beanSimbTemp.getTipo()) )
				{
					endereco = memoria.alocarString();
					buffer.add("byte 100h DUP(?) ;string " + beanSimbTemp.getLexema() + " em " + endereco);
				}

				beanSimbTemp.setEndereco(endereco);
			}

			while (s.getToken() == OpEnum.VIRGULA.getToken())
			{
				confirmaToken(OpEnum.VIRGULA.getToken());
				beanSimbTemp = s;

				if ( s.getClasse() != null )
					erro.setErro( ErroEnum.ID_DECLARADO );
				else
				{
					/* Ação semântica */
					s.setClasse( ClasseEnum.VAR );
					s.setTipo(tipoToken);
				}

				confirmaToken( OpEnum.ID.getToken() );

				if(s.getToken() == OpEnum.ATRIB.getToken())
				{
					confirmaToken(OpEnum.ATRIB.getToken());

					if ( s.getToken() == OpEnum.SUB.getToken() )
						confirmaToken(OpEnum.SUB.getToken());
					
					/* Acao Semantica */
					if ((beanSimbTemp.getTipo() == null && s.getTipo() != null || 
						 beanSimbTemp.getTipo() != null && s.getTipo() == null ||
						 beanSimbTemp.getTipo() != s.getTipo()) &&
						 ! (TipoEnum.isInteiro(beanSimbTemp.getTipo()) && TipoEnum.isByte(s.getTipo())))
					{
						erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
					}
					
					String lexTemp = s.getLexema();

					if (s.getLexema().toLowerCase().equals("true"))
						lexTemp = "0FFh";
					else if (s.getLexema().toLowerCase().equals("false"))
						lexTemp = "0h";
					
					if (subtracao)
					{
						endereco = memoria.alocarInteiro();
						buffer.add("sword -" + lexTemp + "; valor negativo " + beanSimbTemp.getLexema());
						beanSimbTemp.setTipo( TipoEnum.INTEIRO );
					}
					else if ( TipoEnum.isByte(beanSimbTemp.getTipo()) )
					{
						buffer.add("byte " + lexTemp + "; valor positivo " + beanSimbTemp.getLexema());
						endereco = memoria.alocarByte();
					}
					else if ( TipoEnum.isLogico(beanSimbTemp.getTipo()) )
					{
						buffer.add("byte " + lexTemp + "; valor positivo " + beanSimbTemp.getLexema());
						endereco = memoria.alocarLogico();
					}
					else if ( TipoEnum.isInteiro(beanSimbTemp.getTipo()) )
					{
						buffer.add("sword " + lexTemp + "; valor positivo " + beanSimbTemp.getLexema());
						endereco = memoria.alocarInteiro();
					}
					else if ( TipoEnum.isString(beanSimbTemp.getTipo()) )
					{							
						buffer.add("byte " + s.getLexema().substring(0, s.getLexema().length() - 1) + "$" + s.getLexema().charAt(s.getLexema().length() - 1));
						endereco = memoria.alocarString(s.getLexema().length() - 1);
					}

					beanSimbTemp.setEndereco(endereco);

					confirmaToken( OpEnum.CONST.getToken() );
				}
				else
				{
					beanSimbTemp.setEndereco(endereco);

					if ( TipoEnum.isByte(beanSimbTemp.getTipo()) )
					{
						endereco = memoria.alocarByte();
						buffer.add("byte ? ;byte " + beanSimbTemp.getLexema());
					}
					else if ( TipoEnum.isLogico(beanSimbTemp.getTipo()) )
					{
						endereco = memoria.alocarLogico();
						buffer.add("byte ? ;logico " + beanSimbTemp.getLexema());
					}
					else if ( TipoEnum.isInteiro(beanSimbTemp.getTipo()) )
					{
						endereco = memoria.alocarInteiro();
						buffer.add("sword ? ;inteiro " + beanSimbTemp.getLexema());
					}
					else if ( TipoEnum.isString(beanSimbTemp.getTipo()) )
					{
						endereco = memoria.alocarString();
						buffer.add("byte 100h DUP(?) ; string " + beanSimbTemp.getLexema());
					}
				}
			}
		}
		
		confirmaToken(OpEnum.PONTO_VIRG.getToken());
	}
	
	public TipoEnum getTipoToken() throws Exception
	{
		if (s.getToken() == OpEnum.INT.getToken())
		{
			confirmaToken(OpEnum.INT.getToken());
			
			return TipoEnum.INTEIRO;
		}
		else if (s.getToken() == OpEnum.BOOL.getToken())
		{
			confirmaToken( OpEnum.BOOL.getToken() );

			return TipoEnum.LOGICO;
		}
		else if (s.getToken() == OpEnum.BYTE.getToken())
		{
			confirmaToken(OpEnum.BYTE.getToken());
			
			return TipoEnum.BYTE;
		}
		else if (s.getToken() == OpEnum.STR.getToken())
		{
			confirmaToken( OpEnum.STR.getToken() );

			return TipoEnum.STRING;
		}
		else
		{
			erro.setErro( ErroEnum.TOKEN_NAO_ESPERADO );
			return null;
		}
	}

	/*
	 * Método chamado quando se inicia o programa ( program )
	 */
	private void iniciaPrograma() throws Exception
	{
		confirmaToken( OpEnum.PROGRAMA.getToken() );

		novoBloco();

		confirmaToken( OpEnum.END.getToken() );
	}
	
	/*
	 * Método chamado quando se inicia um bloco ( { )
	 */
	public void novoBloco() throws Exception
	{
		confirmaToken( OpEnum.INICIO_BLOCO.getToken() );
	
		while ( s.getToken() == OpEnum.ID.getToken() || 
				s.getToken() == OpEnum.WHILE.getToken() || 
				s.getToken() == OpEnum.IF.getToken() || 
				s.getToken() == OpEnum.READLN.getToken() || 
				s.getToken() == OpEnum.WRITE.getToken() || 
				s.getToken() == OpEnum.WRITELN.getToken() || 
				s.getToken() == OpEnum.PONTO_VIRG.getToken() )
		{
			condicao();
		}

		confirmaToken( OpEnum.FIM_BLOCO.getToken() );
	}
	
	public void condicao() throws Exception
	{
		TipoEnum tipoExp = null;
		SimboloBean simbTmp;

		if ( s.getToken() == OpEnum.ID.getToken() )
		{
			/* Acao Semantica */
			if (s.getClasse() == null)
				erro.setErro( ErroEnum.ID_NAO_DECLARADO );
			else if ( s.getClasse() == ClasseEnum.CONST )
				erro.setErro( ErroEnum.CLASSE_INCOMPATIVEL );

			simbTmp = s;

			confirmaToken( OpEnum.ID.getToken() );
			confirmaToken( OpEnum.ATRIB.getToken() );

			tipoExp = analisarExpressao();

			if ((simbTmp.getTipo() == null && tipoExp != null || 
				 simbTmp.getTipo() != null && tipoExp == null ||
				 simbTmp.getTipo() != tipoExp) &&
				! (TipoEnum.isInteiro(simbTmp.getTipo()) && TipoEnum.isByte(tipoExp)))
			{
				erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
			}

			buffer.add("mov al, DS:[" + enderecoExp + "]");

			if ( TipoEnum.isByte(tipoExp) )
				buffer.add("mov ah, 0");

			buffer.add("mov DS:[" + simbTmp.getEndereco() + "], ax");
			
			confirmaToken( OpEnum.PONTO_VIRG.getToken() );
		}
		else if(s.getToken() == OpEnum.WHILE.getToken())
		{
			confirmaToken( OpEnum.WHILE.getToken() );
			confirmaToken( OpEnum.AP.getToken() );

			String RotuloInicio = rotulo.novoRotulo();
			String RotuloFim = rotulo.novoRotulo();

			buffer.add(RotuloInicio + ":");

			/* Acao Semantica */
			tipoExp = analisarExpressao();

			if (! TipoEnum.isLogico(tipoExp))
				erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

			while ( s.getToken() == OpEnum.OR.getToken() || 
					s.getToken() == OpEnum.AND.getToken() )
			{
				if (s.getToken() == OpEnum.OR.getToken())
					confirmaToken( OpEnum.OR.getToken() );
				else
					confirmaToken( OpEnum.AND.getToken() );

				/* Acao Semantica */
				tipoExp = analisarExpressao();

				if (! TipoEnum.isLogico(tipoExp))
					erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
			}

			confirmaToken( OpEnum.FP.getToken() );

			buffer.add("mov ax, DS:[" + enderecoExp + "]");
			buffer.add("cmp ax, 0");	
			buffer.add("je " + RotuloFim);
			
			if ( s.getToken() == OpEnum.ID.getToken() || 
				 s.getToken() == OpEnum.WHILE.getToken() || 
				 s.getToken() == OpEnum.IF.getToken() || 
				 s.getToken() == OpEnum.READLN.getToken() || 
				 s.getToken() == OpEnum.WRITE.getToken() || 
				 s.getToken() == OpEnum.WRITELN.getToken() )
			{
				condicao();
			}
			else if(s.getToken() == OpEnum.INICIO_BLOCO.getToken())
				novoBloco();

			buffer.add("jmp " + RotuloInicio);
			buffer.add(RotuloFim + ":");
		}
		else if (s.getToken() == OpEnum.IF.getToken())
		{
			confirmaToken( OpEnum.IF.getToken() );
			confirmaToken( OpEnum.AP.getToken() );

			String RotuloFalso = rotulo.novoRotulo();
			String RotuloFim = rotulo.novoRotulo();
			
			/* Acao Semantica */
			tipoExp = analisarExpressao();
			
			if (! TipoEnum.isLogico(tipoExp))
				erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

			while ( s.getToken() == OpEnum.OR.getToken() || 
					s.getToken() == OpEnum.AND.getToken() )
			{
				if (s.getToken() == OpEnum.OR.getToken())
					confirmaToken( OpEnum.OR.getToken() );
				else
					confirmaToken( OpEnum.AND.getToken() );

				/* Acao Semantica */
				tipoExp = analisarExpressao();

				if (! TipoEnum.isLogico(tipoExp))
					erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
			}

			confirmaToken( OpEnum.FP.getToken() );
			
			buffer.add("mov ax, DS:[" + enderecoExp + "]");
			buffer.add("cmp ax, 0");
			buffer.add("je " + RotuloFalso);
			
			if ( s.getToken() == OpEnum.ID.getToken() || 
				 s.getToken() == OpEnum.WHILE.getToken() || 
				 s.getToken() == OpEnum.IF.getToken() || 
				 s.getToken() == OpEnum.READLN.getToken() || 
				 s.getToken() == OpEnum.WRITE.getToken() || 
				 s.getToken() == OpEnum.WRITELN.getToken() )
			{
				condicao();
			}
			else if (s.getToken() == OpEnum.INICIO_BLOCO.getToken())
				novoBloco();

			if (s.getToken() == OpEnum.ELSE.getToken())
			{
				confirmaToken( OpEnum.ELSE.getToken() );

				buffer.add("jmp " + RotuloFim);
				buffer.add(RotuloFalso + ":");
				
				if ( s.getToken() == OpEnum.ID.getToken() || 
					 s.getToken() == OpEnum.WHILE.getToken() || 
					 s.getToken() == OpEnum.IF.getToken() || 
					 s.getToken() == OpEnum.READLN.getToken() || 
					 s.getToken() == OpEnum.WRITE.getToken() || 
					 s.getToken() == OpEnum.WRITELN.getToken() )
				{
					condicao();
				}
				else if ( s.getToken() == OpEnum.INICIO_BLOCO.getToken() )
					novoBloco();
				
				buffer.add(RotuloFim + ":");
			}
		}
		else if (s.getToken() == OpEnum.READLN.getToken())
		{
			confirmaToken(OpEnum.READLN.getToken());
			confirmaToken( OpEnum.AP.getToken() );

			if (! TipoEnum.isInteiro(s.getTipo()) && 
				! TipoEnum.isString(s.getTipo()) && 
				! TipoEnum.isByte(s.getTipo()))
			{
				erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
			}

			simbTmp = s;

			confirmaToken( OpEnum.ID.getToken() );
			confirmaToken( OpEnum.FP.getToken() );
			confirmaToken( OpEnum.PONTO_VIRG.getToken() );

			int bufferEnd = memoria.alocarTempString();

			memoria.addContTemp(3);

			buffer.add("mov dx, " + bufferEnd);
			buffer.add("mov al, 0FFh");
			buffer.add("mov ds:[" + bufferEnd + "], al");
			buffer.add("mov ah, 0Ah");
			buffer.add("int 21h");
			buffer.add("mov ah, 02h");
			buffer.add("mov dl, 0Dh");
			buffer.add("int 21h");
			buffer.add("mov DL, 0Ah");
			buffer.add("int 21h");
			buffer.add("mov di, " + bufferEnd+2 + ";posição do string");
			
			if (! TipoEnum.isString(simbTmp.getTipo()))
			{
				buffer.add("mov ax, 0");
				buffer.add("mov cx, 10");
				buffer.add("mov dx, 1");
				buffer.add("mov bh, 0");
				buffer.add("mov bl, ds:[di]");
				buffer.add("cmp bx, 2Dh");
				
				String rot = rotulo.novoRotulo();
				
				buffer.add("jne " + rot);
				buffer.add("mov dx, -1");
				buffer.add("add di, 1");
				buffer.add("mov bl, ds:[di]");
				buffer.add(rot + ":");
				buffer.add("push dx");
				buffer.add("mov dx, 0");
				
				String rot1 = rotulo.novoRotulo();
				
				buffer.add(rot1 + ":");
				buffer.add("cmp bx, 0Dh");
				
				String rot2 = rotulo.novoRotulo();
				
				buffer.add("je " + rot2);
				buffer.add("imul cx");
				buffer.add("add bx, -48");
				buffer.add("add ax, bx");
				buffer.add("add di, 1");
				buffer.add("mov bh, 0");
				buffer.add("mov bl, ds:[di]");
				buffer.add("jmp " + rot1);
				buffer.add(rot2 + ":");
				buffer.add("pop cx");
				buffer.add("imul cx");
				
				buffer.add("mov DS:[" + simbTmp.getEndereco() + "], ax");
			}
			else
			{			
				buffer.add("mov si, " + simbTmp.getEndereco());
				
				String rotString = rotulo.novoRotulo();

				buffer.add(rotString + ":");
				buffer.add("mov al, ds:[di]");
				buffer.add("cmp al, 0dh ;verifica fim string");
				
				String rot2 = rotulo.novoRotulo();

				buffer.add("je " + rot2 + " ;salta se fim string");
	 			buffer.add("mov ds:[si], al ;próximo caractere");
				buffer.add("add di, 1 ;incrementa base");
				buffer.add("add si, 1");
				buffer.add("jmp " + rotString + " ;loop");
				buffer.add(rot2 + ":");
				buffer.add("mov al, 024h ;fim de string");
				buffer.add("mov ds:[si], al ;grava '$'");
			}
		}
		else if ( s.getToken() == OpEnum.WRITE.getToken() || s.getToken() == OpEnum.WRITELN.getToken() )
		{
			int stringEnd = memoria.novoTemp();
			boolean ln = false;

			if (s.getToken() == OpEnum.WRITE.getToken())
			{
				confirmaToken( OpEnum.WRITE.getToken() );
				ln = false;
			}
			else if (s.getToken() == OpEnum.WRITELN.getToken())
			{
				confirmaToken( OpEnum.WRITELN.getToken() );
				ln = true;
			}

			confirmaToken( OpEnum.AP.getToken() );

			tipoExp = analisarExpressao();

			if ( TipoEnum.isString(tipoExp) )
			{
				buffer.add("mov dx, " + enderecoExp);
				buffer.add("mov ah, 09h");
				buffer.add("int 21h");
			}
			else
			{
				buffer.add("mov ax, DS:[" + enderecoExp + "]");
				buffer.add("mov di, " + stringEnd + " ;end. string temp.");
				
				buffer.add("mov cx, 0 ;contador");
				
				buffer.add("cmp ax,0 ;verifica sinal");
				
				String rot = rotulo.novoRotulo();
			
				buffer.add("jge " + rot + " ;salta se numero positivo");
				buffer.add("mov bl, 2Dh ;senao, escreve sinal ");
				buffer.add("mov ds:[di], bl");
				buffer.add("add di, 1 ;incrementa indice");
				buffer.add("neg ax ;toma modulo do numero");
				buffer.add(rot + ":");
				buffer.add("mov bx, 10 ;divisor");

				String rot1 = rotulo.novoRotulo();
				
				buffer.add(rot1 + ":");
				buffer.add("add cx, 1 ;incrementa contador");
				buffer.add("mov dx, 0 ;estende 32bits p/ div.");
				buffer.add("idiv bx ;divide DXAX por BX");
				buffer.add("push dx ;empilha valor do resto");
				buffer.add("cmp ax, 0 ;verifica se quoc.  0");
				buffer.add("jne " + rot1 + " ;se nao  0, continua");
								
				String rot2 = rotulo.novoRotulo();

				buffer.add(rot2 + ":");
				buffer.add("pop dx ;desempilha valor");
				buffer.add("add dx, 30h ;transforma em caractere");
				buffer.add("mov ds:[di],dl ;escreve caractere");
				buffer.add("add di, 1 ;incrementa base");
				buffer.add("add cx, -1 ;decrementa contador");
				buffer.add("cmp cx, 0 ;verifica pilha vazia");
				buffer.add("jne " + rot2 + " ;se nao pilha vazia, loop");
				buffer.add("mov dl, 024h ;fim de string");
				buffer.add("mov ds:[di], dl ;grava '$'");
				buffer.add("mov dx, " + stringEnd);
				buffer.add("mov ah, 09h");
				buffer.add("int 21h");
			}
			
			if (! (TipoEnum.isInteiro(tipoExp) || TipoEnum.isString(tipoExp) || TipoEnum.isByte(tipoExp)))
				erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
			
			while (s.getToken() == OpEnum.VIRGULA.getToken())
			{
				confirmaToken( OpEnum.VIRGULA.getToken() );

				tipoExp = analisarExpressao();
				stringEnd = memoria.novoTemp();

				if ( TipoEnum.isString(tipoExp) )
				{
					buffer.add("mov dx, " + enderecoExp);
					buffer.add("mov ah, 09h");
					buffer.add("int 21h");
				}
				else
				{
					buffer.add("mov ax, DS:[" + enderecoExp + "]");
					buffer.add("mov di, " + stringEnd + " ;end. string temp.");
					buffer.add("mov cx, 0 ;contador");
					buffer.add("cmp ax,0 ;verifica sinal");
					
					String rot = rotulo.novoRotulo();

					buffer.add("jge " + rot + " ;salta se numero positivo");
					buffer.add("mov bl, 2Dh ;senao, escreve sinal ");
					buffer.add("mov ds:[di], bl");
					buffer.add("add di, 1 ;incrementa indice");
					buffer.add("neg ax ;toma modulo do numero");
					buffer.add(rot + ":");
					buffer.add("mov bx, 10 ;divisor");

					String rot1 = rotulo.novoRotulo();
					
					buffer.add(rot1 + ":");
					buffer.add("add cx, 1 ;incrementa contador");
					buffer.add("mov dx, 0 ;estende 32bits p/ div.");
					buffer.add("idiv bx ;divide DXAX por BX");
					buffer.add("push dx ;empilha valor do resto");
					buffer.add("cmp ax, 0 ;verifica se quoc.  0");
					buffer.add("jne " + rot1 + " ;se nao  0, continua");
									
					String rot2 = rotulo.novoRotulo();

					buffer.add(rot2 + ":");
					buffer.add("pop dx ;desempilha valor");
					buffer.add("add dx, 30h ;transforma em caractere");
					buffer.add("mov ds:[di],dl ;escreve caractere");
					buffer.add("add di, 1 ;incrementa base");
					buffer.add("add cx, -1 ;decrementa contador");
					buffer.add("cmp cx, 0 ;verifica pilha vazia");
					buffer.add("jne " + rot2 + " ;se nao pilha vazia, loop");
					buffer.add("mov dl, 024h ;fim de string");
					buffer.add("mov ds:[di], dl ;grava '$'");
					buffer.add("mov dx, " + stringEnd);
					buffer.add("mov ah, 09h");
					buffer.add("int 21h");
				}

				if (! (TipoEnum.isInteiro(tipoExp) || TipoEnum.isString(tipoExp) || TipoEnum.isByte(tipoExp)))
					erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
			}
			
			if (ln)
			{
				buffer.add("mov ah, 02h");
				buffer.add("mov dl, 0Dh");
				buffer.add("int 21h");
				buffer.add("mov DL, 0Ah");
				buffer.add("int 21h");
			}
			
			confirmaToken( OpEnum.FP.getToken() );
			confirmaToken( OpEnum.PONTO_VIRG.getToken() );
		}
		else if(s.getToken() == OpEnum.PONTO_VIRG.getToken())
			confirmaToken( OpEnum.PONTO_VIRG.getToken() );

		memoria.restetTemp();
	}
	
	public TipoEnum analisarExpressao() throws Exception
	{
		/* Acao Semantica */
		TipoEnum tipoSubExp = analisarSubExpressao();
		TipoEnum tipoExp = tipoSubExp;
		
		if (! s.isComparacao())
			return tipoExp;

		if ( ! TipoEnum.isInteiro(tipoSubExp) && ! TipoEnum.isByte(tipoSubExp) && ! TipoEnum.isString(tipoSubExp) )
			erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

		int op = 0;

		if ( s.getToken() == OpEnum.IGUAL.getToken() )
		{
			op = 5;
			confirmaToken( OpEnum.IGUAL.getToken() );
		}

		if ( TipoEnum.isString(tipoSubExp) )
			erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
		else if (s.getToken() == OpEnum.MAIOR.getToken())
		{
			op = 1;
			confirmaToken( OpEnum.MAIOR.getToken() );
		}
		else if (s.getToken() == OpEnum.MENOR.getToken())
		{
			op = 2;
			confirmaToken( OpEnum.MENOR.getToken() );
		}
		else if (s.getToken() == OpEnum.MAIOR_IGUAL.getToken())
		{
			op = 3;
			confirmaToken( OpEnum.MAIOR_IGUAL.getToken() );
		}
		else if (s.getToken() == OpEnum.MENOR_IGUAL.getToken())
		{
			op = 5;
			confirmaToken( OpEnum.MENOR_IGUAL.getToken() );
		}
		else if (s.getToken() == OpEnum.DIF.getToken())
		{
			op = 6;
			confirmaToken( OpEnum.DIF.getToken() );
		}

		TipoEnum tipoExpSub = analisarSubExpressao();

		if (! TipoEnum.isInteiro(tipoExpSub) && ! TipoEnum.isByte(tipoExpSub))
			erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

		buffer.add("mov ax, DS:[" + enderecoExp + "]");

		if ( TipoEnum.isByte(tipoExpSub) || TipoEnum.isLogico(tipoExpSub) )
		{
			buffer.add("mov cx, ax");
			buffer.add("mov bl, DS:[" + enderecoExpSub + "]");
			buffer.add("mov al, bl");
			buffer.add("mov ah, 0");
			buffer.add("mov bx, ax");
			buffer.add("mov ax, cx");
		}
		else
			buffer.add("mov bx, DS:[" + enderecoExpSub + "]");

		buffer.add("cmp ax, bx");

		String rotTrue = rotulo.novoRotulo();

		switch (op)
		{
			case 1:
				buffer.add("jg " + rotTrue);
				
				break;
			case 2:
				buffer.add("jl " + rotTrue);
				
				break;
			case 3:
				buffer.add("jge " + rotTrue);
				
				break;
			case 4:
				buffer.add("jle " + rotTrue);
				
				break;
			case 5:
				buffer.add("je " + rotTrue);
				
				break;
			case 6:
				buffer.add("jne " + rotTrue);

				break;
		}

		buffer.add("mov AL, 0");

		String rotFalse = rotulo.novoRotulo();

		buffer.add("jmp " + rotFalse);
		buffer.add(rotTrue + ":");
		buffer.add("mov AL, 0FFh");
		buffer.add(rotFalse + ":");

		enderecoExp = memoria.novoTemp();

		/* Acao Semantica */
		tipoExp = TipoEnum.LOGICO;
		buffer.add("mov DS:[" + enderecoExp + "], AL");

		return tipoExp;
	}

	private TipoEnum analisarSubExpressao() throws Exception
	{
		TipoEnum tipoTk2;
		boolean subtracao = false;

		if(s.getToken() == OpEnum.SUB.getToken())
		{
			subtracao = true;
			confirmaToken( OpEnum.SUB.getToken() );
		}
		else if (s.getToken() == OpEnum.SOMA.getToken())
		{
			subtracao = false;
			confirmaToken( OpEnum.SOMA.getToken() );
		}

		/* Acao Semantica */
		tipoTk2 = analisarSimbolo();

		if (subtracao)
		{
			enderecoExpSub = memoria.novoTemp();

			buffer.add("mov al, DS:[" + enderecoToken + "] ;");
			buffer.add("not al");
			buffer.add("mov DS:[" + enderecoToken + "], al");
		}

		enderecoExpSub = enderecoToken;
		
		int op = 0;

		while ( s.getToken() == OpEnum.SUB.getToken() || 
				s.getToken() == OpEnum.SOMA.getToken() )
		{
			if (s.getToken() == OpEnum.SUB.getToken())
			{
				if ( TipoEnum.isString(tipoTk2) )
					erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

				op = 1;
				confirmaToken( OpEnum.SUB.getToken() );
			}
			else if (s.getToken() == OpEnum.SOMA.getToken())
			{
				op = 2;
				confirmaToken( OpEnum.SOMA.getToken() );
			}

			/* Acao Semantica */
			TipoEnum tipoTk1 = analisarSimbolo();

			buffer.add("mov ax, DS:[" + enderecoExpSub + "]");
			buffer.add("mov bx, DS:[" + enderecoToken + "]");

			if ( (tipoTk2 != null && tipoTk1 == null || tipoTk2 == null && tipoTk1 != null || tipoTk2 != tipoTk1) && 
				 ! (TipoEnum.isInteiro(tipoTk1) && TipoEnum.isByte(tipoTk2) || TipoEnum.isInteiro(tipoTk2) && TipoEnum.isByte(tipoTk1)) )
				erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );
			
			switch (op)
			{
				case 1:
					buffer.add("sub ax, bx ; subtracao");
					break;

				case 2:
					if ( TipoEnum.isString(tipoTk2) && TipoEnum.isString(tipoTk1) )
					{
						int temp1 = memoria.novoTemp();
					
						buffer.add("mov DS:[" + temp1 + "], ax");
						buffer.add("add ax, bx");
						buffer.add("mov ax, DS:[" + temp1 + "]");
					}
					else
						buffer.add("add ax, bx ; soma");

					break;

				case 3:
					buffer.add("or ax, bx ; and");
					break;
			}

			enderecoExpSub = memoria.novoTemp();

			buffer.add("mov DS:[" + enderecoExpSub + "], ax");
		}
		
		return tipoTk2;
	}
	
	public TipoEnum analisarSimbolo() throws Exception
	{
		/* Acao Semantica */
		TipoEnum token1 = analisarSimboloSub();

		enderecoToken = enderecoToken2;
		
		int op = 0;

		while ( s.getToken() == OpEnum.MULT.getToken() || 
				s.getToken() == OpEnum.DIV.getToken() ||
				s.getToken() == OpEnum.POT.getToken() ||
				s.getToken() == OpEnum.MOD.getToken() )
		{
			if (s.getToken() == OpEnum.MULT.getToken())
			{
				if ( TipoEnum.isString(token1) )
					erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

				op = 1;
				confirmaToken( OpEnum.MULT.getToken() );
			}
			else if (s.getToken() == OpEnum.DIV.getToken())
			{
				if ( TipoEnum.isString(token1) )
					erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

				op = 2;
				confirmaToken(OpEnum.DIV.getToken());
			}
			else if (s.getToken() == OpEnum.POT.getToken())
			{
				if ( TipoEnum.isString(token1) )
					erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

				op = 3;
				confirmaToken(OpEnum.POT.getToken());
			}
			else if (s.getToken() == OpEnum.MOD.getToken())
			{
				if ( TipoEnum.isString(token1) )
					erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

				op = 4;
				confirmaToken(OpEnum.MOD.getToken());
			}

			TipoEnum tipoTk2 = analisarSimboloSub();
			
			buffer.add("mov al, DS:[" + enderecoToken + "]");
			buffer.add("mov bx, DS:[" + enderecoToken2 + "]");
			
			if (op == 2)
			{
				buffer.add("cwd");
					
				buffer.add("mov cx, ax ; salvar o que tinha em al");
				buffer.add("mov ax, DS:[" + enderecoToken2 + "] ; mover F1.end para al");
				buffer.add("cwd");
				buffer.add("mov bx, ax ; voltar F1.end para bx");
				buffer.add("mov ax, cx ;voltar valor anterior de ax");
			}

			switch (op)
			{
				case 1:
					buffer.add("imul bx ; multiplicacao");
					break;
				
				case 2:
					buffer.add("idiv bx ; divisao");
					buffer.add("sub ax, 256; divisao");
					break;

				case 3:
					buffer.add("ipot bx ; potencia");
					break;
				
				case 4:
					buffer.add("imod bx ; mod");
					break;
				
				case 5:
					buffer.add("and ax, bx ; and");
					break;
			}
			
			enderecoToken = memoria.novoTemp();
			
			buffer.add("mov DS:[" + enderecoToken + "], ax");
			
			/* Acao Semantica */
			if ((token1 != null && tipoTk2 == null || token1 == null && tipoTk2 != null || token1 != tipoTk2) &&
				 ! ((TipoEnum.isInteiro(token1) && TipoEnum.isByte(tipoTk2)) || (TipoEnum.isInteiro(tipoTk2) && TipoEnum.isByte(token1))) )
				erro.setErro( ErroEnum.TIPO_INCOMPATIVEL );

			if ( TipoEnum.isInteiro(token1) && TipoEnum.isByte(tipoTk2) || 
				 TipoEnum.isInteiro(tipoTk2) && TipoEnum.isByte(token1) )
				token1 = TipoEnum.INTEIRO;
		}
		
		return token1;
	}

	public TipoEnum analisarSimboloSub() throws Exception
	{
		TipoEnum tokenTipo = null;

		if (s.getToken() == OpEnum.AP.getToken())
		{
			confirmaToken( OpEnum.AP.getToken() );
			
			tokenTipo = analisarExpressao();
			enderecoToken2 = enderecoExp;
			
			confirmaToken(OpEnum.FP.getToken());
		}
		else if (s.getToken() == OpEnum.NOT.getToken())
		{
			confirmaToken( OpEnum.NOT.getToken() );

			tokenTipo = analisarSimboloSub();

			int Fend = memoria.novoTemp();

			buffer.add("mov al, DS:[" + enderecoToken2 + "] ;");
			buffer.add("not al");
			buffer.add("mov DS:[" + Fend + "], al");
			
			enderecoToken2 = Fend;
		}
		else if (s.getToken() == OpEnum.CONST.getToken() )
		{
			/* Acao Semantica */
			tokenTipo = s.getTipo();

			if ( s.getTipo() != null && s.getTipo() == TipoEnum.STRING )
			{
				//declarar constante na área de dados:
				buffer.add("dseg SEGMENT PUBLIC");
				buffer.add("byte " + s.getLexema().substring(0, s.getLexema().length() - 1) + "$" + s.getLexema().charAt(s.getLexema().length() - 1));
				buffer.add("dseg ENDS");
				
				enderecoToken2 = memoria.getContador();

				memoria.alocarString(s.getLexema().length() - 1);
			}
			else
			{
				String lexTemp = s.getLexema();

				if ( s.getLexema().toLowerCase().equals("true") )
					lexTemp = "0FFh";
				else if( s.getLexema().toLowerCase().equals("false") )
					lexTemp = "0h";

				enderecoToken2 = memoria.novoTemp();

				buffer.add("mov ax, " + lexTemp + " ; const " + s.getLexema());
				buffer.add("mov DS:[" + enderecoToken2 + "], al");

				if ( TipoEnum.isByte( s.getTipo() ) )
					memoria.alocarTempByte();
				else if ( TipoEnum.isLogico( s.getTipo() ) )
					memoria.alocarTempLogico();
				else if ( TipoEnum.isInteiro( s.getTipo() ) )
					memoria.alocarTempInteiro();
			}

			buffer.add("; " + s.getLexema() + " em " + enderecoToken2);

			confirmaToken( OpEnum.CONST.getToken() );
		}
		else if ( s.getToken() == OpEnum.ID.getToken() )
		{
			/* Acao Semantica */
			if ( s.getClasse() == null )
				erro.setErro( ErroEnum.ID_NAO_DECLARADO );
			else
				tokenTipo = s.getTipo();

			enderecoToken2 = s.getEndereco();

			buffer.add("; " + s.getLexema() + " em " + s.getEndereco());

			confirmaToken( OpEnum.ID.getToken() );
		}

		return tokenTipo;
	}
}