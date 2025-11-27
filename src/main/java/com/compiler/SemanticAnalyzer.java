package com.compiler;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private SymbolTable globalTable;
    private SymbolTable currentScope;

    public SemanticAnalyzer() {
        this.globalTable = new SymbolTable("global");
        this.currentScope = globalTable;
    }

    public void analyze(AstNode root) {
        if (root == null) return;
        visit(root);
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
        }
    }

    // -------------------------------------------------------------
    // RELACIONAL
    // -------------------------------------------------------------
    private void handleRelOp(AstNode node) {
        AstNode left = node.getChild(0);
        AstNode right = node.getChild(1);

        visit(left);
        visit(right);

        if (left.getDataType() == SymbolTable.DataType.CHAR ||
                right.getDataType() == SymbolTable.DataType.CHAR) {
            System.err.println("Erro: operação relacional inválida com CHAR.");
        }

        node.setDataType(SymbolTable.DataType.INT); // booleano → inteiro (0/1)
    }

    // -------------------------------------------------------------
    // RETURN
    // -------------------------------------------------------------
    private void handleReturn(AstNode node) {
        if (currentScope.getReturnType() == null) return;

        AstNode expr = node.getChild(0);
        visit(expr);

        SymbolTable.DataType expected = currentScope.getReturnType();
        SymbolTable.DataType actual = expr.getDataType();

        if (expected != actual) {
            System.err.println("Erro de retorno: função espera " + expected +
                    " mas retornou " + actual);
        }
    }

    // -------------------------------------------------------------
    // CALL
    // -------------------------------------------------------------
    private void handleCall(AstNode node) {
        String functionName = node.getValue();
        SymbolTable.FunctionRegister fn = globalTable.getFunction(Integer.parseInt(functionName));

        if (fn == null) {
            System.err.println("Erro: função '" + functionName + "' não declarada.");
            node.setDataType(SymbolTable.DataType.ERROR);
            return;
        }

        List<AstNode> args = node.getChildren();
        if (args.size() != fn.getParameters().size()) {
            System.err.println("Erro: chamada de função '" + functionName +
                    "' com número incorreto de argumentos.");
        }

        for (AstNode arg : args) visit(arg);

        node.setDataType(fn.getReturnType());
    }

    // -------------------------------------------------------------
    // PRINT
    // -------------------------------------------------------------
    private void handlePrint(AstNode node) {
        AstNode expr = node.getChild(0);
        visit(expr);
    }

    // -------------------------------------------------------------
    // WHILE
    // -------------------------------------------------------------
    private void handleWhile(AstNode node) {
        AstNode condition = node.getChild(0);
        AstNode block = node.getChild(1);

        visit(condition);

        if (condition.getDataType() != SymbolTable.DataType.INT &&
                condition.getDataType() != SymbolTable.DataType.FLOAT) {
            System.err.println("Erro: condição do WHILE deve ser numérica.");
        }

        visit(block);
    }

    // -------------------------------------------------------------
    // IF
    // -------------------------------------------------------------
    private void handleIf(AstNode node) {
        AstNode condition = node.getChild(0);
        AstNode thenBlock = node.getChild(1);

        visit(condition);

        if (condition.getDataType() != SymbolTable.DataType.INT &&
                condition.getDataType() != SymbolTable.DataType.FLOAT) {
            System.err.println("Erro: condição do IF deve ser numérica.");
        }

        visit(thenBlock);

        if (node.getChildren().size() == 3) {
            AstNode elseBlock = node.getChild(2);
            visit(elseBlock);
        }
    }

    // -------------------------------------------------------------
    // FUNCTION
    // -------------------------------------------------------------
    private void handleFunction(AstNode node) {
        String functionName = node.getValue();
        SymbolTable functionTable = new SymbolTable(functionName);

        functionTable.setReturnType(SymbolTable.DataType.INT);

        SymbolTable previous = currentScope;
        currentScope = functionTable;

        visit(node.getChild(0));

        globalTable.getFunctions().add(new SymbolTable.FunctionRegister(
                functionName,
                functionTable.getReturnType().ordinal(),
                new ArrayList<>()
        ));

        currentScope = previous;
    }

    // -------------------------------------------------------------
    // BLOCK
    // -------------------------------------------------------------
    private void handleBlock(AstNode node) {
        for (AstNode child : node.getChildren()) {
            visit(child);
        }
    }

    // -------------------------------------------------------------
    // ASSIGN
    // -------------------------------------------------------------
    private void handleAssign(AstNode node) {
        AstNode idNode = node.getChild(0);
        AstNode exprNode = node.getChild(1);

        visit(exprNode);

        SymbolTable.Symbol symbol = currentScope.getSymbol(idNode.getValue());
        if (symbol == null) {
            System.err.println("Erro: variável '" + idNode.getValue() + "' não declarada.");
            return;
        }

        SymbolTable.DataType varType =
                SymbolTable.DataType.valueOf(symbol.getDataType().toUpperCase());

        if (varType != exprNode.getDataType()) {
            System.err.println("Erro de tipo na variável '" + idNode.getValue() + "'");
        }
    }

    // -------------------------------------------------------------
    // ARITOP
    // -------------------------------------------------------------
    private void handleAritOp(AstNode node) {
        AstNode left = node.getChild(0);
        AstNode right = node.getChild(1);

        visit(left);
        visit(right);

        if (left.getDataType() == SymbolTable.DataType.CHAR ||
                right.getDataType() == SymbolTable.DataType.CHAR) {
            System.err.println("Erro: operação aritmética com CHAR.");
        }

        if (left.getDataType() == SymbolTable.DataType.FLOAT ||
                right.getDataType() == SymbolTable.DataType.FLOAT)
            node.setDataType(SymbolTable.DataType.FLOAT);
        else
            node.setDataType(SymbolTable.DataType.INT);
    }

    // -------------------------------------------------------------
    // ID
    // -------------------------------------------------------------
    private void handleId(AstNode node) {
        SymbolTable.Symbol symbol = currentScope.getSymbol(node.getValue());
        if (symbol == null) {
            System.err.println("Erro: variável '" + node.getValue() + "' não declarada.");
            node.setDataType(SymbolTable.DataType.ERROR);
        } else {
            node.setDataType(SymbolTable.DataType.valueOf(symbol.getDataType().toUpperCase()));
        }
    }

    // -------------------------------------------------------------
    // CONST
    // -------------------------------------------------------------
    private void handleConst(AstNode node) {
        switch (node.getNodeType()) {
            case INTCONST -> node.setDataType(SymbolTable.DataType.INT);
            case FLOATCONST -> node.setDataType(SymbolTable.DataType.FLOAT);
            case CHARCONST -> node.setDataType(SymbolTable.DataType.CHAR);
        }
    }
}
