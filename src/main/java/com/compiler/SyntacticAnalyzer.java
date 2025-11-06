package com.compiler;

import java.util.Vector;

public class SyntacticAnalyzer {
    private Vector<Token> tokensToAnalyse;
    private int currentTokenIndex;
    private Token.Type currentTokenType;
    private Vector<SymbolTable> symbolTables;
    private Vector<String> errors;

    public void analyse(Vector<Token> tokens){
        System.out.println("Entrou em analyse()");
        symbolTables = new Vector<>();
        errors = new Vector<>();
        tokensToAnalyse = tokens;
        currentTokenIndex = 0;
        currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();
        programa();
    }

    private void lidaComErro(Token.Type expectedToken) {
        System.out.println("Entrou em lidaComErro()");
        String foundToken = (currentTokenIndex < tokensToAnalyse.size())
                ? tokensToAnalyse.get(currentTokenIndex).getType().toString()
                : "EOF";
        String errorMsg = "Erro sintático: esperado token " + expectedToken + " mas encontrado " + foundToken +
                " na posição " + currentTokenIndex;
        System.out.println(errorMsg);
        errors.add(errorMsg);
    }

    private void match(Token.Type expectedToken){
        System.out.println("Entrou em match()");
        if (currentTokenIndex >= tokensToAnalyse.size()) {
            lidaComErro(expectedToken);
            return;
        }

        currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();

        if (currentTokenType == expectedToken) {
            System.out.println("Token " + currentTokenType + " na entrada");
            currentTokenIndex++;
        } else {
            lidaComErro(expectedToken);
            currentTokenIndex++;
        }

        if (currentTokenIndex < tokensToAnalyse.size()) {
            currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();
        }else{
            currentTokenType = null;
        }
    }

    private void nomeFuncao() {
        System.out.println("Entrou em nomeFuncao()");
        String lexeme = (currentTokenIndex < tokensToAnalyse.size())
                ? tokensToAnalyse.get(currentTokenIndex).getLexeme() : "UNKNOWN";

        SymbolTable newSymbolTable = new SymbolTable(lexeme);

        if (currentTokenType == Token.Type.ID) {
            match(Token.Type.ID);
        } else if (currentTokenType == Token.Type.MAIN) {
            match(Token.Type.MAIN);
        } else {
            match(Token.Type.ID);
        }

        symbolTables.add(newSymbolTable);
    }

    private void programa() {
        System.out.println("Entrou em programa()");
        if (tokensToAnalyse.get(currentTokenIndex).getType() == Token.Type.FUNCTION){
            funcao();
            funcaoSeq();
        }
    }

    private void funcaoSeq() {
        System.out.println("Entrou em funcaoSeq()");
        if (currentTokenType != null && tokensToAnalyse.get(currentTokenIndex).getType() == Token.Type.FUNCTION){
            funcao();
            funcaoSeq();
        }
    }

    private void funcao() {
        System.out.println("Entrou em funcao()");
        if(currentTokenType == null) {
            return;
        }
        match(Token.Type.FUNCTION);
        nomeFuncao();
        match(Token.Type.LBRACKET);
        listaParams();
        match(Token.Type.RBRACKET);
        tipoRetornoFuncao();
        bloco();
    }

    private void listaParams() {
        System.out.println("Entrou em listaParams()");
        if (currentTokenType == Token.Type.ID){
            match(Token.Type.ID);
            match(Token.Type.COLON);
            type();
            listaParams2();
        }
    }

    private void listaParams2() {
        System.out.println("Entrou em listaParams2()");
        if (currentTokenType == Token.Type.COMMA){
            match(Token.Type.COMMA);
            match(Token.Type.ID);
            match(Token.Type.COLON);
            type();
            listaParams2();
        }
    }

