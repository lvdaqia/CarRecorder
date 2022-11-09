package com.azhon.jtt808.VideoList;

public class timeBean {
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String path;
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    private long size;
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    String startTime;
    String endTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    int duration;

    int channelNum;

    public int getChannelNum(){return channelNum; }

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }
}
