package com.compiler;

import java.util.*;

import com.compiler.SyntacticAnalyzer.Aux;

public class SemanticAnalyzer {

    private SymbolTable globalTable;
    private SymbolTable currentScope;

    public SemanticAnalyzer() {
        this.globalTable = new SymbolTable("global");
        this.currentScope = globalTable;
    }

    public void analyze(Aux aux) {
        if (aux.root != null) visit(aux.root);
    }

    private void visit(AstNode node) {
        if (node == null) return;

        switch (node.getNodeType()) {
            case FUNCTION -> handleFunction(node);
            case BLOCK -> handleBlock(node);
            case ASSIGN -> handleAssign(node);
            case IF -> handleIf(node);
            case WHILE -> handleWhile(node);
            case PRINT -> handlePrint(node);
            case RETURN -> handleReturn(node);
            case CALL -> handleCall(node);
            case ARITOP -> handleAritOp(node);
            case RELOP -> handleRelOp(node);
            case ID -> handleId(node);
            case INTCONST, FLOATCONST, CHARCONST -> handleConst(node);
            default -> node.getChildren().forEach(this::visit);
        }
    }

    // ---------------- Escopo ----------------
    private SymbolTable.Symbol resolveSymbol(String name) {
        SymbolTable scope = currentScope;
        while (scope != null) {
            if (scope.containsSymbol(name)) return scope.getSymbol(name);
            scope = scope.getParent();
        }
        return null;
    }

    private SymbolTable.FunctionRegister findFunctionByName(String name) {
        for (var fn : globalTable.getAllFunctions()) {
            if (fn.getName().equals(name)) return fn;
        }
        return null;
    }

    // ---------------- FUNCTION ----------------
// Dentro de SemanticAnalyzer.java
    private void handleFunction(AstNode node) {
        String name = node.getValue();

        // Cria escopo da função como filho do escopo atual
        SymbolTable fnTable = new SymbolTable(name, currentScope);
        fnTable.setReturnType(node.getDataType());

        List<AstNode> params = new ArrayList<>();
        AstNode body = null;

        // Verifica se o primeiro filho é bloco ou parâmetros
        AstNode firstChild = node.getChild(0);
        if (firstChild.getNodeType() == AstNode.NodeType.BLOCK) {
            body = firstChild;
        } else {
            // primeiro filho = parâmetros
            params = firstChild.getChildren();
            for (int i = 0; i < params.size(); i++) {
                AstNode p = params.get(i);
                fnTable.addSymbol(p.getValue(), p.getDataType().name(), true, i);
            }
            // segundo filho = corpo
            if (node.getChildren().size() > 1) body = node.getChild(1);
        }

        // salva escopo atual e troca para o da função
        SymbolTable oldScope = currentScope;
        currentScope = fnTable;

        // visita corpo
        if (body != null) visit(body);

        // adiciona a função no escopo global
        globalTable.addFunction(
                name,
                params.size(),
                params.stream().map(p -> p.getDataType().name()).toList(),
                fnTable.getReturnType()
        );

        // volta para escopo anterior
        currentScope = oldScope;
    }

    // ---------------- BLOCK ----------------
    private void handleBlock(AstNode node) {
        for (AstNode c : node.getChildren()) visit(c);
    }

    // ---------------- ASSIGN ----------------
    private void handleAssign(AstNode node) {
        AstNode id = node.getChild(0);
        AstNode expr = node.getChild(1);
        visit(expr);

        SymbolTable.Symbol s = resolveSymbol(id.getValue());
        if (s == null) {
            System.err.println("Erro: variável '" + id.getValue() + "' não declarada.");
            node.setDataType(SymbolTable.DataType.ERROR);
            return;
        }

        SymbolTable.DataType varType =
                SymbolTable.DataType.valueOf(s.getDataType().toUpperCase());

        if (expr.getDataType() != varType) {
            System.err.println("Erro: variável '" + id.getValue() + "' recebe tipo incompatível.");
            node.setDataType(SymbolTable.DataType.ERROR);
        } else node.setDataType(varType);
    }

    // ---------------- CALL ----------------
    private void handleCall(AstNode node) {
        String name = node.getValue();
        var fn = findFunctionByName(name);
        if (fn == null) {
            System.err.println("Erro: função '" + name + "' não declarada.");
            node.setDataType(SymbolTable.DataType.ERROR);
            return;
        }

        List<AstNode> args = node.getChildren();
        if (args.size() != fn.getNumArgs()) {
            System.err.println("Erro: chamada de '" + name + "' com número incorreto de argumentos.");
        }

        for (AstNode arg : args) visit(arg);

        node.setDataType(fn.getReturnType());
    }

