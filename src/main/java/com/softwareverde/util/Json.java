//  ****************************************************************************
//  The MIT License (MIT)
//
//  Copyright (c) 2016 Joshua Green <josh@softwareverde.com>
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
//  ****************************************************************************

package com.softwareverde.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
    - Json is a wrapper for JSONObject and JSONArray, and is Jsonable-aware:
        This means that Jsonable objects have their toJson() method invoked
        before they are compiled into Json.

    - Json is intelligently either an array or an object. By invoking the
        put(key, value) method, the Json object will become an Object. If
        values exist within the Json object before an invocation of put(),
        then the values will be arbitrarily indexed as stringified-integers;
        the index used will be smallest unused non-negative integer.
*/

public class Json implements Jsonable {
    private synchronized static void debug(final String str) {
        System.out.println("com.softwareverde.util :: Json :: "+ str);
    }

    public static Json fromString(String string) {
        final Json json = new Json();
        string = string.trim();

        if (string.length() > 0) {
            try {
                if (string.charAt(0) == '[') {
                    json._jsonArray = new JSONArray(string);
                    json._isArray = true;
                }
                else {
                    json._jsonObject = new JSONObject(string);
                    json._isArray = false;
                }
            } catch (JSONException e) {
                Json.debug("Exception 0: " + e.getMessage());
            }
        }

        return json;
    }

    private Boolean _isArray = false;
    private JSONObject _jsonObject;
    private JSONArray _jsonArray;

    public Json() {
        this._jsonObject = new JSONObject();
        this._jsonArray = new JSONArray();

        this._isArray = true;
    }

    public <T> Json(final Collection<T> c) {
        this._jsonObject = new JSONObject();
        this._jsonArray = new JSONArray();

        for (T item : c) {
            if (item instanceof Jsonable) {
                Json json = ((Jsonable) item).toJson();
                if (json.isArray()) {
                    this._jsonArray.put(json._jsonArray);
                }
                else {
                    this._jsonArray.put(json._jsonObject);
                }
            }
            else {
                this._jsonArray.put(item);
            }
        }
        this._isArray = true;
    }

    public <T> Json(final Map<String, T> c) {
        this._jsonObject = new JSONObject();
        this._jsonArray = new JSONArray();

        for (Map.Entry<String, T> item : c.entrySet()) {
            if (item instanceof Jsonable) {
                Json json = ((Jsonable) item).toJson();
                if (json.isArray()) {
                    try {
                        this._jsonObject.put(item.getKey(), json._jsonArray);
                    } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
                }
                else {
                    try {
                        this._jsonObject.put(item.getKey(), json._jsonObject);
                    } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
                }
            }
            else {
                try {
                    this._jsonObject.put(item.getKey(), item.getValue());
                } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
            }
        }
        this._isArray = false;
    }

    public Boolean isArray() {
        return this._isArray;
    }

    public Integer length() {
        if (this.isArray()) {
            return this._jsonArray.length();
        }
        else {
            return this._jsonObject.length();
        }
    }

    public <T> void add(final T value) {
        if (this.isArray()) {
            if (value instanceof Jsonable) {
                Json json = ((Jsonable) value).toJson();
                if (json.isArray()) {
                    this._jsonArray.put(json._jsonArray);
                }
                else {
                    this._jsonArray.put(json._jsonObject);
                }
            }
            else {
                this._jsonArray.put(value);
            }
        }
        else {
            // Append the item while keeping the object-form.. Index will be a stringified int of the lowest possible non-negative value.
            Integer index = 0;
            while (this._jsonObject.has(index.toString())) index++;
            if (value instanceof Jsonable) {
                Json json = ((Jsonable) value).toJson();
                if (json.isArray()) {
                    try {
                        this._jsonObject.put(index.toString(), json._jsonArray);
                    } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
                }
                else {
                    try {
                        this._jsonObject.put(index.toString(), json._jsonObject);
                    } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
                }
            }
            else {
                try {
                    this._jsonObject.put(index.toString(), value);
                } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
            }
        }
    }

    public <T> void put(final String key, final T value) {
        // If Json has objects within array, copy them as object-values with stringified integer indexes..
        Integer index = 0;
        while (this._jsonArray.length() > 0) {
            try {
                this._jsonObject.put(index.toString(), this._jsonArray.remove(0));
            } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
            index++;
        }
        this._isArray = false;

        if (value instanceof Jsonable) {
            Json json = ((Jsonable) value).toJson();
            if (json.isArray()) {
                try {
                    this._jsonObject.put(key, json._jsonArray);
                } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
            }
            else {
                try {
                    this._jsonObject.put(key, json._jsonObject);
                } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
            }
        }
        else {
            try {
                this._jsonObject.put(key, value);
            } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
        }
    }

