package network.discov.core.commons;

import java.io.*;

public class DataUtil {
    public static <T> void saveObjectToPath(T objectToSave, String pathToSaveTo) {
        try {
            File file = new File(pathToSaveTo);
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
            if(!file.exists()) file.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(objectToSave);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T loadObjectFromPath(String pathToLoadFrom) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pathToLoadFrom));
            T result = (T) ois.readObject();
            ois.close();
            return result;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

