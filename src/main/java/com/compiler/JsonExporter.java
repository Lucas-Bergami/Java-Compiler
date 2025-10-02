package com.compiler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Vector;
import java.io.FileWriter;
import java.io.IOException;

public class JsonExporter {

  private final Gson gson;

  public JsonExporter() {
    this.gson = new GsonBuilder().setPrettyPrinting().create();
  }

  public String export(Vector<Token> tokens) {
    return gson.toJson(tokens);
  }

  public void exportToConsole(Vector<Token> tokens) {
    System.out.println(export(tokens));
  }

  public void exportToFile(Vector<Token> tokens, String filePath) {
    try (FileWriter writer = new FileWriter(filePath)) {
      gson.toJson(tokens, writer);
      System.out.println("JSON salvo em: " + filePath);
    } catch (IOException e) {
      System.err.println("Erro ao salvar JSON: " + e.getMessage());
    }
  }
}
