package com.softwareverde.http.form;

import com.softwareverde.util.ByteUtil;
import com.softwareverde.util.StringUtil;
import com.softwareverde.util.bytearray.ByteArrayBuilder;
import com.softwareverde.util.bytearray.ByteArrayReader;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MultiParserTests {
    @Test
    public void should_match_needle_in_haystack() {
        Assert.assertTrue(MultipartParser._doesDataContainData("abc".getBytes(), "abc".getBytes()));
        Assert.assertTrue(MultipartParser._doesDataContainData("abc".getBytes(), "abcdef".getBytes()));
        Assert.assertTrue(MultipartParser._doesDataContainData("c".getBytes(), "abc".getBytes()));
        Assert.assertTrue(MultipartParser._doesDataContainData("c".getBytes(), "abcdef".getBytes()));
        Assert.assertTrue(MultipartParser._doesDataContainData("".getBytes(), "abcdef".getBytes()));
        Assert.assertTrue(MultipartParser._doesDataContainData("".getBytes(), "".getBytes()));
        Assert.assertTrue(MultipartParser._doesDataContainData("a".getBytes(), "abcdef".getBytes()));
        Assert.assertTrue(MultipartParser._doesDataContainData("ef".getBytes(), "abcdef".getBytes()));
        Assert.assertFalse(MultipartParser._doesDataContainData("d".getBytes(), "abc".getBytes()));
        Assert.assertFalse(MultipartParser._doesDataContainData("a".getBytes(), "def".getBytes()));
        Assert.assertFalse(MultipartParser._doesDataContainData("abcdef".getBytes(), "abc".getBytes()));
        Assert.assertFalse(MultipartParser._doesDataContainData("abcdef".getBytes(), "abcde".getBytes()));
        Assert.assertFalse(MultipartParser._doesDataContainData("abcdef".getBytes(), "bcdef".getBytes()));
    }

    @Test
    public void should_split_byte_array() {
        // Setup
        final int segmentCount = 13;
        final ArrayList<Integer> values = new ArrayList<Integer>();

        final byte[] boundary = ("--" + MultipartParser._generateRandomBoundary()).getBytes();
        final ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();

        for (int i = 0; i < segmentCount; ++i) {
            final int value = (int) (Math.random() * Integer.MAX_VALUE);

            byteArrayBuilder.appendBytes(boundary);
            byteArrayBuilder.appendBytes(ByteUtil.integerToBytes(value));
            values.add(value);
        }

        // Action
        final List<byte[]> segments = MultipartParser._splitByteArray(new ByteArrayReader(byteArrayBuilder.build()), boundary, null);

        // Assert
        Assert.assertEquals(segmentCount, segments.size());

        for (int i = 0 ; i < segmentCount; ++i) {
            final byte[] segment = segments.get(i);
            final Integer expectedValue = values.get(i);
            final Integer segmentValue = ByteUtil.bytesToInteger(segment);
            Assert.assertEquals(expectedValue, segmentValue);
        }
    }

    @Test
    public void should_inflate_form_data_request() {
        // Setup
        final String content = "------WebKitFormBoundaryaGPTqBHiCjQ73BJm\r\nContent-Disposition: form-data; name=\"path\"\r\n\r\n/1px.png\r\n------WebKitFormBoundaryaGPTqBHiCjQ73BJm\r\nContent-Disposition: form-data; name=\"content\"; filename=\"1px.png\"\r\nContent-Type: image/png\r\n\r\nPNG\r\n\r\nIDATXXX\r\n------WebKitFormBoundaryaGPTqBHiCjQ73BJm--\r\n";

        final MultipartParser multipartParser = MultipartParser.fromRequest("----WebKitFormBoundaryaGPTqBHiCjQ73BJm", content.getBytes());

        // Action
        System.out.println(new String(multipartParser.getPayload().payload));

        // Assert
    }
}
