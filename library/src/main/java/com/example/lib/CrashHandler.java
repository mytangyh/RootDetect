package com.example.lib;

import android.content.Context;
import android.os.Process;
import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
public class CrashHandler implements Thread.UncaughtExceptionHandler{
    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;
    private Context mContext;
    private static CrashHandler INSTANCE;
    private static CrashHandler getInstance(){
        if (INSTANCE == null){
            synchronized (CrashHandler.class){
                if (INSTANCE == null){
                    INSTANCE = new CrashHandler();
                }
            }
        }
        return INSTANCE;
    }
    public void init(Context context){
        mContext = context.getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    private CrashHandler(){
        this.mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    @Override
    public void uncaughtException(@NonNull @NotNull Thread thread, @NonNull @NotNull Throwable throwable) {
        try {
            if (!throwable.getStackTrace()[0].toString().contains("com.example.lib")){
                closeApp(thread,throwable);
                return;

            }
            saveCrashInfo2File(throwable);
            closeApp(thread,throwable);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void saveCrashInfo2File(Throwable throwable) {
    }

    private void closeApp(Thread thread,Throwable ex){
        if (mDefaultUncaughtExceptionHandler != null){
            mDefaultUncaughtExceptionHandler.uncaughtException(thread,ex);
        }else {
            Process.killProcess(Process.myPid());
        }
    }
}
