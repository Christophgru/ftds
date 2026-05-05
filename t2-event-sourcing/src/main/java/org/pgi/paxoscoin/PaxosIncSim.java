package org.pgi.paxoscoin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.pgi.paxoscoin.banking.BankingBackend;
import org.pgi.paxoscoin.commands.Command;
import org.pgi.paxoscoin.exceptions.UnsupportedCommandException;
import org.pgi.paxoscoin.worldmodel.Card;
import org.pgi.paxoscoin.worldmodel.Employee;
import org.pgi.paxoscoin.worldmodel.Terminal;

public class PaxosIncSim implements Runnable {
    public static List<Command> commands;
    public static Map<UUID, Card> cards;
    public static Map<UUID, Employee> employees;
    public static Map<UUID, Terminal> terminals;

    public PaxosIncSim(List<Command> commands, Map<UUID, Card> cards, Map<UUID, Employee> employees, Map<UUID, Terminal> terminals) {
        PaxosIncSim.commands = commands;
        PaxosIncSim.cards = cards;
        PaxosIncSim.employees = employees;
        PaxosIncSim.terminals = terminals;
    }

    @Override
    public void run() {
        // log commands
        int i = 0;
        for (Command command: commands) {
            i += 1;

            // save a checkpoint every 420th event
            if (i % 420 == 0) {
                BankingBackend.getInstance().saveCheckpoint();
            }

            // after 30k commands the system is suddenly cut from power and loses all state
            if (i == 30000) {
                powerloss();
                restoreAfterPowerloss();
            }

            // System.out.print(i + " :: ");
            // System.out.println(command);

            // let banking backend handle the incoming command
            try {
                BankingBackend.getInstance().handleCommand(command);
            } catch (UnsupportedCommandException e) {
                e.printStackTrace();
            }
        }
        System.err.println("Simulation finished; global balance=" + Util.formatCurrency(BankingBackend.getInstance().getGlobalBalance()));
    }

    /**
     * Simulates a power loss of the system, which leads to a loss of the application state.
     */
    private void powerloss() {
        System.err.println("FATAL ERROR: power loss detected; global balance=" + Util.formatCurrency(BankingBackend.getInstance().getGlobalBalance()));

        // forget the current global balance
        BankingBackend.getInstance().setGlobalBalance(0.0);

        // all accounts are discarded in case of a powerloss, since they are only held in memory
        employees.entrySet().stream().forEach(entry -> entry.getValue().setAccount(null));

        System.err.println("Shutting down...");
    }

    /**
     * Simulates the reboot process after a power loss, which should restore the last saved state.
     */
    private void restoreAfterPowerloss() {
        System.err.println("Restarting systems...");

        System.err.println("Restoring last saved system state...");
        BankingBackend.getInstance().restoreCheckpoint();

        System.err.println("Restore successful; global balance=" + Util.formatCurrency(BankingBackend.getInstance().getGlobalBalance()));
        System.err.println("Continuing normal operation.");
    }
}
