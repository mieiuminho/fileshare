package model;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class User {

    @SuppressWarnings({ "checkstyle:MagicNumber" })
    public static String encryptPassword(final String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String ret = no.toString(16);
            while (ret.length() < 32) {
                ret = "0" + ret;
            }
            return ret;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown for incorrect algorithm " + e);
            return null;
        }
    }

    private String username;
    private String password;

    public User(final String username, final String password) {
        this.username = username;
        this.password = User.encryptPassword(password);
    }

    public boolean validate(final String input) {
        return User.encryptPassword(input).equals(this.password);
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = User.encryptPassword(password);
    }

    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return this.username == user.getUsername() && this.password == user.getPassword();
    }

    public int hashCode() {
        return Objects.hash(username, password);
    }
}
