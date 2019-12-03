package server;

import model.FileShare;

import java.io.PrintWriter;

public interface Command {
    void execute(String[] argv, PrintWriter out, FileShare model);
}
