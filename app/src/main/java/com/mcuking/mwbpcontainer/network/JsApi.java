package com.mcuking.mwbpcontainer.network;

import android.webkit.JavascriptInterface;

import com.mcuking.mwbpcontainer.utils.CalendarReminderUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import wendu.dsbridge.CompletionHandler;

public class JsApi {
    /**
     * 同步日历接口
     * msg 格式如下：
     * {
     *   "id": 日程唯一标识符    字符串
     *   "title": 日程名称   字符串
     *   "location": 日程地址   字符串
     *   "startTime": 日程开始时间  13位时间戳
     *   "endTime": 日程结束时间  13位时间戳
     *   "alarm": [  提前提醒时间，数组，单位分钟
     *      5
     *    ]
     * }
     *
     */
    @JavascriptInterface
    public void syncCalendar(Object msg, CompletionHandler<Integer> handler) {
        try {
            JSONObject obj = new JSONObject(msg.toString());
            String id = obj.getString("id");
            String title = obj.getString("title");
            String location = obj.getString("location");
            long startTime = obj.getLong("startTime");
            long endTime = obj.getLong("endTime");
            JSONArray earlyRemindTime = obj.getJSONArray("alarm");
            String res = CalendarReminderUtils.addCalendarEvent(id, title, location, startTime, endTime, earlyRemindTime);
            handler.complete(Integer.valueOf(res));
        } catch (Exception e) {
            e.printStackTrace();
            handler.complete(6005);
        }
    }
}

