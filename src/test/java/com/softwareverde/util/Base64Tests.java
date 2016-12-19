package com.softwareverde.util;

import org.junit.Assert;
import org.junit.Test;

public class Base64Tests {
    @Test
    public void should_decode_base64_strings() throws Exception {
        // Setup
        final String expectedOutput = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu tempor ante. Suspendisse ultricies faucibus elementum. Vivamus id mi eget tortor pulvinar tristique nec nec lacus. Proin pharetra tellus id mollis tempus. Nullam sit amet tincidunt ex. Morbi purus massa, bibendum ornare arcu in, pharetra convallis mauris. Ut nulla nibh, malesuada id libero eget, placerat efficitur sapien. Maecenas malesuada maximus ipsum accumsan porttitor. Phasellus ullamcorper augue sodales ligula faucibus mattis. Vestibulum pellentesque tellus vel dui suscipit, sed pulvinar libero malesuada. Suspendisse potenti. Aliquam justo nibh, posuere nec pharetra sit amet, bibendum sit amet ligula. Proin dignissim malesuada orci.";
        final String input = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4gRXRpYW0gZXUgdGVtcG9yIGFudGUuIFN1c3BlbmRpc3NlIHVsdHJpY2llcyBmYXVjaWJ1cyBlbGVtZW50dW0uIFZpdmFtdXMgaWQgbWkgZWdldCB0b3J0b3IgcHVsdmluYXIgdHJpc3RpcXVlIG5lYyBuZWMgbGFjdXMuIFByb2luIHBoYXJldHJhIHRlbGx1cyBpZCBtb2xsaXMgdGVtcHVzLiBOdWxsYW0gc2l0IGFtZXQgdGluY2lkdW50IGV4LiBNb3JiaSBwdXJ1cyBtYXNzYSwgYmliZW5kdW0gb3JuYXJlIGFyY3UgaW4sIHBoYXJldHJhIGNvbnZhbGxpcyBtYXVyaXMuIFV0IG51bGxhIG5pYmgsIG1hbGVzdWFkYSBpZCBsaWJlcm8gZWdldCwgcGxhY2VyYXQgZWZmaWNpdHVyIHNhcGllbi4gTWFlY2VuYXMgbWFsZXN1YWRhIG1heGltdXMgaXBzdW0gYWNjdW1zYW4gcG9ydHRpdG9yLiBQaGFzZWxsdXMgdWxsYW1jb3JwZXIgYXVndWUgc29kYWxlcyBsaWd1bGEgZmF1Y2lidXMgbWF0dGlzLiBWZXN0aWJ1bHVtIHBlbGxlbnRlc3F1ZSB0ZWxsdXMgdmVsIGR1aSBzdXNjaXBpdCwgc2VkIHB1bHZpbmFyIGxpYmVybyBtYWxlc3VhZGEuIFN1c3BlbmRpc3NlIHBvdGVudGkuIEFsaXF1YW0ganVzdG8gbmliaCwgcG9zdWVyZSBuZWMgcGhhcmV0cmEgc2l0IGFtZXQsIGJpYmVuZHVtIHNpdCBhbWV0IGxpZ3VsYS4gUHJvaW4gZGlnbmlzc2ltIG1hbGVzdWFkYSBvcmNpLg==";

        // Action
        final byte[] decodedBytes = Base64.decode(input);

        // Assert
        Assert.assertEquals(expectedOutput, new String(decodedBytes));
    }

    @Test
    public void should_decode_and_encode_the_same_base64_string() throws Exception {
        // Setup
        final String input = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4gRXRpYW0gZXUgdGVtcG9yIGFudGUuIFN1c3BlbmRpc3NlIHVsdHJpY2llcyBmYXVjaWJ1cyBlbGVtZW50dW0uIFZpdmFtdXMgaWQgbWkgZWdldCB0b3J0b3IgcHVsdmluYXIgdHJpc3RpcXVlIG5lYyBuZWMgbGFjdXMuIFByb2luIHBoYXJldHJhIHRlbGx1cyBpZCBtb2xsaXMgdGVtcHVzLiBOdWxsYW0gc2l0IGFtZXQgdGluY2lkdW50IGV4LiBNb3JiaSBwdXJ1cyBtYXNzYSwgYmliZW5kdW0gb3JuYXJlIGFyY3UgaW4sIHBoYXJldHJhIGNvbnZhbGxpcyBtYXVyaXMuIFV0IG51bGxhIG5pYmgsIG1hbGVzdWFkYSBpZCBsaWJlcm8gZWdldCwgcGxhY2VyYXQgZWZmaWNpdHVyIHNhcGllbi4gTWFlY2VuYXMgbWFsZXN1YWRhIG1heGltdXMgaXBzdW0gYWNjdW1zYW4gcG9ydHRpdG9yLiBQaGFzZWxsdXMgdWxsYW1jb3JwZXIgYXVndWUgc29kYWxlcyBsaWd1bGEgZmF1Y2lidXMgbWF0dGlzLiBWZXN0aWJ1bHVtIHBlbGxlbnRlc3F1ZSB0ZWxsdXMgdmVsIGR1aSBzdXNjaXBpdCwgc2VkIHB1bHZpbmFyIGxpYmVybyBtYWxlc3VhZGEuIFN1c3BlbmRpc3NlIHBvdGVudGkuIEFsaXF1YW0ganVzdG8gbmliaCwgcG9zdWVyZSBuZWMgcGhhcmV0cmEgc2l0IGFtZXQsIGJpYmVuZHVtIHNpdCBhbWV0IGxpZ3VsYS4gUHJvaW4gZGlnbmlzc2ltIG1hbGVzdWFkYSBvcmNpLg==";

        // Action
        final byte[] decodedString = Base64.decode(input);
        final String encodedBytes = Base64.encode(decodedString);

        // Assert
        Assert.assertEquals(input, encodedBytes);
    }
}
