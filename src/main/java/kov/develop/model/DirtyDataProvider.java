package kov.develop.model;

import kov.develop.controller.AppContext;
import kov.develop.modbus.ModBusDasClient;

import java.sql.*;
import java.util.Random;
import java.util.Date;

public class DirtyDataProvider implements Runnable{

    private static Connection connection;
    private static ModBusDasClient modBusClient;

    private final String WRITE_DIRTY_DATA = "insert into das_dirty_data (das_status, das_interruption, sensor_values, ds) values (?,?,?,?)";


    static {
        connection = getConnection();
        modBusClient = new ModBusDasClient();
    }

    @Override
    public void run() {

        String perimeter;

        while (true) {

            int[] data = null;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}

            if (!AppContext.isArmAvailable()) {
                try {
                    data = modBusClient.readFloatRegs(0, AppContext.DRIVER_DAS_SENSOR_CONDITION_REGISTERS + AppContext.DRIVER_DAS_PERIMETER_REGISTERS, new Exception[0]);
                } catch (Exception e) {
                    System.out.println("Modbus error: " + e.getMessage());
                    continue;
                }

                if (data != null) {
                    perimeter = getPerimeterData(data);
                } else {
                    continue;
                }


                try {
                    PreparedStatement statement = connection.prepareStatement(WRITE_DIRTY_DATA);
                    statement.setInt(1, data[0]);
                    statement.setInt(2, data[1]);
                    statement.setString(3, perimeter);
                    statement.setTimestamp(4, new Timestamp(new Date().getTime()));
                    statement.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("SQL error: " + e.getMessage());
                }
            }
        }
    }

    private String getPerimeterData(int[] data) {
        StringBuffer buffer = new StringBuffer();

        for (int i = AppContext.DRIVER_DAS_SENSOR_CONDITION_REGISTERS; i < data.length; i++) {
            buffer.append(data[i]);
        }
        return buffer.toString();
    }


    private static Connection getConnection(){
        if(connection == null){
            connection = createConnection();
        }
        return connection;
    }

    private static Connection createConnection() {

        Connection connection = null;
        ResultSet resultSet = null;
        Statement statement = null;

        try {
            Class.forName(AppContext.DB_DRIVER);
            String connectStr = AppContext.DB_URL;
            String user = AppContext.DB_USER;
            String pwd = AppContext.DB_PASSWORD;
            connection = DriverManager.getConnection(connectStr, user, pwd);
            connection.setAutoCommit(true);
            statement = connection.createStatement();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return connection;

    }


}
