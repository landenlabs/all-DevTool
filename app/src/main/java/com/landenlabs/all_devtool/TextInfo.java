package com.landenlabs.all_devtool;

/*
 * Copyright (c) 2015 - 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang  (3/21/2015)
 * @see http://LanDenLabs.com/
 */

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Text information wrapper class used by TextFragment.
 *
 * @author Dennis Lang
 *
 */
public class TextInfo implements Parcelable {
    public final int m_sizeSP;
    private final int m_typefaceIdx;
    // public final String m_style;

    private static Typeface[] s_typefaces = new Typeface[]
            {Typeface.DEFAULT, Typeface.SANS_SERIF, Typeface.SERIF, Typeface.MONOSPACE};
    private static String[] s_typefaceStr = new String[]
            {"Default", "Sans Serif", "Serif", "Monospace"};

    TextInfo(int sizeSP, int typefaceIdx /*, String style */) {
        m_sizeSP = sizeSP;
        m_typefaceIdx = typefaceIdx;
        // m_style = style;
    }

    // Construct from Parcel
    private TextInfo(Parcel in) {
        m_sizeSP = in.readInt();
        m_typefaceIdx = in.readInt();
        // m_style = in.readString();
    }

    public static final Creator<TextInfo> CREATOR = new Creator<TextInfo>() {
        @Override
        public TextInfo createFromParcel(Parcel in) {
            return new TextInfo(in);
        }

        @Override
        public TextInfo[] newArray(int size) {
            return new TextInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(m_sizeSP);
        dest.writeInt(m_typefaceIdx);
        // dest.writeString(m_style);
    }

    public static String getTypefaceStr(int idx) {
        return s_typefaceStr[idx];
    }

    public static Typeface getTypeface(int idx) {
        return s_typefaces[idx];
    }

    public static int getCount() {
        return s_typefaces.length;
    }

    public String getTypefaceStr() {
        return s_typefaceStr[m_typefaceIdx];
    }

    public Typeface getTypeface() {
        return s_typefaces[m_typefaceIdx];
    }
    
    /*
    public String getStyle() {
    	return m_style;
    }
    */
}

