//package ru.mirea.game-coordinator;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Server {
    private final int port;

    private static final int MAX_BOSS_THREAD_COUNT = 1;

    private static final int MAX_WORKER_THREAD_COUNT = 4;

    public Server(int port) {
        this.port = port;
    }

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public Database db;

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(MAX_BOSS_THREAD_COUNT);
        EventLoopGroup workerGroup = new NioEventLoopGroup(MAX_WORKER_THREAD_COUNT);
        try {
            db = new Database("");

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.handler(new LoggingHandler(LogLevel.INFO));
            b.childHandler(new ServerInitializer(null, db));

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            logger.info("Started on port {}", port);
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            db.shutdown();
            logger.info("Shutting down");
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e) {
                logger.error("Cannot parse argument as port");
                port = 8080;
            }

        } else {
            port = 8080;
        }
        new Server(port).run();
    }
}
