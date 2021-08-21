package br.edu.ifc.compilador.io;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Felipe Tenfen
 * 
 * Classe onde � armazenado todo o c�digo assembly durante o processo de compila��o, e ao final
 * da compila��o � realizada a �timiza��o do c�digo gerado e gravado no arquivo C:/Lipex/codigo.asm
 */
public class Buffer
{
	private List<String> buffer, otimizado;
	private BufferedWriter arquivo;

	public Buffer() throws Exception
	{
		buffer = new ArrayList<>();
		otimizado = new ArrayList<>();
		arquivo = new BufferedWriter(new FileWriter("c:/Lipex/codigo.asm"));
	}

	/*
	 * M�todo realiza a otimiza��o do c�digo assembly gerado
	 */
	public void otimizar()
	{
		String[] tmp = buffer.get(0).split(" ", buffer.get(0).indexOf(';') > 0 ? buffer.get(0).indexOf(';') : buffer.get(0).length());

		for (String s : buffer)
		{
			if(! s.equals(buffer.get(0)))
			{
				String[] comando = s.split(" ", s.indexOf(';') > 0 ? s.indexOf(';') : s.length());

				if ( tmp[0].equals("mov") && comando[0].equals("mov") )
				{
					if(! (tmp[1].contains(comando[2]) && tmp[2].contains(comando[1])))
						otimizado.add(s);
				}
				else if ( tmp[0].equals("j") || tmp[0].equals("je") || tmp[0].equals("jg") || tmp[0].equals("jge") || 
						  tmp[0].equals("jl") || tmp[0].equals("jle") || tmp[0].equals("jmp") || tmp[0].equals("jne") )
				{
					if(! tmp[1].contains(comando[0]))
						otimizado.add(s);
				}
				else
					otimizado.add(s);

				tmp = comando;
			}
			else
				otimizado.add(s);
		}
	}

	/*
	 * M�todo cria o arquivo com c�digo assembly criado e otimizado
	 */
	public void criarArquivo() throws IOException
	{
		for(String s : otimizado)
		{
			arquivo.write(s);
			arquivo.newLine();
		}

		arquivo.close();
	}
	
	public void add(String buf)
	{
		buffer.add(buf);
	}
}