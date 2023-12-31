package com.example.lib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.util.List;

/**
 * 通用的工具类
 */
public class CommonUtils {


    //针对蓝叠模拟器单独获取包名的方法
    public static String isBluePackageName(Context context, String[] app) {
        StringBuilder builder = new StringBuilder("");
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(intent, 0);
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo info = apps.get(i);
            String packageName = info.activityInfo.packageName;
            for (int j = 0; j < app.length; j++) {
                if (app[j].equals(packageName)) {
                    builder.append("1");
                }
            }
        }
        if (!builder.toString().contains("1")) {
            builder.append("0");
        }
        return builder.toString();
    }

    //一般模拟器获取包名的方法
    public static String isAppPackage(Context context, String[] app) {
        PackageManager packageManager = context.getPackageManager();
        int flag = PackageManager.GET_UNINSTALLED_PACKAGES;
        /*
         GET_UNINSTALLED_PACKAGES 这个常数在API级别24中被弃用。用MATCH_UNINSTALLED_PACKAGES替换
       */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            flag = PackageManager.MATCH_UNINSTALLED_PACKAGES;
        }
        StringBuilder builder = new StringBuilder();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(flag);
        for (PackageInfo installedPackage : installedPackages) {
            String packageName = installedPackage.packageName;
            for (String value : app) {
                if (value.equals(packageName)) {
                    builder.append("1");
                }
            }
        }
        if (!builder.toString().contains("1")) {
            builder.append("0");
        }
        return builder.toString();
    }

    //获取系统包名
    public static String isSystemApp(Context context, String[] app) {
        String value = "";
        List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(0);
        for (String s : app) {
            for (int j = 0; j < packageInfos.size(); j++) {
                PackageInfo packageInfo = packageInfos.get(j);
                String packageName = packageInfo.packageName;
                if (packageName.equals(s)) {
                    value = value + 1;
                }
            }
        }
        if (!value.contains("1")) {
            ShellUtils.CommandResult commandResult = ShellUtils.execCommand(ShellUtils.SYSTEM_PACKAGE_COMMAND, false);
            if (commandResult.result != -1) {
                String successMsg = commandResult.successMsg;
                for (String s : app) {
                    if (successMsg.contains(s)) {
                        value = value + 1;
                    }
                }
            }
        }
        return value;
    }
}
