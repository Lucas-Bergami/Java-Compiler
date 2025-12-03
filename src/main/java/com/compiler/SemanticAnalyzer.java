package com.compiler;

import java.util.*;

import com.compiler.SymbolTable.FunctionRegister;
import com.compiler.SyntacticAnalyzer.Aux;

public class SemanticAnalyzer {

    private Aux aux; 
    private List<String> errors;
    private Map<String, SymbolTable> tableMap; // nome -> tabela

    public SemanticAnalyzer(Aux aux) {
        this.aux = aux;
        this.errors = new ArrayList<>();
        this.tableMap = new HashMap<>();

        // cria um map para acesso rápido às tabelas por nome
        for (SymbolTable st : aux.symbolTables) {
            tableMap.put(st.getTableName(), st);
        }
    }

    public List<String> analyze() {
        AstNode root = aux.root;

        if (root == null) {
            errors.add("AST Root is null.");
            return errors;
        }

        // cada FUNCTION na AST corresponde a uma tabela
        for (AstNode fun : root.getChildren()) {
            analyzeFunction(fun);
        }

        return errors;
    }

    // ------------------------------------------------------------
    //  Função
    // ------------------------------------------------------------
    private void analyzeFunction(AstNode functionNode) {
        String functionName = functionNode.getValue();
        SymbolTable functionTable = tableMap.get(functionName);

        if (functionTable == null) {
            errors.add("Erro interno: tabela de função não encontrada: " + functionName);
            return;
        }

        AstNode block = functionNode.getChild(0);

        // tipo de retorno esperado
        SymbolTable.DataType expectedReturn = functionTable.getReturnType();

        analyzeBlock(block, functionTable, expectedReturn);
    }

    // ------------------------------------------------------------
    //  Bloco
    // ------------------------------------------------------------
    private void analyzeBlock(AstNode block, SymbolTable table, SymbolTable.DataType expectedReturn) {
        for (AstNode child : block.getChildren()) {
            analyzeNode(child, table, expectedReturn);
        }
    }

    // ------------------------------------------------------------
    //  Análise geral
    // ------------------------------------------------------------
    private SymbolTable.DataType analyzeNode(
            AstNode node,
            SymbolTable table,
            SymbolTable.DataType expectedReturn
    ) {

        switch (node.getNodeType()) {

            case ID:
                return analyzeId(node, table);

            case INTCONST:
                node.setDataType(SymbolTable.DataType.INT);
                return SymbolTable.DataType.INT;

            case FLOATCONST:
                node.setDataType(SymbolTable.DataType.FLOAT);
                return SymbolTable.DataType.FLOAT;

            case CHARCONST:
                node.setDataType(SymbolTable.DataType.CHAR);
                return SymbolTable.DataType.CHAR;

            case ASSIGN:
                return analyzeAssign(node, table);

            case ARITOP:
                return analyzeAritOp(node, table);

            case RELOP:
                return analyzeRelOp(node, table);

            case RETURN:
                return analyzeReturn(node, table, expectedReturn);

            case IF:
                return analyzeIf(node, table, expectedReturn);

            case WHILE:
                return analyzeWhile(node, table, expectedReturn);

            case CALL:
                return analyzeCall(node, table);

            case PRINT:
                return analyzePrint(node, table);

            case BLOCK:
                analyzeBlock(node, table, expectedReturn);
                return SymbolTable.DataType.VOID;

            default:
                return SymbolTable.DataType.ERROR;
        }
    }

    // ------------------------------------------------------------
    //  ID
    // ------------------------------------------------------------
    private SymbolTable.DataType analyzeId(AstNode node, SymbolTable table) {
        String name = node.getValue();
        SymbolTable current = table;

        while (current != null) {
            if (current.containsSymbol(name)) {
                SymbolTable.Symbol sym = current.getSymbol(name);

                SymbolTable.DataType dt = convert(sym.getDataType());
                node.setDataType(dt);
                return dt;
            }
            current = current.getParent();
        }

        errors.add("Variável não declarada: " + name);
        node.setDataType(SymbolTable.DataType.ERROR);
        return SymbolTable.DataType.ERROR;
    }

    private SymbolTable.DataType convert(String s) {
        return switch (s) {
        case "int" -> SymbolTable.DataType.INT;
        case "float" -> SymbolTable.DataType.FLOAT;
        case "char" -> SymbolTable.DataType.CHAR;
        case "void" -> SymbolTable.DataType.VOID;
        default -> SymbolTable.DataType.ERROR;
        };
    }

    // ------------------------------------------------------------
    //  Atribuição
    // ------------------------------------------------------------
    private SymbolTable.DataType analyzeAssign(AstNode node, SymbolTable table) {
        AstNode id = node.getChild(0);
        AstNode expr = node.getChild(1);

        SymbolTable.DataType t1 = analyzeNode(id, table, null);
        SymbolTable.DataType t2 = analyzeNode(expr, table, null);

        if (t1 != t2 && t2 != SymbolTable.DataType.ERROR) {
            errors.add("Tipos incompatíveis em atribuição: "
                    + t1 + " = " + t2);
        }

        node.setDataType(t1);
        return t1;
    }

