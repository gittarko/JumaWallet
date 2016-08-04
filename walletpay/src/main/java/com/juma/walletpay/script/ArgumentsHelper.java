package com.juma.walletpay.script;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Administrator on 2016/8/3 0003.
 */

public class ArgumentsHelper {
    private String raw;
    private JSONArray jsonArgsArray;
    private Class<?>[] argsTypes;
    private Object[] argsObjects;
    private boolean hasNullArg;

    public ArgumentsHelper(String jsonArgs) {
        try {
            this.jsonArgsArray = new JSONArray(jsonArgs);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid JSON arguments from JS!", e);
        }

        this.argsTypes = initArgsTypes();
        this.argsObjects = initArgs();
        this.hasNullArg = hasNull(argsObjects);
    }

    public String getRaw() {
        return raw;
    }

    private Class<?>[] initArgsTypes() {
        Class<?>[] argsTypes = new Class<?>[jsonArgsArray.length()];
        for (int i = 0; i < jsonArgsArray.length(); i++) {
            try {
                argsTypes[i] = jsonArgsArray.get(i).getClass();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return argsTypes;
    }

    private Object[] initArgs() {
        Object[] args = new Object[jsonArgsArray.length()];
        for (int i = 0; i < jsonArgsArray.length(); i++) {
            try {
                if (jsonArgsArray.isNull(i)) {
                    args[i] = null;
                } else {
                    args[i] = jsonArgsArray.get(i);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return args;
    }

    public static boolean hasNull(Object[] objects) {
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    public Object[] getArgs() {
        return argsObjects;
    }

    public Class<?>[] getArgsTypes() {
        return argsTypes;
    }

    public boolean hasNullArg() {
        return hasNullArg;
    }

}
