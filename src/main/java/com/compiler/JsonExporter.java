package com.compiler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class JsonExporter {

    private final Gson gson;

    public JsonExporter() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public <T> String export(Vector<T> data) {
        return gson.toJson(data);
    }

    public <T> void exportToConsole(Vector<T> data) {
        System.out.println(export(data));
    }

    public <T> void exportToFile(Vector<T> data, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
            System.out.println("JSON salvo em: " + filePath);
        } catch (IOException e) {
            System.err.println("Erro ao salvar JSON: " + e.getMessage());
        }
    }
}
