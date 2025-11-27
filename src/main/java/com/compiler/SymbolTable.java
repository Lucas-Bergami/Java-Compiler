package com.compiler;

import java.util.*;


public class SymbolTable {

    public enum DataType {
        ERROR,
        INT,
        FLOAT,
        CHAR,
        VOID
    }

    private String tableName;
    private Map<String, Symbol> symbols;
    private List<FunctionRegister> functions;
    private static DataType returnType;

    public SymbolTable(String tableName) {
        this.tableName = tableName;
        this.symbols = new HashMap<>();
        this.functions = new ArrayList<>();
        this.returnType = DataType.VOID; 
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(Map<String, Symbol> symbols) {
        this.symbols = symbols;
    }

    public List<FunctionRegister> getFunctions() {
        return functions;
    }

    public void setFunctions(List<FunctionRegister> functions) {
        this.functions = functions;
    }

    public DataType getReturnType() {
        return returnType;
    }

    public void setReturnType(DataType returnType) {
        this.returnType = returnType;
    }

    // ==========================
    //  MÉTODOS DE SÍMBOLOS
    // ==========================

    public void addSymbol(String name, String dataType, boolean isParam, int posParam) {
        symbols.put(name, new Symbol(name, dataType, isParam, posParam));
    }

    public Symbol getSymbol(String name) {
        return symbols.get(name);
    }

    public boolean containsSymbol(String name) {
        return symbols.containsKey(name);
    }

    public int addFunction(String name, int numArgs, List<String> args) {
        FunctionRegister func = new FunctionRegister(name, numArgs, args);
        functions.add(func);
        return functions.size() - 1;
    }

    public FunctionRegister getFunction(int index) {
        if (index < 0 || index >= functions.size()) return null;
        return functions.get(index);
    }

    public List<FunctionRegister> getAllFunctions() {
        return Collections.unmodifiableList(functions);
    }

    public static class Symbol {
        private String name;
        private String dataType;
        private boolean isParam;
        private int posParam;
        private List<Integer> callRefs;

        public Symbol(String name, String dataType, boolean isParam, int posParam) {
            this.name = name;
            this.dataType = dataType;
            this.isParam = isParam;
            this.posParam = posParam;
            this.callRefs = new ArrayList<>();
        }

        public void addCallRef(int functionIndex) {
            callRefs.add(functionIndex);
        }

        public List<Integer> getCallRefs() {
            return callRefs;
        }

        public String getName() {
            return name;
        }

        public String getDataType() {
            return dataType;
        }

        public boolean isParam() {
            return isParam;
        }

        public int getPosParam() {
            return posParam;
        }

        @Override
        public String toString() {
            return "Symbol{" +
                    "name='" + name + '\'' +
                    ", dataType='" + dataType + '\'' +
                    ", isParam=" + isParam +
                    ", posParam=" + posParam +
                    ", callRefs=" + callRefs +
                    '}';
        }
    }

    static class FunctionRegister {
        private String name;
        private int numArgs;
        private List<String> args;

        public FunctionRegister(String name, int numArgs, List<String> args) {
            this.name = name;
            this.numArgs = numArgs;
            this.args = (args != null) ? args : new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public int getNumArgs() {
            return numArgs;
        }

        public List<String> getArgs() {
            return args;
        }

        @Override
        public String toString() {
            return "FunctionRegister{" +
                    "name='" + name + '\'' +
                    ", numArgs=" + numArgs +
                    ", args=" + args +
                    '}';
        }

        public DataType getReturnType() {
            return returnType;
        }
        public Collection<Object> getParameters() {
            return List.of();
        }
    }
}
