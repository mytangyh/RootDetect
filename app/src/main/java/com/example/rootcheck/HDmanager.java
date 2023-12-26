package com.example.rootcheck;

public class HDmanager {
    private static HDmanager hDmanager;

    public static HDmanager gethDmanager() {
        if (hDmanager==null){
            hDmanager = new HDmanager();
        }
        return hDmanager;
    }
    public HDmanager(){
        fun1();
    }

    private void fun1() {
    }
}
