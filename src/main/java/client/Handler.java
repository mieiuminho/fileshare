package client;

import java.io.PrintWriter;

public interface Handler {
    void execute(String[] argv, PrintWriter out);
}
