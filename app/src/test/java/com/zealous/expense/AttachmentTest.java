package com.zealous.expense;

import com.google.gson.JsonObject;
import com.zealous.R;
import com.zealous.errors.ZealousException;
import com.zealous.utils.FileUtils;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.util.ArrayList;
import java.util.List;

import static com.zealous.expense.Attachment.FIELD_BLOB;
import static com.zealous.expense.Attachment.FIELD_MIME_TYPE;
import static com.zealous.expense.Attachment.FIELD_SHA1SUM;
import static com.zealous.expense.Attachment.FIELD_TITLE;
import static com.zealous.utils.FileUtils.sha1;
import static java.lang.System.currentTimeMillis;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by yaaminu on 5/30/17.
 */
@PrepareForTest(GenericUtils.class)
public class AttachmentTest {

    static {
        PLog.setLogLevel(PLog.LEVEL_NONE);
    }

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test
    public void testConstructor() throws Exception {
        mockStatic(GenericUtils.class);
        when(GenericUtils.getString(anyInt())).thenReturn("resources");
        try {
            new Attachment("title", new byte[0], "image/jpeg");
            fail("must not allow empty blobs");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            new Attachment("title", new byte[(int) (FileUtils.ONE_MB * 5 + 1)], "image/jpeg");
            fail("must not allow too large blobs");
        } catch (ZealousException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        //must not throw
        new Attachment("title", new byte[(int) (FileUtils.ONE_MB * 5 - 1)], "image/jpeg");
        try {
            new Attachment(null, new byte[(int) (FileUtils.ONE_MB)], "image/jpeg");
            fail("must not allow null titles");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            new Attachment(" ", new byte[(int) (FileUtils.ONE_MB)], "image/jpeg");
            fail("must not allow empty titles");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            new Attachment("title", new byte[(int) (FileUtils.ONE_MB)], " ");
            fail("must not allow empty mimeType");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
        try {
            new Attachment("title", new byte[(int) (FileUtils.ONE_MB)], null);
            fail("must not allow null mimeType");
        } catch (IllegalArgumentException e) {
            System.out.println("correctly threw " + e.getClass().getName());
        }
    }

    @Test
    public void getBlob() throws Exception {
        Attachment attachment = new Attachment("hello", new byte[1024], "text/plain");
        assertNotNull(attachment.getBlob());
        assertArrayEquals(new byte[1024], attachment.getBlob());

        String currentTimeMillis = currentTimeMillis() + "";
        attachment = new Attachment("hello", currentTimeMillis.getBytes(), "text/plain");
        assertNotNull(attachment.getBlob());
        assertArrayEquals(currentTimeMillis.getBytes(), attachment.getBlob());
    }

    @Test
    public void getTitle() throws Exception {
        Attachment attachment = new Attachment("title", "hello".getBytes(), "tex/plain");
        assertEquals("title", attachment.getTitle());
        attachment = new Attachment(" title", "hello".getBytes(), "text/plain");
        assertEquals("title", attachment.getTitle());
    }

    @Test
    public void getMimeType() throws Exception {
        Attachment attachment = new Attachment("title", "hello".getBytes(), "type");
        assertEquals("type", attachment.getMimeType());
        attachment = new Attachment(" title", "hello".getBytes(), " type ");
        assertEquals("type", attachment.getMimeType());
    }

    @Test
    public void getSha1Sum() throws Exception {
        Attachment attachment = new Attachment("title", "hello".getBytes(), "type");
        assertEquals(sha1("hello"), attachment.getSha1Sum());
        attachment = new Attachment(" title", "blob2".getBytes(), " type ");
        assertEquals(sha1("blob2"), attachment.getSha1Sum());
    }

    @Test
    public void getPlaceHolderIcon() throws Exception {
        Attachment attachment = new Attachment("title", "hello".getBytes(), "image/*");
        assertEquals(R.drawable.picture_preview, attachment.getPlaceHolderIcon());
        attachment = new Attachment("title", "hello".getBytes(), "application/pdf");
        assertEquals(R.drawable.pdf_preview, attachment.getPlaceHolderIcon());
        attachment = new Attachment("title", "hello".getBytes(), "unknown/*");
        assertEquals(R.drawable.preview_unknown, attachment.getPlaceHolderIcon());

    }

    @Test
    public void toJson() throws Exception {
        String titlePrefix = "hello";
        String typePrefix = "type";
        JsonObject expected;
        for (int i = 0; i < 15; i++) {
            expected = new JsonObject();
            expected.addProperty(FIELD_TITLE, titlePrefix + i);
            expected.addProperty(FIELD_MIME_TYPE, typePrefix + i);
            expected.addProperty(FIELD_SHA1SUM, FileUtils.sha1((titlePrefix + i).getBytes()));
            expected.addProperty(FIELD_BLOB, Base64.encodeBase64String((titlePrefix + i).getBytes()));

            Attachment attachment = new Attachment(titlePrefix + i, (titlePrefix + i).getBytes(), typePrefix + i);
            JsonObject actualJsonObject = attachment.toJson();
            assertEquals(titlePrefix + i, actualJsonObject.get(FIELD_TITLE).getAsString());
            assertEquals(typePrefix + i, actualJsonObject.get(FIELD_MIME_TYPE).getAsString());
            assertEquals(sha1(titlePrefix + i), actualJsonObject.get(FIELD_SHA1SUM).getAsString());
            String blob = actualJsonObject.get(FIELD_BLOB).getAsString();
            assertEquals(Base64.encodeBase64String((titlePrefix + i).getBytes()), blob);
            assertArrayEquals((titlePrefix + i).getBytes(), decodeBase64(blob));

            assertEquals(expected, actualJsonObject);

        }
    }

    @Test
    public void fromJson() throws Exception {
        String titlePrefix = "hello";
        String typePrefix = "type";
        JsonObject expected;
        List<Attachment> attachments = new ArrayList<>(15);
        List<JsonObject> objects = new ArrayList<>(15);
        for (int i = 0; i < 15; i++) {
            expected = new JsonObject();
            expected.addProperty(FIELD_TITLE, titlePrefix + i);
            expected.addProperty(FIELD_MIME_TYPE, typePrefix + i);
            expected.addProperty(FIELD_SHA1SUM, FileUtils.sha1((titlePrefix + i).getBytes()));
            expected.addProperty(FIELD_BLOB, Base64.encodeBase64String((titlePrefix + i).getBytes()));
            objects.add(expected);
            attachments.add(new Attachment(titlePrefix + i, (titlePrefix + i).getBytes(), typePrefix + i));
        }

        for (int i = 0; i < attachments.size(); i++) {
            Attachment actual = Attachment.fromJson(objects.get(i));
            Attachment expectedAttachment = attachments.get(i);
            assertEquals(expectedAttachment, actual);
            assertEquals(expectedAttachment.getTitle(), actual.getTitle());
            assertEquals(expectedAttachment.getMimeType(), actual.getMimeType());
            assertEquals(expectedAttachment.getSha1Sum(), actual.getSha1Sum());
            assertArrayEquals(expectedAttachment.getBlob(), actual.getBlob());
        }

        //test checksum verification
        for (JsonObject object : objects) {
            object.addProperty(FIELD_SHA1SUM, "changed" + currentTimeMillis());
            try {
                Attachment.fromJson(object);
                fail("must rejected entries whose checksum has changed");
            } catch (IllegalStateException e) {
                System.out.println("correctly threw " + e.getClass().getName());
            }
        }
    }

}