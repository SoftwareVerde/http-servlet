package com.softwareverde.http.form;

import com.softwareverde.constable.bytearray.ByteArray;
import com.softwareverde.constable.bytearray.MutableByteArray;
import com.softwareverde.constable.list.List;
import com.softwareverde.util.Util;
import com.softwareverde.util.bytearray.ByteArrayBuilder;
import com.softwareverde.util.bytearray.ByteArrayReader;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class MultiParserTests {
    @Test
    public void should_match_needle_in_haystack() {
        Assert.assertTrue(MultiPartFormData.doesDataContainData("abc".getBytes(), MutableByteArray.wrap("abc".getBytes())));
        Assert.assertTrue(MultiPartFormData.doesDataContainData("abc".getBytes(), MutableByteArray.wrap("abcdef".getBytes())));
        Assert.assertTrue(MultiPartFormData.doesDataContainData("c".getBytes(), MutableByteArray.wrap("abc".getBytes())));
        Assert.assertTrue(MultiPartFormData.doesDataContainData("c".getBytes(), MutableByteArray.wrap("abcdef".getBytes())));
        Assert.assertTrue(MultiPartFormData.doesDataContainData("".getBytes(), MutableByteArray.wrap("abcdef".getBytes())));
        Assert.assertTrue(MultiPartFormData.doesDataContainData("".getBytes(), MutableByteArray.wrap("".getBytes())));
        Assert.assertTrue(MultiPartFormData.doesDataContainData("a".getBytes(), MutableByteArray.wrap("abcdef".getBytes())));
        Assert.assertTrue(MultiPartFormData.doesDataContainData("ef".getBytes(), MutableByteArray.wrap("abcdef".getBytes())));
        Assert.assertFalse(MultiPartFormData.doesDataContainData("d".getBytes(), MutableByteArray.wrap("abc".getBytes())));
        Assert.assertFalse(MultiPartFormData.doesDataContainData("a".getBytes(), MutableByteArray.wrap("def".getBytes())));
        Assert.assertFalse(MultiPartFormData.doesDataContainData("abcdef".getBytes(), MutableByteArray.wrap("abc".getBytes())));
        Assert.assertFalse(MultiPartFormData.doesDataContainData("abcdef".getBytes(), MutableByteArray.wrap("abcde".getBytes())));
        Assert.assertFalse(MultiPartFormData.doesDataContainData("abcdef".getBytes(), MutableByteArray.wrap("bcdef".getBytes())));
    }

    @Test
    public void should_read_to_boundary() {
        // Setup
        final int segmentCount = 3;
        final ArrayList<Integer> values = new ArrayList<Integer>();

        final ByteArray boundary = MutableByteArray.wrap(("|" + MultiPartFormData.generateRandomBoundary() + "|").getBytes());

        final byte[] payload;
        {
            final ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
            for (int i = 0; i < segmentCount; ++i) {
                final Integer value = (int) (Math.random() * Integer.MAX_VALUE);
                values.add(value);

                byteArrayBuilder.appendBytes(value.toString().getBytes());
                byteArrayBuilder.appendBytes(boundary);
            }
            payload = byteArrayBuilder.build();
        }

        final ByteArrayReader byteArrayReader = new ByteArrayReader(payload);

        for (int i = 0; i < segmentCount; ++i) {
            // Action
            final byte[] segment = MultiPartFormData.readToBoundary(byteArrayReader, boundary);

            // Assert
            Assert.assertNotNull(segment);
            final Integer expectedValue = values.get(i);
            final Integer segmentValue = Util.parseInt(new String(segment));
            Assert.assertEquals(expectedValue, segmentValue);
        }
    }

    @Test
    public void should_inflate_form_data_request() {
        // Setup
        final String content = "------WebKitFormBoundaryaGPTqBHiCjQ73BJm\r\nContent-Disposition: form-data; name=\"path\"\r\n\r\n/1px.png\r\n------WebKitFormBoundaryaGPTqBHiCjQ73BJm\r\nContent-Disposition: form-data; name=\"content\"; filename=\"1px.png\"\r\nContent-Type: image/png\r\n\r\nPNG\r\n\r\nIDATXXX\r\n------WebKitFormBoundaryaGPTqBHiCjQ73BJm--\r\n";

        // Action
        final MultiPartFormData multiPartFormData = MultiPartFormData.parseFromRequest("----WebKitFormBoundaryaGPTqBHiCjQ73BJm", content.getBytes());
        Assert.assertNotNull(multiPartFormData);

        final List<MultiPartFormData.Part> multiParts = multiPartFormData.getParts();

        // Assert
        Assert.assertEquals(2, multiParts.getSize());

        {
            final MultiPartFormData.Part part = multiParts.get(0);
            Assert.assertEquals("path", part.getName());
            Assert.assertEquals("/1px.png", new String(part.getData().getBytes()));
        }

        {
            final MultiPartFormData.Part part = multiParts.get(1);
            Assert.assertEquals("content", part.getName());
            Assert.assertEquals("1px.png", part.getFilename());
            Assert.assertEquals("image/png", part.getContentType());
            Assert.assertEquals("PNG\r\n\r\nIDATXXX", new String(part.getData().getBytes()));
        }
    }
}
