package kov.develop.controller;

import kov.develop.modbus.ModBusDasClient;
import kov.develop.modbus.ModBusDasSlave;
import kov.develop.model.DirtyDataProvider;
import kov.develop.rmi.RmiAliveImpl;
import kov.develop.rmi.RmiClient;

import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.*;

public class Controller {

    static AppContext context;

    static  {
        context = AppContext.getContext();
    }

    public static void main(String[] args) {

        //Start emulation of ARM
        //TODO Remove on production!!
        Thread thread = new Thread(new RmiAliveImpl(), "Rmi Simulator");
        thread.setDaemon(true);
        thread.start();

        //Start knocking to ARM
        Thread thread1 = new Thread(new RmiClient(), "Rmi Client");
        thread1.start();

        //Start emulation of Driver Das
        //TODO Remove on production!!
        ModBusDasSlave simulator = new ModBusDasSlave(context.DRIVER_DAS_IP, context.DRIVER_DAS_PORT, context.DRIVER_DAS_UNITIP,
                context.DRIVER_DAS_SENSOR_CONDITION_REGISTERS + context.DRIVER_DAS_PERIMETER_REGISTERS, context.DRIVER_DAS_HOLDING_REGISTER);
        simulator.start();

        //start data writer
        Thread thread2 = new Thread(new DirtyDataProvider(), "Data Writer");
        thread2.start();

    }
}
