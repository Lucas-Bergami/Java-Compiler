package com.compiler;

import java.util.*;

public class AstNode {
    public enum NodeType {
        FUNCTION, RELOP, ARITOP, ASSIGN,
        IF, WHILE, PRINT, RETURN, CALL, BLOCK,
        ID, INTCONST, FLOATCONST, CHARCONST, AST
    }

    private NodeType nodeType;
    private List<AstNode> children;
    private SymbolTable.DataType dataType; // definido pelas regras semânticas
    private String op;      // operador (+, -, *, /, ==, etc.)
    private String value;   // usado em IDs e literais

    public AstNode(NodeType nodeType) {
        this.nodeType = nodeType;
        this.children = new ArrayList<>();
        this.dataType = SymbolTable.DataType.VOID;
    }

    public void addChild(AstNode child) {
        children.add(child);
    }

    public void addChildren(List<AstNode> newChildren) {
        children.addAll(newChildren);
    }

    public List<AstNode> getChildren() {
        return children;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public SymbolTable.DataType getDataType() {
        return dataType;
    }

    public void setDataType(SymbolTable.DataType dataType) {
        this.dataType = dataType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AstNode getChild(int index) {
        return children.get(index);
    }
}
