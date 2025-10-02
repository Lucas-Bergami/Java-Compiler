package com.compiler;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import com.compiler.Token;
import com.compiler.ReadFileToVector;
import com.compiler.LexicalAnalyser;
import com.compiler.JsonExporter;

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
