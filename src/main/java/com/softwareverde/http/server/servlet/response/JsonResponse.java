package com.softwareverde.http.server.servlet.response;

import com.softwareverde.http.server.servlet.content.ContentTypeResolver;
import com.softwareverde.json.Jsonable;

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