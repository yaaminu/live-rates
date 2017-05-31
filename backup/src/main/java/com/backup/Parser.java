package com.backup;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by yaaminu on 5/29/17.
 */

public class Parser implements Closeable {

    public static final byte LF = 10;
    public static final byte CR = 13;
    public static final int HEADER_SIZE = 6;
    private final BufferedInputStream stream;
    private final Serializer<LogEntry<? extends Operation>> serializer;
    private ByteArrayOutputStream buffer;

    public Parser(@NonNull InputStream stream, Serializer<LogEntry<? extends Operation>> serializer) {
        this.stream = stream instanceof BufferedInputStream ?
                (BufferedInputStream) stream : new BufferedInputStream(stream);
        this.serializer = serializer;
        buffer = new ByteArrayOutputStream();
    }

    @Nullable
    LogEntry<? extends Operation> next() throws IOException {
        try {
            int read;
            boolean previousIsCr = false;
            buffer.reset(); //reset

            while ((read = stream.read()) != -1) {
                if (previousIsCr && read == LF) { //at end of record
                    byte[] payload = buffer.toByteArray();
                    LogEntry<? extends Operation> logEntry = decode(payload);
                    if (logEntry == null) { //we encountered a corrupted input
                        System.err.println("encountered corrupted data, skipping");
                        System.err.println("corrupt data is " + payload.length + " bytes long");
                        //don't log the content as it could contain sensitive data
                        buffer.reset(); //reset and start again
                        continue /*parsing*/;
                    }
                    return logEntry; //next call to this routine will move to next entry;
                }
                if (read == CR) {//\r
                    previousIsCr = true;
                    continue;
                }
                buffer.write(read); //buffer it

                previousIsCr = false;
            }
            //if we are here we are at the end of the stream
            byte[] lastEntryBuf = buffer.toByteArray();
            if (lastEntryBuf.length > 0) {
                return decode(lastEntryBuf);
            }
            return null;
        } catch (BackupException e) {
            System.err.println("corrupt data");
            buffer.reset();//redundant but safe
            return next();
        }
    }

    @Nullable
    private LogEntry<? extends Operation> decode(byte[] tmp) throws BackupException {

        //too short,
        if (tmp.length < 6) {
            throw new BackupException(BackupException.EMALFORMEDINPUT, "Malformed data", null);
        }
        ByteBuffer buffer = ByteBuffer.wrap(tmp);

        char flags = buffer.getChar();
        int entrySize = buffer.getInt(); //for the mean time just to advance to  the  payload

        LogEntryFlags logEntryFlags = new LogEntryFlags(flags);
        // TODO: 5/19/17 do something (like decompress) to the payload based on the flags

        return serializer.deserialize(buffer.array(), HEADER_SIZE, tmp.length - HEADER_SIZE);
    }


    public static byte[] encode(Serializer<LogEntry<? extends Operation>> serializer, LogEntry<? extends Operation> entry) throws BackupException {
        byte[] payload = serializer.serialize(entry);
        ByteBuffer buffer = ByteBuffer.allocate(payload.length + 2/*preceding \r\n*/
                + 2/*flags*/ + 4 /*size*/);
        return buffer.put(CR).put(LF)
                .putChar((char) 0)
                .putInt(entry.getSize())
                .put(payload).array();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    private static final class OP implements Operation {

        private JsonObject object;

        @Override
        public JsonObject data() {
            return new JsonObject();
        }

        @Override
        public void setData(@NonNull JsonObject object) {
            this.object = object;
        }

        @Override
        public void replay() throws BackupException {
            System.out.println("replaying with data " + object.toString());
        }
    }
}
