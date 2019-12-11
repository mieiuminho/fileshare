package client;

import java.io.PrintWriter;

public interface ClientCommand {
    void execute(String[] argv, PrintWriter out);
}
