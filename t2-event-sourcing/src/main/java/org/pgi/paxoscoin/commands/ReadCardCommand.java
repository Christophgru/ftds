package org.pgi.paxoscoin.commands;

import java.time.Instant;

import org.pgi.paxoscoin.Util;
import org.pgi.paxoscoin.banking.BankingBackend;
import org.pgi.paxoscoin.worldmodel.Card;
import org.pgi.paxoscoin.worldmodel.Terminal;


public class ReadCardCommand implements Command {

    private Card card;
    private Terminal terminal;
    private Instant time;
    private double amount;
    
    public ReadCardCommand(Terminal terminal, Card card, Instant time, double amount) {
        this.terminal = terminal;
        this.card = card;
        this.time = time;
        this.amount = amount;
    }

    public Card getCard() {
        return card;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public Instant getTime() {
        return time;
    }

    public double getAmount() {
        return amount;
    }
    
    @Override
    public String toString() {
        return "ReadCardCommand{" +
            "cardId=" + card.getCardId().toString() +
            ", employee=" + card.getEmployee().getName() +
            ", terminal='" + terminal.getLocation() +
            ", time='" + time.toString() +
            ", amount='" + amount + '\'' +
            "} :: globalBalance=" + Util.formatCurrency(BankingBackend.getInstance().getGlobalBalance());
    }
}
