package kov.develop.rmi;

import kov.develop.controller.AppContext;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiClient implements Runnable {

    public static final String BINDING_NAME = "IsArmAlive";

    private Registry registry;
    private RmiAlive service;

    public RmiClient() {
        try {
            registry = LocateRegistry.getRegistry(AppContext.ARM_IP, 2099);
            service = (RmiAlive) registry.lookup(BINDING_NAME);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        while (true){
        try {
            AppContext.setArmAvailable(Boolean.parseBoolean(service.isAlive()));
            System.out.println(AppContext.isArmAvailable());
            Thread.sleep(1000);
        } catch (Exception e) {
            //e.printStackTrace();
            AppContext.setArmAvailable(false);
            System.out.println(AppContext.isArmAvailable());
        }}
    }

}