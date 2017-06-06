package com.softwareverde.httpserver.response;

import com.softwareverde.httpserver.ContentTypeResolver;
import com.softwareverde.json.Jsonable;

public class JsonResponse extends Response {
    private ContentTypeResolver _contentTypeResolver = new ContentTypeResolver();

    private void _setContentTypeHeader() {
        this.addHeader("Content-Type", _contentTypeResolver.getContentType(".json"));
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