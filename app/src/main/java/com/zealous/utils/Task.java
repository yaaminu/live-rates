package com.zealous.utils;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.persistentQueue.sqlite.SqliteJobQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * by Null-Pointer on 11/28/2015.
 */
public abstract class Task extends com.birbit.android.jobqueue.Job {

    private final boolean isValid;

    protected Task(Params params) {
        super(params);
        isValid = true;
    }

    protected Task() { //required to deserialize task
        super(new Params(1));
        isValid = false;
    }

    protected final boolean isValid() {
        return isValid;
    }

    protected abstract JSONObject toJSON();

    protected abstract Task fromJSON(JSONObject jsonObject);

    public static class JobSerializer implements SqliteJobQueue.JobSerializer {

        private static final String CLASS = "class";

        @Override
        public byte[] serialize(Object o) throws IOException {
            if (o instanceof Task) {
                JSONObject task = ((Task) o).toJSON();
                if (task == null) {
                    throw new IllegalStateException("serialised form of task should not be null or empty");
                }
                if (task.has(CLASS)) {
                    throw new IllegalStateException("key \'" + CLASS + "\' is reserved");
                }
                try {
                    task.put(CLASS, o.getClass().getName());
                } catch (JSONException e) {
                    throw new RuntimeException(e.getCause());
                }
                return task.toString().getBytes();
            }
            throw new IllegalArgumentException("this serializer only works with com.idea.util.Task and its subclasses");
        }

        @SuppressWarnings("unchecked")
        @Override
        public Task deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
            String taskString = new String(bytes);
            try {
                JSONObject object = new JSONObject(taskString);
                String className = object.getString(CLASS);
                Class<?> clazz = Class.forName(className);
                String message = "subclasses are mandated to declare a public no-arg constructor";
                //noinspection TryWithIdenticalCatches
                try {
                    Constructor<?> con = clazz.getConstructor();
                    Task task = (Task) con.newInstance();
                    task = task.fromJSON(object);
                    if (task == null) {
                        throw new IllegalStateException(Task.class.getName() + "#fromJSON must not return null");
                    }
                    return task;
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(message);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(message);
                } catch (InstantiationException e) {
                    throw new RuntimeException(message);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(message);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }
}
