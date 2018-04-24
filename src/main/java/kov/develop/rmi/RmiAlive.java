package kov.develop.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiAlive extends Remote{
	
	public String isAlive() throws RemoteException;

}
