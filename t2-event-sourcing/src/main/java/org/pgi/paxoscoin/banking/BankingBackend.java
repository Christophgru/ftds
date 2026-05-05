package org.pgi.paxoscoin.banking;

import org.pgi.paxoscoin.commands.Command;
import org.pgi.paxoscoin.commands.PayWageCommand;
import org.pgi.paxoscoin.commands.ReadCardCommand;
import org.pgi.paxoscoin.exceptions.UnsupportedCommandException;


/**
 * The banking backend.
 */
public class BankingBackend {
    private static BankingBackend instance;
    
    private double globalBalance;


    /**
     * Private constructor to allow instantiation only through {@link #getInstance()}
     */
    private BankingBackend() {
        // can only be instantiated through {@link #getInstance()}
        this.globalBalance = 0d;

    }

    /**
     * Method to retrieve the banking singleton backend instance.
     *
     * @return instance The banking backend instance
     */
    public static BankingBackend getInstance() {
        if (BankingBackend.instance == null) {
            instance = new BankingBackend();
        }
        return instance;
    }

    /**
     * Handle a command (e.g. a card that is read or a wage that is payed).
     *
     * @param command The command issued to the backend
     * @throws UnsupportedCommandException in case the command is unrecognized
     */
    public void handleCommand(Command command) throws UnsupportedCommandException {
        
        if(command instanceof PayWageCommand) {
            PayWageCommand pwcommand = (PayWageCommand) command;
            pwcommand.getEmployee().getAccount().deposit(pwcommand.getAmount());
            this.globalBalance += pwcommand.getAmount();

            // TODO: create ChangedBalanceEvent

        } else if(command instanceof ReadCardCommand) {
            ReadCardCommand rccommand = (ReadCardCommand) command;
            // reject any read card commands, if the current account balance is not sufficing
            if (rccommand.getAmount() > rccommand.getCard().getEmployee().getAccount().getBalance()) {
                System.err.println(rccommand.getCard().getEmployee().getName() + " just tried to overdraw their account!");
                return;
            }
            rccommand.getCard().getEmployee().getAccount().withdraw(rccommand.getAmount());
            this.globalBalance -= rccommand.getAmount();
            // TODO: create ChagedBalanceEvent



        } else {
            throw new UnsupportedCommandException();
        }
        
    }

    /**
     * Gets the current global balance.
     *
     * @return the current global balance
     */
    public double getGlobalBalance() {
        return globalBalance;
    }

    /**
     * Sets the current global balance.
     */
    public void setGlobalBalance(double globalBalance) {
        this.globalBalance = globalBalance;
    }

    /**
     * Saves a checkpoint of the system state (i.e. the accounts and the current global balance) to the hard drive
     */
    public void saveCheckpoint() {
        // TODO: save a current checkpoint of all accounts to a file
        // the choice of format is completely up to you,
        // but has to match the restore logic in the restoreCheckpoint method
    }

    /**
     * Restores a checkpoint that was saved with {@link #saveCheckpoint()}
     */
    public void restoreCheckpoint() {
        // TODO: restore the last saved checkpoint
        throw new RuntimeException("Restore is not implemented!");
    }
}
