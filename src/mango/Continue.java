package mango;

public class Continue extends RuntimeException {
    public Continue() {
        super(null, null, false, false);
    }
}