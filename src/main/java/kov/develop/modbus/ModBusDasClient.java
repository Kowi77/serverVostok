package kov.develop.modbus;

import kov.develop.controller.AppContext;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.BitVector;
import net.wimpi.modbus.util.ModbusUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class ModBusDasClient {

    private int port;

    private byte[] addrArr;

    private int unitId = 0;

    private boolean started;

    private boolean holding;

    private int offset = 0;

    public ModBusDasClient(){
        this.addrArr = fromString(AppContext.DRIVER_DAS_IP);
        this.port = AppContext.DRIVER_DAS_PORT;
        this.unitId = AppContext.DRIVER_DAS_UNITIP;
        this.holding = AppContext.DRIVER_DAS_HOLDING_REGISTER;
    }

    public int getUnitId() {
        return unitId;
    }

    private float restoreFloatValueFromRegisters(byte[] register1, byte[] register2) {

        byte[] buf1 = register1;
        byte[] buf2 = register2;

        byte[] commonData = new byte[4];
        commonData[0] = buf2[0];
        commonData[1] = buf2[1];
        commonData[2] = buf1[0];
        commonData[3] = buf1[1];

        return ModbusUtil.registersToFloat(commonData);
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

    public boolean isServerStarted() {
        return this.started;
    }

    public synchronized int[] readFloatRegs(int ref, int count, Exception[] wasException) {

        TCPMasterConnection con = null; //the connection

        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(addrArr);
        } catch (UnknownHostException e1) {
            System.out.println("cannot parse address" + e1);
            wasException[0] = e1;
            return null;
        }

        //float[] ret = new float[count];
        int[] ret = new int[count];

        con = new TCPMasterConnection(addr);
        con.setPort(port);
        try {
            con.connect();
            System.out.println("modbus service connected");
            int countRegs = count*2;
            System.out.println("doProcess create registers with ref="+ref+" and offset="+this.offset+" and countRegs="+countRegs);

            byte[][] registers = readRegistersByChunks(countRegs, ref+this.offset, con);

            con.close();

            System.out.println(registers.length);

            if(registers != null){
                System.out.println("doProcess registers count: "+registers.length);
                for(int i = 0; i< count; i++){
                    byte[] register1 = registers[i*2];
                    byte[] register2 = registers[i*2+1];

                    int value = registers[i][1];//restoreFloatValueFromRegisters(register1, register2);
                    ret[i] = value;
                    System.out.println("doProcess value restored from registers: "+value);
                }
            }
            else{
                System.out.println("doProcess registers are null!");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            wasException[0] = e;

        } catch(Exception e){
            System.out.println(e.getMessage());
            wasException[0] = e;
        }
        return ret;
    }

    private int getChunkSize() {
        return 122;
    }

    private byte[][] readRegistersByChunks(int countRegs, int ref,
                                           TCPMasterConnection con) throws ModbusIOException, ModbusSlaveException, ModbusException {

        if(countRegs < getChunkSize()){
            return readRegistersByChunk(countRegs, ref, con);
        }

        byte[][] ret = new byte[countRegs][2];
        int windowSize = getChunkSize();
        int curCount = windowSize;
        int curCountRest = countRegs;
        int destPos = 0;
        int curStart = ref;

        while(curCountRest > 0){
            byte[][] partRet = readRegistersByChunk(curCount, curStart, con);
            System.arraycopy(partRet, 0, ret, destPos, partRet.length);
            destPos += curCount;
            curStart += curCount;
            curCountRest -= curCount;
            if(curCountRest < 0){
                curCount = countRegs + curCountRest;
            }
            else{
                if(curCountRest > 0 && curCountRest < windowSize){
                    curCount = curCountRest;
                }
            }
        }

        return ret;

    }

    private byte[][] readRegistersByChunk(int countRegs, int ref,
                                          TCPMasterConnection con) throws ModbusIOException, ModbusSlaveException, ModbusException {
        ModbusRequest req = null; //the request
        ModbusResponse res = null; //the response
        ModbusTCPTransaction trans = null; //the transaction
        if(!isHolding()){
            System.out.println("using input registers");
            req = new ReadInputRegistersRequest(ref, countRegs);
        }
        else{
            System.out.println("using holding registers");
            req = new ReadMultipleRegistersRequest(ref, countRegs);
        }
        int unitId = getUnitId();
        req.setUnitID(unitId);

        trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);


        System.out.println("doProcess execute transaction:");
        try {
            trans.execute();
        } catch (ModbusException e) {
            e.printStackTrace();
            throw new ModbusException();
        }
        System.out.println("doProcess transaction executed");
        res = trans.getResponse();

        byte[][] registers = createFromRes(res);

        return registers;
    }

    private byte[][] createFromRes(ModbusResponse res) {
        byte[][] registers = null;
        if(!isHolding()){
            ReadInputRegistersResponse resp = (ReadInputRegistersResponse) res;
            InputRegister[] mRegisters = resp.getRegisters();
            if(mRegisters != null && mRegisters.length > 0){
                registers = new byte[mRegisters.length][2];
                int i = 0;
                for(InputRegister register:mRegisters){
                    byte[] bytes = register.toBytes();
                    System.arraycopy(bytes, 0, registers[i], 0, bytes.length);
                    i++;
                }
            }

        }
        else{
            ReadMultipleRegistersResponse resp = (ReadMultipleRegistersResponse) res;
            Register[] mRegisters = resp.getRegisters();
            if(mRegisters != null && mRegisters.length > 0){
                registers = new byte[mRegisters.length][2];
                int i = 0;
                for(Register register:mRegisters){
                    byte[] bytes = register.toBytes();
                    registers[i] = new byte[bytes.length];
                    System.arraycopy(bytes, 0, registers[i], 0, bytes.length);
                    i++;
                }
            }
        }
        return registers;
    }

    private boolean isHolding() {
        return this.holding;
    }

    public byte[] readDigitalIn(int start, int count) throws Exception {
        if(count <= Modbus.MAX_MESSAGE_LENGTH/2){
            return readDigitalInPart(start, count);
        }
        //else: will ready by parts, and copy to resulting array:
        byte[] ret = new byte[count];
        int windowSize = Modbus.MAX_MESSAGE_LENGTH/2;
        int curCount = windowSize;
        int curCountRest = count;
        int destPos = 0;
        int curStart = start;

        while(curCountRest > 0){
            byte[] partRet = readDigitalInPart(curStart, curCount);
            System.arraycopy(partRet, 0, ret, destPos, curCount);
            destPos += curCount;
            curStart += curCount;
            curCountRest -= curCount;
            if(curCountRest < 0){
                curCount = count + curCountRest;
            }
            else{
                if(curCountRest > 0 && curCountRest < windowSize){
                    curCount = curCountRest;
                }
            }
        }

        return ret;
    }
    private byte[] readDigitalInPart(int start, int count) throws Exception {
        byte[] ret = new byte[count];
        boolean done = false;
		/* The important instances of the classes mentioned before */
        TCPMasterConnection con = null; //the connection
        ModbusTCPTransaction trans = null; //the transaction
        ReadInputDiscretesRequest req = null; //the request
        ReadInputDiscretesResponse res = null; //the response

		/* Variables for storing the parameters */
        //int[] port = new int[1];
        InetAddress addr = InetAddress.getByAddress(addrArr);
        con = new TCPMasterConnection(addr);
        con.setPort(port);
        try {
            con.connect();
            int ref = start;
            req = new ReadInputDiscretesRequest(ref, count);
            req.setUnitID(unitId);

            trans = new ModbusTCPTransaction(con);
            trans.setRequest(req);

            trans.execute();
            done = true;
            res = (ReadInputDiscretesResponse) trans.getResponse();

            con.close();

            BitVector vector = res.getDiscretes();

            if(vector != null){
                for(int i = 0; i<count; i++){
                    boolean value = vector.getBit(i);
                    if(value){
                        ret[i] = 1;
                    }
                    else{
                        ret[i] = 0;
                    };
                }
            }

        } catch (IOException e) {
            System.out.println(e.toString());
            throw new Exception(e);
        } catch(Exception e){
            System.out.println(e.toString());
            throw new Exception(e);
        }
        if(done){
        }
        else{
            System.out.println("readDigitalInPart not done for start="+start+", count="+count+". will return null");
        }
        return done?ret:null;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }


}