package com.compiler;

import java.util.Vector;

public class SyntaticAnalyzer {
	private Vector<Token> tokensToAnalyse;
	private int currentTokenIndex;
	private Token.Type currentTokenType;
	private Vector<SymbolTable> symbolTables;

	public void analyse(Vector<Token> tokens){
		tokensToAnalyse = tokens;
		currentTokenIndex = 0;
		currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();
		programa();

	}

private void lidaComErro(Token.Type expectedToken, Token.Type foundToken){
	System.out.println("Erro, esperado token " + expectedToken + " mas encontrado token " +
			 tokensToAnalyse.get(currentTokenIndex).getType());
}

	private void match(Token.Type expectedToken){
		currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();
		if (currentTokenType == expectedToken){
			System.out.println("Token " + currentTokenType + "na entrada");
			currentTokenIndex += 1;
			currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();
		} else {
			lidaComErro(expectedToken, currentTokenType);
			currentTokenIndex += 1;
			currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();
		}
	}

	private void programa() {
		if (tokensToAnalyse.get(currentTokenIndex).getType() == Token.Type.FUNCTION){
			funcao();
			funcaoSeq();
		}
	}

	private void funcaoSeq() {
		if (tokensToAnalyse.get(currentTokenIndex).getType() == Token.Type.FUNCTION){
			funcao();
			funcaoSeq();
		} else {
			return;
		}
	}

	private void funcao() {
		match(Token.Type.FUNCTION);
		nomeFuncao();
		match(Token.Type.LBRACKET);
		listaParams();
		match(Token.Type.RBRACKET);
		tipoRetornoFuncao();
		bloco();
	}

	private void nomeFuncao() {
		SymbolTable newSymbolTable;
		if (currentTokenType == Token.Type.ID){
			newSymbolTable = new SymbolTable(tokensToAnalyse.get(currentTokenIndex).getLexeme());
			match(Token.Type.ID);
			symbolTables.add(newSymbolTable);
		} else {
			newSymbolTable = new SymbolTable(tokensToAnalyse.get(currentTokenIndex).getLexeme());
			match(Token.Type.MAIN);
			symbolTables.add(newSymbolTable);
		}
	}

	private void listaParams() {
		if (currentTokenType == Token.Type.ID){
			match(Token.Type.ID);
			match(Token.Type.COLON);
			type();
			listaParams2();
		} else {
			return;
		}
	}

	private void listaParams2() {
		if (currentTokenType == Token.Type.COMMA){
			match(Token.Type.COMMA);
			match(Token.Type.ID);
			match(Token.Type.COLON);
			type();
			listaParams2();
		} else {
			return;
		}
	}

	private void tipoRetornoFuncao() {
    if (symbolTables.isEmpty()) return;

    SymbolTable currentTable = symbolTables.lastElement();

    if (currentTokenType == Token.Type.INT) {
        currentTable.setReturnType(SymbolTable.DataType.INT);
        type();
    } 
    else if (currentTokenType == Token.Type.FLOAT) {
        currentTable.setReturnType(SymbolTable.DataType.FLOAT);
        type();
    } 
    else if (currentTokenType == Token.Type.CHAR) {
        currentTable.setReturnType(SymbolTable.DataType.CHAR);
        type();
    } 
    else {
        currentTable.setReturnType(SymbolTable.DataType.VOID);
    }
}


	private void bloco() {
		match(Token.Type.LBRACE);
		sequencia();
		match(Token.Type.RBRACE);
	}

	private void sequencia() {
		if (currentTokenType == Token.Type.LET) {
			declaracao();
			sequencia();
		} else if (currentTokenType == Token.Type.ID || currentTokenType == Token.Type.WHILE
		|| currentTokenType == Token.Type.PRINTLN || currentTokenType == Token.Type.RETURN
		|| currentTokenType == Token.Type.IF || currentTokenType == Token.Type.LBRACE){
			comando();
			sequencia();
		} else {
			return;
		}
	}

	private void declaracao() {
		match(Token.Type.LET);
		varList();
		match(Token.Type.COLON);
		type();
		match(Token.Type.SEMICOLON);
	}

	private void varList() {
		match(Token.Type.ID);
		varList2();
	}

	private void varList2() {
		if(currentTokenType == Token.Type.COMMA){
			match(Token.Type.COMMA);
			match(Token.Type.ID);
			varList2();
		} else {
			return;
		}
	}

	private void type() {
		if(currentTokenType == Token.Type.INT) {
			match(Token.Type.INT);
		} else if(currentTokenType == Token.Type.FLOAT) {
			match(Token.Type.FLOAT);
		} else if(currentTokenType == Token.Type.CHAR) {
			match(Token.Type.CHAR);
		}
	}

	private void comando() {
		if(currentTokenType == Token.Type.ID) {
			match(Token.Type.ID);
			atribuicaoOuChamada();
		} else if(currentTokenType == Token.Type.WHILE) {
			match(Token.Type.WHILE);
			expr();
			bloco();
		} else if(currentTokenType == Token.Type.PRINTLN) {
			match(Token.Type.PRINTLN);
			match(Token.Type.LBRACKET);
			match(Token.Type.FMT_STRING);
			match(Token.Type.COMMA);
			listaArgs();
			match(Token.Type.RBRACKET);
			match(Token.Type.SEMICOLON);
		}
		else if(currentTokenType == Token.Type.RETURN) {
			match(Token.Type.RETURN);
			expr();
			match(Token.Type.SEMICOLON);
		} else if(currentTokenType == Token.Type.IF || currentTokenType == Token.Type.LBRACE ) {
			comandoSe();
		}
	}

