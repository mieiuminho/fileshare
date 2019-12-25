package util;

import java.io.PrintWriter;

public interface Command {
    void execute(String[] argv, PrintWriter out);
}
