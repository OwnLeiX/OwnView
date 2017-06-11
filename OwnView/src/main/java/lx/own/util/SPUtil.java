/*
 * Copyright 2016 Freelander
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lx.own.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * <p>SharedPreference工具类</p><br/>
 *
 * @author Lx
 * @date 2017/2/7
 */
public class SPUtil {

    private final static String SP = "config";

    private SharedPreferences mPreferences;

    private SharedPreferences.Editor mEditor;

    private volatile static SPUtil mInstance;

    public static SPUtil $(Context context) {
        if (mInstance == null) {
            synchronized (SPUtil.class) {
                if (mInstance == null)
                    mInstance = new SPUtil(context.getApplicationContext());
            }
        }
        return mInstance;
    }

    private SPUtil(Context context) {
        mPreferences = context.getSharedPreferences(SP, Context.MODE_PRIVATE);
    }

    public boolean putString(String key, String value) {
        mEditor = mPreferences.edit();
        mEditor.putString(key, value);
        return mEditor.commit();
    }

    public String getString(String key) {
        return mPreferences.getString(key, "");
    }

    public String getString(String key, String defValue) {
        return mPreferences.getString(key, defValue);
    }

    public boolean removeString(String key) {
        mEditor = mPreferences.edit();
        mEditor.remove(key);
        return mEditor.commit();
    }

    public boolean putInt(String key, int value) {
        mEditor = mPreferences.edit();
        mEditor.putInt(key, value);
        return mEditor.commit();
    }

    public int getInt(String key) {
        return mPreferences.getInt(key, 0);
    }

    public int getInt(String key, int defValue) {
        return mPreferences.getInt(key, defValue);
    }

    public boolean putBoolean(String key, boolean value) {
        mEditor = mPreferences.edit();
        mEditor.putBoolean(key, value);
        return mEditor.commit();
    }

    public boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mPreferences.getBoolean(key, defValue);
    }

}
