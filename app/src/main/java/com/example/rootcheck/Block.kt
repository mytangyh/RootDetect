package com.example.rootcheck;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Author : Administrator
 * Time : 2024/01/22
 * Desc :
 */
public class Block {
    private Map<String, Object> value;
    private File mFile;
    //版本id
    private Integer mId;
    private RandomAccessFile mAccessFile;
    private FileChannel mChannel;

    public Block(File file) throws IOException {
        this.mFile = file;
        if (!mFile.exists() || !mFile.isFile()) {
            File dir = mFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            mFile.createNewFile();
        }
        value = new HashMap<>();
    }

    public Map<String, Object> getValue() {
        sync();
        return value;
    }

    public long getSize() {
        return mFile.length();
    }

    public boolean write() {
        return doMap2File();
    }

    void sync() {
        ByteBuffer buffer = null;
        FileLock lock = null;
        try {
            //读mid
            lock = lock(0, 4, true);
            buffer = ByteBuffer.allocate(4);
            int size = mChannel.read(buffer, 0);
            unLock(lock);
            if (size == 4) {
                buffer.flip();
                //比较mid
                int mid = buffer.getInt();
                //当前mid为空，没同步过，同步，mid不一致，同步
                if (Block.this.mId == null || Block.this.mId != mid) {
                    doFile2Map();
                    //同步完成，更新mid
                    Block.this.mId = mid;
                }
            }
        } catch (Throwable e) {
            //读取mid出io异常
            unLock(lock);
            e.printStackTrace();
        } finally {
            if (buffer != null) {
                buffer.clear();
            }
        }
    }

    private FileLock lock(long position, long size, boolean shared) {
        try {
            if (mAccessFile == null || mChannel == null || !mChannel.isOpen()) {
                mAccessFile = new RandomAccessFile(mFile, "rw");
                mChannel = mAccessFile.getChannel();
            }
            if (mChannel != null && mChannel.isOpen()) {
                size = Math.min(size, mAccessFile.length());
                return mChannel.lock(position, size, shared);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void unLock(FileLock lock) {
        if (lock != null) {
            try {
                lock.release();
                release();
            } catch (IOException e) {
                e.printStackTrace();
            }
            lock = null;
        }
    }

    private void release() {
        if (mChannel != null) {
            try {
                mChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mChannel = null;
        }
        if (mAccessFile != null) {
            try {
                mAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mAccessFile = null;
        }
    }

    private void doFile2Map() {
        FileLock lock = lock(5, Long.MAX_VALUE, true);
        try {
            //前4位是mid,跳过
            mChannel.position(4);
            ByteBuffer buffer = ByteBuffer.allocate((int) (mChannel.size() - 4));

            int len = mChannel.read(buffer);
            if (len == -1) {
                return;
            }
            buffer.flip();
            value.clear();
            JSONObject object = new JSONObject(StandardCharsets.UTF_8.decode(buffer).toString());
            for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                String k = it.next();
                value.put(k, object.get(k));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            unLock(lock);
            try {
                mFile.delete();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            unLock(lock);
        }
    }

    private boolean doMap2File() {
        boolean result = false;
        FileLock lock = lock(0, Long.MAX_VALUE, false);
        try {
            JSONObject object = new JSONObject(value);
            byte[] bt = object.toString(0).getBytes(StandardCharsets.UTF_8);
            ByteBuffer buf = ByteBuffer.allocate(bt.length + 4);
            if (mId == null) {
                mId = Integer.MIN_VALUE;
            } else {
                mId = (mId + 1) % (Integer.MAX_VALUE - 10);
            }
            buf.putInt(mId);
            buf.put(bt);
            buf.flip();
            mChannel.position(0);
            while (buf.hasRemaining()) {
                mChannel.write(buf);
            }
            mChannel.truncate(4 + bt.length);
            mChannel.force(true);
            result = true;
        } catch (IOException e) {
            //todo 写入文件失败,用备份文件方式处理
            e.printStackTrace();
        } catch (JSONException e) {
            //map转json串会出异常?先不处理,最多就是数据存不进去
            //可能map存储了含有特殊字符串的value会有这个异常.
            e.printStackTrace();
        } finally {
            unLock(lock);
        }
        return result;
    }
}
