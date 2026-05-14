package org.pgi.paxoscoin.events;

import org.pgi.paxoscoin.worldmodel.Card;
import org.pgi.paxoscoin.worldmodel.Employee;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

public class EventLog {
    private static final String FOLDER_NAME = "backup/";
    private String FILE_NAME = "events.txt";
    public EventLog() {
        //clear file
        Path file = Paths.get(this.FOLDER_NAME + this.FILE_NAME);
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void persist(Event evt) {

        File file = new File(this.FOLDER_NAME + this.FILE_NAME);

        try (
                FileOutputStream fileOutputStream =
                        new FileOutputStream(file, true);

                ObjectOutputStream objectOutputStream =
                        (file.exists() && file.length() > 0)
                                ? new AppendableObjectOutputStream(fileOutputStream)
                                : new ObjectOutputStream(fileOutputStream)
        ) {

            objectOutputStream.writeObject(evt);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Vector<Event> restoreEvent() {
        Vector<Event> list = new Vector<>();

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(this.FOLDER_NAME + this.FILE_NAME))) {

            while (true) {
                Event obj = (Event) in.readObject();
                list.add(obj);
            }

        } catch (EOFException e) {
            // end of file reached
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        Path file = Paths.get(this.FOLDER_NAME + this.FILE_NAME);
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
