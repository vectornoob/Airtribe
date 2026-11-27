package com.taskmanager.util;

import java.util.Map;

/** Small helper for reading the flat string-valued JSON object returned by the task parser. */
public final class SimpleJson {
    private SimpleJson() {}
    public static Map<String, String> readStringObject(String json) {
        return Json.readStringObject(json);
    }
}
