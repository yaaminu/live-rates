package com.backup.annotations;

/**
 * Marks a field that needs not be serialised by {@link com.backup.Logger} implementations.
 * This is more of similar to the transient qualifier in mainland java.
 * <p>
 * Created by yaaminu on 5/18/17.
 */

public @interface LoggerIgnore {
}
