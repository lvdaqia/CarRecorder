package com.example.dell.carrecorder.bean;

public class SettingBean {
    String IP;
    int PORT;

    String IP_LOCAL;
    int PORT_LOCAL;

    //终端手机号
    String PHONE;
    //制造商ID
    String MANUFACTURER_ID;
    //终端型号
    String TERMINAL_MODEL;
    //终端ID
    String TERMINAL_ID ;

    int audioType;

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    int platform;
    public static SettingBean instance;

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getPORT() {
        return PORT;
    }

    public void setPORT(int PORT) {
        this.PORT = PORT;
    }

    public String getIP_LOCAL() {
        return IP_LOCAL;
    }

    public void setIP_LOCAL(String IP) {
        this.IP_LOCAL = IP_LOCAL;
    }

    public int getPORT_LOCAL() {
        return PORT_LOCAL;
    }

    public void setPORT_LOCAL(int PORT_LOCAL) {
        this.PORT_LOCAL = PORT_LOCAL;
    }

    public String getPHONE() {
        return PHONE;
    }

    public void setPHONE(String PHONE) {
        this.PHONE = PHONE;
    }

    public String getMANUFACTURER_ID() {
        return MANUFACTURER_ID;
    }

    public void setMANUFACTURER_ID(String MANUFACTURER_ID) {
        this.MANUFACTURER_ID = MANUFACTURER_ID;
    }

    public String getTERMINAL_MODEL() {
        return TERMINAL_MODEL;
    }

    public void setTERMINAL_MODEL(String TERMINAL_MODEL) {
        this.TERMINAL_MODEL = TERMINAL_MODEL;
    }

    public String getTERMINAL_ID() {
        return TERMINAL_ID;
    }

    public void setTERMINAL_ID(String TERMINAL_ID) {
        this.TERMINAL_ID = TERMINAL_ID;
    }

    public void copyFrom(SettingBean bean)
    {
        IP=bean.getIP();
        PORT=bean.getPORT();

        IP_LOCAL=bean.getIP_LOCAL();
        PORT_LOCAL=bean.getPORT_LOCAL();
        //终端手机号
        PHONE=bean.getPHONE();
        //制造商ID
        MANUFACTURER_ID=bean.getMANUFACTURER_ID();
        //终端型号
        TERMINAL_MODEL=bean.getTERMINAL_MODEL();
        //终端ID
        TERMINAL_ID=bean.getTERMINAL_ID();

        audioType = bean.getaudioType();
        platform = bean.getPlatform();
    }
    public int getaudioType() {
        return audioType;
    }

    public void setaudioType(int audioType) {
        this.audioType = audioType;
    }

    public static SettingBean getInstance()
    {
        if(instance==null)
            instance = new SettingBean();
        return instance;
    }

}
