package com.compiler;

import java.util.Vector;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String fileName = "example.txt";
        Vector<Character> characters;
        Map<String, Vector<Token>> result;

        if (args.length > 0) {
            fileName = args[0];
        }

        // === 1️⃣ Leitura e análise léxica ===
        characters = ReadFileToVector.readFileToVector(fileName);
        LexicalAnalyser lexical = new LexicalAnalyser();
        result = lexical.analyse(characters);

        JsonExporter exporter = new JsonExporter();
        // Exporta tokens válidos
        exporter.exportToFile(result.get("tokens"), "tokens.json");

        // Exporta tokens de erro
        exporter.exportToFile(result.get("lexical-errors"), "errors.json");

        // === 2️⃣ Análise sintática ===
        Vector<Token> tokens = result.get("tokens");

        if (tokens == null || tokens.isEmpty()) {
            System.out.println("Nenhum token válido encontrado. Encerrando.");
            return;
        }

        SyntacticAnalyzer syntactic = new SyntacticAnalyzer();
        syntactic.analyse(tokens);

        // === 3️⃣ Salvar resultados da análise sintática ===
        syntactic.saveOutputs();

        System.out.println("Análise sintática concluída!");
        System.out.println("Arquivos gerados: erros_sintaticos.txt e tabelas_de_simbolos.txt");
    }
}