    private void tipoRetornoFuncao() {
        System.out.println("Entrou em tipoRetornoFuncao()");
        if (symbolTables.isEmpty()) return;

        SymbolTable currentTable = symbolTables.lastElement();
        if (currentTokenType == Token.Type.ARROW) {
            match(Token.Type.ARROW);
            if (currentTokenType == Token.Type.INT) {
                currentTable.setReturnType(SymbolTable.DataType.INT);
                type();
            } else if (currentTokenType == Token.Type.FLOAT) {
                currentTable.setReturnType(SymbolTable.DataType.FLOAT);
                type();
            } else if (currentTokenType == Token.Type.CHAR) {
                currentTable.setReturnType(SymbolTable.DataType.CHAR);
                type();
            }
        }
        else {
            currentTable.setReturnType(SymbolTable.DataType.VOID);
        }
    }

    private void bloco() {
        System.out.println("Entrou em bloco()");
        match(Token.Type.LBRACE);
        sequencia();
        match(Token.Type.RBRACE);
    }

    private void sequencia() {
        System.out.println("Entrou em sequencia()");
        if (currentTokenType == Token.Type.LET) {
            declaracao();
            sequencia();
        } else if (currentTokenType == Token.Type.ID || currentTokenType == Token.Type.WHILE
                || currentTokenType == Token.Type.PRINTLN || currentTokenType == Token.Type.RETURN
                || currentTokenType == Token.Type.IF || currentTokenType == Token.Type.LBRACE){
            comando();
            sequencia();
        }
    }

    private void declaracao() {
        System.out.println("Entrou em declaracao()");
        match(Token.Type.LET);
        varList();
        match(Token.Type.COLON);
        type();
        match(Token.Type.SEMICOLON);
    }

    private void varList() {
        System.out.println("Entrou em varList()");
        match(Token.Type.ID);
        varList2();
    }

    private void varList2() {
        System.out.println("Entrou em varList2()");
        if(currentTokenType == Token.Type.COMMA){
            match(Token.Type.COMMA);
            match(Token.Type.ID);
            varList2();
        }
    }

    private void type() {
        System.out.println("Entrou em type()");
        if(currentTokenType == Token.Type.INT) {
            match(Token.Type.INT);
        } else if(currentTokenType == Token.Type.FLOAT) {
            match(Token.Type.FLOAT);
        } else if(currentTokenType == Token.Type.CHAR) {
            match(Token.Type.CHAR);
        }else{
            match(Token.Type.INT);
        }
    }

