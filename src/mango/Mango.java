package mango;

import mango.statement.Statement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Mango {
    private static boolean error = false;

    public static void error(int line, String message) {
        error = true;
        System.err.printf("ERROR [ln: %d]: %s\n", line, message);
    }

    public static void error(Token token, String message) {
        error(token.line, message);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("No source file provided");
            System.exit(2);
        }

        byte[] rawSource = Files.readAllBytes(Paths.get(args[0]));
        String source = new String(rawSource);

        Scanner scanner = new Scanner();
        List<Token> tokens = scanner.scan(source);

        if (error)
            System.exit(3);

        Parser parser = new Parser();
        List<Statement> statements = parser.parse(tokens);

        if (error)
            System.exit(5);

        Interpreter interpreter = new Interpreter();
        interpreter.execute(statements);
    }
}