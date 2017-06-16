package com.softwareverde.servlet.response;

import com.softwareverde.json.Jsonable;
import com.softwareverde.servlet.content.ContentTypeResolver;

public class JsonResponse extends Response {
    private ContentTypeResolver _contentTypeResolver = new ContentTypeResolver();

    private void _setContentTypeHeader() {
        this.addHeader(Headers.CONTENT_TYPE, _contentTypeResolver.getContentType(".json"));
    }

    public JsonResponse() {
        _setContentTypeHeader();
    }

    public JsonResponse(final Integer responseCode, final Jsonable content) {
        _setContentTypeHeader();

        this.setCode(responseCode);
        this.setContent(content.toJson().toString());
    }

    public JsonResponse(final Integer responseCode, final String content) {
        _setContentTypeHeader();

        this.setCode(responseCode);
        this.setContent(content);
    }
}