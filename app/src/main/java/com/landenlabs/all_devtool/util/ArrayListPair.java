package com.landenlabs.all_devtool.util;

import android.util.Pair;

import java.util.ArrayList;

/**
 * Ordered container of two items, like Map but with known order and array behavior.
 */
public class ArrayListPair<S, T> extends ArrayList<Pair<S, T>> {

    public boolean add(S v1, T v2) {
        return this.add(new Pair<>(v1, v2));
    }
}
