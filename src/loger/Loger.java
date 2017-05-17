package loger;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by java_dev on 03.03.17.
 */
public class Loger {
    private static DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT);

    public static void Write(String atr, String msg)
    {
            Date currentDate = new Date();
            String mes = dateFormat.format(currentDate) + " - " + atr + "\t" + msg + "\n";
            consoleHelper(mes);
    }

    private static void consoleHelper(String messeg) {
        System.out.println(messeg);
    }
}
