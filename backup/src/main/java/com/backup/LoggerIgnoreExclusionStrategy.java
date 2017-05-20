package com.backup;

import com.backup.annotations.LoggerIgnore;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * A {@link ExclusionStrategy} that excludes fields that are annotated with
 * {@link LoggerIgnore}.
 * Created by yaaminu on 5/18/17.
 */

class LoggerIgnoreExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(LoggerIgnore.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
