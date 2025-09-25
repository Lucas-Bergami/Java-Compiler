import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class main {
    public static void main(String[] args) {
        String fileName = "example.txt";

        Vector<Character> characters = new Vector<>();

        try (FileReader reader = new FileReader(fileName)) {
            int c;
            while ((c = reader.read()) != -1) {
                characters.add((char) c);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }

        System.out.println("Caracteres lidos do arquivo:");
        for (Character ch : characters) {
            System.out.print(ch);
        }
    }
}
