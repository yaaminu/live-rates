package com.backup;

import com.android.annotations.NonNull;
import com.google.gson.JsonObject;

/**
 * An operation represent a piece of action that mutates
 * the dataStore. All implementations must be <a href=http://en.wikipedia.com/wiki/crdt>CRDT</a>
 * <p>
 * All implementations are required to have at least one public no arg constructor.
 * Created by yaaminu on 5/17/17.
 */
public interface Operation {

    /**
     * This data essentially provides the context and content needed for this
     * operation to be replay()ed at another time on the same or different device
     * for eg. If an operation deleted a  photo from a user's account,
     * on another device say Nexus 6, the operation could be replayed on the user's different
     * device using information similar to this.
     * <Code>
     * {
     * "operation":"delete",
     * "model":"photo",
     * "photo_id":"389f24d444e149bdd400b29c8f619670bfd35b9a",
     * "timestamp":"1495098505176",
     * "deviceId":"Nexus 6"
     * }
     * </Code>
     *
     * @return the information required for this operation to be replayed.
     */
    @NonNull
    JsonObject data();

    /**
     * sets the context and content information required for this operation to be replayed.
     * this is what was returned in {@link #data()}.
     *
     * @param object the context and content information needed to replay this data.
     */
    void setData(@NonNull JsonObject object);

    /**
     * This essentially repeats the operation. All implementations
     * must strive to be idempotent. The replay
     *
     * @throws BackupException
     */
    void replay() throws BackupException;
}
