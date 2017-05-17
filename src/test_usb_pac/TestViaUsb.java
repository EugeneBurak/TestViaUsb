package test_usb_pac;

import loger.Loger;

/**
 * Created by java_dev on 03.03.17.
 */
    public class TestViaUsb {

    private static TestViaUsb instance; //Переменная для реализации паттерна синглтон
    private static UsbPort usbPort;

    private static final byte[] STATUS = new byte[] { 0x18 }; //fujitsu prn 609 - Запрос статуса
    private static final byte[] realTimeStatusTransmission_1 = new byte[] { 0x10, 0x04, 20 };   //Custom

    /**
     * UsbPort(int idVendor, byte IN_ENDPOINT, byte OUT_ENDPOINT)
     * int idVendor - the vendor ID of the device
     * byte IN_ENDPOINT - the input endpoint of the  device
     * byte OUT_ENDPOINT - the output endpoint of the device
     */
    private TestViaUsb() {
        usbPort = new UsbPort(0x13ea, (byte) 0x82, (byte) 0x01);            //fujitsu prn 609;   ID 13ea:0101 Hengstler;  usbPort = new UsbPort(0x13ea, (byte) 0x82, (byte) 0x01);
//        usbPort = new UsbPort(0x0dd4, (byte) 0x81, (byte) 0x02);            //Custom;    ID 0dd4:01a8 Custom Engineering SPA
    }

    public static TestViaUsb getInstance() //Реализация СИНГЛТОНа
    {
        if(instance == null) {
            try {
                instance = new TestViaUsb();
            } catch (Exception e) {
                Loger.Write("==>",  e.toString());
            }
        }
        return instance;
    }

    public void sendCommand() {

        int[] arPrinterStatus = usbPort.writeToUsb(STATUS);

    }
}
