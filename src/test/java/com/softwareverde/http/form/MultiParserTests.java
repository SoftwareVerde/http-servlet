package com.softwareverde.http.form;

import org.junit.Assert;
import org.junit.Test;

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
}
