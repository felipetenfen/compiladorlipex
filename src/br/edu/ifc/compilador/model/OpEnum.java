package br.edu.ifc.compilador.model;
import java.util.Vector;

/**
 * @author Felipe Tenfen
 * 
 * Enum com as palavras reservadas do compilador
 */
public enum OpEnum
{
	INT ("int", 1),	         BYTE("byte", 2),
	STR("str", 3),	         WHILE("while", 4),
	IF("if", 5),	         ELSE("else", 6),
	AND("&&", 7),	         OR("||", 8),
	NOT("!", 9),	         IGUAL("==", 10),
	ATRIB("=", 11),	         AP("(", 12),
	FP(")", 13),	         MAIOR(">", 14),
	MENOR("<", 15),	         DIF("!=", 16),
	MAIOR_IGUAL(">=", 17),	 MENOR_IGUAL("<=", 18),
	VIRGULA(",", 19),		 SOMA("+", 20),
	SUB("-", 21),	         MULT("*", 22),
	DIV("/", 23),	         MOD("%", 24),
	POT("^", 25),	         PONTO_VIRG(";", 26),
	READLN("readln", 27),	 WRITE("write", 28),
	WRITELN("writeln", 29),	 BOOL("bool", 30),
	PROGRAMA("program", 31), INICIO_BLOCO("{", 32),
	FIM_BLOCO("}", 33),		 ID("id", 34),
	CONST("const", 35),		 TRUE("true", 36),
	FALSE("false", 37),		 END("end", 38);

	private int token;
	private String lexema;

	private OpEnum(String lexema, int token)
	{
		this.lexema = lexema;
		this.token = token;
	}
	
	public String getLexema()
	{
		return lexema;
	}

	public int getToken()
	{
		return token;
	}
	
	public static Vector<OpEnum> getOperadores()
	{
		Vector<OpEnum> ops = new Vector<>();

		ops.add(INT);
		ops.add(BYTE);
		ops.add(STR);
		ops.add(WHILE);	
		ops.add(IF);
		ops.add(ELSE);
		ops.add(AND);
		ops.add(OR);
		ops.add(NOT);
		ops.add(IGUAL);
		ops.add(ATRIB);
		ops.add(AP);
		ops.add(FP);
		ops.add(MAIOR);
		ops.add(MENOR);
		ops.add(DIF);
		ops.add(MAIOR_IGUAL);
		ops.add(MENOR_IGUAL);
		ops.add(VIRGULA);
		ops.add(SOMA);
		ops.add(SUB);
		ops.add(MULT);
		ops.add(DIV);
		ops.add(MOD);
		ops.add(POT);
		ops.add(PONTO_VIRG);
		ops.add(READLN);
		ops.add(WRITE);
		ops.add(WRITELN);
		ops.add(BOOL);
		ops.add(PROGRAMA);
		ops.add(END);
		ops.add(INICIO_BLOCO);
		ops.add(FIM_BLOCO);

		return ops;
	}
}