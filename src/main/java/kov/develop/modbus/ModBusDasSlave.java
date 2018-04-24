package kov.develop.modbus;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.ModbusUtil;

import java.net.InetAddress;
import java.util.Random;

public class ModBusDasSlave {

	private String ipAddress;
	private int port;
	private int unitID;
	private int maxFloatUnits;
	private boolean isHolding;
	private int offset;

	public ModBusDasSlave(String ipAddress, int port, int unitID, int maxFloatUnits, boolean isHolding) {
		this.isHolding = isHolding;
		this.ipAddress = ipAddress;
		this.port = port;
		this.unitID = unitID;
		this.maxFloatUnits = maxFloatUnits;
		this.offset = 0;
	}

	static SimpleProcessImage spi = null;

	private Thread t;

	public void start() {
		try {

			ModbusTCPListener listener;

			// 3. Set the image on the coupler
			ModbusCoupler.getReference().setProcessImage(spi);
			ModbusCoupler.getReference().setMaster(false);
			ModbusCoupler.getReference().setUnitID(unitID);

			// 4. Create a listener with 3 threads in pool
			listener = new ModbusTCPListener(3);
			listener.setPort(port);
			try{
				InetAddress addr = InetAddress.getByAddress(fromString(ipAddress));
				listener.setAddress(addr);
			}
			catch (java.net.UnknownHostException e) {
			}
			listener.start();

			t = new Thread(new Runnable(){


				public void run() {
					while(true){
						try{

							Thread.sleep(5000);
							spi = new SimpleProcessImage();

							for(int i = 0; i < maxFloatUnits * 2; i++) {
								if (isHolding) {
									int f = Math.random() > 0.95 ? 2 : 1;
									spi.addRegister(new SimpleRegister(f));
								} else {
									spi.addInputRegister(new SimpleRegister(i));
								}
							}
						}
						catch(Throwable tt){
							tt.printStackTrace();
						}
					}
				}

			});
			t.setName("updater");
			t.start();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void setRegValue(int i, float v) {
		byte[] datas = ModbusUtil.floatToRegisters(v);

		byte[] b = new byte[2];
		b[0] = datas[0];
		b[1] = datas[1];
		
		byte[] b1 = new byte[2];
		b1[0] = datas[2];
		b1[1] = datas[3];;

		if(isHolding){
			Register r = spi.getRegister(2*i);
			Register r1 = spi.getRegister(2*i+1);
			r1.setValue(b);
			r.setValue(b1);
		}
		else{
			SimpleRegister r = (SimpleRegister) spi.getInputRegister(2*i);
			SimpleRegister r1 = (SimpleRegister) spi.getInputRegister(2*i+1);
			r1.setValue(b);
			r.setValue(b1);
		}
	}

	private byte[] fromString(String ipAddress) {
		byte[] ret = new byte[4];
		String[] parts = ipAddress.split("\\.");
		if(parts.length != 4){
			throw new RuntimeException("ip address of wrong format!! "+ipAddress);
		}
		int i = 0;
		for(String part:parts){
			byte p = (byte)(Integer.parseInt(part, 10) & 0x000000ff);
			ret[i] = p;
			i++;
		}
		return ret;
	}
}