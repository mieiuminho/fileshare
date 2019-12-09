package client;

import view.Terminal;

import java.io.BufferedReader;

public final class ReplyHandler implements Runnable {

    private BufferedReader in;
    private boolean stopFlag;

    public ReplyHandler(final BufferedReader in) {
        this.in = in;
        this.stopFlag = false;
    }

    public void stop() {
        this.stopFlag = true;
    }

    public void run() {
        try {
            while (!stopFlag) {
                String reply = in.readLine();
                String[] content = reply.split(":");
                if (content.length == 2) {
                    switch (content[0].toUpperCase()) {
                        case "ERROR":
                            Terminal.error(content[1]);
                            break;
                        case "REPLY":
                            Terminal.response(content[1]);
                            break;
                        default:
                            Terminal.response(content[0], content[1]);
                    }
                } else {
                    Terminal.response(content[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
