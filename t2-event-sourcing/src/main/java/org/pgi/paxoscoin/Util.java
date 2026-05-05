package org.pgi.paxoscoin;

import java.text.DecimalFormat;

public class Util {
    private static DecimalFormat df = new DecimalFormat("€ ###,###,###.##");

    public static String formatCurrency(double amount) {
        return df.format(amount);
    }
}
