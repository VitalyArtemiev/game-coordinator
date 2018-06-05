import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(FrameHandler.class);
    private final Database db;

    public FrameHandler(Database db) {
        this.db = db;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

        Channel ch = ctx.channel();

        if (frame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            logger.info("{} received {}", ctx.channel(), request);

            db.tasks.add(new DBTask(ch, request));

            //ctx.channel().writeAndFlush(new TextWebSocketFrame(response));

        } else {
            String message = "Unsupported frame type: " + frame.getClass().getName();
            logger.error(message);
            throw new UnsupportedOperationException(message);
        }
    }
}