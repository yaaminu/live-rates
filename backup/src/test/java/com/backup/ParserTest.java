package com.backup;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by yaaminu on 5/29/17.
 */
public class ParserTest {
    private List<LogEntry<? extends Operation>> testLogEntries;

    @Before
    public void fillLogEntries() throws Exception {
        testLogEntries = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            testLogEntries.add(new LogEntry<>("test", new DummyOp(i),
                    System.currentTimeMillis()));
        }
    }

    @Test
    public void next() throws Exception {
        //test normal
        byte[] payload = createLogEntries(false);
        testNormalInputNext(payload);

        testCorruptDataHandling(1, 3, 5, 7, 21, 34, 41);
        testCorruptDataHandling(2, 3, 5, 9, 32, 4, 0, 1, 8);
        testCorruptDataHandling(3, 4, 8, 7, 0, 29, 34, 33, 41, 23);
        testCorruptDataHandling(0, 14, 39, 48, 33, 28, 44, 12, 19);

        //we are testing with special edge cases where too little data
        //will be written.  we want to ensure that  even when the header is not written,
        //the parser can still cope.

        LogEntryGsonSerializer serializer = new LogEntryGsonSerializer();
        String malformedData = "\r";
        payload = encodeEntryHere(testLogEntries.get(7));
        ByteBuffer buffer = ByteBuffer.allocate(malformedData.length() + payload.length);
        buffer.put(malformedData.getBytes())
                .put(payload);

        Parser parser = new Parser(new ByteArrayInputStream(buffer.array()), serializer);
        assertEquals(testLogEntries.get(7), parser.next());
        assertNull(parser.next());

        parser = new Parser(new ByteArrayInputStream("\r\n".getBytes()), serializer);
        assertNull(parser.next());
        assertNull(parser.next());

        parser = new Parser(new ByteArrayInputStream("\r\n\u0000".getBytes()), serializer);
        assertNull(parser.next());
        assertNull(parser.next());

        parser = new Parser(new ByteArrayInputStream("\r\n\u0000h".getBytes()), serializer);
        assertNull(parser.next());
        assertNull(parser.next());

        malformedData = "\r\n\u0000h";
        payload = encodeEntryHere(testLogEntries.get(0));
        buffer = ByteBuffer.allocate(malformedData.length() + payload.length);
        buffer.put(malformedData.getBytes())
                .put(payload);

        parser = new Parser(new ByteArrayInputStream(buffer.array()), serializer);
        assertEquals(testLogEntries.get(0), parser.next());
        assertNull(parser.next());
        assertNull(parser.next());
    }

    private void testCorruptDataHandling(int... positionsToCorrupt) throws Exception {
        List<Integer> integers = new ArrayList<>(positionsToCorrupt.length);
        for (int i : positionsToCorrupt) {
            integers.add(i);
        }

        InputStream is = new ByteArrayInputStream(createLogEntries(true, positionsToCorrupt));
        Parser parser = new Parser(is, new LogEntryGsonSerializer());
        List<LogEntry<? extends Operation>> decoded = new ArrayList<>(testLogEntries.size());
        LogEntry<? extends Operation> next;

        while ((next = parser.next()) != null) {
            decoded.add(next);
            assertNotNull(next);
        }
        assertEquals(testLogEntries.size() - positionsToCorrupt.length, decoded.size());
    }


    private void testNormalInputNext(byte[] payload) throws IOException {
        System.out.println("testing normal input");
        InputStream is = new ByteArrayInputStream(payload);
        Parser parser = new Parser(is, new LogEntryGsonSerializer());
        List<LogEntry<? extends Operation>> decoded = new ArrayList<>(testLogEntries.size());
        for (int i = 0; i < testLogEntries.size(); i++) {
            LogEntry<? extends Operation> next = parser.next();
            decoded.add(next);
            assertNotNull(next);
            assertEquals(testLogEntries.get(i), next);
        }
        assertEquals(testLogEntries.size(), decoded.size());
    }

    private byte[] createLogEntries(boolean deliberatelyCorrupt, int... positions) throws Exception {

        List<Integer> entries = new ArrayList<>(positions.length);
        for (int position : positions) {
            entries.add(position);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int cursor = 0;
        for (LogEntry<? extends Operation> testLogEntry : testLogEntries) {
            if (deliberatelyCorrupt &&
                    entries.contains(cursor)) {
                byte[] toBeCurrupted = encodeEntryHere(testLogEntry);

                //deliberately corrupt the entry by truncating it
                //we expect the parser to sensibly skip partially written records.

                SecureRandom random = new SecureRandom();
                //maximum of 10000 and minimum of 99999
                int num = (int) Math.abs(random.nextDouble() * (toBeCurrupted.length));
                //we need an unsigned (+ve) number
                num = Math.abs(num);

                byte[] corrupted = new byte[num];
                System.arraycopy(toBeCurrupted, 0, corrupted, 0, corrupted.length);
                outputStream.write(corrupted);
            } else {
                outputStream.write(encodeEntryHere(testLogEntry));
            }
            cursor++;
        }
        return outputStream.toByteArray();
    }

    private boolean corruptEntryAtPosition(int cursor, int[] positions) {
        for (int i = 0; i < positions.length; i++) {
            return positions[i] == cursor;
        }
        return false;
    }

    // we wont rely on the encoder provided by the Parser since we don't trust it until
    // it's tested. This encoding routine is tested by inspection
    private byte[] encodeEntryHere(LogEntry<? extends Operation> testLogEntry) throws Exception {
        byte[] payload = new LogEntryGsonSerializer().serialize(testLogEntry);
        ByteBuffer buffer = ByteBuffer.allocate(payload.length + 2/*preceding \r\n*/
                + 2/*flags*/ + 4);
        buffer.put("\r\n".getBytes())
                .putChar((char) 0) //flags
                .putInt((testLogEntry.getSize()))
                .put(payload);
        return buffer.array();
    }

    @Test
    public void encode() throws Exception {
        LogEntryGsonSerializer serializer = new LogEntryGsonSerializer();
        for (LogEntry<? extends Operation> testLogEntry : testLogEntries) {
            assertArrayEquals(encodeEntryHere(testLogEntry), Parser.encode(serializer, testLogEntry));
        }
    }

    @Test
    public void close() throws Exception {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream("h".getBytes());
            Parser parser = new Parser(stream, new LogEntryGsonSerializer());
            parser.close();
            parser.next();
            fail("must not allow usage after close");
        } catch (IOException e) {
            System.out.println("correctly threw");
        }
        try {
            InputStream stream = new BufferedInputStream(new ByteArrayInputStream("h".getBytes()));
            Parser parser = new Parser(stream, new LogEntryGsonSerializer());
            parser.close();
            stream.read();
            fail("must not allow usage after close");
        } catch (IOException e) {
            System.out.println("correctly threw");
        }
    }

}