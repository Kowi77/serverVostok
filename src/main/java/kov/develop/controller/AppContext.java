package kov.develop.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppContext {

    private static AppContext context;

    public static String ARM_IP;
    public static String ARM_PORT;

    public static String UPS_IP;

    public static String DB_DRIVER;
    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PASSWORD;

    public static String DRIVER_DAS_IP;
    public static Integer DRIVER_DAS_PORT;
    public static Integer DRIVER_DAS_UNITIP;
    public static boolean DRIVER_DAS_HOLDING_REGISTER;
    public static Integer DRIVER_DAS_SENSOR_CONDITION_REGISTERS;
    public static Integer DRIVER_DAS_PERIMETER_REGISTERS;
    public static Integer DRIVER_DAS_COUNT_OF_REGISTERS;

    static boolean ArmAvailable = false;

    private final String PATH_TO_PROPERTIES = "src/main/resources/das.properties";

    private AppContext() {
        initProperties();
    }

    public static AppContext getContext() {
        if (context == null) {
            context = new AppContext();
            return context;
        } else {
            return context;
        }

    }

    private void initProperties() {

        FileInputStream fileInputStream = null;
        Properties prop = new Properties();

        try {

            fileInputStream = new FileInputStream(PATH_TO_PROPERTIES);
            prop.load(fileInputStream);

            ARM_IP = prop.getProperty("arm_ip");
            ARM_PORT = prop.getProperty("arm_port");

            UPS_IP = prop.getProperty("ups_ip");

            DB_DRIVER = prop.getProperty("db_driver");
            DB_URL = prop.getProperty("db_url");
            DB_USER = prop.getProperty("db_user");
            DB_PASSWORD = prop.getProperty("db_password");

            DRIVER_DAS_IP = prop.getProperty("driver_das_ip");
            DRIVER_DAS_PORT = Integer.parseInt(prop.getProperty("driver_das_port"));
            DRIVER_DAS_UNITIP = Integer.parseInt(prop.getProperty("driver_das_unitId"));
            DRIVER_DAS_HOLDING_REGISTER = Boolean.valueOf(prop.getProperty("driver_das_holding_register"));
            DRIVER_DAS_SENSOR_CONDITION_REGISTERS = Integer.parseInt(prop.getProperty("driver_das_sensor_condition_registers"));
            DRIVER_DAS_PERIMETER_REGISTERS = Integer.parseInt(prop.getProperty("driver_das_perimetr_registers"));
            DRIVER_DAS_COUNT_OF_REGISTERS = Integer.parseInt(prop.getProperty("driver_das_count_of_registers"));

        } catch (IOException e) {
            System.out.println("Ошибка в программе: файл " + PATH_TO_PROPERTIES + " не обнаружен");
            e.printStackTrace();

        }
        try {
            fileInputStream.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean isArmAvailable() {
        return ArmAvailable;
    }

    public static void setArmAvailable(boolean armAvailable) {
        ArmAvailable = armAvailable;
    }
}
