package com.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.compiler.AstNode.NodeType.*;



public class SyntacticAnalyzer {
    private Vector<Token> tokensToAnalyse;
    private int currentTokenIndex;
    private Token.Type currentTokenType;
    private Vector<SymbolTable> symbolTables;
    private Vector<String> errors;
    public AstNode root;

    static class Aux {
        public AstNode root;
        public Vector<SymbolTable> symbolTables;
    }

    public Aux analyse(Vector<Token> tokens){
        System.out.println("Entrou em analyse()");
        root = new AstNode(AST);
        symbolTables = new Vector<>();
        errors = new Vector<>();
        tokensToAnalyse = tokens;
        currentTokenIndex = 0;
        currentTokenType = (tokensToAnalyse.isEmpty() ? null : tokensToAnalyse.get(currentTokenIndex).getType());
        programa();
        Aux aux = new Aux();
        aux.root = root;
        aux.symbolTables = symbolTables;
        return aux;
    }

    private void lidaComErro(Token.Type expectedToken) {
        String foundToken = (currentTokenIndex < tokensToAnalyse.size())
                ? tokensToAnalyse.get(currentTokenIndex).getType().toString()
                : "EOF";
        String errorMsg = "Erro sintático: esperado token " + expectedToken + " mas encontrado " + foundToken +
                " na posição " + currentTokenIndex;
        System.out.println(errorMsg);
        errors.add(errorMsg);
        // tenta sincronizar: avança até encontrar ponto de sincronização simples
        sync();
    }

    private void sync() {
        // avança até encontrar ; } ou início de próxima função para reduzir cascata de erros
        while (currentTokenIndex < tokensToAnalyse.size()) {
            Token.Type t = tokensToAnalyse.get(currentTokenIndex).getType();
            if (t == Token.Type.SEMICOLON || t == Token.Type.RBRACE || t == Token.Type.FUNCTION) {
                break;
            }
            currentTokenIndex++;
        }
        if (currentTokenIndex < tokensToAnalyse.size()) {
            currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();
        } else currentTokenType = null;
    }

    private void match(Token.Type expectedToken){
        if (currentTokenIndex >= tokensToAnalyse.size()) {
            lidaComErro(expectedToken);
            return;
        }

        currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();

        if (currentTokenType == expectedToken) {
            // consume
            currentTokenIndex++;
        } else {
            lidaComErro(expectedToken);
            // try to consume the unexpected token to continue
            if (currentTokenIndex < tokensToAnalyse.size()) currentTokenIndex++;
        }

        if (currentTokenIndex < tokensToAnalyse.size()) {
            currentTokenType = tokensToAnalyse.get(currentTokenIndex).getType();
        }else{
            currentTokenType = null;
        }
    }

    private void nomeFuncao() {
        String lexeme = (currentTokenIndex < tokensToAnalyse.size())
                ? tokensToAnalyse.get(currentTokenIndex).getLexeme() : "UNKNOWN";

        // cria e adiciona tabela de símbolos da função
        SymbolTable newSymbolTable = new SymbolTable(lexeme);

        if (currentTokenType == Token.Type.ID) {
            match(Token.Type.ID);
        } else if (currentTokenType == Token.Type.MAIN) {
            match(Token.Type.MAIN);
        } else {
            // erro mas tenta seguir
            match(Token.Type.ID);
        }

        symbolTables.add(newSymbolTable);
    }

    /* -------------------- Gramática principal -------------------- */
    private void programa() {
        // programa -> funcao funcaoSeq
        while (currentTokenType == Token.Type.FUNCTION) {
            AstNode fn = funcao();
            if (fn != null) root.addChild(fn);
        }
    }

    private AstNode funcaoSeq() {
        // não mais usada (iterativo em programa)
        return null;
    }

