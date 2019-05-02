package com.landenlabs.all_devtool.util;


/*
 * Copyright (c) 2016 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang  (3/21/2015)
 * @see http://LanDenLabs.com/
 *
 */

import java.util.Map;

/**
 *
 */
public class ListInfo {
    public final String m_fieldStr;
    public final String m_valueStr;
    public final Map<String, String> m_valueList;


    public ListInfo(String str1, String str2) {
        m_fieldStr = str1;
        m_valueStr = str2;
        m_valueList = null;
    }

    public ListInfo(String str1, Map<String, String> list2) {
        m_fieldStr = str1;
        m_valueStr = null;
        m_valueList = list2;
    }

    public String toString() {
        return m_fieldStr;
    }

    public String fieldStr() {
        return m_fieldStr;
    }

    public String valueStr() {
        return m_valueStr;
    }

    public Map<String, String> valueListStr() {
        return m_valueList;
    }

    public int getCount() {
        return (m_valueList == null) ? 0 : m_valueList.size();
    }
}
