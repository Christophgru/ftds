package org.pgi.paxoscoin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.pgi.paxoscoin.banking.Account;
import org.pgi.paxoscoin.banking.TransactionType;
import org.pgi.paxoscoin.commands.Command;
import org.pgi.paxoscoin.commands.PayWageCommand;
import org.pgi.paxoscoin.commands.ReadCardCommand;
import org.pgi.paxoscoin.worldmodel.Card;
import org.pgi.paxoscoin.worldmodel.Employee;
import org.pgi.paxoscoin.worldmodel.Terminal;

public class CSVReader<T> {

    private Class<?> clazz;
    private Map<UUID, Card> cards;
    private Map<UUID, Terminal> terminals;
    private Map<UUID, Employee> employees;

    private Field cardEmployeeField;

    private Function<String, T> mapToItem = (line) -> {
        String[] p = line.split(";");
        if (clazz == Command.class) {
            clazz = p[3].equals("PAYWAGE") ? PayWageCommand.class : ReadCardCommand.class;
        }
        Constructor<?> cons = clazz.getConstructors()[0];
        Object[] params = new Object[cons.getParameterTypes().length];

        Card card = null;

        for (int i = 0; i < p.length; i++) {
            Class<?> pType = cons.getParameterTypes()[i];

            if (pType == UUID.class) {
                params[i] = UUID.fromString(p[i]);
            } else if (pType == String.class) {
                params[i] = p[i];
            } else if (pType == double.class) {
                params[i] = Double.parseDouble(p[i]);
            } else if (pType == Account.class) {
                // ignore that parameter for now
                params[i] = null;
            } else if (pType == Card.class) {
                card = cards.get(UUID.fromString(p[i]));
                params[i] = card;
            } else if (pType == Terminal.class) {
                params[i] = terminals.get(UUID.fromString(p[i]));
            } else if (pType == Instant.class) {
                params[i] = Instant.ofEpochMilli(Long.parseLong(p[i]));
            } else if (pType == Employee.class) {
                if (clazz == PayWageCommand.class || clazz == ReadCardCommand.class) {
                    params[i] = employees.get(UUID.fromString(p[i]));
                } else {
                    // ignore that parameter for now
                    params[i] = null;
                }
            } else if (pType == TransactionType.class) {
                if (p[i].equals("DEPOSIT")) {
                    params[i] = TransactionType.DEPOSIT;
                } else {
                    params[i] = TransactionType.WITHDRAW;
                }
            } else {
                System.err.println("unknown type " + pType);
            }
        }

        try {

            // todo set employee in account
            @SuppressWarnings("unchecked")
            T result = (T) cons.newInstance(params);

            if (clazz == Employee.class) {
                Employee employee = (Employee) result;
                employee.setAccount(new Account(employee, 0));
                cardEmployeeField.set(card, employee);
            }

            return result;
        } catch (InvocationTargetException|InstantiationException|IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    };

    public CSVReader(Class<T> clazz) {
        this.clazz = clazz;
        try {
            this.cardEmployeeField = Card.class.getDeclaredField("employee");
            this.cardEmployeeField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public List<T> readFileAsListWithDependencies(String inputFilePath, Map<UUID, Card> cards, Map<UUID, Employee> employees, Map<UUID, Terminal> terminals) {
        this.cards = cards;
        this.employees = employees;
        this.terminals = terminals;

        List<T> inputList = new ArrayList<>();

        try {
            File inputF = new File(inputFilePath);
            InputStream inputFS = new FileInputStream(inputF);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));

            // skip the header of the csv
            inputList = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputList;
    }

    @SuppressWarnings("resource")
    public Map<UUID, T> readFileAsMap(String inputFilePath) {
        Map<UUID, T> inputMap = new HashMap<>();

        try {
            File inputF = new File(inputFilePath);
            InputStream inputFS = new FileInputStream(inputF);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));

            // skip the header of the csv
            inputMap = br.lines().skip(1).map(mapToItem).collect(Collectors.toMap(item -> {
                if (item instanceof Card) {
                    return ((Card) item).getCardId();
                } else if (item instanceof Employee) {
                    return ((Employee) item).getId();
                } else if (item instanceof Terminal) {
                    return ((Terminal) item).getId();
                } else {
                    return null;
                }
            }, item -> item));
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputMap;
    }

    public Map<UUID, T> readFileAsMapWithDependencies(String inputFileString, Map<UUID, Card> cards) {
        this.cards = cards;

        return readFileAsMap(inputFileString);
    }
}
