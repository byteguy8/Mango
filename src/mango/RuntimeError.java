package mango;
public class RuntimeError extends RuntimeException {
    public RuntimeError() {
        super(null, null, false, false);
    }
}