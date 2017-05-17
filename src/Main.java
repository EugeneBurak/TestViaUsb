import loger.Loger;
import test_usb_pac.TestViaUsb;

public class Main {
    public static void main(String[] args) {
        Loger.Write(">>>", "START");
        TestViaUsb.getInstance();
        TestViaUsb.getInstance().sendCommand();
    }
}
