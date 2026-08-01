package com.eu.habbo.networking.gameserver.dailytasks;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.dailytasks.DailyTaskManager;
import com.eu.habbo.networking.gameserver.auth.AccessTokenService;
import com.eu.habbo.networking.gameserver.auth.CorsOriginGate;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class DailyTaskHttpHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyTaskHttpHandler.class);
    private static final String BASE_PATH = "/api/daily-tasks";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest req)) {
            super.channelRead(ctx, msg);
            return;
        }

        String path = new QueryStringDecoder(req.uri()).path();
        if (!path.equals(BASE_PATH) && !path.startsWith(BASE_PATH + "/")) {
            super.channelRead(ctx, msg);
            return;
        }

        try {
            handle(ctx, req);
        } finally {
            ReferenceCountUtil.release(req);
        }
    }

    private void handle(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (req.method() == HttpMethod.OPTIONS) {
            sendCors(ctx, req);
            return;
        }

        int userId = authenticate(req);
        if (userId <= 0) {
            sendJson(ctx, req, HttpResponseStatus.UNAUTHORIZED, error("Unauthorized."));
            return;
        }

        try {
            String path = new QueryStringDecoder(req.uri()).path();
            DailyTaskManager manager = Emulator.getGameEnvironment().getDailyTaskManager();

            if (req.method() == HttpMethod.GET && path.equals(BASE_PATH)) {
                sendJson(ctx, req, HttpResponseStatus.OK, manager.getState(userId));
                return;
            }

            if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/accept")) {
                JsonObject body = parseBody(req);
                String difficulty = body.has("difficulty") ? body.get("difficulty").getAsString() : "easy";
                JsonObject payload = manager.accept(userId, difficulty);
                sendJson(ctx, req, payload.has("error") ? HttpResponseStatus.BAD_REQUEST : HttpResponseStatus.OK, payload);
                return;
            }

            if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/cancel")) {
                sendJson(ctx, req, HttpResponseStatus.OK, manager.cancel(userId));
                return;
            }

            if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/claim")) {
                JsonObject body = parseBody(req);
                int taskId = body.has("taskId") ? body.get("taskId").getAsInt() : 0;
                String taskDate = body.has("taskDate") ? body.get("taskDate").getAsString() : "";
                JsonObject payload = manager.claim(userId, taskId, taskDate);
                sendJson(ctx, req, payload.has("error") ? HttpResponseStatus.BAD_REQUEST : HttpResponseStatus.OK, payload);
                return;
            }

            sendJson(ctx, req, HttpResponseStatus.NOT_FOUND, error("Unknown daily task endpoint."));
        } catch (Exception e) {
            LOGGER.error("[daily-tasks] unexpected error", e);
            sendJson(ctx, req, HttpResponseStatus.INTERNAL_SERVER_ERROR, error("Server error."));
        }
    }

    private int authenticate(FullHttpRequest req) {
        String authorization = req.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) return 0;

        return AccessTokenService.verify(authorization.substring("Bearer ".length()).trim());
    }

    private JsonObject parseBody(FullHttpRequest req) {
        if (!req.content().isReadable()) return new JsonObject();

        String text = req.content().toString(StandardCharsets.UTF_8);
        if (text == null || text.isBlank()) return new JsonObject();

        return JsonParser.parseString(text).getAsJsonObject();
    }

    private void sendCors(ChannelHandlerContext ctx, FullHttpRequest req) {
        FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.NO_CONTENT);
        addCors(response, req);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendJson(ChannelHandlerContext ctx, FullHttpRequest req, HttpResponseStatus status, JsonObject payload) {
        byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
        FullHttpResponse response =
                new DefaultFullHttpResponse(req.protocolVersion(), status, Unpooled.wrappedBuffer(bytes));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        addCors(response, req);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void addCors(FullHttpResponse response, FullHttpRequest req) {
        String origin = req.headers().get(HttpHeaderNames.ORIGIN);
        if (origin != null && !origin.isEmpty() && CorsOriginGate.isAllowed(req)) {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.headers().set(HttpHeaderNames.VARY, "Origin, Access-Control-Request-Headers, Access-Control-Request-Method");
        }

        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type, X-Requested-With, X-Nitro-Api");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, "600");
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-store");
    }

    private static JsonObject error(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        return error;
    }
}
