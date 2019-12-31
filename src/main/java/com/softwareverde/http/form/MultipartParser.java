package com.softwareverde.http.form;

import com.softwareverde.logging.Logger;
import com.softwareverde.util.ByteUtil;
import com.softwareverde.util.StringUtil;
import com.softwareverde.util.Util;
import com.softwareverde.util.bytearray.ByteArrayBuilder;
import com.softwareverde.util.bytearray.ByteArrayReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartParser {
    public static class MultiPart {
        public enum ContentDisposition {
            INLINE("inline"),
            ATTACHMENT("attachment"),
            FORM_DATA("form-data"),
            SIGNAL("signal"),
            ALERT("alert"),
            ICON("icon"),
            RENDER("render"),
            RECIPIENT_LIST_HISTORY("recipient-list-history"),
            SESSION("session"),
            AUTHENTICATED_IDENTITY_BODY("aib"),
            EARLY_SESSION("early-session"),
            RECIPIENT_LIST("recipient-list"),
            NOTIFICATION("notification"),
            BY_REFERENCE("by-reference"),
            INFO_PACKAGE("info-package"),
            RECORDING_SESSION("recording-session");

            private final String _value;
            ContentDisposition(final String value) {
                _value = value;
            }

            public static ContentDisposition fromString(final String value) {
                final String lowerCaseValue = value.toLowerCase();
                for (final ContentDisposition contentDisposition : ContentDisposition.values()) {
                    if (Util.areEqual(lowerCaseValue, contentDisposition.getValue())) {
                        return contentDisposition;
                    }
                }
                return null;
            }

            public String getValue() { return _value; }
        }

        protected ContentDisposition _contentDisposition;
        protected String _name;
        protected String _filename;
        protected String _contentType;
        protected byte[] _data;

        protected String _creationDate;
        protected String _modificationDate;
        protected String _readDate;
        protected Integer _size;
        protected String _voice;
        protected String _handling;

        public MultiPart() { }

        public void setName(final String name) { _name = name; }
        public void setContentDisposition(final ContentDisposition contentDisposition) { _contentDisposition = contentDisposition; }
        public void setFilename(final String filename) { _filename = filename; }
        public void setContentType(final String contentType) { _contentType = contentType; }
        public void setData(final byte[] data) { _data = data; }
        public void setCreationDate(final String creationDate) { _creationDate = creationDate; }
        public void setModificationDate(final String modificationDate) { _modificationDate = modificationDate; }
        public void setReadDate(final String readDate) { _readDate = readDate; }
        public void setSize(final Integer size) { _size = size; }

        public ContentDisposition getContentDisposition() { return _contentDisposition; }
        public String getName() { return _name; }
        public String getFilename() { return _filename; }
        public String getContentType() { return _contentType; }
        public byte[] getData() { return _data; }
        public String getCreationDate() { return _creationDate; }
        public String getModificationDate() { return _modificationDate; }
        public String getReadDate() { return _readDate; }
        public Integer getSize() { return _size; }
        public String getVoice() { return _voice; }
        public String getHandling() { return _handling; }

        public Boolean isValid() {
            if (_filename != null) {
                return (_contentType != null && _data != null);
            }

            return (_data != null);
        }
    }

    protected static String _generateRandomBoundary() {
        final int boundaryStartLength = 4;
        final int boundaryIdentifierLength = 32;
        final char[] candidateCharacters = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};

        final StringBuilder boundary = new StringBuilder();
        for (int i = 0; i < boundaryStartLength; ++i) {
            boundary.append("-");
        }

        for (int i = 0; i < boundaryIdentifierLength; ++i) {
            final int index = (int) (Math.random() * candidateCharacters.length);
            boundary.append(candidateCharacters[index]);
        }

        return boundary.toString();
    }

    protected final ArrayList<MultiPart> _multiParts = new ArrayList<MultiPart>();

    protected static Boolean _doesDataContainData(final byte[] needle, final byte[] haystack) {
        int matchedLength = 0;
        for (int i = 0; i < haystack.length; ++i) {
            if (matchedLength >= needle.length) { return true; }

            final byte needleByte = needle[matchedLength];
            final byte haystackByte = haystack[i];

            if (needleByte != haystackByte) {
                matchedLength = 0;
            }
            else {
                matchedLength += 1;
            }
        }
        return (matchedLength == needle.length);
    }

    protected static List<byte[]> _splitByteArray(final ByteArrayReader byteArrayReader, final byte[] boundary, final Integer maxSegmentCount) {
        final ArrayList<byte[]> segments = new ArrayList<byte[]>();

        boolean firstBoundaryReached = false;

        final ByteArrayBuilder boundaryBuffer = new ByteArrayBuilder();
        final ByteArrayBuilder segmentBuilder = new ByteArrayBuilder();
        while (byteArrayReader.hasBytes()) {
            final int boundaryIndex = boundaryBuffer.getByteCount();

            final byte b = byteArrayReader.readByte();
            final byte boundaryByte = boundary[boundaryIndex];
            if (b != boundaryByte) { // Not at a boundary.
                final byte[] invalidBoundary = boundaryBuffer.build();
                boundaryBuffer.clear();

                segmentBuilder.appendBytes(invalidBoundary);
                segmentBuilder.appendByte(b);
            }
            else {
                boundaryBuffer.appendByte(b);
                if (boundaryBuffer.getByteCount() >= boundary.length) { // Boundary reached.
                    boundaryBuffer.clear(); // Dispose of boundary bytes.
                    final byte[] segment = segmentBuilder.build();
                    segmentBuilder.clear();

                    if (firstBoundaryReached) {
                        segments.add(segment);
                    }

                    firstBoundaryReached = true;

                    if ( (maxSegmentCount != null) && (segments.size() >= maxSegmentCount) ) {
                        final Integer remainingByteCount = byteArrayReader.remainingByteCount();
                        if (remainingByteCount > 0) {
                            segmentBuilder.appendBytes(byteArrayReader.readBytes(remainingByteCount));
                        }
                        break;
                    }
                }
            }
        }

        { // Append any remaining data...
            final ByteArrayBuilder remainingData = new ByteArrayBuilder();
            final byte[] invalidBoundary = boundaryBuffer.build();
            boundaryBuffer.clear();
            remainingData.appendBytes(invalidBoundary);

            final byte[] segment = segmentBuilder.build();
            segmentBuilder.clear();
            remainingData.appendBytes(segment);

            if ( (remainingData.getByteCount() > 0) || (! firstBoundaryReached) ) {
                final byte[] remainingBytes = remainingData.build();
                remainingData.clear();
                segments.add(remainingBytes);
            }
        }

        return segments;
    }

    public static MultipartParser fromRequest(final String boundary, final byte[] request) {
        final String preBoundaryString = "--";
        final String newlineDeliminatorString = "\r\n";
        final byte[] newlineDeliminator = StringUtil.stringToBytes(newlineDeliminatorString);

        final int preBoundaryLength = preBoundaryString.length();

        final ByteArrayReader byteArrayReader = new ByteArrayReader(request);
        final String actualBoundary = byteArrayReader.readString(preBoundaryLength + boundary.length());
        if (! Util.areEqual(actualBoundary, (preBoundaryString + boundary))) { return null; }
        if (byteArrayReader.didOverflow()) { return null; }

        final byte[] boundaryBytes = (actualBoundary + newlineDeliminatorString).getBytes();
        byteArrayReader.setPosition(0);
        final List<byte[]> segments = _splitByteArray(byteArrayReader, boundaryBytes, null);

        if (segments.isEmpty()) { return null; }

        final MultipartParser multipartParser = new MultipartParser();

        final int segmentCount = segments.size();
        for (int i = 0; i < segmentCount; ++i) {
            final byte[] segment = segments.get(i);
            final boolean isLastSegment = (i == (segmentCount - 1));
            if (isLastSegment) {
                final String endOfContentTag = (preBoundaryString + newlineDeliminatorString);
                if (! ByteUtil.areEqual(endOfContentTag.getBytes(), segment)) { // Multipart data should end with a boundary concatenated with the preBoundary.
                    return null;
                }
                continue;
            }

            final List<byte[]> segmentDataChunks = _splitByteArray(new ByteArrayReader(segment), newlineDeliminator, 1);

            final String segmentHeaders;
            final byte[] segmentBytes;
            {
                if (segmentDataChunks.size() == 2) {
                    segmentHeaders = StringUtil.bytesToString(segmentDataChunks.get(0));
                    segmentBytes = segmentDataChunks.get(1);
                }
                else {
                    segmentHeaders = "";
                    segmentBytes = segmentDataChunks.get(0);
                }
            }

            final MultiPart multiPart = new MultiPart();
            multiPart.setData(segmentBytes);

            { // Parse MultiPart headers...
                final String[] headers = segmentHeaders.split(newlineDeliminatorString);
                for (final String header : headers) {
                    final int headerKeyEndIndex = header.indexOf(":");
                    if (headerKeyEndIndex < 0) { return null; }

                    final String headerKey = header.substring(0, headerKeyEndIndex).trim();
                    final String headerValues = ((headerKeyEndIndex + 1) < header.length() ? header.substring(headerKeyEndIndex + 1).trim() : "");
                    final String lowerCaseHeaderKey = headerKey.toLowerCase();

                    String headerValue = "";
                    final String[] splitHeaderValues = headerValues.split(";");
                    for (int j = 0; j < splitHeaderValues.length; ++j) {
                        final String splitValue = splitHeaderValues[j].trim();
                        if (j == 0) {
                            headerValue = splitValue;
                            continue;
                        }

                        if (Util.areEqual("content-disposition", lowerCaseHeaderKey)) {
                            final String[] headerKeyValueExtras = splitValue.split("=", 2);
                            if (headerKeyValueExtras.length != 2) { continue; }

                            final String extraKey = headerKeyValueExtras[0].trim();
                            final String extraValue = StringUtil.pregMatch("^(\"?)(.*)\\1$", headerKeyValueExtras[1]).get(1); // Allow for values surrounded by double quotes.

                            switch (extraKey.toLowerCase()) {
                                case "name": {
                                    multiPart.setName(extraValue);
                                } break;
                                case "size": {
                                    multiPart.setSize(Util.parseInt(extraValue));
                                } break;
                                case "filename": {
                                    multiPart.setFilename(extraValue);
                                } break;
                                case "creation-date": {
                                    multiPart.setCreationDate(extraValue);
                                } break;
                                case "modification-date": {
                                    multiPart.setModificationDate(extraValue);
                                } break;
                                case "read-date": {
                                    multiPart.setReadDate(extraValue);
                                } break;
                                case "voice":
                                case "handling":
                                default: {
                                    // Unknown extra...
                                }
                            }
                        }
                        else if (Util.areEqual("content-type", lowerCaseHeaderKey)) {
                            multiPart.setContentType(splitValue);
                        }
                        else {
                            // Unknown header...
                        }
                    }

                    if (Util.areEqual("content-disposition", lowerCaseHeaderKey)) {
                        final MultiPart.ContentDisposition contentDisposition = MultiPart.ContentDisposition.fromString(headerValue);
                        multiPart.setContentDisposition(contentDisposition);
                    }
                }
            }

            multipartParser.addMultiPart(multiPart);
        }

        return multipartParser;
    }

    // Description:
    //  If the paramValue is not null, the key/value pair will be appended to multiPartBody, prepended with a "; "
    protected void _appendOptionalParam(final ByteArrayBuilder multiPartBody, final String paramKey, final String paramValue) {
        if (paramValue != null) {
            final byte[] appendedBytes = StringUtil.stringToBytes("; " + paramKey + "=\"" + paramValue + "\"");
            multiPartBody.appendBytes(appendedBytes);
        }
    }

    public void addMultiPart(final MultiPart multiPart) {
        if (multiPart.isValid()) {
            _multiParts.add(multiPart);
        }
        else {
            Logger.warn("NOTICE: Invalid multipart.");
        }
    }

    public static class MultiPartRequest {
        public final Map<String, String> headers = new HashMap<String, String>();
        public final byte[] payload;

        public MultiPartRequest(final byte[] payload) {
            this.payload = payload;
        }
    }

    public MultiPartRequest getPayload() {
        final ByteArrayBuilder multiPartBody = new ByteArrayBuilder();

        String boundary = _generateRandomBoundary();
        boolean boundaryIsValid = false;
        while (! boundaryIsValid) {
            boundaryIsValid = true;

            final byte[] needle = StringUtil.stringToBytes(boundary);
            for (final MultiPart multiPart : _multiParts) {
                if (_doesDataContainData(needle, multiPart.getData())) {
                    boundaryIsValid = false;
                }
            }

            if (! boundaryIsValid) {
                boundary = _generateRandomBoundary();
            }
        }

        final byte[] preBoundaryBytes = StringUtil.stringToBytes("--");
        final byte[] boundaryBytes = StringUtil.stringToBytes(boundary);
        final byte[] newlineDeliminator = StringUtil.stringToBytes("\r\n");

        for (final MultiPart multiPart : _multiParts) {
            multiPartBody.appendBytes(preBoundaryBytes);
            multiPartBody.appendBytes(boundaryBytes);
            multiPartBody.appendBytes(newlineDeliminator);

            // Set Content-Disposition and Optional-Params
            final MultiPart.ContentDisposition contentDisposition = multiPart.getContentDisposition();
            multiPartBody.appendBytes(StringUtil.stringToBytes("Content-Disposition: " + (contentDisposition != null ? contentDisposition.getValue() : "")));
            multiPartBody.appendBytes(StringUtil.stringToBytes("; name=\"" + multiPart.getName() + "\""));

            final Integer size = multiPart.getSize();
            _appendOptionalParam(multiPartBody, "size", (size != null ? size.toString() : null));
            _appendOptionalParam(multiPartBody, "filename", multiPart.getFilename());
            _appendOptionalParam(multiPartBody, "creation-date", multiPart.getCreationDate());
            _appendOptionalParam(multiPartBody, "modification-date", multiPart.getModificationDate());
            _appendOptionalParam(multiPartBody, "read-date", multiPart.getReadDate());
            _appendOptionalParam(multiPartBody, "voice", multiPart.getVoice());
            _appendOptionalParam(multiPartBody, "handling", multiPart.getHandling());

            multiPartBody.appendBytes(newlineDeliminator);

            // Set Content-Type
            final String contentType = multiPart.getContentType();
            if (contentType != null) {
                multiPartBody.appendBytes(StringUtil.stringToBytes("Content-Type: " + contentType));
                multiPartBody.appendBytes(newlineDeliminator);
            }

            multiPartBody.appendBytes(newlineDeliminator);

            multiPartBody.appendBytes(multiPart.getData());

            multiPartBody.appendBytes(newlineDeliminator);
        }

        // Closing Boundary...
        multiPartBody.appendBytes(preBoundaryBytes);
        multiPartBody.appendBytes(boundaryBytes);
        multiPartBody.appendBytes(preBoundaryBytes);
        multiPartBody.appendBytes(newlineDeliminator);

        final MultiPartRequest multiPartRequest = new MultiPartRequest(multiPartBody.build());
        multiPartRequest.headers.put("Content-Type", "multipart/form-data; boundary=" + boundary);
        multiPartRequest.headers.put("Content-Length", ("" + multiPartBody.getByteCount()));
        return multiPartRequest;
    }
}