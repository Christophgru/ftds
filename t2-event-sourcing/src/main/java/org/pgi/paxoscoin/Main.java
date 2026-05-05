package org.pgi.paxoscoin;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.pgi.paxoscoin.commands.Command;
import org.pgi.paxoscoin.commands.PayWageCommand;
import org.pgi.paxoscoin.commands.ReadCardCommand;
import org.pgi.paxoscoin.worldmodel.Card;
import org.pgi.paxoscoin.worldmodel.Employee;
import org.pgi.paxoscoin.worldmodel.Terminal;

public class Main {
    public static void main(String[] args) {
        Map<UUID, Card> cards = new CSVReader<Card>(Card.class).readFileAsMap("csv/cards.csv");
        Map<UUID, Employee> employees = new CSVReader<Employee>(Employee.class).readFileAsMapWithDependencies("csv/employees.csv", cards);
        Map<UUID, Terminal> terminals = new CSVReader<Terminal>(Terminal.class).readFileAsMap("csv/terminals.csv");

        List<Command> commands = new LinkedList<>();
        // parse "read card commands"
        commands.addAll(new CSVReader<ReadCardCommand>(ReadCardCommand.class).readFileAsListWithDependencies("csv/commands.csv", cards, employees, terminals));
        // parse "pay wage commands"
        commands.addAll(new CSVReader<PayWageCommand>(PayWageCommand.class).readFileAsListWithDependencies("csv/paywage.csv", cards, employees, terminals));
        // order commands by time
        commands.sort(Comparator.comparing(Command::getTime));

        // start simulation
        PaxosIncSim simulation = new PaxosIncSim(commands, cards, employees, terminals);
        simulation.run();
    }
}