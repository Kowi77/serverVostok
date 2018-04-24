package kov.develop.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiAliveImpl implements RmiAlive, Runnable{

    public static final String BINDING_NAME = "IsArmAlive";

    @Override
    public String isAlive() throws RemoteException {
        return "true";
    }

   /* public static void main(String... args) throws Exception {
        System.out.print("Starting registry...");
        final Registry registry = LocateRegistry.createRegistry( 2099);
        System.out.println(" OK");

        final RmiAlive service = new RmiAliveImpl();
        Remote stub = UnicastRemoteObject.exportObject(service, 0);

        System.out.print("Binding service...");
        registry.bind(BINDING_NAME, stub);
        System.out.println(" OK");

        while (true) {
            Thread.sleep(Integer.MAX_VALUE);
        }
    }*/

    @Override
    public void run() {
        try {
            System.out.print("Starting registry...");
            final Registry registry = LocateRegistry.createRegistry( 2099);
            System.out.println(" OK");

            final RmiAlive service = new RmiAliveImpl();
            Remote stub = UnicastRemoteObject.exportObject(service, 0);

            System.out.print("Binding service...");
            registry.bind(BINDING_NAME, stub);
            System.out.println(" OK");

            while (true) {
                Thread.sleep(Integer.MAX_VALUE);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
