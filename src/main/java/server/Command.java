package server;

public enum Command {
    REGISTER, LOGIN, UPLOAD, DOWNLOAD, SEARCH, UNKNOWN;

    public static Command getValue(final String key) {
        switch (key.toLowerCase()) {
            case "register" : {
                return REGISTER;
            }
            case "login" : {
                return LOGIN;
            }
            case "upload" : {
                return UPLOAD;
            }
            case "download" : {
                return DOWNLOAD;
            }
            case "search" : {
                return SEARCH;
            }
            default : {
                return UNKNOWN;
            }

        }
    }
}
