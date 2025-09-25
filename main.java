public class Main {
    public static void main(String[] args) {
        String fileName = "example.txt";

        if (args.length > 0) {
            fileName = args[0];
        }

        ReadFileToVector.readFileToVector(fileName);
    }
}
