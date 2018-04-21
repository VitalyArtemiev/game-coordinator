import java.util.Locale;

        import io.netty.channel.ChannelHandlerContext;
        import io.netty.channel.SimpleChannelInboundHandler;
        import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
        import io.netty.handler.codec.http.websocketx.WebSocketFrame;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

/**
 * Echoes uppercase content of text frames.
 */
public class FrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(FrameHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

        if (frame instanceof TextWebSocketFrame) {


            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            logger.info("{} received {}", ctx.channel(), request);
            String response = "";
            switch (request) {
                case "getRoomList\n": {
                    response = "roomList\n4\n";
                    response += "1 room1 2 8\n";
                    response += "2 room2 3 10\n";
                    response += "3 room3 5 6\n";
                    response += "4 room4 1 4\n";
                    break;
                }
                default: {
                    response = "Command not supported: '" + request + '\'';
                }
            }

            ctx.channel().writeAndFlush(new TextWebSocketFrame(response));

        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }
}