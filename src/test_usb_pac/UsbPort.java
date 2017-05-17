package test_usb_pac;

import loger.Loger;
import org.usb4java.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Created by java_dev on 03.03.17.
 */
public class UsbPort {

    /**
     * private static final byte IN_ENDPOINT = (byte) 0x81;          //for custom
     * private static final byte OUT_ENDPOINT = (byte) 0x02;         //for custom
     * private static byte IN_ENDPOINT = (byte) 0x82;         //Fujitsu PRN 609
     * private static byte OUT_ENDPOINT = (byte) 0x01;         //Fujitsu PRN 609
     */
    private static byte IN_ENDPOINT;         //
    private static byte OUT_ENDPOINT;         //

    /**
     * private static int idVendor = 0x0dd4;         //vkp80 / tg2480
     * private static int idDevice = 0x015d;           //vkp80
     * private static int idVendor = 5098;         //Fujitsu PRN 609  DEC -> 5098
     * private static int idDevice = 0x0101;           //Fujitsu PRN 609
     * idDevice - Not used !!!!!
     */
    private static int idVendor;         //Fujitsu PRN 609  DEC -> 5098!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static int idDevice = 0x0101;           //Not used !!!!!           //Fujitsu PRN 609  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    private static String printerBus;
    private static String printerDev;
    private String fileNameDirectoryFileC = "/home/java_dev/SGSoft/restart_usb/";
    private String fileNameCompiledFileC = "/home/java_dev/SGSoft/restart_usb/restart_usb";

    private static DeviceHandle handle;
    private Context context;

    /** The communication timeout in milliseconds. */
    private static final int TIMEOUT = 5000;            //5000

    /**
     * Initialization with parameters a new Print via usb.
     */
    public UsbPort(int idVendor, byte IN_ENDPOINT, byte OUT_ENDPOINT) {
        UsbPort.idVendor = idVendor;
        UsbPort.IN_ENDPOINT = IN_ENDPOINT;
        UsbPort.OUT_ENDPOINT = OUT_ENDPOINT;
    }

    public static int[] writeToUsb(byte[] data)   {
        int[] printerStatus = null;
        try {

            UsbPort usb = new UsbPort();
            System.out.println("idVendor = " + idVendor);
            usb.claimDevice(idVendor, idDevice);

            write(handle, data);            //OK

            printerStatus = read(handle, 12);           //OK

            usb.releaseUsb();

        }   catch (Exception ex)    {
            ex.printStackTrace();
            Loger.Write("==>",  "Ошибка в методе - writeToUsb(byte[] data)");
        }
        return printerStatus;
    }