    // ---------------- RETURN ----------------
    private void handleReturn(AstNode node) {
        if (currentScope == globalTable) {
            System.err.println("Erro: return fora de função.");
            node.setDataType(SymbolTable.DataType.ERROR);
            return;
        }

        if (node.getChildren().isEmpty()) {
            if (currentScope.getReturnType() != SymbolTable.DataType.VOID) {
                System.err.println("Erro: função deve retornar " + currentScope.getReturnType());
                node.setDataType(SymbolTable.DataType.ERROR);
            }
            return;
        }

        AstNode expr = node.getChild(0);
        visit(expr);

        if (expr.getDataType() != currentScope.getReturnType()) {
            System.err.println("Erro de retorno: esperado " + currentScope.getReturnType() +
                    " mas retornado " + expr.getDataType());
            node.setDataType(SymbolTable.DataType.ERROR);
        } else node.setDataType(expr.getDataType());
    }

    // ---------------- RELACIONAL ----------------
    private void handleRelOp(AstNode node) {
        AstNode left = node.getChild(0);
        AstNode right = node.getChild(1);

        visit(left);
        visit(right);

        if (left.getDataType() == SymbolTable.DataType.ERROR ||
                right.getDataType() == SymbolTable.DataType.ERROR) {
            node.setDataType(SymbolTable.DataType.ERROR);
            return;
        }

        if (left.getDataType() == SymbolTable.DataType.CHAR ||
                right.getDataType() == SymbolTable.DataType.CHAR) {
            System.err.println("Erro semântico: operação relacional inválida com CHAR.");
            node.setDataType(SymbolTable.DataType.ERROR);
            return;
        }

        node.setDataType(SymbolTable.DataType.INT);
    }

    // ---------------- ARITOP ----------------
    private void handleAritOp(AstNode node) {
        AstNode left = node.getChild(0);
        AstNode right = node.getChild(1);

        visit(left);
        visit(right);

        if (left.getDataType() == SymbolTable.DataType.FLOAT ||
                right.getDataType() == SymbolTable.DataType.FLOAT) {
            node.setDataType(SymbolTable.DataType.FLOAT);
        } else node.setDataType(SymbolTable.DataType.INT);
    }

    // ---------------- IF ----------------
    private void handleIf(AstNode node) {
        AstNode cond = node.getChild(0);
        AstNode thenBlock = node.getChild(1);

        visit(cond);
        if (cond.getDataType() == SymbolTable.DataType.CHAR ||
                cond.getDataType() == SymbolTable.DataType.ERROR) {
            System.err.println("Erro: condição do IF deve ser numérica.");
        }

        visit(thenBlock);
        if (node.getChildren().size() == 3) visit(node.getChild(2));
    }

    // ---------------- WHILE ----------------
    private void handleWhile(AstNode node) {
        AstNode cond = node.getChild(0);
        AstNode block = node.getChild(1);

        visit(cond);
        if (cond.getDataType() == SymbolTable.DataType.CHAR ||
                cond.getDataType() == SymbolTable.DataType.ERROR) {
            System.err.println("Erro: condição do WHILE deve ser numérica.");
        }

        visit(block);
    }

    // ---------------- PRINT ----------------
    private void handlePrint(AstNode node) {
        AstNode expr = node.getChild(0);
        visit(expr);
    }

    // ---------------- ID ----------------
    private void handleId(AstNode node) {
        var s = resolveSymbol(node.getValue());
        if (s == null) {
            System.err.println("Erro semântico: variável '" + node.getValue() + "' não declarada.");
            node.setDataType(SymbolTable.DataType.ERROR);
            return;
        }
        node.setDataType(SymbolTable.DataType.valueOf(s.getDataType().toUpperCase()));
    }

    // ---------------- CONST ----------------
    private void handleConst(AstNode node) {
        switch (node.getNodeType()) {
            case INTCONST -> node.setDataType(SymbolTable.DataType.INT);
            case FLOATCONST -> node.setDataType(SymbolTable.DataType.FLOAT);
            case CHARCONST -> node.setDataType(SymbolTable.DataType.CHAR);
        }
    }
}
