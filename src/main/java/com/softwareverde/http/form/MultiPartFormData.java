package com.softwareverde.http.form;

import com.softwareverde.constable.bytearray.ByteArray;
import com.softwareverde.constable.bytearray.ImmutableByteArray;
import com.softwareverde.constable.bytearray.MutableByteArray;
import com.softwareverde.constable.list.List;
import com.softwareverde.constable.list.mutable.MutableList;
import com.softwareverde.http.util.StringUtil;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.ByteUtil;
import com.softwareverde.util.Util;
import com.softwareverde.util.bytearray.ByteArrayBuilder;
import com.softwareverde.util.bytearray.ByteArrayReader;

import java.util.HashMap;
import java.util.Map;

public class MultiPartFormData {
    public static class MultiPartRequest {
        public final Map<String, String> headers = new HashMap<String, String>();
        public final byte[] payload;

        public MultiPartRequest(final byte[] payload) {
            this.payload = payload;
        }
    }

    public static class Part {
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
        protected ByteArray _data;

        protected String _creationDate;
        protected String _modificationDate;
        protected String _readDate;
        protected Integer _size;
        protected String _voice;
        protected String _handling;

        public Part() { }

        public void setName(final String name) { _name = name; }
        public void setContentDisposition(final ContentDisposition contentDisposition) { _contentDisposition = contentDisposition; }
        public void setFilename(final String filename) { _filename = filename; }
        public void setContentType(final String contentType) { _contentType = contentType; }
        public void setData(final byte[] data) { _data = new ImmutableByteArray(data); }
        public void setData(final ByteArray data) { _data = data; }
        public void setCreationDate(final String creationDate) { _creationDate = creationDate; }
        public void setModificationDate(final String modificationDate) { _modificationDate = modificationDate; }
        public void setReadDate(final String readDate) { _readDate = readDate; }
        public void setSize(final Integer size) { _size = size; }

        public ContentDisposition getContentDisposition() { return _contentDisposition; }
        public String getName() { return _name; }
        public String getFilename() { return _filename; }
        public String getContentType() { return _contentType; }
        public ByteArray getData() { return _data; }
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

