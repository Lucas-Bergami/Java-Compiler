package com.compiler;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.compiler.SyntacticAnalyzer.Aux;

import static com.compiler.AstNode.printAst;

public class Main {
    public static void main(String[] args) {

        String fileName = "example.txt";

        if (args.length > 0) {
            fileName = args[0];
        }

        // === 1️⃣ Leitura do arquivo e análise léxica ===
        Vector<Character> characters = ReadFileToVector.readFileToVector(fileName);

        LexicalAnalyser lexical = new LexicalAnalyser();
        Map<String, Vector<Token>> result = lexical.analyse(characters);

        JsonExporter exporter = new JsonExporter();
        exporter.exportToFile(result.get("tokens"), "tokens.json");
        exporter.exportToFile(result.get("lexical-errors"), "errors.json");

        Vector<Token> tokens = result.get("tokens");

        if (tokens == null || tokens.isEmpty()) {
            System.out.println("Nenhum token válido encontrado. Encerrando.");
            return;
        }

        // === 2️⃣ Análise sintática ===
        SyntacticAnalyzer syntactic = new SyntacticAnalyzer();
        Aux aux = syntactic.analyse(tokens);   // Agora deve retornar a AST corretamente


        if (aux == null) {
            System.out.println("A AST não foi construída. Análise semântica cancelada.");
            return;
        }

        System.out.println("=== Árvore de Sintaxe Abstrata (AST) ===");
        syntactic.saveOutputs();
        // === 3️⃣ Análise semântica ===
            // === 3️⃣ Análise semântica ===
        SemanticAnalyzer semantic = new SemanticAnalyzer(aux);  // ⚡ Passa o Aux aqui
        List<String> semanticErrors = semantic.analyze();        // ⚡ Executa a análise

        if (!semanticErrors.isEmpty()) {
            System.out.println("Erros semânticos encontrados:");
            semanticErrors.forEach(System.out::println);
        } else {
            System.out.println("Análise semântica concluída com sucesso!");
        }

        System.out.println("Análise semântica concluída com sucesso!");
        System.out.println("Arquivos gerados: tokens.json, errors.json, erros_sintaticos.json, tabelas_de_simbolos.json");
    }
}
