package server;

import model.FileShare;

import java.io.PrintWriter;

public interface Command {
    void run(String[] argv, PrintWriter out, FileShare model);
}
