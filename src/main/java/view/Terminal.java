package view;

import java.util.List;

public final class Terminal {
    private Terminal() {
    }

    public static void show(final String text) {
        System.out.println(text);
    }

    public static void show(final List<String> text) {
        for (String line : text) {
            System.out.println(line);
        }
    }

    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void error(final String error) {
        System.out.println(Color.ANSI_RED + "\nError: " + Color.ANSI_RESET + error);
    }

    public static void info(final String info) {
        System.out.println(Color.ANSI_BLUE + "$> " + Color.ANSI_RESET + info);
    }
}
