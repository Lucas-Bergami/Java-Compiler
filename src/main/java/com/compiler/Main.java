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

    characters = ReadFileToVector.readFileToVector(fileName);
    LexicalAnalyser lexical = new LexicalAnalyser();
    result = lexical.analyse(characters);

    JsonExporter exporter = new JsonExporter();

    // Exporta tokens válidos
    exporter.exportToFile(result.get("tokens"), "tokens.json");

    // Exporta tokens de erro
    exporter.exportToFile(result.get("errors"), "errors.json");
  }
}
