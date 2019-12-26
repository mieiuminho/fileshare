package view;

import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class Terminal {

    private static final String LANG = System.getenv("FILESHARE_LANG");
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages", new Locale(LANG));

    private static ConsoleReader console;

    static {
        try {
            console = new ConsoleReader();
        } catch (IOException e) {
            Terminal.error(e.getMessage());
        }
    }

    private Terminal() {
    }

    public static String input() throws IOException {
        return console.readLine();
    }

    public static void show(final String text, final String color) {
        try {
            console.println(color + text + Color.ANSI_RESET);
            console.flush();
        } catch (IOException e) {
            Terminal.error(e.getMessage());
        }
    }

    public static void show(final String text, final String color, final String pretext) {
        try {
            console.println(color + pretext + Color.ANSI_WHITE + text + Color.ANSI_RESET);
            console.flush();
        } catch (IOException e) {
            Terminal.error(e.getMessage());
        }
    }

    public static void show(final List<String> text) {
        for (String line : text) {
            Terminal.show(line, Color.ANSI_WHITE);
        }
    }

    public static void clear() {
        try {
            console.clearScreen();
        } catch (IOException e) {
            Terminal.error(e.getMessage());
        }
    }

    public static void welcome(final List<String> logo) {
        Terminal.clear();
        for (String line : logo) {
            Terminal.show(line, Color.ANSI_YELLOW);
        }
        Terminal.message("welcome");
    }

    public static void message(final String key) {
        Terminal.show(MESSAGES.getString(key), Color.ANSI_WHITE);
    }

    public static void error(final String error) {
        Terminal.show(error, Color.ANSI_RED, "Error: ");
    }

    public static void info(final String info) {
        Terminal.show(info, Color.ANSI_BLUE, "$ ");
    }

    public static void response(final String message) {
        String[] lines = message.split(";");
        for (String line : lines) {
            Terminal.show(line, Color.ANSI_GREEN);
        }
    }

    public static void response(final String title, final String[] content) {
        Terminal.show(title, Color.ANSI_BLUE);
        for (String line : content) {
            Terminal.show(line, Color.ANSI_WHITE);
        }
    }

    public static void notification(final String message) {
        String[] lines = message.split(";");
        for (String line : lines) {
            Terminal.show(line, Color.ANSI_PURPLE);
        }
    }
}