    // ------------------------------------------------------------
    //  Op Aritmética
    // ------------------------------------------------------------
    private SymbolTable.DataType analyzeAritOp(AstNode node, SymbolTable table) {
        SymbolTable.DataType left = analyzeNode(node.getChild(0), table, null);
        SymbolTable.DataType right = analyzeNode(node.getChild(1), table, null);

        if (left == SymbolTable.DataType.ERROR || right == SymbolTable.DataType.ERROR) {
            node.setDataType(SymbolTable.DataType.ERROR);
            return SymbolTable.DataType.ERROR;
        }

        // regra simples: int + int = int, float misturado vira float
        if (left == SymbolTable.DataType.FLOAT || right == SymbolTable.DataType.FLOAT) {
            node.setDataType(SymbolTable.DataType.FLOAT);
            return SymbolTable.DataType.FLOAT;
        }

        node.setDataType(SymbolTable.DataType.INT);
        return SymbolTable.DataType.INT;
    }

    // ------------------------------------------------------------
    //  Operação Relacional
    // ------------------------------------------------------------
    private SymbolTable.DataType analyzeRelOp(AstNode node, SymbolTable table) {
        SymbolTable.DataType left = analyzeNode(node.getChild(0), table, null);
        SymbolTable.DataType right = analyzeNode(node.getChild(1), table, null);

        if (left == SymbolTable.DataType.ERROR || right == SymbolTable.DataType.ERROR) {
            node.setDataType(SymbolTable.DataType.ERROR);
            return SymbolTable.DataType.ERROR;
        }

        // comparações sempre resultam booleano → mas não temos bool → usei INT como padrão
        node.setDataType(SymbolTable.DataType.INT);
        return SymbolTable.DataType.INT;
    }

    // ------------------------------------------------------------
    //  return
    // ------------------------------------------------------------
    private SymbolTable.DataType analyzeReturn(
            AstNode node,
            SymbolTable table,
            SymbolTable.DataType expectedReturn
    ) {

        AstNode expr = node.getChild(0);
        SymbolTable.DataType t = analyzeNode(expr, table, expectedReturn);

        if (expectedReturn != t && t != SymbolTable.DataType.ERROR) {
            errors.add("Tipo de retorno incompatível. Esperado: "
                    + expectedReturn + ", obtido: " + t);
        }

        node.setDataType(t);
        return t;
    }

    // ------------------------------------------------------------
    //  IF
    // ------------------------------------------------------------
    private SymbolTable.DataType analyzeIf(AstNode node, SymbolTable table, SymbolTable.DataType expectedReturn) {
        AstNode cond = node.getChild(0);
        analyzeNode(cond, table, expectedReturn);

        // bloco verdadeiro
        analyzeNode(node.getChild(1), table, expectedReturn);

        // else?
        if (node.getChildren().size() == 3) {
            analyzeNode(node.getChild(2), table, expectedReturn);
        }

        node.setDataType(SymbolTable.DataType.VOID);
        return SymbolTable.DataType.VOID;
    }

    // ------------------------------------------------------------
    //  WHILE
    // ------------------------------------------------------------
    private SymbolTable.DataType analyzeWhile(AstNode node, SymbolTable table, SymbolTable.DataType expectedReturn) {
        analyzeNode(node.getChild(0), table, expectedReturn); // condição
        analyzeNode(node.getChild(1), table, expectedReturn); // corpo
        node.setDataType(SymbolTable.DataType.VOID);
        return SymbolTable.DataType.VOID;
    }

    // ------------------------------------------------------------
    //  print
    // ------------------------------------------------------------
    private SymbolTable.DataType analyzePrint(AstNode node, SymbolTable table) {
        if (!node.getChildren().isEmpty()) {
            analyzeNode(node.getChild(0), table, null);
        }

        node.setDataType(SymbolTable.DataType.VOID);
        return SymbolTable.DataType.VOID;
    }

    // ------------------------------------------------------------
    //  call
    // ------------------------------------------------------------

    private SymbolTable.DataType getExpectedParamType(SymbolTable calledTable, int i) {
    for (SymbolTable.Symbol sym : calledTable.getSymbols().values()) {
        if (sym.isParam() && sym.getPosParam() == i) {
            return convert(sym.getDataType());
        }
    }
    return SymbolTable.DataType.ERROR;
    }


    private SymbolTable.DataType analyzeCall(AstNode node, SymbolTable table) {
        String functionName = node.getValue();

        FunctionRegister fn = null;

        for (FunctionRegister fr : table.getAllFunctions()) {
            if (fr.getName().equals(functionName)) {
                fn = fr;
                break;
            }
        }

        if (fn == null) {
            errors.add("Função não declarada: " + functionName);
            node.setDataType(SymbolTable.DataType.ERROR);
            return SymbolTable.DataType.ERROR;
        }

        if (node.getChildren().size() != fn.getNumArgs()) {
            errors.add("Número de argumentos incorreto ao chamar "
                    + functionName + ". Esperado: "
                    + fn.getNumArgs() + ", obtido: " + node.getChildren().size());
        }

        // verifica tipos
        for (int i = 0; i < node.getChildren().size(); i++) {
            SymbolTable.DataType t = analyzeNode(node.getChild(i), table, null);
            SymbolTable calledTable = new SymbolTable("teste");
            for (SymbolTable tempTable : aux.symbolTables){
                if (tempTable.getTableName().equals(fn.getName())){
                    calledTable = tempTable;
                }
            }
            SymbolTable.DataType expected = getExpectedParamType(calledTable, i);


            if (t != expected && t != SymbolTable.DataType.ERROR) {
                errors.add("Tipo do argumento " + (i + 1) + " incorreto na chamada de "
                        + functionName + ". Esperado: " + expected + ", obtido: " + t);
            }
        }

        node.setDataType(fn.getReturnType());
        return fn.getReturnType();
    }
}
