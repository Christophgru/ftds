package org.pgi.paxoscoin.commands;

import java.time.Instant;

import org.pgi.paxoscoin.Util;
import org.pgi.paxoscoin.banking.BankingBackend;
import org.pgi.paxoscoin.worldmodel.Employee;


public class PayWageCommand implements Command {

    private Employee employee;
    private Instant time;
    private double amount;
    
    public PayWageCommand(Employee employee, Instant time, double amount) {
        this.employee = employee;
        this.time = time;
        this.amount = amount;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Instant getTime() {
        return time;
    }

    public double getAmount() {
        return amount;
    }
    
    @Override
    public String toString() {
        return "PayWageCommand{" +
            "employeeId=" + employee.getId().toString() +
            ", time='" + time.toString() +
            ", amount='" + amount + '\'' +
            "} :: globalBalance=" + Util.formatCurrency(BankingBackend.getInstance().getGlobalBalance());
    }
}
