package mango;

public class Utils {
    public static String stringify(Object value) {
        if (value == null)
            return "NIL";

        if (value instanceof MangoCallable)
            return ((MangoCallable) value).stringify();

        return value.toString();
    }
}