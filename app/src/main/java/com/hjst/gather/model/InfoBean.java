package com.hjst.gather.model;

import java.util.Objects;

/**
 * 设备历史记录实体，由 Gson 直接反序列化 Result<List<InfoBean>> 的 data 元素
 * 注意：服务端数字字段带小数（79.0），用 double 接收避免 String 字段反序列化出 "79.0" 这种脏数据
 */
public class InfoBean {

    private String time;
    private double battery;
    private double signal;

    public InfoBean(String time, double battery, double signal) {
        this.time = time;
        this.battery = battery;
        this.signal = signal;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getBattery() {
        return battery;
    }

    public void setBattery(double battery) {
        this.battery = battery;
    }

    public double getSignal() {
        return signal;
    }

    public void setSignal(double signal) {
        this.signal = signal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InfoBean)) return false;
        InfoBean other = (InfoBean) o;
        return Double.compare(other.battery, battery) == 0
                && Double.compare(other.signal, signal) == 0
                && Objects.equals(time, other.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, battery, signal);
    }

    @Override
    public String toString() {
        return "InfoBean{time='" + time + "', battery=" + battery + ", signal=" + signal + '}';
    }



}
