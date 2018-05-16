package de.tinf15b4.ihatestau.util;

public class ExceptionUtil {
    private ExceptionUtil() {}

    public static void throwUnchecked(Exception e) {
        doThrow(e);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void doThrow(Throwable t) throws T {
        throw (T) t;
    }
}
