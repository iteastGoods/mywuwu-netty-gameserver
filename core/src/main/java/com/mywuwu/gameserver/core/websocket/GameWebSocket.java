package com.mywuwu.gameserver.core.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mywuwu.gameserver.core.TransferData;
import com.mywuwu.gameserver.core.security.JwtTokenUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.yeauty.annotation.*;
import org.yeauty.pojo.ParameterMap;
import org.yeauty.pojo.Session;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * websocket 初始化类
 * websocket 初始化类
 */
public abstract class GameWebSocket {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ConcurrentHashMap<ChannelId, Session> map = new ConcurrentHashMap<>();


    private AttributeKey<String> channelNameKey = AttributeKey.valueOf("WEBSOCKET_GAME_ID");

    @Autowired
    protected JwtTokenUtil jwtTokenUtil;

    @Autowired
    protected RedisTemplate redisTemplate;


    @OnOpen
    public void onOpen(Session session, HttpHeaders headers, ParameterMap parameterMap) throws IOException {
        String id = parameterMap.getParameter("id");
        String token = parameterMap.getParameter("token");
//        jwtTokenUtil.validateToken(token, id)

        if (jwtTokenUtil.validateToken(token, id)) {

            map.put(session.id(), session);


            Attribute<String> attributeName = session.channel().attr(channelNameKey);
            attributeName.set(id);

            ValueOperations<String, GameWebSocketSession> valueOperationsByGameWebSocketSession = this.redisTemplate.opsForValue();

            GameWebSocketSession gameWebSocketSession = valueOperationsByGameWebSocketSession.get(id);
            if (gameWebSocketSession != null) {
                gameWebSocketSession.setSessionId(session.id());
                gameWebSocketSession.setState("0");
                valueOperationsByGameWebSocketSession.set(id, gameWebSocketSession);
            } else {
                gameWebSocketSession = new GameWebSocketSession(id, session.id(), token, Instant.now().toString(),
                        "", "0", session.remoteAddress().toString(), null, null);
                valueOperationsByGameWebSocketSession.set(id, gameWebSocketSession);
            }
            openHandle(gameWebSocketSession);
        } else {
            session.close();
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {

        Attribute<String> attributeName = session.channel().attr(channelNameKey);

        ValueOperations<String, GameWebSocketSession> valueOperationsByGameWebSocketSession = this.redisTemplate.opsForValue();

        GameWebSocketSession gameWebSocketSession = valueOperationsByGameWebSocketSession.get(attributeName.get());
        gameWebSocketSession.setState("1");
        valueOperationsByGameWebSocketSession.set(attributeName.get(), gameWebSocketSession);

        closeHandle(gameWebSocketSession);

        map.remove(session.id());

        logger.info("close client:" + session.remoteAddress().toString());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("close error:" + session.remoteAddress().toString(), throwable);
    }


    @OnBinary
    public void onBinary(Session session, byte[] bytes) {
        System.out.println(new String(bytes));
        int channel = (int) bytes[0];
        int protocol = (bytes[1] << 8 | (bytes[2] & 0xFF));
        byte[] buffer = null;
        if (bytes.length > 3)
            buffer = Arrays.copyOfRange(bytes, 3, bytes.length);

        Attribute<String> attributeName = session.channel().attr(channelNameKey);
        channel = 2;
        protocol = 1003;
        ValueOperations<String, GameWebSocketSession> valueOperationsByGameWebSocketSession = this.redisTemplate.opsForValue();
        GameWebSocketSession gameWebSocketSession = valueOperationsByGameWebSocketSession.get(attributeName.get());
        receiveHandle(gameWebSocketSession,
                channel,
                protocol,
                buffer
        );
    }

    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    System.out.println("read idle");
                    break;
                case WRITER_IDLE:
                    System.out.println("write idle");
                    break;
                case ALL_IDLE:
                    System.out.println("all idle");
                    break;
                default:
                    break;
            }
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        JSONObject obj = JSON.parseObject(message);
        int channel = obj.getInteger("channel");
        int protocol = obj.getInteger("protocol");
//                obj.getString("id");
        byte[] data = obj.getString("data").getBytes();


        Attribute<String> attributeName = session.channel().attr(channelNameKey);

        ValueOperations<String, GameWebSocketSession> valueOperationsByGameWebSocketSession = this.redisTemplate.opsForValue();
        GameWebSocketSession gameWebSocketSession = valueOperationsByGameWebSocketSession.get(attributeName.get());
        receiveHandle(gameWebSocketSession, channel, protocol, data);

//        ValueOperations<String, GameWebSocketSession> valueOperationsByGameWebSocketSession = this.redisTemplate.opsForValue();
//        GameWebSocketSession gameWebSocketSession = valueOperationsByGameWebSocketSession.get(attributeName.get());
//        System.out.println(message + "=======" + map.get(session.id()));
//        ValueOperations<String, GameWebSocketSession> valueOperationsByGameWebSocketSession = this.redisTemplate.opsForValue();
//        Map<String, Object> map = new HashMap<>();
//        map.put("playerLowerlimit","1");
//        map.put("playerUpLimit","5");
//        map.put("xiaZhuTop","2");
//        map.put("juShu","15");
//        GameWebSocketSession gameWebSocketSession = valueOperationsByGameWebSocketSession.get(id);
//        gameWebSocketSession.setSessionId(session.id());
//        gameWebSocketSession.setRoomNumber("10006");
//        TransferData transferData = new TransferData(gameWebSocketSession, "yingsanzhang", 1003, JSON.toJSONString(map).getBytes());
//        onMessageHandle(transferData);
//        session.sendText("Hello Netty!");

    }

    protected abstract boolean receiveHandle(GameWebSocketSession session, int channel, int protocol,
                                             byte[] buffer);

    protected abstract void openHandle(GameWebSocketSession session);

    protected abstract void closeHandle(GameWebSocketSession session);

    protected abstract void onMessageHandle(TransferData transferData);

    public static void send(int channel, int protocol, ChannelId sessionId, byte[] buffer) {
        Session session = map.get(sessionId);
        ByteBuf buf = session.channel().alloc().buffer(buffer.length + 3);
        buf.writeByte(channel);
        buf.writeByte(protocol >> 8);
        buf.writeByte(protocol & 0xFF);
        buf.writeBytes(buffer);
//        session.sendBinary(buffer);
        session.sendText(new String(buffer));
    }

    public static void send(ChannelId sessionId, String buffer) {
        Session session = map.get(sessionId);
        session.sendText(buffer);
    }


}