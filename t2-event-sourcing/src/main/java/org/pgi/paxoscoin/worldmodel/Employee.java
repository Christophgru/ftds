package org.pgi.paxoscoin.worldmodel;

import java.io.Serializable;
import java.util.UUID;

import org.pgi.paxoscoin.banking.Account;

/**
 * The employee class represents an employee record of the Paxos Global Inc.
 */
public class Employee implements Serializable {
    
    private static final long serialVersionUID = 5234445491333520794L;
    
    private UUID id;
    private String name;

    private Card card;
    private Account account;

    /**
     * Instantiates a new employee.
     *
     * @param id      the unique identifier of the employee
     * @param name    the full name of the employee
     * @param account the account that is linked to the employee
     * @param card    the card that was issued to the employee
     */
    public Employee(UUID id, String name, Card card, Account account) {
        this.id = id;
        this.name = name;
        this.card = card;
        this.account = account;
    }

    /**
     * Gets the unique identifier of the employee.
     *
     * @return the unique identifier of the employee
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the full name of the employee.
     *
     * @return the full name of the employee
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the card that was issued to the employee.
     *
     * @return the card that was issued to the employee.
     */
    public Card getCard() {
        return card;
    }

    /**
     * Gets the account that is linked to the employee.
     *
     * @return the account that is linked to the employee
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Sets the account that is linked to the employee.
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", card=" + card +
                ", account=" + account +
                '}';
    }
}
