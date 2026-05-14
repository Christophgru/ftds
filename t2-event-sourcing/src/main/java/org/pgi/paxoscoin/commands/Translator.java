package org.pgi.paxoscoin.commands;

import org.pgi.paxoscoin.CSVReader;
import org.pgi.paxoscoin.worldmodel.Card;
import org.pgi.paxoscoin.worldmodel.Employee;
import org.pgi.paxoscoin.worldmodel.Terminal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Translator {
    private Map<UUID, Card> cards;
    private Map<UUID, Employee> employees;
    private Map<UUID, Terminal> terminals;

    public Translator(Map<UUID, Card> card, Map<UUID, Employee> employee, Map<UUID, Terminal> terminal) {
        this.cards = card;
        this.employees = employee;
        this.terminals = terminal;
    }

    public void translate (String origin, String destination) {
        List<Command> commands = new LinkedList<>();
        // parse "read card commands"
        commands.addAll(new CSVReader<ReadCardCommand>(ReadCardCommand.class).readFileAsListWithDependencies(origin, cards, employees, terminals));
        // order commands by time
        commands.sort(Comparator.comparing(Command::getTime));

        // Create file inside folder
        File file = new File(destination);

        // FileWriter without 'true' overwrites previous content
        FileWriter writer = null;   // save global balance
        try {
            writer = new FileWriter(file);
            writer.write("terminal;card;timestamp;amount\n");
            for  (Command command : commands) {
                if(command instanceof ReadCardCommand) {
                    if(((ReadCardCommand) command).getAmount() <0) {
                        command = new ReadCardCommand(((ReadCardCommand) command).getTerminal(), ((ReadCardCommand) command).getCard(), command.getTime(), - ((ReadCardCommand) command).getAmount());
                    }
                    writer.write(((ReadCardCommand) command).toCSV());
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
