package com.zealous.expense;

import android.support.annotation.NonNull;

import com.zealous.utils.GenericUtils;

/**
 * Created by yaaminu on 4/9/17.
 */
class NonNullTuple<E, T> {
    public final E first;
    public final T second;

    public NonNullTuple(@NonNull E first, @NonNull T second) {
        GenericUtils.ensureNotNull(first, second);
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "NonNullTuple{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NonNullTuple<?, ?> that = (NonNullTuple<?, ?>) o;

        return first.equals(that.first) && second.equals(that.second);

    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }
}
