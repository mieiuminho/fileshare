package util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class Parse {

    private Parse() {
    }

    /**
     * Function that saves a object in serialize form to a file.
     *
     * @param object to keep
     * @param file where to keep
     * @throws IOException
     */
    public static void saveObject(final Object object, final String file) throws IOException {
        new ObjectOutputStream(new FileOutputStream(file)).writeObject(object);
    }

    /**
     * Function that loads an object from a file.
     *
     * @param file from where should load the object
     * @return Object loaded
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Object loadObject(final String file) throws ClassNotFoundException, IOException {
        return new ObjectInputStream(new FileInputStream(file)).readObject();
    }

    /**
     * Function that reads all lines from a file.
     *
     * @param file from where should read
     * @return List of Strings
     */
    public static List<String> readFile(final String file) {
        List<String> linhas = new ArrayList<>();
        BufferedReader inFile;
        String linha;

        try {
            inFile = new BufferedReader(new FileReader(file));
            while ((linha = inFile.readLine()) != null) {
                linhas.add(linha);
            }
        } catch (IOException exc) {
            System.out.println(exc);
        }

        return linhas;
    }

}
