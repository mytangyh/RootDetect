package com.example.rootcheck;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Author : Administrator
 * Time : 2024/01/22
 * Desc :
 */
public class SysnKV implements SharedPreferences {

    private static final String TAG = "SysnKV";

    private static final String DEF_NAME = "sysn_kv";
    private static final String SUFFIX = ".skv";
    /**
     * 默认200kb
     * <p>
     * 分块存储文件最大值,超过这个值就加一块
     */
    private int mMaxBlockSize = 1024 * 10;
    private final Context context;

    private String name = "def_sysnkv";
    private ArrayList<Block> mBlockList;

    private Queue<Editor> mEditorQueue;
    private Handler mHandler;


    public SysnKV(Context context) {
        this(context, DEF_NAME);
    }
    public SysnKV(Context context, String name) {
        this.name = name;
        this.context = context;
        mBlockList = new ArrayList<>();
        try {
            for (int i = 0; ; i++) {
                String path = getBlockFile(context, name, i);
                File blockFile = new File(path);
                if (blockFile.exists() && blockFile.isFile()) {
                    Block block = new Block(blockFile);
                    mBlockList.add(block);

                } else {
                    break;
                }
            }

            if (mBlockList.isEmpty()) {
                String path = getBlockFile(context, name, mBlockList.size());
                Block block = new Block(new File(path));
                mBlockList.add(block);
            }

            mEditorQueue = new LinkedList<>();
            HandlerThread thread = new HandlerThread("SysnKV");
            thread.start();
            mHandler = new Handler(thread.getLooper(), new Work());
        } catch (Throwable e) {
            //1.文件禁止访问
            //2.无法创建文件
            e.printStackTrace();
        }
    }
    private String getBlockFile(Context context, String name, int num) {
        String dir = context.getExternalFilesDir(null).getAbsolutePath().concat(File.separator).concat("testSysnP/");
//        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
//                .concat(File.separator).concat("testSysnP/");
        return dir.concat(name).concat(String.valueOf(num)).concat(name.indexOf('.') != -1 ? "" : SUFFIX);
    }

    @Override
    public Map<String, ?> getAll() {
        Map<String, Object> mValue = new HashMap<>();
        for (Block block : mBlockList) {
            mValue.putAll(block.getValue());
        }
        return mValue;
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        try {
            for (Block block : mBlockList) {
                String o = (String) block.getValue().get(key);
                if (o != null) {
                    return o;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        try {
            for (Block block : mBlockList) {
                Object array = block.getValue().get(key);
                //hashmap 存完了json解析出来是jsonarray
                if (array instanceof Set) {
                    return (Set<String>) array;
                } else if (array instanceof JSONArray) {
                    if (array == null) {
                        return defValues;
                    }
                    JSONArray jsonArray = (JSONArray) array;
                    Set<String> strings;
                    strings = new HashSet<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        strings.add((String) jsonArray.opt(i));
                    }
                    return strings;
                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        try {
            for (Block block : mBlockList) {
                Object val = block.getValue().get(key);
                if (val != null) {
                    return (int) val;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        try {
            for (Block block : mBlockList) {
                Object val = block.getValue().get(key);
                if (val != null) {
                    if (val instanceof Integer) {
                        return (int) val;
                    } else {
                        return (long) val;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        try {
            for (Block block : mBlockList) {
                Object val = block.getValue().get(key);
                if (val != null) {
                    if (val instanceof Double) {
                        double d = (double) val;
                        return (float) d;
                    } else {
                        return (float) val;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        try {
            for (Block block : mBlockList) {
                Object val = block.getValue().get(key);
                if (val != null) {
                    return (boolean) val;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Override
    public boolean contains(String key) {
        for (Block block : mBlockList) {
            Object o = block.getValue().get(key);
            if (o != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Editor edit() {
        return new EditorImpl();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }
    final static class Work implements Handler.Callback {

        public final static int WHAT_APPLY = 1;
        public final static int WHAT_INIT_SYSN = 2;

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_APPLY:
                    Queue<Editor> queue = null;
                    if (msg.obj instanceof Queue) {
                        queue = (Queue<Editor>) msg.obj;
                    }
                    if (queue == null) {
                        break;
                    }
                    while (!queue.isEmpty()) {
                        Editor editor = queue.poll();
                        editor.commit();
                    }
                    break;
                case WHAT_INIT_SYSN:

                    break;
                default:
                    break;
            }
            return true;
        }
    }
    final class EditorImpl implements Editor {
        Map<String, Object> addMap = new HashMap<>();
        Set<String> deleteKey = new HashSet<>();
        boolean isClear;

        @Override
        public Editor putString(String key, String value) {
            addMap.put(key, value);
            return this;
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            addMap.put(key, values);
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            addMap.put(key, value);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            addMap.put(key, value);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            addMap.put(key, value);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            addMap.put(key, value);
            return this;
        }

        @Override
        public Editor remove(String key) {
            deleteKey.add(key);
            addMap.remove(key);
            return this;
        }

        @Override
        public Editor clear() {
            isClear = true;
            deleteKey.clear();
            addMap.clear();
            return this;
        }

        @Override
        public boolean commit() {
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                //在主线程操作可能会因为等待文件锁anr
                Log.w(TAG, "在主线程操作,最好使用apply防止ANR");
            }
            boolean result = false;

            try {

                for (int i = 0; i < mBlockList.size(); i++) {
                    boolean isMdf = false;

                    Block block = mBlockList.get(i);
                    if (isClear) {
                        block.getValue().clear();
                        isMdf = true;
                    } else {
                        for (String key : deleteKey) {
                            block.sync();
                            Object value = block.getValue().remove(key);
                            if (value != null) {
                                deleteKey.remove(key);
                                isMdf = true;
                            }
                        }
                        if (block.getSize() > mMaxBlockSize) {
                            continue;
                        }

                    }
                    if (!addMap.isEmpty() && block.getSize() < mMaxBlockSize) {
                        block.getValue().putAll(addMap);
                        addMap.clear();
                        isMdf = true;
                    }
                    if (isMdf) {
                        result = block.write();
                    }
                }

                if (!addMap.isEmpty()) {
                    String path = getBlockFile(context, name, mBlockList.size());
                    Block block = new Block(new File(path));
                    mBlockList.add(block);
                    block.getValue().putAll(addMap);
                    result = block.write();
                }


            } catch (Throwable e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        public void apply() {
            SysnKV.this.mEditorQueue.add(this);
            Message.obtain(SysnKV.this.mHandler, Work.WHAT_APPLY, SysnKV.this.mEditorQueue);
        }
    }
}