    protected static String generateRandomBoundary() {
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

    protected static Boolean doesDataContainData(final byte[] needle, final ByteArray haystack) {
        int matchedLength = 0;
        for (int i = 0; i < haystack.getByteCount(); ++i) {
            if (matchedLength >= needle.length) { return true; }

            final byte needleByte = needle[matchedLength];
            final byte haystackByte = haystack.getByte(i);

            if (needleByte != haystackByte) {
                matchedLength = 0;
            }
            else {
                matchedLength += 1;
            }
        }
        return (matchedLength == needle.length);
    }

    protected static byte[] readToBoundary(final ByteArrayReader byteArrayReader, final ByteArray boundary) {
        final int boundaryByteCount = boundary.getByteCount();
        final int position = byteArrayReader.getPosition();
        final ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();

        while (byteArrayReader.hasBytes()) {
            final ByteArray bytes = MutableByteArray.wrap(byteArrayReader.peakBytes(boundaryByteCount));
            if (byteArrayReader.didOverflow()) { break; }

            if (Util.areEqual(boundary, bytes)) {
                byteArrayReader.readBytes(boundaryByteCount); // Consume the deliminator...
                return byteArrayBuilder.build();
            }

            final byte b = byteArrayReader.readByte(); // Advance a single byte...
            byteArrayBuilder.appendByte(b);
        }

        byteArrayReader.setPosition(position);
        return null;
    }

    protected static String readLine(final ByteArrayReader byteArrayReader, final String newlineDeliminator) {
        final ByteArray newlineDeliminatorBytes = MutableByteArray.wrap(newlineDeliminator.getBytes());

        final byte[] bytes = MultiPartFormData.readToBoundary(byteArrayReader, newlineDeliminatorBytes);
        if (bytes == null) { return null; }

        return new String(bytes);
    }

    protected final MutableList<Part> _multiParts = new MutableList<Part>();

    // Description:
    //  If the paramValue is not null, the key/value pair will be appended to multiPartBody, prepended with a "; "
    protected void _appendOptionalParam(final ByteArrayBuilder multiPartBody, final String paramKey, final String paramValue) {
        if (paramValue != null) {
            final byte[] appendedBytes = StringUtil.stringToBytes("; " + paramKey + "=\"" + paramValue + "\"");
            multiPartBody.appendBytes(appendedBytes);
        }
    }

    public static MultiPartFormData parseFromRequest(final String boundary, final byte[] request) {
        final String preBoundaryString = "--";
        final String newlineDeliminator = "\r\n";

        final String expectedBoundary = (preBoundaryString + boundary);

        final ByteArrayReader byteArrayReader = new ByteArrayReader(request);
        final MultiPartFormData multiPartFormData = new MultiPartFormData();

        while (byteArrayReader.hasBytes()) {
            final String readBoundary = MultiPartFormData.readLine(byteArrayReader, newlineDeliminator);
            if (Util.areEqual(readBoundary, expectedBoundary)) {
                break;
            }
        }

        while (byteArrayReader.hasBytes()) {
            final Part multiPart = new Part();

            { // Parse MultiPart headers...
                String header = MultiPartFormData.readLine(byteArrayReader, newlineDeliminator);
                while ( (header != null) && (! header.isEmpty()) ) {
                    final int headerKeyEndIndex = header.indexOf(":");
                    if (headerKeyEndIndex < 0) { return null; }

                    final String headerKey = header.substring(0, headerKeyEndIndex).trim();
                    final String headerValues = ((headerKeyEndIndex + 1) < header.length() ? header.substring(headerKeyEndIndex + 1).trim() : "");
                    final String lowerCaseHeaderKey = headerKey.toLowerCase();

                    String headerValue = "";
                    final HashMap<String, String> extras = new HashMap<String, String>();
                    {
                        final String[] splitHeaderValues = headerValues.split(";");
                        for (int j = 0; j < splitHeaderValues.length; ++j) {
                            final String splitValue = splitHeaderValues[j].trim();
                            if (j == 0) {
                                headerValue = splitValue;
                                continue;
                            }

                            final String[] headerKeyValueExtras = splitValue.split("=", 2);
                            if (headerKeyValueExtras.length != 2) { continue; }

                            final String extraKey = headerKeyValueExtras[0].trim();
                            final String extraValue = StringUtil.unquoteString(headerKeyValueExtras[1]);
                            extras.put(extraKey, extraValue);
                        }
                    }

                    if (Util.areEqual("content-disposition", lowerCaseHeaderKey)) {
                        final Part.ContentDisposition contentDisposition = Part.ContentDisposition.fromString(headerValue);
                        multiPart.setContentDisposition(contentDisposition);

                        for (final String extraKey : extras.keySet()) {
                            final String extraValue = extras.get(extraKey);

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
                    }
                    else if (Util.areEqual("content-type", lowerCaseHeaderKey)) {
                        multiPart.setContentType(headerValue);
                    }
                    else {
                        // Unknown header...
                    }

                    header = MultiPartFormData.readLine(byteArrayReader, newlineDeliminator);
                }
            }

            { // Read binary content...
                final String endBoundary = (newlineDeliminator + preBoundaryString + boundary);
                final byte[] data = MultiPartFormData.readToBoundary(byteArrayReader, MutableByteArray.wrap(endBoundary.getBytes()));
                if (data == null) {
                    continue; // Discard invalid segment...
                }

                multiPart.setData(data);
            }

            multiPartFormData.addPart(multiPart);

            { // Clear end of segment newline or closing boundary...
                if (! byteArrayReader.hasBytes()) { return null; }
                final byte[] closingBytes = byteArrayReader.readBytes(newlineDeliminator.length());
                if (! ByteUtil.areEqual(closingBytes, newlineDeliminator.getBytes())) {
                    final byte[] closingBytes2 = byteArrayReader.readBytes(preBoundaryString.length());
                    final byte[] endOfSegmentBytes = new byte[closingBytes.length + closingBytes2.length];
                    ByteUtil.setBytes(endOfSegmentBytes, closingBytes);
                    ByteUtil.setBytes(endOfSegmentBytes, closingBytes2, closingBytes.length);

                    final byte[] expectedEndBytes = (preBoundaryString + newlineDeliminator).getBytes();
                    if (! ByteUtil.areEqual(endOfSegmentBytes, expectedEndBytes)) {
                        return null;
                    }

                    if (byteArrayReader.hasBytes()) { return null; } // Should have reached the end of stream...
                }
            }
        }

        return multiPartFormData;
    }

    public void addPart(final Part multiPart) {
        if (multiPart.isValid()) {
            _multiParts.add(multiPart);
        }
        else {
            Logger.warn("NOTICE: Invalid multipart.");
        }
    }

    public List<Part> getParts() {
        return _multiParts;
    }

    public Part getPart(final String multiPartName) {
        final String lowerCaseMultiPartName = multiPartName.toLowerCase();

        for (final Part multiPart : _multiParts) {
            final String existingPartName = multiPart.getName();
            final String lowerCaseExistingPartName = existingPartName.toLowerCase();
            if (Util.areEqual(lowerCaseMultiPartName, lowerCaseExistingPartName)) {
                return multiPart;
            }
        }

        return null;
    }

    public MultiPartRequest buildRequest() {
        final ByteArrayBuilder multiPartBody = new ByteArrayBuilder();

        String boundary = generateRandomBoundary();
        boolean boundaryIsValid = false;
        while (! boundaryIsValid) {
            boundaryIsValid = true;

            final byte[] needle = StringUtil.stringToBytes(boundary);
            for (final Part multiPart : _multiParts) {
                if (doesDataContainData(needle, multiPart.getData())) {
                    boundaryIsValid = false;
                }
            }

            if (! boundaryIsValid) {
                boundary = generateRandomBoundary();
            }
        }

        final byte[] preBoundaryBytes = StringUtil.stringToBytes("--");
        final byte[] boundaryBytes = StringUtil.stringToBytes(boundary);
        final byte[] newlineDeliminator = StringUtil.stringToBytes("\r\n");

        for (final Part multiPart : _multiParts) {
            multiPartBody.appendBytes(preBoundaryBytes);
            multiPartBody.appendBytes(boundaryBytes);
            multiPartBody.appendBytes(newlineDeliminator);

            // Set Content-Disposition and Optional-Params
            final Part.ContentDisposition contentDisposition = multiPart.getContentDisposition();
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