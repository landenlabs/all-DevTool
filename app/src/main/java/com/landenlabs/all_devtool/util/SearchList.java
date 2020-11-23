/*
 * Copyright (c) 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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
 * @author Dennis Lang
 * @see http://LanDenLabs.com/
 */

package com.landenlabs.all_devtool.util;


import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 *  Search list.
 */
public class SearchList {
    public int matchCnt;
    public int groupIdx;
    public int childIdx;

    public void search(ArrayList<ListInfo> list, String filter) {
        matchCnt = 0;
        if (!TextUtils.isEmpty(filter) && !filter.equals("*")) {
            for (int groupPosition = 0; groupPosition < list.size(); groupPosition++) {
                ListInfo buildInfo = list.get(groupPosition);
                int childPosition = 0;

                String text;
                if (!TextUtils.isEmpty(buildInfo.m_valueStr)) {
                    text = buildInfo.m_fieldStr + buildInfo.m_valueStr;
                    if (text.matches(filter) || Utils.containsIgnoreCase(text, filter)) {
                        if (matchCnt++ == 0) {
                            groupIdx = groupPosition;
                            childIdx = childPosition;
                        }
                    }
                }

                if (buildInfo.valueListStr() != null) {
                    for (Map.Entry<String, String> entry : buildInfo.valueListStr().entrySet()) {
                        text = entry.getKey() + entry.getValue();

                        if (text.matches(filter) || Utils.containsIgnoreCase(text, filter)) {
                            if (matchCnt++ == 0) {
                                groupIdx = groupPosition;
                                childIdx = childPosition;
                            }
                        }
                        childPosition++;
                    }
                }
            }
        }
        Log.d("search", "matches=" + matchCnt);
    }

}
