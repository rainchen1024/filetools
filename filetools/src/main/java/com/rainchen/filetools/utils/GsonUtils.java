package com.rainchen.filetools.utils;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取单例的Gson
 * Created by HeHu on 2017/4/7.
 */

public class GsonUtils {

    private static class LazyHolder {
        private static final Gson INSTANCE = new Gson();
    }

    public static Gson getInstance() {
        return LazyHolder.INSTANCE;
    }


    /**
     * 将jsonObject转成T类型对象
     *
     * @param jsonObject
     * @param clazz
     * @return
     */
    public static <T> T jsonToBean(JsonObject jsonObject, Class<T> clazz) throws
            JsonSyntaxException {
        return getInstance().fromJson(jsonObject, clazz);
    }

    /**
     * 将jsonString转成T类型对象
     *
     * @param jsonString
     * @param clazz
     * @return
     */
    public static <T> T jsonToBean(String jsonString, Class<T> clazz) throws JsonSyntaxException {
        return getInstance().fromJson(jsonString, clazz);
    }


    /**
     * 将jsonArray转成T类型的数组
     *
     * @param jsonArray
     * @param clazz
     * @return
     */
    public static <T> List<T> jsonToList(JsonArray jsonArray, Class<T> clazz) throws
            JsonSyntaxException {
        List<T> list = new ArrayList<T>();
        for (final JsonElement elem : jsonArray) {
            list.add(getInstance().fromJson(elem, clazz));
        }
        return list;
    }

    /**
     * 将jsonObject转成T类型的数组
     *
     * @param jsonObject
     * @param clazz
     * @param data
     * @return
     */
    public static <T> List<T> jsonToList(JsonObject jsonObject, Class<T> clazz, String data) throws
            JsonSyntaxException {
        List<T> list = new ArrayList<T>();
        if (jsonObject.get(data).isJsonNull())return list;
        JsonArray jsonArray = jsonObject.getAsJsonArray(data);
        for (final JsonElement elem : jsonArray) {
            list.add(getInstance().fromJson(elem, clazz));
        }
        return list;
    }

    /**
     * 将jsonString转成T类型的数组
     *
     * @param jsonString
     * @param clazz
     * @return
     */
    public static <T> List<T> jsonToList(String jsonString, Class<T> clazz) throws
            JsonSyntaxException {
        List<T> list = new ArrayList<T>();
        JsonArray jsonArray = new JsonParser().parse(jsonString).getAsJsonArray();
        for (final JsonElement elem : jsonArray) {
            list.add(getInstance().fromJson(elem, clazz));
        }
        return list;
    }
}
