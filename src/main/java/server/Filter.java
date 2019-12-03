package server;

import java.io.PrintWriter;

public interface Filter {
    void execute(String[] argv, PrintWriter out);
}
