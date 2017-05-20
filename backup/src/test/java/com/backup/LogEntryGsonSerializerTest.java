package com.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by yaaminu on 5/18/17.
 */
public class LogEntryGsonSerializerTest {
    @Test
    public void serialize() throws Exception {
        LogEntryTypeAdapter typeAdapter = new LogEntryTypeAdapter();
        LogEntryGsonSerializer serializer = new LogEntryGsonSerializer();
        assertNotNull(serializer.serialize(null));
        assertArrayEquals("must return an empty payload when object is null", new byte[0], serializer.serialize(null));

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LogEntry.class, typeAdapter)
                .setExclusionStrategies(new LoggerIgnoreExclusionStrategy())
                .create();
        for (int i = 0; i < 100; i++) {
            LogEntry<DummyOp> entry = new LogEntry<>("group",
                    new DummyOp(i), System.currentTimeMillis());

            byte[] tmp = gson.toJson(entry).getBytes();

            byte[] blob =
                    serializer.serialize(entry);

            assertNotNull(blob);

            assertArrayEquals("must use gson to transform pojos to json string and the blob",
                    tmp, blob);
        }

    }

    @Test
    public void deserialize() throws Exception {
        LogEntryGsonSerializer serializer = new LogEntryGsonSerializer();
        try {
            serializer.deserialize(null);
            fail("must not accept null blobs");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

        assertNull("must return null when blob is empty", serializer.deserialize(new byte[0]));
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LogEntry.class, new LogEntryTypeAdapter())
                .setExclusionStrategies(new LoggerIgnoreExclusionStrategy())
                .create();
        for (int i = 0; i < 100; i++) {
            byte[] blob =
                    gson.toJson(new LogEntry<>("group",
                            new DummyOp(i), System.currentTimeMillis()), LogEntry.class).getBytes();
            assertNotNull(blob);
            assertEquals("must use transform the blob to a json string and use json to serialize " +
                            "the resulting string",
                    gson.fromJson(new String(blob), LogEntry.class), serializer.deserialize(blob));
        }

    }

    @Test
    public void deserialize2() throws Exception {
        LogEntryGsonSerializer serializer = new LogEntryGsonSerializer();
        try {
            serializer.deserialize(null, 0, 0);
            fail("must not accept null blobs");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            serializer.deserialize(new byte[5], -1, 3);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

        try {
            serializer.deserialize(new byte[19], 4, -2);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            serializer.deserialize(new byte[28], -1, -5);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            serializer.deserialize(new byte[6], 0, 7);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            serializer.deserialize(new byte[57], 0, 58);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

        assertNull("must return null when blob is empty", serializer.deserialize(new byte[0], 0, 0));
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LogEntry.class, new LogEntryTypeAdapter())
                .setExclusionStrategies(new LoggerIgnoreExclusionStrategy())
                .create();
        for (int i = 0; i < 100; i++) {
            String json = gson.toJson(new LogEntry<>("group",
                    new DummyOp(i), System.currentTimeMillis()), LogEntry.class);
            ByteBuffer buffer = ByteBuffer.allocate(json.length() + 10);
            buffer.put(json.getBytes());
            assertNotNull(json);
            assertEquals("must use transform the blob to a json string and use json to serialize " +
                            "the resulting string",
                    gson.fromJson(new String(json.getBytes()), LogEntry.class),
                    serializer.deserialize(buffer.array(), 0, buffer.array().length - 10));
        }
    }

}