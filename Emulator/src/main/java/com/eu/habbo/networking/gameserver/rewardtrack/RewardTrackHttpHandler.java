package com.eu.habbo.networking.gameserver.rewardtrack;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rewardtrack.RewardTrackManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.networking.gameserver.auth.AccessTokenService;
import com.eu.habbo.networking.gameserver.auth.CorsOriginGate;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class RewardTrackHttpHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RewardTrackHttpHandler.class);
    private static final String BASE_PATH = "/api/reward-track";

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
            QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
            String path = decoder.path();
            RewardTrackManager manager = Emulator.getGameEnvironment().getRewardTrackManager();

            if (req.method() == HttpMethod.GET && path.equals(BASE_PATH)) {
                String trackCode = decoder.parameters().getOrDefault("track", java.util.List.of("")).get(0);
                JsonObject payload = manager.getState(userId, trackCode);
                if (payload == null) {
                    sendJson(ctx, req, HttpResponseStatus.NOT_FOUND, error("Reward track not found."));
                    return;
                }

                sendJson(ctx, req, HttpResponseStatus.OK, payload);
                return;
            }

            if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/claim")) {
                JsonObject body = parseBody(req);
                int prizeId = body.has("prizeId") ? body.get("prizeId").getAsInt() : 0;
                JsonObject payload = manager.claimPrize(userId, prizeId);
                sendJson(ctx, req, payload.has("error") ? HttpResponseStatus.BAD_REQUEST : HttpResponseStatus.OK, payload);
                return;
            }

            if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/premium")) {
                JsonObject body = parseBody(req);
                int trackId = body.has("trackId") ? body.get("trackId").getAsInt() : 0;
                JsonObject payload = manager.unlockPremium(userId, trackId);
                sendJson(ctx, req, payload.has("error") ? HttpResponseStatus.BAD_REQUEST : HttpResponseStatus.OK, payload);
                return;
            }

            if (path.equals(BASE_PATH + "/admin") || path.startsWith(BASE_PATH + "/admin/")) {
                if (!isAdmin(userId)) {
                    sendJson(ctx, req, HttpResponseStatus.FORBIDDEN, error("Forbidden."));
                    return;
                }

                if (req.method() == HttpMethod.GET && path.equals(BASE_PATH + "/admin")) {
                    JsonObject payload = manager.getEditorState();
                    sendJson(ctx, req, payload == null ? HttpResponseStatus.NOT_FOUND : HttpResponseStatus.OK, payload == null ? error("Reward track not found.") : payload);
                    return;
                }

                if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/admin/track")) {
                    sendJson(ctx, req, HttpResponseStatus.OK, manager.saveTrack(parseBody(req)));
                    return;
                }

                if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/admin/task")) {
                    sendJson(ctx, req, HttpResponseStatus.OK, manager.saveTask(parseBody(req)));
                    return;
                }

                if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/admin/task/delete")) {
                    JsonObject body = parseBody(req);
                    sendJson(ctx, req, HttpResponseStatus.OK, manager.deleteTask(body.has("id") ? body.get("id").getAsInt() : 0));
                    return;
                }

                if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/admin/prize")) {
                    sendJson(ctx, req, HttpResponseStatus.OK, manager.savePrize(parseBody(req)));
                    return;
                }

                if (req.method() == HttpMethod.POST && path.equals(BASE_PATH + "/admin/prize/delete")) {
                    JsonObject body = parseBody(req);
                    sendJson(ctx, req, HttpResponseStatus.OK, manager.deletePrize(body.has("id") ? body.get("id").getAsInt() : 0));
                    return;
                }
            }

            sendJson(ctx, req, HttpResponseStatus.NOT_FOUND, error("Unknown reward track endpoint."));
        } catch (Exception e) {
            LOGGER.error("[reward-track] unexpected error", e);
            sendJson(ctx, req, HttpResponseStatus.INTERNAL_SERVER_ERROR, error("Server error."));
        }
    }

    private int authenticate(FullHttpRequest req) {
        String authorization = req.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) return 0;

        return AccessTokenService.verify(authorization.substring("Bearer ".length()).trim());
    }

    private boolean isAdmin(int userId) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(userId);

        return habbo != null && habbo.getHabboInfo().getRank().getId() >= 7;
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
