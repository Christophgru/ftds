package org.pgi.paxoscoin.events;

import java.time.Instant;
import java.util.Optional;

import org.pgi.paxoscoin.worldmodel.Terminal;
import org.pgi.paxoscoin.worldmodel.Employee;
import org.pgi.paxoscoin.banking.TransactionType;

public class ChangedBalanceEvent implements Event {
    private TransactionType transactionType;
    private Employee employee;
    private Terminal terminal;
    private double amount;
    private Instant time;

    @Override
    public Instant getTime() {
        return this.time;
    }



    public ChangedBalanceEvent (TransactionType transactiontype, Employee employee, Terminal terminal, double amount, Instant time) {
        this.transactionType = transactiontype;
        this.employee = employee;
        this.terminal = terminal;
        this.amount = amount;
        this.time = time;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public double getAmount() {
        return amount;
    }
}