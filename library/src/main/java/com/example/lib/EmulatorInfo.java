package com.example.lib;

public class EmulatorInfo {
    private boolean isVm;
    private String name;
    private String tag1;
    private String tag2;
    private String describe;

    public EmulatorInfo(boolean isVm, String name, String tag1, String tag2, String describe) {
        this.isVm = isVm;
        this.name = name;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.describe = describe;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public boolean isVm() {
        return isVm;
    }

    public void setVm(boolean vm) {
        isVm = vm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag1() {
        return tag1;
    }

    public void setTag1(String tag1) {
        this.tag1 = tag1;
    }

    public String getTag2() {
        return tag2;
    }

    public void setTag2(String tag2) {
        this.tag2 = tag2;
    }

    @Override
    public String toString() {
        return "EmulatorInfo{" +
                "isVm=" + isVm +
                ", name='" + name + '\'' +
                ", tag1='" + tag1 + '\'' +
                ", tag2='" + tag2 + '\'' +
                ", describe='" + describe + '\'' +
                '}';
    }
}
