package br.edu.ifc.compilador;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * @author Felipe Tenfen
 */
public class Principal
{
	static Parser parser;
	static BufferedReader arquivo;

	public static void main(String[] args) throws Exception
	{
		try
		{
			lerArquivo();

			parser = new Parser(arquivo);
			parser.parse();

			System.out.println("Compilado com sucesso!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Erro: " + e.getMessage());
		}
	}
	
	/*
	 * Método responsável capturar o arquivo a ser compilado e verificar se é um formato válido
	 */
	private static void lerArquivo()
	{
		String file = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));  	

		try
		{
			do
			{
				System.out.print("Informe o arquivo: ");

				if ( (file = in.readLine()).length() > 0 && ! isArquivoValido(file))
				{
					System.err.println("Arquivo incompatível!");
					System.out.println("Informe o arquivo: ");

					file = in.readLine();
				}
			}
			while (file.length() == 0);

			arquivo = new BufferedReader(new FileReader(file));
		}
		catch (Exception e)
		{
			System.err.println("Arquivo não encontrado");

			lerArquivo();
		}
	}
	
	private static boolean isArquivoValido(String file)
	{
		int tamArq = file.length();

		return file.charAt(tamArq-6) == '.' && "lipex".equals( file.substring(tamArq - 5, tamArq) );
	}
}