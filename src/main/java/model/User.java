package model;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Random;

public final class User implements Serializable {

    @SuppressWarnings("checkstyle:MagicNumber")
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

    @SuppressWarnings("checkstyle:MagicNumber")
    public static String generateHashingSeed() {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < 4; i++) {
            Random r = new Random();
            char c = (char) (r.nextInt(26) + 'a');
            sb.insert(i, c);
        }
        return sb.toString();
    }

    private String username;
    private String password;
    private String seed;

    public User(final String username, final String password) {
        this.username = username;
        this.seed = User.generateHashingSeed();
        this.password = User.encryptPassword(this.seed + password);
    }

    public User(final String username, final String hashedPassword, final String seed) {
        this.username = username;
        this.password = hashedPassword;
        this.seed = seed;
    }

    public boolean validate(final String input) {
        return User.encryptPassword(this.seed + input).equals(this.password);
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
        this.password = User.encryptPassword(this.seed + password);
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
