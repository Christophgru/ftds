package org.pgi.paxoscoin.worldmodel;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import org.pgi.paxoscoin.banking.BankingBackend;
import org.pgi.paxoscoin.commands.ReadCardCommand;
import org.pgi.paxoscoin.exceptions.UnsupportedCommandException;

public class Terminal implements Serializable {

    private static final long serialVersionUID = 9189451734282258000L;

    /**
     * The UUID of this Terminal
     */
    private UUID id;
    
    /**
     * The location of this terminal - just a String like "Mensa A"
     */
    private String location;

    /**
     * Create a Terminal (RfidCard Reader)
     * @param id The UUID of this Terminal
     * @param location The location of this terminal - just a String like "Mensa A"
     * @param backend A reference to the Banking Backend, since terminals are connected to our backend and have to dispatch events upon reading cards
     */
    public Terminal(UUID id, String location, BankingBackend backend) {
        this.id = id;
        this.location = location;
    }
    
    /**
     * @return The UUID of this terminal
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * @return The location (a string representation like "Mensa A") of this Terminal
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Simulates reading of a card.
     * 
     * @param card The RfidCard being read
     * @param time The time the card was read
     * @param amount The amount of PaxosCoins involved in this transaction
     */
    public void readCard(Card card, Instant time, double amount) {
        ReadCardCommand command = new ReadCardCommand(this, card, time, amount);
        try {
            BankingBackend.getInstance().handleCommand(command);
        } catch (UnsupportedCommandException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Terminal{" +
                "id=" + id +
                ", location='" + location + '\'' +
                '}';
    }
}
