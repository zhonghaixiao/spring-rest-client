package com.github.bingoohuang.springrestclient.utils;

import com.github.bingoohuang.springrestclient.xml.Xmls;
import com.github.bingoohuang.utils.codec.Json;
import org.apache.commons.lang3.StringUtils;

public class Beans {
    public static Object unmarshal(String text, Class<?> clazz) {
        if (StringUtils.isEmpty(text)) return null;

        return text.startsWith("<")
                ? Xmls.unmarshal(text, clazz)
                : Json.unJson(text, clazz);
    }

    public static Object unmarshalArr(String text, Class<?> clazz) {
        return Json.unJsonArray(text, clazz);
    }

}
