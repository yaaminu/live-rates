package com.zealous.adapter;

/**
 * Created by yaaminu on 12/20/16.
 */
public class Tuple implements ITuple {
    private final String first;
    private final String second;

    public Tuple(String first, String second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String getFirst() {
        return first;
    }

    @Override
    public String getSecond() {
        return second;
    }
}
