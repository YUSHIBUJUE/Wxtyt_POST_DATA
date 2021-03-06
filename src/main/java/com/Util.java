package com;

import okhttp3.*;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.MarshalException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author: Seayon
 * @date: 2017/12/31
 * @time: 21:11
 */
public class Util {
    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    private static final String WXGAME_URL = "https://mp.weixin.qq.com/wxagame/wxagame_settlement";
    /**
     * 写死的游戏次数 TIMES
     */
    private static final int TIMES = 1000;
    private static final String SESSIONID_ERROR = "SESSIONID有误，请检查";
    private static String game_data = null;
    private static final DecimalFormat decimalFormat1 = new DecimalFormat("#.###");
    private static final DecimalFormat decimalFormat2 = new DecimalFormat("#.##");
    private static final DecimalFormat decimalFormat3 = new DecimalFormat("###");

    /**
     * 该函数使用Key，iv对需要加密的内容进行AES加密，返回加密后又进行Base64编码的字符串
     *
     * @param sessionKey
     * @param encryptedData
     * @param iv
     * @return
     */
    private static String getActionData(String sessionKey, String encryptedData, String iv) {
        byte[] sessionKeyBy = sessionKey.getBytes();
        byte[] en = new byte[0];
        try {
            en = encryptedData.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] ivBy = iv.getBytes();
        byte[] enc = Pkcs7Encoder.encryptOfDiyIV(en, sessionKeyBy, ivBy);
        return new String(Base64.toBase64String(enc));
    }

    /**
     * 该工具类在我的Web提交工具中使用，故构造的参数全部写成静态变量，在类加载时只加载一次，减小服务器开销
     */
    static {
        JSONArray action = new JSONArray();
        JSONArray musicList = new JSONArray();
        JSONArray touchList = new JSONArray();
        JSONArray steps = new JSONArray();
        JSONArray timestamp = new JSONArray();
        /**
         * 伪造的时间戳，这里把开始时间从当前时间减小大概25分钟
         */
        long startTime = System.currentTimeMillis() - 1500000;
        for (int i = 3000; i > 0; i--) {
            steps.put(new JSONArray());
            musicList.put(false);
            JSONArray actionData = new JSONArray();
            double first = Double.valueOf(decimalFormat1.format(Math.random()));
            Double second = Double.valueOf(decimalFormat2.format(Math.random() * 2));
            boolean booleFlag = i / 1500 == 0 ? true : false;
            actionData.put(first);
            actionData.put(second);
            actionData.put(booleFlag);
            action.put(actionData);
            JSONArray touchListData = new JSONArray();
            double tFirst = Double.valueOf(decimalFormat3.format(250 - (Math.random() * 10)));
            double tSecond = Double.valueOf(decimalFormat3.format(670 - (Math.random() * 20)));
            touchListData.put(tFirst);
            touchListData.put(tSecond);
            touchList.put(touchListData);
            long newTime = startTime + Math.round(Math.random() * 2700);
            timestamp.put(newTime);
            startTime = newTime;
        }
        game_data = "action\":" + action.toString() + "," +
                "\"musicList\":" + musicList.toString() + "," +
                "\"touchList\":" + touchList.toString() + "," +
                "\"steps\":" + steps.toString() + "," +
                "\"timestamp:\"" + timestamp + ",\"version\":2}";
    }

    public static String postData(String score, String session_id) {
        String result = null;
        JSONObject actionDataInfo = new JSONObject();
        actionDataInfo.put("score", Integer.valueOf(score));
        actionDataInfo.put("times", TIMES);
        actionDataInfo.put("game_data", "{\"seed\":" + System.currentTimeMillis() + "123" + ",\"" + game_data);
        String AES_KEY = null;
        try {
            AES_KEY = session_id.substring(0, 16);
        } catch (Exception e) {
            return SESSIONID_ERROR;
        }

        String AES_IV = AES_KEY;
        OkHttpClient okHttpClient = new OkHttpClient();

        String actionData = Util.getActionData(AES_KEY, actionDataInfo.toString(), AES_IV);

        String json = "{\"base_req\":{\"session_id\":\"" + session_id + "\",\"fast\":1},\"action_data\":\"" + actionData + "\"}";
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(WXGAME_URL)
                .header("Accept", "*/*")
                .header("Accept-Language", "zh-cn")
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Mobile/14E304 MicroMessenger/6.6.1 NetType/WIFI Language/zh_CN")
                .header("Content-Type", "application/json")
                .header("Referer", "https://servicewechat.com/wx7c8d593b2c3a7703/5/page-frame.html")
                .header("Host", "mp.weixin.qq.com")
                .header("Connection", "keep-alive")
                .post(requestBody)
                .build();
        ResponseBody responseBody = null;
        try {
            responseBody = okHttpClient.newCall(request).execute().body();
            result = responseBody.string();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
        return result;
    }
}