    public void rebootUsb() {
        try {
            String commandLsusb = "lsusb";
            Process process = Runtime.getRuntime().exec(commandLsusb);

            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = buf.readLine()) != null) {            //что будет если нет принтера - OK
                if (line.contains("13ea")) {
                    break;
                }
            }
            if (line != null) {
                String[] lines = line.split(" ");
                printerBus = lines[1];
                printerDev = lines[3].split(":")[0];
                System.out.println(" => printerBus = " + printerBus);
                System.out.println(" => printerDev = " + printerDev);
            }
            if (!new File(fileNameCompiledFileC).exists()) {
                String commandGcc = "gcc " + fileNameDirectoryFileC + "restart_usb.c -o " + fileNameCompiledFileC;
                System.out.println(" => commandGcc" + commandGcc);
                Runtime.getRuntime().exec(commandGcc);
            }
            String commandReboot = "sudo " + fileNameCompiledFileC +  " /dev/bus/usb/" + printerBus + "/" + printerDev;          //sudo ./restart_usb /dev/bus/usb/003/002
            System.out.println(" => command " + commandReboot);
            Process processReboot = Runtime.getRuntime().exec(commandReboot);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(processReboot.getInputStream()));
            while ((line = bufferedReader.readLine()) != null) {            //что будет если нет принтера????????????????????????????????????????
                if (line.contains("Reset successful")) {
                    System.out.println(" //////////////////////// USB порт перезагружен //////////////////////// ");
                    break;
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Instantiates a new Print via usb.
     */
    private UsbPort() {

        context = new Context();

    }

    /**
     * Claim device.
     *
     * @param vendor the vendor
     * @param device the device
     */
    private void claimDevice(int vendor, int device) {           //start claiming device - начать утверждать устройства
        System.out.println("start claiming device");
        findDevice(context, vendor, device);
    }

    /**
     * Find device.
     *
     * @param context   the context
     * @param vendorId  the vendor id
     * @param productId the product id
     */
    private static void findDevice(Context context, int vendorId, int productId) {

        // Initialize the libusb context
        System.out.println("start finding device");
        int result = LibUsb.init(context);
        if (result < 0) {
            throw new LibUsbException("Unable to initialize libusb", result);
        }

        // Read the USB device list - Читал список устройств USB
        DeviceList list = new DeviceList();
        result = LibUsb.getDeviceList(context, list);
        if (result < 0) {
            throw new LibUsbException("Unable to get device list", result);
        }

        try {
            // Iterate over all devices and list them - Перебрать все устройства и список их
            for (Device device : list) {

//                int address = LibUsb.getDeviceAddress(device);
//                int busNumber = LibUsb.getBusNumber(device);
/*                System.out.print("address"+address);
                System.out.print("busNumber"+busNumber);*/
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
//                System.out.print("result"+result);
                if (result < 0) {
                    throw new LibUsbException(
                            "Unable to read device descriptor", result);
                }

                if (result != LibUsb.SUCCESS) {
                    throw new LibUsbException("Unable to read device descriptor", result);
                }
//                System.out.println("vendor desc "+descriptor.idVendor());
//                System.out.println("product desc "+descriptor.idProduct());
                if (descriptor.idVendor() == vendorId) {
//                    System.out.println("Device Found");
                    getDeviceHandle(device);
//                    System.out.println(device);         //БЫЛО ЗАКОМЕНТИРОВАННО
//                    LibUsb.claimInterface(handle, 0);           //БЫЛО ЗАКОМЕНТИРОВАННО

                }

            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }
    }

    /**
     * Gets device handle.
     *
     * @param device the device
     */
    private static void getDeviceHandle(Device device) {

        handle = new DeviceHandle();

        int result = LibUsb.open(device, handle);

        System.out.println(result);

        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to open USB device", result);
        }

        try {
            // Use device handle here
            claimDevice(handle, 0);         //OK for Custom & Fujitsu
        } finally {
//            LibUsb.close(handle);
        }
    }

    /**
     * Claim device.
     *
     * @param handle          the handle
     * @param interfaceNumber the interface number
     */
    private static void claimDevice(DeviceHandle handle, int interfaceNumber) {
        int result = LibUsb.detachKernelDriver(handle, interfaceNumber);
        if(result!= LibUsb.SUCCESS&&result!= LibUsb.ERROR_NOT_SUPPORTED&&result!= LibUsb.ERROR_NOT_FOUND){
            throw new LibUsbException("Unable to detach kernel driver", result);}
        else
            result = LibUsb.claimInterface(handle, interfaceNumber);


        if (result != LibUsb.SUCCESS) {

            throw new LibUsbException("Unable to claim interface", result);
            
        }

    }

    /**
     * Release usb.
     */
    private void releaseUsb() {

        LibUsb.releaseInterface(handle, 0);
        LibUsb.close(handle);
        LibUsb.exit(context);

        System.out.println("Release usb.");

    }

    /**
     * Writes some data to the device.
     *
     * @param handle
     *            The device handle.
     * @param transferred
     *            The data to send to the device.
     */
    private static void writeIntBuffer(DeviceHandle handle, IntBuffer transferred) throws IOException {            //
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(10);
//        buffer.put(data);
//        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        System.out.println(" >>>>> OUT_ENDPOINT " + OUT_ENDPOINT);
        int result = LibUsb.bulkTransfer(handle, OUT_ENDPOINT, buffer,
                transferred, TIMEOUT);
        if (result != LibUsb.SUCCESS)
        {
//            reboot();
            throw new LibUsbException("Unable to send data", result);
        }
        System.out.println(transferred.get() + " bytes sent to device");
    }

    /**
     * Writes some data to the device.
     *
     * @param handle
     *            The device handle.
     * @param buffer
     *            The data to send to the device.
     */
    private static void writeByteBuffer(DeviceHandle handle, ByteBuffer buffer) throws IOException {            //
//        ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.s);
//        buffer.put(data);
        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        System.out.println(" >>>>> OUT_ENDPOINT " + OUT_ENDPOINT);
        int result = LibUsb.bulkTransfer(handle, OUT_ENDPOINT, buffer,
                transferred, TIMEOUT);
        if (result != LibUsb.SUCCESS)
        {
//            reboot();
            throw new LibUsbException("Unable to send data", result);
        }
        System.out.println(transferred.get() + " bytes sent to device");
    }

    /**
     * Writes some data to the device.
     *
     * @param handle
     *            The device handle.
     * @param data
     *            The data to send to the device.
     */
    private static void write(DeviceHandle handle, byte[] data) throws IOException {            //All OK!!!
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.length);
        buffer.put(data);
        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        System.out.println(" >>>>> OUT_ENDPOINT " + OUT_ENDPOINT);
        int result = LibUsb.bulkTransfer(handle, OUT_ENDPOINT, buffer,
                transferred, TIMEOUT);
        if (result != LibUsb.SUCCESS)
        {
//            reboot();
            throw new LibUsbException("Unable to send data", result);
        }
        System.out.println(transferred.get() + " bytes sent to device");
    }

    /**
     * Writes some data to the device.
     *
     * @param handle
     *            The device handle.
     * @param data
     *            The data to send to the device.
     */
    private static void writeString(DeviceHandle handle, String data) throws IOException {          //
        ByteBuffer buffer;
        buffer = ByteBuffer.allocateDirect(data.getBytes().length*5);

//        System.out.println(buffer + "buffer1");
        buffer.put(data.getBytes());         //CP866 это кодировка

        IntBuffer transferred = IntBuffer.allocate(data.getBytes().length*5);

//        ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.length);
//        buffer.put(data);
        System.out.println(" >>>>> OUT_ENDPOINT " + OUT_ENDPOINT);
        System.out.println("buffer -> " + buffer);
        System.out.println("transferred -> " + transferred.toString());
//        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        int result = LibUsb.bulkTransfer(handle, OUT_ENDPOINT, buffer,
                transferred, TIMEOUT);
        if (result != LibUsb.SUCCESS)
        {
//            reboot();
            throw new LibUsbException("Unable to send data", result);
        }
        System.out.println(transferred.get() + " bytes sent to device");
    }

    /**
     * Reads some data from the device.
     *
     * @param handle
     *            The device handle.
     * @param size
     *            The number of bytes to read from the device.
     * @return The read data.
     */
    private static  int [] read(DeviceHandle handle, int size)           //The best witch vkp80
    {
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(size).order(
                ByteOrder.LITTLE_ENDIAN);
        System.out.println(" >>>>> IN_ENDPOINT " + IN_ENDPOINT);
        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        int result = LibUsb.bulkTransfer(handle, IN_ENDPOINT, buffer,
                transferred, TIMEOUT);
        if (result != LibUsb.SUCCESS)
        {
            throw new LibUsbException("Unable to read data", result);
        }
        int [] array = new int[12];
        for (int i = 0; i < 12; i++) {          //для наглядности
            System.out.println(buffer.get(i));
            array [i] = buffer.get(i);
        }
        return array;
    }
}
