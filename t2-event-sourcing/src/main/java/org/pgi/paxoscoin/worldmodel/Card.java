package org.pgi.paxoscoin.worldmodel;

import java.util.UUID;

/**
 * The card class represents a physical NFC card that was issued to an employee.
 */
public class Card {
    private UUID cardId;
    private Employee employee;

    /**
     * Instantiates a new card.
     *
     * @param cardId   the unique identifier of the card
     * @param employee the employee to which that card was issued to
     */
    public Card(UUID cardId, Employee employee) {
        this.cardId = cardId;
        this.employee = employee;
    }

    /**
     * Gets the unique identifier of the card.
     *
     * @return the unique identifier of the card
     */
    public UUID getCardId() {
        return cardId;
    }

    /**
     * Gets the employee.to which the card was issued to
     *
     * @return the employee to which the card was issued to
     */
    public Employee getEmployee() {
        return employee;
    }

    @Override
    public String toString() {
        return "Card{" +
                "cardId=" + cardId +
                ", employeeId=" + employee.getId() +
                '}';
    }
}
