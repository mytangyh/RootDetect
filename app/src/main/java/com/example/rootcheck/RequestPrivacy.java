package com.example.rootcheck;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestPrivacy
{

    Context context;

    final String SMS_URI_INBOX = "content://sms/inbox"; // 收件箱
    final String SMS_URI_SEND = "content://sms/sent"; // 已发送
    final String SMS_URI_DRAFT = "content://sms/draft"; // 草稿
    final String SMS_URI_OUTBOX = "content://sms/outbox"; // 发件箱
    final String SMS_URI_FAILED = "content://sms/failed"; // 发送失败
    final String SMS_URI_QUEUED = "content://sms/queued"; // 待发送列表

    //获取短信
    public void getSmsInPhone(View view)
    {
        final String SMS_URI_ALL = "content://sms/"; // 所有短信

        String msg = "";
        try
        {
            Uri uri = Uri.parse(SMS_URI_ALL);
            String[] projection = new String[]{"_id", "address", "person",
                    "body", "date", "type",};
            Cursor cur = context.getContentResolver().query(uri, projection, null,
                    null, "date desc"); // 获取手机内部短信
            if (cur == null)
            {
                return;
            }
            // 获取短信中最新的未读短信
            // Cursor cur = getContentResolver().query(uri, projection,
            // "read = ?", new String[]{"0"}, "date desc");
            if (cur.moveToFirst())
            {
                int index_Address = cur.getColumnIndex("address");
                int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                int index_Type = cur.getColumnIndex("type");

                List<Sms> smsList = new ArrayList<>();
                while (cur.moveToNext())
                {
                    String strAddress = cur.getString(index_Address);
                    int intPerson = cur.getInt(index_Person);
                    String strBody = cur.getString(index_Body);
                    long longDate = cur.getLong(index_Date);
                    int intType = cur.getInt(index_Type);
                    String strDate = formatDate(longDate);
                    String strType;
                    switch (intType)
                    {
                        case 1:
                            strType = "接收";
                            break;
                        case 2:
                            strType = "发送";
                            break;
                        case 3:
                            strType = "草稿";
                            break;
                        case 4:
                            strType = "发件箱";
                            break;
                        case 5:
                            strType = "发送失败";
                            break;
                        case 6:
                            strType = "待发送列表";
                            break;
                        case 0:
                            strType = "所有短信";
                            break;
                        default:
                            strType = "null";
                            break;
                    }

                    Sms sms = new Sms();
                    sms.PhoneNumber = strAddress;
                    sms.Body = strBody;
                    sms.Date = strDate;
                    sms.Person = intPerson;
                    sms.Type = strType;
                    smsList.add(sms);
                }

                if (!cur.isClosed())
                {
                    cur.close();
                }
                msg += "共获取到" + smsList.size() + "条短信记录";
                if (smsList.size() >= 2)
                {
                    msg += "\n最近两条短信是：\n\n";
                    msg += smsList.get(0).toString() + "\n\n";
                    msg += smsList.get(1).toString();
                }
            }
            else
            {
                msg = "未获取到短信记录";
            }

        }
        catch (SQLiteException ex)
        {
            msg = "获取短信失败：\n" + ex.getMessage();
        }
    }


    //获取通话记录
    public void queryCallLog(View view)
    {
        ContentResolver resolver = context.getContentResolver();
        //获取cursor对象
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(context, "未授权", Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, new String[]{
                CallLog.Calls.CACHED_FORMATTED_NUMBER,
                CallLog.Calls.CACHED_MATCHED_NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.GEOCODED_LOCATION,
        }, null, null, "date DESC");

        //"date DESC" 按时间排序
        List<CallRecord> callRecords = new ArrayList<>();
        if (cursor != null)
        {
            try
            {
                while (cursor.moveToNext())
                {
                    CallRecord record = new CallRecord();
                    record.formatted_number = cursor.getString(0);
                    record.matched_number = cursor.getString(1);
                    record.name = cursor.getString(2);
                    record.type = getCallType(cursor.getInt(3));
                    record.date = cursor.getLong(4);
                    record.duration = cursor.getLong(5);
                    record.location = cursor.getString(6);
                    callRecords.add(record);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                cursor.close();  //关闭cursor，避免内存泄露
            }
        }
        String s = "共获取到" + callRecords.size() + "条通话记录";
        if (callRecords.size() >= 2)
        {
            s += "\n最近两条为：\n\n";
            s += callRecords.get(0).toString() + "\n\n";
            s += callRecords.get(1).toString();
        }
    }

    //获取联系人
    public void getContacts(View view)
    {
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        List<MyContact> contacts = new ArrayList<>();
        while (cursor.moveToNext())
        {
            //新建一个联系人实例
            MyContact temp = new MyContact();
            String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));
            //获取联系人姓名
            temp.name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            //获取联系人电话号码
            Cursor phoneCursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                    null, null);
            while (phoneCursor.moveToNext())
            {
                String phone = phoneCursor.getString(phoneCursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                phone = phone.replace("-", "");
                phone = phone.replace(" ", "");
                temp.phone = phone;
            }

            //获取联系人备注信息
            Cursor noteCursor = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data._ID,
                            ContactsContract.CommonDataKinds.Nickname.NAME},
                    ContactsContract.Data.CONTACT_ID + "=?" + " AND " +
                            ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'",
                    new String[]{contactId}, null);
            if (noteCursor.moveToFirst())
            {
                do
                {
                    temp.note = noteCursor.getString(noteCursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                } while (noteCursor.moveToNext());
            }
            contacts.add(temp);
            phoneCursor.close();
            noteCursor.close();
        }
        cursor.close();

        StringBuilder s = new StringBuilder();
        s.append("获取到的联系人数量：").append(contacts.size()).append("\n");
        if (contacts.size() >= 2)
        {
            s.append("前两条是：\n\n");
            s.append(contacts.get(0).toString()).append("\n\n");
            s.append(contacts.get(1).toString());
        }
    }

    @SuppressLint("HardwareIds")
    public void getPhoneState(View view)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int res = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (res != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(context, "未授权", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取电话号码
        String NativePhoneNumber = tm.getLine1Number();
        //获取运营商
        String ProvidersName = "未知";

        //注意：targetSdkVersion要改成小于29才行，否则报错
        String IMSI = tm.getSubscriberId();
        // IMSI号前面3位460是国家，紧接着后面2位00/02是中国移动，01是中国联通，03是中国电信。
        if(IMSI!=null)
        {
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002"))
            {
                ProvidersName = "中国移动";
            }
            else if (IMSI.startsWith("46001"))
            {
                ProvidersName = "中国联通";
            }
            else if (IMSI.startsWith("46003"))
            {
                ProvidersName = "中国电信";
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("手机号码：").append(NativePhoneNumber).append("\n");
        sb.append("运营商：").append(ProvidersName);
        sb.append("\n----\n序列号 = ").append(android.os.Build.getSerial());
        sb.append("\n----\n设备ID = ").append(tm.getDeviceId());
        sb.append("\n----\nIMEI = ").append(tm.getImei());
        sb.append("\n----\nmeid = ").append(tm.getMeid());
        sb.append("\n----\n网络接入身份 = ").append(tm.getNai());
        sb.append("\n----\n设备的软件版本号 = ").append(tm.getDeviceSoftwareVersion());
        sb.append("\n----\n国际长途区号 = ").append(tm.getNetworkCountryIso());
        sb.append("\n----\nMCC+MNC(判断运营商的) = ").append(tm.getNetworkOperator());
        sb.append("\n----\n运营商名字 = ").append(tm.getNetworkOperatorName());
        sb.append("\n----\n网络类型(4G/Wifi/3G等)代码 = ").append(tm.getNetworkType());
        sb.append("\n----\n手机信号类型 = ").append(tm.getPhoneType());
        sb.append("\n----\n国际长途区号(SIM卡返回) = ").append(tm.getSimCountryIso());
        sb.append("\n----\nMCC+MNC(SIM卡返回) = ").append(tm.getSimOperator());
        sb.append("\n----\n运营商名字(SIM卡返回) = ").append(tm.getSimOperatorName());
        sb.append("\n----\nSIM卡序列号 = ").append(tm.getSimSerialNumber());
        sb.append("\n----\nSIM卡状态代码(0~5) = ").append(tm.getSimState());
        sb.append("\n----\n语音邮箱号码 = ").append(tm.getVoiceMailNumber());
        sb.append("\n----\n语音邮箱包名 = ").append(tm.getVisualVoicemailPackageName());
        sb.append("\n----\n语音邮件相关的标签 = ").append(tm.getVoiceMailAlphaTag());
        sb.append("\n----\n群组身份Lv1 = ").append(tm.getGroupIdLevel1());
        sb.append("\n----\nCarrierConfig = ").append(tm.getCarrierConfig());
        sb.append("\n----\n数据网络类型 = ").append(tm.getDataNetworkType());
        sb.append("\n----\n语音网络类型 = ").append(tm.getVoiceNetworkType());
        String[] Plmns=tm.getForbiddenPlmns();
        sb.append("\n----\nForbiddenPlmns = ");
        if (Plmns != null)
        {
            for (String plmn : Plmns)
            {
                sb.append("\n").append(plmn);
            }
        }
        else
        {
            sb.append("\nnull");
        }
        sb.append("\n----\n服务状态信息 = ").append(tm.getServiceState());
    }

    private String getCallType(int anInt)
    {
        switch (anInt)
        {
            case CallLog.Calls.INCOMING_TYPE:
                return "呼入";
            case CallLog.Calls.OUTGOING_TYPE:
                return "呼出";
            case CallLog.Calls.MISSED_TYPE:
                return "未接";
            default:
                break;
        }
        return null;
    }

    public String formatDate(long time)
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date(time));
    }

    public String formatDuration(long time)
    {
        long s = time % 60;
        long m = time / 60;
        long h = time / 60 / 60;
        StringBuilder sb = new StringBuilder();
        if (h > 0)
        {
            sb.append(h).append("小时");
        }
        if (m > 0)
        {
            sb.append(m).append("分");
        }
        sb.append(s).append("秒");
        return sb.toString();
    }

    static class MyContact
    {
        private String name;
        private String note;
        private String phone;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getNote()
        {
            return note;
        }

        public void setNote(String note)
        {
            this.note = note;
        }

        public String getPhone()
        {
            return phone;
        }

        public void setPhone(String phone)
        {
            this.phone = phone;
        }

        @NonNull
        @Override
        public String toString()
        {
            return "姓名 " + name.substring(0, 1) + "XX(马赛克)\n" +
                    "号码 " + phone.substring(0, 3) + "--------(隐私保护)";
        }
    }

    class CallRecord
    {
        long date;
        String formatted_number;
        String matched_number;
        String name;
        String type;
        String location;
        long duration;

        @NonNull
        @Override
        public String toString()
        {
            return "时间 " + formatDate(date) +
                    "\n号码 " + matched_number.substring(0, 3) + "--------(隐私保护)" +
                    "\n联系人姓名 " + name +
                    "\n类型 " + type +
                    "\n运营商地址 " + location.substring(0, 3) + "■■市(马赛克)" +
                    "\n通话时长 " + formatDuration(duration);
        }

    }

    static class Sms
    {
        String PhoneNumber;
        int Person;
        String Body;
        String Date;
        String Type;

        @NonNull
        @Override
        public String toString()
        {
            return "对方号码 " + PhoneNumber.substring(0, 6) + "--------(隐私保护)" +
                    "\n信息内容 " + Body +
                    "\n日期 " + Date +
                    "\n类型 " + Type;
        }
    }
}
