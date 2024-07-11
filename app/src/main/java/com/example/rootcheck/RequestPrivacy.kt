package com.example.rootcheck

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestPrivacy(val context: Context) {

    private val SMS_URI_INBOX = "content://sms/inbox"
    private val SMS_URI_SEND = "content://sms/sent"
    private val SMS_URI_DRAFT = "content://sms/draft"
    private val SMS_URI_OUTBOX = "content://sms/outbox"
    private val SMS_URI_FAILED = "content://sms/failed"
    private val SMS_URI_QUEUED = "content://sms/queued"

    // 获取短信
    fun getSmsInPhone(): String {
        val SMS_URI_ALL = "content://sms/"
        var msg = ""

        try {
            val uri = Uri.parse(SMS_URI_ALL)
            val projection = arrayOf("_id", "address", "person", "body", "date", "type")
            val cur = context.contentResolver.query(uri, projection, null, null, "date desc")

            if (cur == null) {
                return "未获取到短信记录"
            }

            if (cur.moveToFirst()) {
                val indexAddress = cur.getColumnIndex("address")
                val indexPerson = cur.getColumnIndex("person")
                val indexBody = cur.getColumnIndex("body")
                val indexDate = cur.getColumnIndex("date")
                val indexType = cur.getColumnIndex("type")

                val smsList = mutableListOf<Sms>()
                while (cur.moveToNext()) {
                    val strAddress = cur.getString(indexAddress)
                    val intPerson = cur.getInt(indexPerson)
                    val strBody = cur.getString(indexBody)
                    val longDate = cur.getLong(indexDate)
                    val intType = cur.getInt(indexType)
                    val strDate = formatDate(longDate)
                    val strType = when (intType) {
                        1 -> "接收"
                        2 -> "发送"
                        3 -> "草稿"
                        4 -> "发件箱"
                        5 -> "发送失败"
                        6 -> "待发送列表"
                        0 -> "所有短信"
                        else -> "null"
                    }

                    val sms = Sms(strAddress, intPerson, strBody, strDate, strType)
                    smsList.add(sms)
                }

                if (!cur.isClosed) {
                    cur.close()
                }

                msg += "共获取到${smsList.size}条短信记录"
                if (smsList.size >= 2) {
                    msg += "\n最近两条短信是：\n\n"
                    msg += smsList[0].toString() + "\n\n"
                    msg += smsList[1].toString()
                }
            } else {
                msg = "未获取到短信记录"
            }

        } catch (ex: SQLiteException) {
            msg = "获取短信失败：\n${ex.message}"
        }

        return msg
    }

    // 获取通话记录
    fun queryCallLog(): String {
        val resolver = context.contentResolver
        val permissionCheck =
            ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "未授权", Toast.LENGTH_SHORT).show()
            return "未授权"
        }

        val cursor = resolver.query(
            CallLog.Calls.CONTENT_URI, arrayOf(
                CallLog.Calls.CACHED_FORMATTED_NUMBER,
                CallLog.Calls.CACHED_MATCHED_NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.GEOCODED_LOCATION
            ), null, null, "date DESC"
        )

        val callRecords = mutableListOf<CallRecord>()
        cursor?.use {
            while (it.moveToNext()) {
                val record = CallRecord(
                    date = it.getLong(4),
                    formattedNumber = it.getString(0),
                    matchedNumber = it.getString(1),
                    name = it.getString(2),
                    type = getCallType(it.getInt(3)),
                    location = it.getString(6),
                    duration = it.getLong(5)
                )
                callRecords.add(record)
            }
        }

        val s = StringBuilder("共获取到${callRecords.size}条通话记录")
        if (callRecords.size >= 2) {
            s.append("\n最近两条为：\n\n")
            s.append(callRecords[0].toString()).append("\n\n")
            s.append(callRecords[1].toString())
        }

        return s.toString()
    }

    // 获取联系人
    fun getContacts(): String {
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null
        )
        val contacts = mutableListOf<MyContact>()

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val temp = MyContact(name = name)

                val phoneCursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    arrayOf(contactId),
                    null
                )

                phoneCursor?.use { pc ->
                    while (pc.moveToNext()) {
                        var phone =
                            pc.getString(pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        phone = phone.replace("-", "").replace(" ", "")
                        temp.phone = phone
                    }
                }

                val noteCursor = context.contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    arrayOf(
                        ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Nickname.NAME
                    ),
                    "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}='${ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE}'",
                    arrayOf(contactId),
                    null
                )

                noteCursor?.use { nc ->
                    if (nc.moveToFirst()) {
                        do {
                            temp.note =
                                nc.getString(nc.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME))
                        } while (nc.moveToNext())
                    }
                }

                contacts.add(temp)
            }
        }

        val s = StringBuilder("获取到的联系人数量：${contacts.size}\n")
        if (contacts.size >= 2) {
            s.append("前两条是：\n\n")
            s.append(contacts[0].toString()).append("\n\n")
            s.append(contacts[1].toString())
        }

        return s.toString()
    }

    fun getPhoneState(): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val permissionCheck =
            ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "未授权", Toast.LENGTH_SHORT).show()
            return "未授权"
        }

        val NativePhoneNumber = tm.line1Number
        var ProvidersName = "未知"

        try {
            val IMSI = tm.subscriberId
            if (IMSI != null) {
                ProvidersName = when {
                    IMSI.startsWith("46000") || IMSI.startsWith("46002") -> "中国移动"
                    IMSI.startsWith("46001") -> "中国联通"
                    IMSI.startsWith("46003") -> "中国电信"
                    else -> "未知"
                }
            }
        } catch (e: Exception) {
            Log.e("Tag", "获取运营商失败：${e.message}")
        }

        val sb = StringBuilder()
        try {
            sb.append("手机号码：").append(NativePhoneNumber).append("\n")
            sb.append("运营商：").append(ProvidersName)
//            sb.append("\n----\n序列号 = ").append(android.os.Build.getSerial())
//            sb.append("\n----\n设备ID = ").append(tm.deviceId)
//            sb.append("\n----\nIMEI = ").append(tm.imei)
//            sb.append("\n----\nmeid = ").append(tm.meid)
//            sb.append("\n----\n网络接入身份 = ").append(tm.nai)
            sb.append("\n----\n设备的软件版本号 = ").append(tm.deviceSoftwareVersion)
            sb.append("\n----\n国际长途区号 = ").append(tm.networkCountryIso)
            sb.append("\n----\nMCC+MNC(判断运营商的) = ").append(tm.networkOperator)
            sb.append("\n----\n运营商名字 = ").append(tm.networkOperatorName)
            sb.append("\n----\n网络类型(4G/Wifi/3G等)代码 = ").append(tm.networkType)
            sb.append("\n----\n手机信号类型 = ").append(tm.phoneType)
            sb.append("\n----\n国际长途区号(SIM卡返回) = ").append(tm.simCountryIso)
            sb.append("\n----\nMCC+MNC(SIM卡返回) = ").append(tm.simOperator)
            sb.append("\n----\n运营商名字(SIM卡返回) = ").append(tm.simOperatorName)
//            sb.append("\n----\nSIM卡序列号 = ").append(tm.simSerialNumber)
            sb.append("\n----\nSIM卡状态代码(0~5) = ").append(tm.simState)
            sb.append("\n----\n语音邮箱号码 = ").append(tm.voiceMailNumber)
            sb.append("\n----\n语音邮箱包名 = ").append(tm.visualVoicemailPackageName)
            sb.append("\n----\n语音邮件相关的标签 = ").append(tm.voiceMailAlphaTag)
            sb.append("\n----\n群组身份Lv1 = ").append(tm.groupIdLevel1)
            sb.append("\n----\nCarrierConfig = ").append(tm.carrierConfig)
            sb.append("\n----\n数据网络类型 = ").append(tm.dataNetworkType)
            sb.append("\n----\n语音网络类型 = ").append(tm.voiceNetworkType)
        }catch (e: Exception) {
            Log.e("TAG ", "获取手机状态失败：${e.message}")
        }
        val Plmns = tm.forbiddenPlmns
        sb.append("\n----\nForbiddenPlmns = ")
        if (Plmns != null) {
            for (plmn in Plmns) {
                sb.append("\n").append(plmn)
            }
        } else {
            sb.append("\nnull")
        }
        sb.append("\n----\n服务状态信息 = ").append(tm.serviceState)

        return sb.toString()
    }

    private fun getCallType(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "呼入"
            CallLog.Calls.OUTGOING_TYPE -> "呼出"
            CallLog.Calls.MISSED_TYPE -> "未接"
            else -> "未知"
        }
    }

    data class MyContact(
        var name: String = "", var note: String? = null, var phone: String? = null
    ) {
        override fun toString(): String {
            return "姓名 ${name.substring(0, 1)}XX(马赛克)\n" + "号码 ${
                phone?.substring(
                    0,
                    3
                )
            }--------(隐私保护)"
        }
    }

    data class CallRecord(
        val date: Long,
        val formattedNumber: String?,
        val matchedNumber: String?,
        val name: String?,
        val type: String?,
        val location: String?,
        val duration: Long
    ) {

        override fun toString(): String {
            return "时间 ${formatDate(date)}" + "\n号码 ${
                matchedNumber?.substring(
                    0,
                    3
                )
            }--------(隐私保护)" + "\n联系人姓名 $name" + "\n类型 $type" + "\n运营商地址 ${
                location?.substring(
                    0,
                    3
                )
            }■■市(马赛克)" + "\n通话时长 ${formatDuration(duration)}"
        }
    }

    data class Sms(
        val phoneNumber: String,
        val person: Int,
        val body: String,
        val date: String,
        val type: String
    ) {
        override fun toString(): String {
            return "对方号码 ${
                phoneNumber.substring(
                    0,
                    6
                )
            }--------(隐私保护)" + "\n信息内容 $body" + "\n日期 $date" + "\n类型 $type"
        }
    }
}

fun formatDuration(time: Long): String {
    val s = time % 60
    val m = time / 60
    val h = time / 60 / 60
    val sb = StringBuilder()
    if (h > 0) {
        sb.append(h).append("小时")
    }
    if (m > 0) {
        sb.append(m).append("分")
    }
    sb.append(s).append("秒")
    return sb.toString()
}

fun formatDate(time: Long): String {
    val format: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(Date(time))
}