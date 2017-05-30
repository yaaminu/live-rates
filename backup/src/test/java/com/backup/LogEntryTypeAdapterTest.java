package com.backup;


import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static com.backup.LogEntry.FIELD_DATE_LOGGED;
import static com.backup.LogEntry.FIELD_GROUP;
import static com.backup.LogEntry.FIELD_HASH_SUM;
import static com.backup.LogEntry.FIELD_OP;
import static com.backup.LogEntry.FIELD_OP_CLAZZ;
import static com.backup.LogEntry.FIELD_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by yaaminu on 5/18/17.
 */
public class LogEntryTypeAdapterTest {
    @Test
    public void write() throws Exception {
        LogEntryTypeAdapter adapter = new LogEntryTypeAdapter();

        try {
            adapter.write(null, PowerMockito.mock(LogEntry.class));
            fail("must not accept null writers");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }

        //CORRECTLY DEAL WITH NULL VALUES
        JsonWriter mockedWriter = PowerMockito.mock(JsonWriter.class);
        adapter.write(mockedWriter, null);
        verify(mockedWriter, times(1)).nullValue();
        adapter.write(mockedWriter, null);
        verify(mockedWriter, times(2)).nullValue();
        RegistringJsonWriter jsonWriter;
        Gson gson = new Gson();
        for (int i = 0; i < 100; i++) {
            LogEntry<DummyOp> entry = new LogEntry<>("group",
                    new DummyOp(i), System.currentTimeMillis());

            jsonWriter = new RegistringJsonWriter(writer);
            adapter.write(jsonWriter, entry);
            Map<String, ?> data = jsonWriter.data;
            Assert.assertEquals(entry.getDateLogged(), data.get(FIELD_DATE_LOGGED));
            Assert.assertEquals(entry.getHashSum(), data.get(FIELD_HASH_SUM));
            Assert.assertEquals(entry.getSize(), ((Long) data.get(FIELD_SIZE)).longValue());

            //we are adding the FIELD_OP_CLAZZ prop to make the two same if this property is
            //the only difference. This is only to make the test  simple otherwise we
            //have to write multiple assertEquals statement when only one property that differs
            //
            //Also do not that we will have to check that the FIELD_OP_CLAZZ  also in the
            //serialized json later.
            entry.getOp().data().addProperty(FIELD_OP_CLAZZ, DummyOp.class.getName());
            Assert.assertEquals(entry.getOp().data(), gson.fromJson(data.get(FIELD_OP).toString(),
                    JsonObject.class));

            Assert.assertEquals(entry.getCollectionName(), data.get(FIELD_GROUP));
        }
    }

    @Test
    public void read() throws Exception {
        LogEntryTypeAdapter adapter = new LogEntryTypeAdapter();
        try {
            adapter.read(null);
            fail("must not accept null readers");
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly threw " + e.getClass().getName());
        }
        JsonReader reader;

        reader = mock(JsonReader.class);
        when(reader.peek()).thenReturn(JsonToken.NULL);
        assertNull("must handle null values", adapter.read(reader));

        for (int i = 0; i < 100; i++) {
            LogEntry<DummyOp> entry = new LogEntry<>("group",
                    new DummyOp(i), System.currentTimeMillis());
            JsonObject json = new Gson().toJsonTree(entry).getAsJsonObject();
            JsonObject tmp = json.get(FIELD_OP).getAsJsonObject();
            JsonObject data = tmp.remove("data").getAsJsonObject();
            data.addProperty(FIELD_OP_CLAZZ, DummyOp.class.getName());
            json.addProperty(FIELD_OP, data.toString());
            reader = new JsonReader(new StringReader(json.toString()));
            LogEntry<? extends Operation> read = adapter.read(reader);
            assertNotNull("must not return null", read);
            assertEquals(entry, read);
        }
        //must check integrity
        for (int i = 0; i < 100; i++) {

        }

    }

    @Test
    public void integrationTest() throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LogEntry.class, new LogEntryTypeAdapter())
                .create();
        for (int i = 0; i < 100; i++) {
            LogEntry<DummyOp> entry = new LogEntry<>("group",
                    new DummyOp(i), System.currentTimeMillis());

            //test the writer
            JsonObject json = gson.toJsonTree(entry, LogEntry.class).getAsJsonObject();
            checkProps(json, entry);

            //test the reader
            LogEntry<DummyOp> tmp = gson.fromJson(json, entry.getClass());
            assertEquals("must be able to convert back to POJO", entry, tmp);
        }
    }

    private void checkProps(JsonObject object, LogEntry<DummyOp> entry) throws Exception {
        JsonObject opJsonObject = new Gson().fromJson(object.get(FIELD_OP).getAsString(), JsonObject.class);
        assertEquals(opJsonObject.get(FIELD_OP_CLAZZ).getAsString(), entry.getOp().getClass().getName());
        assertEquals(1/*for the OP_CLAZZ member we add internally*/ + entry.getOp().data().entrySet().size(),
                opJsonObject.entrySet().size());
        assertEquals(object.get(FIELD_DATE_LOGGED).getAsLong(), entry.getDateLogged());
        assertEquals(object.get(FIELD_GROUP).getAsString(), entry.getCollectionName());
        assertEquals(object.get(FIELD_HASH_SUM).getAsString(), entry.getHashSum());
        assertEquals(object.get(FIELD_SIZE).getAsLong(), entry.getSize());
    }

    private static Writer writer = new Writer() {
        @Override
        public void write(char[] chars, int i, int i1) throws IOException {

        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {

        }
    };

    private static class RegistringJsonWriter extends JsonWriter {

        private final Map<String, Object> data;
        @Nullable
        private String currentName;

        /**
         * Creates a new instance that writes a JSON-encoded stream to {@code out}.
         * For best performance, ensure {@link Writer} is buffered; wrapping in
         * {@link BufferedWriter BufferedWriter} if necessary.
         *
         * @param out
         */
        public RegistringJsonWriter(Writer out) {
            super(out);
            data = new HashMap<>();
        }

        @Override
        public JsonWriter value(String value) throws IOException {
            super.value(value);
            return doWrite(value);
        }

        private JsonWriter doWrite(Object value) throws IOException {
            if (currentName == null) {
                throw new IOException();
            }
            data.put(currentName, value);
            currentName = null;
            return this;
        }

        @Override
        public JsonWriter value(long value) throws IOException {
            super.value(value);
            doWrite(value);
            return this;
        }


        @Override
        public JsonWriter name(String name) throws IOException {
            currentName = name;
            return super.name(name);
        }
    }

}