	private void atribuicaoOuChamada() {
		if(currentTokenType == Token.Type.ASSIGN){
			match(Token.Type.ASSIGN);
			expr();
			match(Token.Type.SEMICOLON);
		} else if(currentTokenType == Token.Type.LBRACKET){
			match(Token.Type.LBRACKET);
			listaArgs();
			match(Token.Type.RBRACKET);
			match(Token.Type.SEMICOLON);
		}
	}

	private void comandoSe() {
		if(currentTokenType == Token.Type.IF){
			match(Token.Type.IF);
			expr();
			bloco();
			comandoSenao();
		} else if(currentTokenType == Token.Type.LBRACE){
			bloco();
		}
	}

	private void comandoSenao() {
		if(currentTokenType == Token.Type.ELSE){
			comandoSe();
		} else {
			return;
		}
	}

	private void expr() {
		rel();
		exprOpc();
	}

	private void exprOpc() {
		if(currentTokenType == Token.Type.EQ || currentTokenType == Token.Type.NE){
			opIgual();
			rel();
			exprOpc();
		} else {
			return;
		}
	}

	private void opIgual() {
		if(currentTokenType == Token.Type.EQ){
			match(Token.Type.EQ);
		} else if(currentTokenType == Token.Type.NE){
			match(Token.Type.NE);
		}
	}

	private void rel() {
		adicao();
		relOpc();
	}

	private void relOpc() {
		if(currentTokenType == Token.Type.LT || currentTokenType == Token.Type.LE
		|| currentTokenType == Token.Type.GT || currentTokenType == Token.Type.GE){
			opRel();
			adicao();
			relOpc();
		} else {
			return;
		}
	}

	private void opRel() {
		if(currentTokenType == Token.Type.LT){
			match(Token.Type.LT);
		} else if(currentTokenType == Token.Type.LE){
			match(Token.Type.LE);
		} else if(currentTokenType == Token.Type.GT){
			match(Token.Type.GT);
		} else if(currentTokenType == Token.Type.GE){
			match(Token.Type.GE);
		}
	}

	private void adicao() {
		termo();
		adicaoOpc();
	}

	private void adicaoOpc() {
		if(currentTokenType == Token.Type.PLUS || currentTokenType == Token.Type.MINUS){
			opAdicao();
			termo();
			adicaoOpc();
		} else {
			return;
		}
	}

	private void opAdicao() {
		if(currentTokenType == Token.Type.PLUS){
			match(Token.Type.PLUS);
		} else if(currentTokenType == Token.Type.MINUS){
			match(Token.Type.MINUS);
		}
	}

	private void termo() {
		fator();
		termoOpc();
	}

	private void termoOpc() {
		if(currentTokenType == Token.Type.MULT || currentTokenType == Token.Type.DIV){
			opMult();
			fator();
			termoOpc();
		} else {
			return;
		}
	}

	private void opMult() {
		if(currentTokenType == Token.Type.MULT){
			match(Token.Type.MULT);
		} else if(currentTokenType == Token.Type.DIV){
			match(Token.Type.DIV);
		}
	}

	private void fator() {
		if(currentTokenType == Token.Type.ID) {
			match(Token.Type.ID);
			chamadaFuncao();
		} else if(currentTokenType == Token.Type.INT_CONST) {
			match(Token.Type.INT_CONST);
		} else if(currentTokenType == Token.Type.FLOAT_CONST) {
			match(Token.Type.FLOAT_CONST);
		}
		else if(currentTokenType == Token.Type.CHAR_LITERAL) {
			match(Token.Type.CHAR_LITERAL);
		} else if(currentTokenType == Token.Type.LBRACKET ) {
			match(Token.Type.LBRACKET);
			expr();
			match(Token.Type.RBRACKET);
		}
	}

	private void chamadaFuncao() {
		if(currentTokenType == Token.Type.LBRACKET ){
			match(Token.Type.LBRACKET);
			listaArgs();
			match(Token.Type.RBRACKET);
		} else {
			return;
		}
	}

	private void listaArgs() {
		if(currentTokenType == Token.Type.ID || currentTokenType == Token.Type.INT_CONST
		|| currentTokenType == Token.Type.FLOAT_CONST || currentTokenType == Token.Type.CHAR_LITERAL){
			arg();
			listaArgs2();
		} else {
			return;
		}
	}

	private void listaArgs2() {
		if(currentTokenType == Token.Type.COMMA){
			match(Token.Type.COMMA);
			arg();
			listaArgs2();
		} else {
			return;
		}
	}

	private void arg() {
		if(currentTokenType == Token.Type.ID) {
			match(Token.Type.ID);
			chamadaFuncao();
		} else if(currentTokenType == Token.Type.INT_CONST) {
			match(Token.Type.INT_CONST);
		} else if(currentTokenType == Token.Type.FLOAT_CONST) {
			match(Token.Type.FLOAT_CONST);
		}
		else if(currentTokenType == Token.Type.CHAR_LITERAL) {
			match(Token.Type.CHAR_LITERAL);
		}
	}

}
