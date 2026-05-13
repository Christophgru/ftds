package org.pgi.paxoscoin.banking;

import org.pgi.paxoscoin.commands.Command;
import org.pgi.paxoscoin.commands.PayWageCommand;
import org.pgi.paxoscoin.commands.ReadCardCommand;
import org.pgi.paxoscoin.exceptions.UnsupportedCommandException;
import org.pgi.paxoscoin.worldmodel.Card;
import org.pgi.paxoscoin.worldmodel.Employee;
import org.pgi.paxoscoin.events.ChangedBalanceEvent;
import org.pgi.paxoscoin.banking.TransactionType;

import java.io.*;
import java.time.Instant;
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
    Map<UUID, Employee>employees;



    /**
     * Private constructor to allow instantiation only through {@link #getInstance()}
     */
    private BankingBackend(Map<UUID, Employee> employees) {
        // can only be instantiated through {@link #getInstance()}
        this.globalBalance = 0d;
        this.employees=employees;
        System.out.println("banking backend created");

    }

    /**
     * Method to retrieve the banking singleton backend instance.
     *
     * @return instance The banking backend instance
     */
    public static BankingBackend getInstance(Map<UUID, Employee> employees) {
        if (BankingBackend.instance == null) {
            instance = new BankingBackend(employees);
        }
        return instance;
    }
    public static BankingBackend getInstance() {
        if (BankingBackend.instance == null) {
            throw new RuntimeException("Initial getInstance call must be made with employees map");
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
        System.out.print(".");
        if(command instanceof PayWageCommand) {
            PayWageCommand pwcommand = (PayWageCommand) command;
            if(pwcommand.getEmployee().getAccount()==null) {
                System.out.println("nullpointer alert!");
                UUID key = pwcommand.getEmployee().getId();
                Account a_tmp = employees.get(key).getAccount();
                //could not recover null object, create new account
                if (a_tmp == null) a_tmp = new Account(pwcommand.getEmployee(), 0.0);
                pwcommand.getEmployee().setAccount(a_tmp);
            }
            pwcommand.getEmployee().getAccount().deposit(pwcommand.getAmount());
            this.globalBalance += pwcommand.getAmount();

            ChangedBalanceEvent cbe = new ChangedBalanceEvent(TransactionType.DEPOSIT, pwcommand.getEmployee(), Optional.empty(), pwcommand.getAmount(), Instant.now());

            UUID id = pwcommand.getEmployee().getId();
            //in case the user doesnt exist in banking backend, create a new account
            Employee e=employees.get(id);
            Account acc = (e!=null) ?e.getAccount(): new Account(pwcommand.getEmployee(),0.0);
        } else if(command instanceof ReadCardCommand) {
            ReadCardCommand rccommand = (ReadCardCommand) command;
            // reject any read card commands, if the current account balance is not sufficing
            //if nullpointer is triggered, try to recover with stored accounts
            if(rccommand.getCard().getEmployee().getAccount()==null){
                System.out.println("nullpointer alert!");
                UUID key=rccommand.getCard().getEmployee().getId();
                Employee e=employees.get(key);
                if (e == null) {
                    System.out.println("Employee doesnt exist, reject payment");
                    return;
                }
                Account a_tmp = e.getAccount();
                //could not recover null object, abort transaction
                if(a_tmp==null) return;
                rccommand.getCard().getEmployee().setAccount(a_tmp);
            }
            if (rccommand.getAmount() > rccommand.getCard().getEmployee().getAccount().getBalance()) {
                System.err.println(rccommand.getCard().getEmployee().getName() + " just tried to overdraw their account!");
                return;
            }
            rccommand.getCard().getEmployee().getAccount().withdraw(rccommand.getAmount());
            this.globalBalance -= rccommand.getAmount();

            ChangedBalanceEvent cbe = new ChangedBalanceEvent(TransactionType.WITHDRAW, rccommand.getCard().getEmployee(), Optional.of(rccommand.getTerminal()), rccommand.getAmount(), Instant.now());

            UUID id = rccommand.getCard().getEmployee().getAccount().getEmployee().getId();
            Employee e=employees.get(id);
            Account acc = e.getAccount();


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
            for (Map.Entry<UUID, Employee> emp : employees.entrySet()) {
                Employee e=emp.getValue();
                Card c=emp.getValue().getCard();
                writer.write(
                         e.getId().toString() + "="+emp.getValue().getAccount().getBalance()+"="+ e.getName()+ "="+
                                c.getCardId().toString()+"\n"
                );
            }
            double total = 0;

            for (Employee e : employees.values()) {
                total += e.getAccount().getBalance();
            }
            if(total==this.globalBalance) {
                System.out.println("global balance and sum of all accounts at checkpoint are both =" + Double.toString(total));
            }else{
                System.out.println("global balance = "+Double.toString(this.globalBalance)+
                        " but sum of all accounts at checkpoint is =" + Double.toString(total));
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
        // FALSE => Subtask 3
        // TRUE => Subtask 2
        Boolean subtask_switch = Boolean.TRUE;

        if (subtask_switch) {
            try {
                File file = new File(FOLDER_NAME, FILE_NAME);

                BufferedReader reader = new BufferedReader(new FileReader(file));

                employees.clear();

                String line;

                while ((line = reader.readLine()) != null) {

                    String[] parts = line.split("=");

                    if (parts[0].equals("GLOBAL")) {
                        double recv_global_Balance=Double.parseDouble(parts[1]);
                        if(recv_global_Balance!=-1){
                            setGlobalBalance(recv_global_Balance);
                        }
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
                        //i believe here we get some inconsitency because employees might still be saved but global balance is reset
                        employees.put(employee_id, employee);
                    }
                }

                reader.close();


                double total = 0;

                for (Employee e : employees.values()) {
                    total += e.getAccount().getBalance();
                }
                if(total==this.globalBalance) {
                    System.out.println("global balance and sum of all accounts at recovery are both =" + Double.toString(total));
                }else{
                    System.out.println("global balance = "+Double.toString(this.globalBalance)+
                            " but sum of all accounts at recovery is =" + Double.toString(total));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

        }
    }
}
