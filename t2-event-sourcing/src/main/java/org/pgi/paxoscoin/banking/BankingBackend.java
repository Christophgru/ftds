package org.pgi.paxoscoin.banking;

import org.pgi.paxoscoin.commands.Command;
import org.pgi.paxoscoin.commands.PayWageCommand;
import org.pgi.paxoscoin.commands.ReadCardCommand;
import org.pgi.paxoscoin.exceptions.UnsupportedCommandException;
import org.pgi.paxoscoin.worldmodel.Card;
import org.pgi.paxoscoin.worldmodel.Employee;

import java.io.*;
import java.util.*;


/**
 * The banking backend.
 */
public class BankingBackend {
    private static BankingBackend instance;
    //sum of all known accounts
    private double globalBalance;
    // Folder and file path
    private static final String FOLDER_NAME = "backup";
    private static final String FILE_NAME = "number.txt";
    HashMap<UUID,Account> accounts = new HashMap<>();


    /**
     * Private constructor to allow instantiation only through {@link #getInstance()}
     */
    private BankingBackend() {
        // can only be instantiated through {@link #getInstance()}
        this.globalBalance = 0d;
        System.out.println("banking backend created");

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

            UUID id = pwcommand.getEmployee().getId();
            //in case the user doesnt exist in banking backend, create a new account
            Account acc = accounts.getOrDefault(id, new Account(pwcommand.getEmployee(),0.0));
            acc.deposit(pwcommand.getAmount());
            accounts.put(acc.getEmployee().getId(),acc);
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

            UUID id = rccommand.getCard().getEmployee().getAccount().getEmployee().getId();
            Account acc = accounts.getOrDefault(id, new Account(rccommand.getCard().getEmployee(),0.0));
            acc.withdraw(rccommand.getAmount());
            accounts.put(acc.getEmployee().getId(),acc);


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
        try {
            // Create folder if it doesn't exist
            File folder = new File(FOLDER_NAME);
            if (!folder.exists()) {
                folder.mkdir();
            }

            // Create file inside folder
            File file = new File(folder, FILE_NAME);

            // FileWriter without 'true' overwrites previous content
            FileWriter writer = new FileWriter(file);   // save global balance
            writer.write("GLOBAL=" + this.globalBalance + "\n");

            // save accounts
            for (Map.Entry<UUID, Account> acc : accounts.entrySet()) {
                Employee e=acc.getValue().getEmployee();
                Card c=acc.getValue().getEmployee().getCard();
                writer.write(
                         e.getId().toString() + "="+acc.getValue().getBalance()+"="+ e.getName()+ "="+
                                c.getCardId().toString()+"\n"
                );
            }

            writer.close();

            System.out.println("Number written successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restores a checkpoint that was saved with {@link #saveCheckpoint()}
     */
    public void restoreCheckpoint() {
        double globalBalance = -1;


        try {
            File file = new File(FOLDER_NAME, FILE_NAME);

            BufferedReader reader = new BufferedReader(new FileReader(file));

            accounts.clear();

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("=");

                if (parts[0].equals("GLOBAL")) {

                    globalBalance = Double.parseDouble(parts[1]);

                } else {

                    UUID employee_id=UUID.fromString(parts[0]);
                    double balance=Double.parseDouble(parts[1]);
                    String name = parts[2];
                    UUID cardId=UUID.fromString(parts[3]);

                    Employee employee=new Employee(employee_id,name,null,null);
                    Card card=new Card(cardId,employee);
                    Account acc = new Account(employee,balance);
                    employee.setAccount(acc);
                    employee.setCard(card);

                    accounts.put(employee_id, acc);
                }
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(globalBalance!=1){
            setGlobalBalance(globalBalance);
        }

    }
}
