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
        System.out.println(Color.ANSI_RED + "Error: " + Color.ANSI_RESET + error);
    }

    public static void info(final String info) {
        System.out.println(Color.ANSI_BLUE + "$> " + Color.ANSI_RESET + info);
    }

    public static void response(final String message) {
        String[] lines = message.split(";");
        for (String line : lines) {
            System.out.println(Color.ANSI_GREEN + line + Color.ANSI_RESET);
        }
    }

    public static void response(final String title, final String[] content) {
        System.out.println(Color.ANSI_BLUE + title + Color.ANSI_RESET);
        for (String line : content) {
            System.out.println(line);
        }
    }

    public static void notification(final String message) {
        String[] lines = message.split(";");
        for (String line : lines) {
            System.out.println(Color.ANSI_PURPLE + line + Color.ANSI_RESET);
        }
    }

}