    public static class Types {
        public static final String  STRING  = "";
        public static final Integer INTEGER = 0;
        public static final Long    LONG    = 0L;
        public static final Double  DOUBLE  = 0D;
        public static final Float   FLOAT   = 0F;
        public static final Boolean BOOLEAN = false;
        public static final Json    OBJECT  = new Json();
        public static final Json    ARRAY   = new Json();
        public static final Json    JSON    = new Json();
    }

    @SuppressWarnings("unchecked")
    private static final <T> T _convert(final Object obj, final T type) {
        T value = null;
        if (type instanceof String) {
            if (obj instanceof String) {
                value = (T) obj;
            }
            else {
                value = (T) obj.toString();
            }
        }
        else if (type instanceof Integer) {
            if (obj instanceof Integer) {
                value = (T) obj;
            }
            else if (obj instanceof String) {
                try {
                    value = (T) Integer.valueOf(Integer.parseInt((String) obj));
                }
                catch (Exception e) { Json.debug("Exception 1: "+ e.getMessage()); }
            }
            else {
                Json.debug("WARNING: Returning null for Json._convert. (Integer)");
            }
        }
        else if (type instanceof Long) {
            if (obj instanceof Long) {
                value = (T) obj;
            }
            else if (obj instanceof Integer) {
                value = (T) Long.valueOf(((Integer) obj).longValue());
            }
            else if (obj instanceof String) {
                try {
                    value = (T) Long.valueOf(Long.parseLong((String) obj));
                }
                catch (Exception e) { Json.debug("Exception 1: "+ e.getMessage()); }
            }
            else {
                Json.debug("WARNING: Returning null for Json._convert. (Long)");
            }
        }
        else if (type instanceof Double) {
            if (obj instanceof Double) {
                value = (T) obj;
            }
            else if (obj instanceof Float) {
                value = (T) Double.valueOf(((Float) obj).doubleValue());
            }
            else if (obj instanceof Integer) {
                value = (T) Double.valueOf((Integer) obj);
            }
            else if (obj instanceof Long) {
                value = (T) Double.valueOf((Long) obj);
            }
            else if (obj instanceof String) {
                try {
                    value = (T) Double.valueOf(Double.parseDouble((String) obj));
                }
                catch (Exception e) { Json.debug("Exception 2: "+ e.getMessage()); }
            }
            else {
                Json.debug("WARNING: Returning null for Json._convert. (Double)");
            }
        }
        else if (type instanceof Float) {
            if (obj instanceof Float) {
                value = (T) obj;
            }
            else if (obj instanceof Double) {
                value = (T) Float.valueOf(((Double) obj).floatValue());
            }
            else if (obj instanceof Integer) {
                value = (T) Float.valueOf((Integer) obj);
            }
            else if (obj instanceof Long) {
                value = (T) Float.valueOf((Long) obj);
            }
            else if (obj instanceof String) {
                try {
                    value = (T) Float.valueOf(Float.parseFloat((String) obj));
                }
                catch (Exception e) { Json.debug("Exception 4: "+ e.getMessage()); }
            }
            else {
                Json.debug("WARNING: Returning null for Json._convert. (Float)");
            }
        }
        else if (type instanceof Boolean) {
            if (obj instanceof Boolean) {
                value = (T) obj;
            }
            if (obj instanceof Integer) {
                value = (T) Boolean.valueOf((Integer) obj > 0);
            }
            else if (obj instanceof Long) {
                value = (T) Boolean.valueOf((Long) obj > 0);
            }
            else if (obj instanceof String) {
                try {
                    value = (T) Boolean.valueOf((Integer.parseInt((String) obj) > 0));
                }
                catch (Exception e) { Json.debug("Exception 3: "+ e.getMessage()); }
            }
            else {
                Json.debug("WARNING: Returning null for Json._convert. (Boolean)");
            }
        }
        else if (type instanceof Json) {
            if (obj instanceof Json) {
                value = (T) obj;
            }
            else if (obj instanceof JSONObject) {
                Json json = new Json();
                json._jsonObject = (JSONObject) obj;
                json._isArray = false;
                value = (T) json;
            }
            else if (obj instanceof JSONArray) {
                Json json = new Json();
                json._jsonArray = (JSONArray) obj;
                json._isArray = true;
                value = (T) json;
            }
            else if (obj instanceof String) {
                value = (T) Json.fromString((String) obj);
            }
            else {
                Json.debug("WARNING: Returning empty Json for Json._convert. (Json)");
                value = (T) new Json();
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static <T> T _getInstance(final T type) {
        if (type instanceof String) {
            return (T) "";
        }
        else if (type instanceof Integer) {
            return (T) Integer.valueOf(0);
        }
        else if (type instanceof Long) {
            return (T) Long.valueOf(0);
        }
        else if (type instanceof Float) {
            return (T) Float.valueOf(0);
        }
        else if (type instanceof Double) {
            return (T) Double.valueOf(0);
        }
        else if (type instanceof Boolean) {
            return (T) Boolean.valueOf(false);
        }
        else if (type instanceof Json) {
            return (T) new Json();
        }
        else return null;
    }

    public String getString(final String key) { return this.get(key, Types.STRING); }
    public Integer getInteger(final String key) { return this.get(key, Types.INTEGER); }
    public Long getLong(final String key) { return this.get(key, Types.LONG); }
    public Float getFloat(final String key) { return this.get(key, Types.FLOAT); }
    public Double getDouble(final String key) { return this.get(key, Types.DOUBLE); }
    public Boolean getBoolean(final String key) { return this.get(key, Types.BOOLEAN); }
    public Json get(String key) { return this.get(key, Json.Types.JSON); }

    public <T> T get(String key, T type) {
        if (! this.isArray()) {
            if (this._jsonObject.has(key)) {
                try {
                    return Json._convert(this._jsonObject.get(key), type);
                }
                catch (Exception e) { Json.debug("Exception 4: "+ e.getMessage()); }
            }
        }

        if (type instanceof Json) return (T) new Json();
        // else return (T) _getInstance(type);
        else return type;
    }

    public String getString(final int index) { return this.get(index, Types.STRING); }
    public Integer getInteger(final int index) { return this.get(index, Types.INTEGER); }
    public Long getLong(final int index) { return this.get(index, Types.LONG); }
    public Float getFloat(final int index) { return this.get(index, Types.FLOAT); }
    public Double getDouble(final int index) { return this.get(index, Types.DOUBLE); }
    public Boolean getBoolean(final int index) { return this.get(index, Types.BOOLEAN); }
    public Json get(final int index) {
        return this.get(index, Json.Types.JSON);
    }
    public <T> T get(final int index, final T type) {
        if (this.isArray()) {
            if (index < this._jsonArray.length() && index >= 0) {
                try {
                    return _convert(this._jsonArray.get(index), type);
                }
                catch (Exception e) { Json.debug("Exception 5: "+ e.getMessage()); }
            }
        }

        if (type instanceof Json) return (T) new Json();
        // else return (T) _getInstance(type);
        else return type;
    }

    public Boolean hasKey(final String key) {
        if (this.isArray()) {
            return false;
        }

        return (this._jsonObject.has(key));
    }

    public List<String> getKeys() {
        final List<String> keys = new ArrayList<String>();

        if (this.isArray()) {
            for (Integer i=0; i<this.length(); i++) {
                keys.add(i.toString());
            }
        }
        else {
            Iterator<String> keysIt = this._jsonObject.keys();
            while (keysIt.hasNext()) {
                keys.add(keysIt.next());
            }
        }
        return keys;
    }

    @Override
    public String toString() {
        if (this.isArray()) {
            final JSONArray out = new JSONArray();
            for (int i=0; i<this._jsonArray.length(); i++) {
                try {
                    if (this._jsonArray.get(i) instanceof Jsonable) {
                        final Json j = ((Jsonable) this._jsonArray.get(i)).toJson();
                        if (j.isArray()) {
                            out.put(j._jsonArray);
                        }
                        else {
                            out.put(j._jsonObject);
                        }
                    }
                    else {
                        out.put(this._jsonArray.get(i));
                    }
                } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
            }
            return out.toString();
        }
        else {
            final JSONObject out = new JSONObject();
            final Iterator<String> it = this._jsonObject.keys();
            while (it.hasNext()) {
                final String key = it.next();
                try {
                    if (this._jsonObject.get(key) instanceof Jsonable) {
                        final Json j = ((Jsonable) this._jsonObject.get(key)).toJson();
                        if (j.isArray()) {
                            out.put(key, j._jsonArray);
                        }
                        else {
                            out.put(key, j._jsonObject);
                        }
                    }
                    else {
                        out.put(key, this._jsonObject.get(key));
                    }
                } catch (JSONException e) { Json.debug("Exception: "+ e.getMessage()); }
            }
            return out.toString();
        }
    }

    @Override
    public Json toJson() {
        return this;
    }
}
