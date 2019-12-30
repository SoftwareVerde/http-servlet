package com.softwareverde.http.form;

import com.softwareverde.logging.Logger;
import com.softwareverde.util.StringUtil;
import com.softwareverde.util.bytearray.ByteArrayBuilder;

import java.util.ArrayList;
import java.util.HashMap;
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

        public MultiPart(final ContentDisposition contentDisposition, final String name) {
            _contentDisposition = contentDisposition;
            _name = name;
        }

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

    public MultiPartRequest getPayload(final Runnable callback) {
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
        final byte[] newlineDeliminator = StringUtil.stringToBytes("\r\n");
        final byte[] boundaryBytes = StringUtil.stringToBytes(boundary);

        for (final MultiPart multiPart : _multiParts) {
            multiPartBody.appendBytes(preBoundaryBytes);
            multiPartBody.appendBytes(boundaryBytes);
            multiPartBody.appendBytes(newlineDeliminator);

            // Set Content-Disposition and Optional-Params
            multiPartBody.appendBytes(StringUtil.stringToBytes("Content-Disposition: " + multiPart.getContentDisposition()));
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