    private void comando() {
        System.out.println("Entrou em comando()");
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
        }else{
            match(Token.Type.ID);
        }
    }

    private void atribuicaoOuChamada() {
        System.out.println("Entrou em atribuicaoOuChamada()");
        if(currentTokenType == Token.Type.ASSIGN){
            match(Token.Type.ASSIGN);
            expr();
            match(Token.Type.SEMICOLON);
        } else if(currentTokenType == Token.Type.LBRACKET){
            match(Token.Type.LBRACKET);
            listaArgs();
            match(Token.Type.RBRACKET);
            match(Token.Type.SEMICOLON);
        }else {
            match(Token.Type.ASSIGN);
        }
    }

    private void comandoSe() {
        System.out.println("Entrou em comandoSe()");
        if(currentTokenType == Token.Type.IF){
            match(Token.Type.IF);
            expr();
            bloco();
            comandoSenao();
        } else if(currentTokenType == Token.Type.LBRACE){
            bloco();
        }else {
            match(Token.Type.IF);
        }
    }

    private void comandoSenao() {
        System.out.println("Entrou em comandoSenao()");
        if(currentTokenType == Token.Type.ELSE){
            match(Token.Type.ELSE);
            comandoSe();
        }
    }

    private void expr() {
        System.out.println("Entrou em expr()");
        rel();
        exprOpc();
    }

    private void exprOpc() {
        System.out.println("Entrou em exprOpc()");
        if(currentTokenType == Token.Type.EQ || currentTokenType == Token.Type.NE){
            opIgual();
            rel();
            exprOpc();
        }
    }

    private void opIgual() {
        System.out.println("Entrou em opIgual()");
        if(currentTokenType == Token.Type.EQ){
            match(Token.Type.EQ);
        } else if(currentTokenType == Token.Type.NE){
            match(Token.Type.NE);
        }else{
            match(Token.Type.EQ);
        }
    }

    private void rel() {
        System.out.println("Entrou em rel()");
        adicao();
        relOpc();
    }

    private void relOpc() {
        System.out.println("Entrou em relOpc()");
        if(currentTokenType == Token.Type.LT || currentTokenType == Token.Type.LE
                || currentTokenType == Token.Type.GT || currentTokenType == Token.Type.GE){
            opRel();
            adicao();
            relOpc();
        }
    }

    private void opRel() {
        System.out.println("Entrou em opRel()");
        if(currentTokenType == Token.Type.LT){
            match(Token.Type.LT);
        } else if(currentTokenType == Token.Type.LE){
            match(Token.Type.LE);
        } else if(currentTokenType == Token.Type.GT){
            match(Token.Type.GT);
        } else if(currentTokenType == Token.Type.GE){
            match(Token.Type.GE);
        }else{
            match(Token.Type.LT);
        }
    }

    private void adicao() {
        System.out.println("Entrou em adicao()");
        termo();
        adicaoOpc();
    }

    private void adicaoOpc() {
        System.out.println("Entrou em adicaoOpc()");
        if(currentTokenType == Token.Type.PLUS || currentTokenType == Token.Type.MINUS){
            opAdicao();
            termo();
            adicaoOpc();
        }
    }

    private void opAdicao() {
        System.out.println("Entrou em opAdicao()");
        if(currentTokenType == Token.Type.PLUS){
            match(Token.Type.PLUS);
        } else if(currentTokenType == Token.Type.MINUS){
            match(Token.Type.MINUS);
        }else{
            match(Token.Type.PLUS);
        }
    }

    private void termo() {
        System.out.println("Entrou em termo()");
        fator();
        termoOpc();
    }

    private void termoOpc() {
        System.out.println("Entrou em termoOpc()");
        if(currentTokenType == Token.Type.MULT || currentTokenType == Token.Type.DIV){
            opMult();
            fator();
            termoOpc();
        }
    }

    private void opMult() {
        System.out.println("Entrou em opMult()");
        if(currentTokenType == Token.Type.MULT){
            match(Token.Type.MULT);
        } else if(currentTokenType == Token.Type.DIV){
            match(Token.Type.DIV);
        }else{
            match(Token.Type.MULT);
        }
    }

    private void fator() {
        System.out.println("Entrou em fator()");
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
        else {
            match(Token.Type.ID);
        }
    }

    private void chamadaFuncao() {
        System.out.println("Entrou em chamadaFuncao()");
        if(currentTokenType == Token.Type.LBRACKET ){
            match(Token.Type.LBRACKET);
            listaArgs();
            match(Token.Type.RBRACKET);
        }
    }

    private void listaArgs() {
        System.out.println("Entrou em listaArgs()");
        if(currentTokenType == Token.Type.ID || currentTokenType == Token.Type.INT_CONST
                || currentTokenType == Token.Type.FLOAT_CONST || currentTokenType == Token.Type.CHAR_LITERAL){
            arg();
            listaArgs2();
        }
    }

    private void listaArgs2() {
        System.out.println("Entrou em listaArgs2()");
        if(currentTokenType == Token.Type.COMMA){
            match(Token.Type.COMMA);
            arg();
            listaArgs2();
        }
    }

    private void arg() {
        System.out.println("Entrou em arg()");
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
        else{
            match(Token.Type.ID);
        }
    }

    public void saveOutputs() {
        System.out.println("Entrou em saveOutputs()");
        JsonExporter exporter = new JsonExporter();

        exporter.exportToFile(errors, "erros_sintaticos.json");
        exporter.exportToFile(symbolTables, "tabelas_de_simbolos.json");

        System.out.println("=== Tabelas de Símbolos ===");
        for (SymbolTable table : symbolTables) {
            System.out.println(table);
        }
    }
}
