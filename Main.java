import java.util.Vector;

public class Main {
  public static void main(String[] args) {
    String fileName = "example.txt";
		Vector<Character> characters;
		Vector<Token> tokens;
    if (args.length > 0) {
      fileName = args[0];
    }

    characters = ReadFileToVector.readFileToVector(fileName);
		LexicalAnalyser lexical = new LexicalAnalyser();
		tokens = lexical.analyse(characters);

		for(Token token : tokens){
			System.out.println(token);
		}

  }

}