    private AstNode funcao() {
        // Funcao -> fn ID ( ListaParams ) TipoRetornoFuncao Bloco
        System.out.println("Entrou em funcao()");
        if (currentTokenType == null || currentTokenType != Token.Type.FUNCTION) {
            lidaComErro(Token.Type.FUNCTION);
            return null;
        }

        match(Token.Type.FUNCTION);
        // pega nome e cria symbol table
        String funcName = (currentTokenType == Token.Type.ID) ? tokensToAnalyse.get(currentTokenIndex).getLexeme() : "main";
        nomeFuncao(); // consumes ID and adds symbol table

        AstNode functionNode = new AstNode(FUNCTION);
        functionNode.setValue(funcName);

        match(Token.Type.LBRACKET);
        // params: for now keep names/types in symbol table (parsing)
        List<String> params = listaParams();
        match(Token.Type.RBRACKET);

        tipoRetornoFuncao(); // sets return type in symbol table

        AstNode blockNode = bloco();
        if (blockNode != null) functionNode.addChild(blockNode);

        // após processar função, pop da symbolTables (opcional manter)
        // symbolTables.remove(symbolTables.size()-1);

        return functionNode;
    }

    private List<String> listaParams() {
        System.out.println("Entrou em listaParams()");
        List<String> params = new ArrayList<>();
        if (currentTokenType == Token.Type.ID){
            String name = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.ID);
            match(Token.Type.COLON);
            String typeName = type();
            // registra na tabela de símbolos atual como parâmetro
            if (!symbolTables.isEmpty()) {
                SymbolTable st = symbolTables.lastElement();
                st.addSymbol(name, typeName, true, st.getSymbols().size());
            }
            params.add(name);
            params.addAll(listaParams2());
        }
        return params;
    }

    private List<String> listaParams2() {
        System.out.println("Entrou em listaParams2()");
        List<String> more = new ArrayList<>();
        if (currentTokenType == Token.Type.COMMA){
            match(Token.Type.COMMA);
            String name = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.ID);
            match(Token.Type.COLON);
            String typeName = type();
            if (!symbolTables.isEmpty()) {
                SymbolTable st = symbolTables.lastElement();
                st.addSymbol(name, typeName, true, st.getSymbols().size());
            }
            more.add(name);
            more.addAll(listaParams2());
        }
        return more;
    }

    private String tipoRetornoFuncao() {
        System.out.println("Entrou em tipoRetornoFuncao()");
        if (symbolTables.isEmpty()) return null;

        SymbolTable currentTable = symbolTables.lastElement();
        if (currentTokenType == Token.Type.ARROW) {
            match(Token.Type.ARROW);
            if (currentTokenType == Token.Type.INT) {
                currentTable.setReturnType(SymbolTable.DataType.INT);
                type();
                return "int";
            } else if (currentTokenType == Token.Type.FLOAT) {
                currentTable.setReturnType(SymbolTable.DataType.FLOAT);
                type();
                return "float";
            } else if (currentTokenType == Token.Type.CHAR) {
                currentTable.setReturnType(SymbolTable.DataType.CHAR);
                type();
                return "char";
            } else {
                // erro - assume void
                currentTable.setReturnType(SymbolTable.DataType.VOID);
                return "void";
            }
        } else {
            currentTable.setReturnType(SymbolTable.DataType.VOID);
            return "void";
        }
    }

    private AstNode bloco() {
        System.out.println("Entrou em bloco()");
        if (currentTokenType != Token.Type.LBRACE) {
            lidaComErro(Token.Type.LBRACE);
            return null;
        }
        match(Token.Type.LBRACE);
        AstNode blockNode = new AstNode(BLOCK);

        List<AstNode> children = sequencia();
        if (children != null) blockNode.addChildren(children);

        if (currentTokenType != Token.Type.RBRACE) {
            lidaComErro(Token.Type.RBRACE);
        } else {
            match(Token.Type.RBRACE);
        }
        return blockNode;
    }

    private List<AstNode> sequencia() {
        System.out.println("Entrou em sequencia()");
        List<AstNode> children = new ArrayList<>();
        // sequencia -> (declaracao | comando)*
        while (currentTokenType != null &&
                (currentTokenType == Token.Type.LET
                        || currentTokenType == Token.Type.ID
                        || currentTokenType == Token.Type.WHILE
                        || currentTokenType == Token.Type.PRINTLN
                        || currentTokenType == Token.Type.RETURN
                        || currentTokenType == Token.Type.IF
                        || currentTokenType == Token.Type.LBRACE)) {

            if (currentTokenType == Token.Type.LET) {
                AstNode decl = declaracao();
                if (decl != null) children.add(decl);
            } else {
                AstNode cmd = comando();
                if (cmd != null) children.add(cmd);
            }
        }
        return children;
    }

    private AstNode declaracao() {
        System.out.println("Entrou em declaracao()");
        // LET varList : type ;
        match(Token.Type.LET);
        List<String> vars = varList();
        match(Token.Type.COLON);
        String t = type();
        match(Token.Type.SEMICOLON);

        // registra símbolos
        if (!symbolTables.isEmpty()) {
            SymbolTable st = symbolTables.lastElement();
            for (String v : vars) {
                st.addSymbol(v, t, false, -1);
            }
        }

        // podemos retornar um node de declaração (opcional). Para a AST, normalmente variáveis ficam na symbol table
        return null;
    }

    private List<String> varList() {
        System.out.println("Entrou em varList()");
        List<String> names = new ArrayList<>();
        if (currentTokenType == Token.Type.ID) {
            names.add(tokensToAnalyse.get(currentTokenIndex).getLexeme());
            match(Token.Type.ID);
            names.addAll(varList2());
        } else {
            lidaComErro(Token.Type.ID);
        }
        return names;
    }

    private List<String> varList2() {
        System.out.println("Entrou em varList2()");
        List<String> more = new ArrayList<>();
        if(currentTokenType == Token.Type.COMMA){
            match(Token.Type.COMMA);
            more.add(tokensToAnalyse.get(currentTokenIndex).getLexeme());
            match(Token.Type.ID);
            more.addAll(varList2());
        }
        return more;
    }

    private String type() {
        System.out.println("Entrou em type()");
        if(currentTokenType == Token.Type.INT) {
            match(Token.Type.INT);
            return "int";
        } else if(currentTokenType == Token.Type.FLOAT) {
            match(Token.Type.FLOAT);
            return "float";
        } else if(currentTokenType == Token.Type.CHAR) {
            match(Token.Type.CHAR);
            return "char";
        }else{
            lidaComErro(Token.Type.INT);
            return "int";
        }
    }

    private AstNode comando() {
        System.out.println("Entrou em comando()");
        AstNode node = null;
        if(currentTokenType == Token.Type.ID) {
            // could be assignment or call
            node = atribuicaoOuChamada();
        } else if(currentTokenType == Token.Type.WHILE) {
            node = whileCommand();
        } else if(currentTokenType == Token.Type.PRINTLN) {
            node = printCommand();
        } else if(currentTokenType == Token.Type.RETURN) {
            node = returnCommand();
        } else if(currentTokenType == Token.Type.IF || currentTokenType == Token.Type.LBRACE) {
            if (currentTokenType == Token.Type.LBRACE) {
                node = bloco();
            } else {
                node = comandoSe();
            }
        } else {
            // unexpected token
            lidaComErro(Token.Type.ID);
            // try to recover
            if (currentTokenType != null) match(currentTokenType);
        }
        return node;
    }

    private AstNode atribuicaoOuChamada() {
        System.out.println("Entrou em atribuicaoOuChamada()");
        if (currentTokenType != Token.Type.ID) {
            lidaComErro(Token.Type.ID);
            return null;
        }

        String idLexeme = tokensToAnalyse.get(currentTokenIndex).getLexeme();
        match(Token.Type.ID);

        if (currentTokenType == Token.Type.ASSIGN) {
            // assignment
            match(Token.Type.ASSIGN);
            AstNode right = expr();
            match(Token.Type.SEMICOLON);

            AstNode assignNode = new AstNode(ASSIGN);
            AstNode leftId = new AstNode(ID);
            leftId.setValue(idLexeme);
            assignNode.addChild(leftId);
            if (right != null) assignNode.addChild(right);
            return assignNode;

        } else if (currentTokenType == Token.Type.LBRACKET) {
            // function call
            match(Token.Type.LBRACKET);
            AstNode callNode = new AstNode(CALL);
            callNode.setValue(idLexeme);
            List<AstNode> args = listaArgs();
            if (args != null) callNode.addChildren(args);
            match(Token.Type.RBRACKET);
            match(Token.Type.SEMICOLON);

            // ==== REGISTRA NA TABELA DE SÍMBOLOS ====
            if (!symbolTables.isEmpty()) {
                SymbolTable currentTable = symbolTables.lastElement();
                int funcIndex = currentTable.addFunction(idLexeme, args != null ? args.size() : 0,
                        null, SymbolTable.DataType.VOID);
                // opcional: registrar a chamada no símbolo se existir
                SymbolTable.Symbol s = currentTable.getSymbol(idLexeme);
                if (s != null) s.addCallRef(funcIndex);
            }

            return callNode;
        } else {
            lidaComErro(Token.Type.SEMICOLON);
            return null;
        }
    }

    private AstNode whileCommand() {
        System.out.println("Entrou em whileCommand()");
        match(Token.Type.WHILE);
        AstNode cond = expr();
        AstNode body = bloco();
        AstNode whileNode = new AstNode(WHILE);
        if (cond != null) whileNode.addChild(cond);
        if (body != null) whileNode.addChild(body);
        return whileNode;
    }

    private AstNode printCommand() {
        System.out.println("Entrou em printCommand()");
        match(Token.Type.PRINTLN);
        match(Token.Type.LBRACKET);
        match(Token.Type.FMT_STRING);
        match(Token.Type.COMMA);
        List<AstNode> args = null;
        if (currentTokenType != Token.Type.RBRACKET) {
            args = listaArgs();
        }        
        match(Token.Type.RBRACKET);
        match(Token.Type.SEMICOLON);

        AstNode printNode = new AstNode(PRINT);
        if (args != null) printNode.addChildren(args);
        return printNode;
    }

    private AstNode argNode() {

        AstNode node;

        switch (currentTokenType) {

            case FMT_STRING:
                node = new AstNode(AstNode.NodeType.CHARCONST);
                node.setValue(tokensToAnalyse.get(currentTokenIndex).getLexeme());
                match(Token.Type.FMT_STRING);
                return node;

            case ID:
                node = new AstNode(AstNode.NodeType.ID);
                node.setValue(tokensToAnalyse.get(currentTokenIndex).getLexeme());
                match(Token.Type.ID);
                return node;

            case INT_CONST:
                node = new AstNode(AstNode.NodeType.INTCONST);
                node.setValue(tokensToAnalyse.get(currentTokenIndex).getLexeme());
                match(Token.Type.INT_CONST);
                return node;

            case FLOAT_CONST:
                node = new AstNode(AstNode.NodeType.FLOATCONST);
                node.setValue(tokensToAnalyse.get(currentTokenIndex).getLexeme());
                match(Token.Type.FLOAT_CONST);
                return node;

            case CHAR_LITERAL:
                node = new AstNode(AstNode.NodeType.CHARCONST);
                node.setValue(tokensToAnalyse.get(currentTokenIndex).getLexeme());
                match(Token.Type.CHAR_LITERAL);
                return node;

            default:
                lidaComErro(Token.Type.FMT_STRING);
                return null;
        }
    }

    private AstNode returnCommand() {
        System.out.println("Entrou em returnCommand()");
        match(Token.Type.RETURN);
        AstNode exprNode = expr();
        match(Token.Type.SEMICOLON);
        AstNode ret = new AstNode(RETURN);
        if (exprNode != null) ret.addChild(exprNode);
        return ret;
    }

    private AstNode comandoSe() {
        System.out.println("Entrou em comandoSe()");
        match(Token.Type.IF);
        AstNode cond = expr();
        AstNode thenBlock = bloco();
        AstNode ifNode = new AstNode(IF);
        if (cond != null) ifNode.addChild(cond);
        if (thenBlock != null) ifNode.addChild(thenBlock);

        if (currentTokenType == Token.Type.ELSE) {
            match(Token.Type.ELSE);
            // else can be either bloco or another comandoSe()
            AstNode elseNode;
            if (currentTokenType == Token.Type.IF) {
                elseNode = comandoSe(); // nested if as else
            } else if (currentTokenType == Token.Type.LBRACE) {
                elseNode = bloco();
            } else {
                // try to parse single command as else
                elseNode = comando();
            }
            if (elseNode != null) ifNode.addChild(elseNode);
        }
        return ifNode;
    }

    /* -------------------- EXPRESSIONS -------------------- */

    private AstNode expr() {
        System.out.println("Entrou em expr()");
        // expr -> rel ( (== | !=) rel )*
        AstNode left = rel();
        while (currentTokenType == Token.Type.EQ || currentTokenType == Token.Type.NE) {
            Token.Type op = currentTokenType;
            match(op);
            AstNode right = rel();
            AstNode relNode = new AstNode(RELOP);
            relNode.setOp(op == Token.Type.EQ ? "==" : "!=");
            relNode.addChild(left);
            relNode.addChild(right);
            left = relNode;
        }
        return left;
    }

    private AstNode rel() {
        System.out.println("Entrou em rel()");
        // rel -> adicao ( (< | <= | > | >=) adicao )*
        AstNode left = adicao();
        while (currentTokenType == Token.Type.LT || currentTokenType == Token.Type.LE
                || currentTokenType == Token.Type.GT || currentTokenType == Token.Type.GE) {
            Token.Type op = currentTokenType;
            match(op);
            AstNode right = adicao();
            AstNode relNode = new AstNode(RELOP);
            switch (op) {
                case LT -> relNode.setOp("<");
                case LE -> relNode.setOp("<=");
                case GT -> relNode.setOp(">");
                case GE -> relNode.setOp(">=");
                default -> relNode.setOp("?");
            }
            relNode.addChild(left);
            relNode.addChild(right);
            left = relNode;
        }
        return left;
    }

    private AstNode adicao() {
        System.out.println("Entrou em adicao()");
        // adicao -> termo ( (+ | -) termo )*
        AstNode left = termo();
        while (currentTokenType == Token.Type.PLUS || currentTokenType == Token.Type.MINUS) {
            Token.Type op = currentTokenType;
            match(op);
            AstNode right = termo();
            AstNode a = new AstNode(ARITOP);
            a.setOp(op == Token.Type.PLUS ? "+" : "-");
            a.addChild(left);
            a.addChild(right);
            left = a;
        }
        return left;
    }

    private AstNode termo() {
        System.out.println("Entrou em termo()");
        // termo -> fator ( (* | /) fator )*
        AstNode left = fator();
        while (currentTokenType == Token.Type.MULT || currentTokenType == Token.Type.DIV) {
            Token.Type op = currentTokenType;
            match(op);
            AstNode right = fator();
            AstNode a = new AstNode(ARITOP);
            a.setOp(op == Token.Type.MULT ? "*" : "/");
            a.addChild(left);
            a.addChild(right);
            left = a;
        }
        return left;
    }

    private AstNode fator() {
        System.out.println("Entrou em fator()");
        // fator -> ID (call?) | INT_CONST | FLOAT_CONST | CHAR_LITERAL | ( expr )
        if (currentTokenType == Token.Type.ID) {
            String idLexeme = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.ID);
            if (currentTokenType == Token.Type.LBRACKET) {
                // call as expression
                match(Token.Type.LBRACKET);
                AstNode callNode = new AstNode(CALL);
                callNode.setValue(idLexeme); // function name stored in value
                List<AstNode> args = listaArgs();
                if (args != null) callNode.addChildren(args);
                match(Token.Type.RBRACKET);
                return callNode;
            } else {
                AstNode idNode = new AstNode(ID);
                idNode.setValue(idLexeme);
                return idNode;
            }
        } else if (currentTokenType == Token.Type.INT_CONST) {
            String lex = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.INT_CONST);
            AstNode n = new AstNode(INTCONST);
            n.setValue(lex);
            n.setDataType(SymbolTable.DataType.INT);
            return n;
        } else if (currentTokenType == Token.Type.FLOAT_CONST) {
            String lex = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.FLOAT_CONST);
            AstNode n = new AstNode(FLOATCONST);
            n.setValue(lex);
            n.setDataType(SymbolTable.DataType.FLOAT);
            return n;
        } else if (currentTokenType == Token.Type.CHAR_LITERAL) {
            String lex = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.CHAR_LITERAL);
            AstNode n = new AstNode(CHARCONST);
            n.setValue(lex);
            n.setDataType(SymbolTable.DataType.CHAR);
            return n;
        } else if (currentTokenType == Token.Type.LBRACKET) {
            match(Token.Type.LBRACKET);
            AstNode inner = expr();
            match(Token.Type.RBRACKET);
            return inner;
        } else {
            lidaComErro(Token.Type.ID);
            return null;
        }
    }

    private List<AstNode> listaArgs() {
        System.out.println("Entrou em listaArgs()");
        List<AstNode> children = new ArrayList<>();
        if (currentTokenType == Token.Type.ID || currentTokenType == Token.Type.INT_CONST
                || currentTokenType == Token.Type.FLOAT_CONST || currentTokenType == Token.Type.CHAR_LITERAL) {
            
            AstNode a = arg();
            if (a != null) children.add(a);
            children.addAll(listaArgs2());
        }
        return children;
    }

    private List<AstNode> listaArgs2() {
        System.out.println("Entrou em listaArgs2()");
        List<AstNode> more = new ArrayList<>();
        if (currentTokenType == Token.Type.COMMA) {
            match(Token.Type.COMMA);
            AstNode a = arg();
            if (a != null) more.add(a);
            more.addAll(listaArgs2());
        }
        return more;
    }

    private AstNode arg() {
        System.out.println("Entrou em arg()");
        if (currentTokenType == Token.Type.ID) {
            String idLexeme = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.ID);

            if (currentTokenType == Token.Type.LBRACKET) {
                // chamada de função como argumento
                match(Token.Type.LBRACKET);
                AstNode callNode = new AstNode(CALL);
                callNode.setValue(idLexeme);
                List<AstNode> args = listaArgs();
                if (args != null) callNode.addChildren(args);
                match(Token.Type.RBRACKET);

                // registra na tabela de símbolos da função atual
                if (!symbolTables.isEmpty()) {
                    SymbolTable currentTable = symbolTables.lastElement();
                    SymbolTable.DataType returnType = SymbolTable.DataType.VOID;
                    for (SymbolTable table : symbolTables) {
                        if (table.getTableName().equals(idLexeme)){
                            returnType = table.getReturnType();
                        }
                    }
                    List<String> argNames = new ArrayList<String>();
                    for (AstNode arg : args){
                        String name = arg.getValue();
                        argNames.add(name);
                    }
                    currentTable.addFunction(idLexeme, args != null ? args.size() : 0, argNames, returnType);
                    currentTable.addSymbol(idLexeme, returnType.name(), false, -1, currentTable.getAllFunctions().size() - 1);
                }

                return callNode;
            } else {
                // variável simples
                AstNode idNode = new AstNode(ID);
                idNode.setValue(idLexeme);
                return idNode;
            }
        } else if (currentTokenType == Token.Type.INT_CONST) {
            String lex = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.INT_CONST);
            AstNode n = new AstNode(INTCONST);
            n.setValue(lex);
            n.setDataType(SymbolTable.DataType.INT);
            return n;
        } else if (currentTokenType == Token.Type.FLOAT_CONST) {
            String lex = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.FLOAT_CONST);
            AstNode n = new AstNode(FLOATCONST);
            n.setValue(lex);
            n.setDataType(SymbolTable.DataType.FLOAT);
            return n;
        } else if (currentTokenType == Token.Type.CHAR_LITERAL) {
            String lex = tokensToAnalyse.get(currentTokenIndex).getLexeme();
            match(Token.Type.CHAR_LITERAL);
            AstNode n = new AstNode(CHARCONST);
            n.setValue(lex);
            n.setDataType(SymbolTable.DataType.CHAR);
            return n;
        } else {
            lidaComErro(Token.Type.ID);
            return null;
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