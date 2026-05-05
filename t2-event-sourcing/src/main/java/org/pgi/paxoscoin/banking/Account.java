package org.pgi.paxoscoin.banking;

import org.pgi.paxoscoin.worldmodel.Employee;

/**
 * The account class that represents an employee's internal banking account at Paxos Global Inc.
 */
public class Account {
    private Employee employee;

    private double balance;

    /**
     * Instantiates a new account.
     *
     * @param employee the employee who owns the account
     * @param balance  the current balance, i.e. the amount of money that is currently stored on the account
     */
    public Account(Employee employee, double balance) {
        this.employee = employee;
        this.balance = balance;
    }

    /**
     * Gets the employee who owns this banking account.
     *
     * @return the owning employee
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * Gets the current balance, i.e. the amount of money that is currently stored on the account.
     *
     * @return the current balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Deposit an amount to the banking account, i.e. adds the specified amount to the current balance.
     *
     * @param amount the amount that is being deposited
     */
    public void deposit(double amount) {
        balance += amount;
    }

    /**
     * Withdraw an amount from the banking account, i.e. subtracts the specified amount from the current balance.
     *
     * @param amount the amount that is being withdrawn
     */
    public void withdraw(double amount) {
        balance -= amount;
    }

    @Override
    public String toString() {
        return "Account{employeeId=" + employee.getId() +
                ", balance=" + balance +
                '}';
    }
}
