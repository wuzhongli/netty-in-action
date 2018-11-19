package nia.chapter4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 代码清单 4-5 写出到 Channel
 *
 * 代码清单 4-6 从多个线程使用同一个 Channel
 *
 * @author wzl
 */
public class ChannelOperationExamples {

    private static final Channel CHANNEL_FROM_SOMEWHERE = new NioSocketChannel();

    /**
     * 代码清单 4-5 写出到 Channel
     */
    public static void writingToChannel() {
        Channel channel = CHANNEL_FROM_SOMEWHERE;
        // 创建持有要写数据的ByteBuf
        ByteBuf buf = Unpooled.copiedBuffer("your data", CharsetUtil.UTF_8);
        // 写数据并冲刷它
        ChannelFuture future = channel.writeAndFlush(buf);
        // 添加ChannelFutureListener以便在写操作完成后接收通知
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // 写操作完成，并且没有错误发生
                if (future.isSuccess()) {
                    System.out.println("Write successful");
                } else {
                    System.out.println("Write error");
                    // 记录错误
                    future.cause().printStackTrace();
                }
            }
        });
    }

    /**
     * 代码清单 4-6 从多个线程使用同一个 Channel
     */
    public static void writingToChannelFromManyThreads() {
        final Channel channel = CHANNEL_FROM_SOMEWHERE;
        // 创建持有要写数据的ByteBuf
        final ByteBuf buf = Unpooled.copiedBuffer("your data", CharsetUtil.UTF_8).retain();
        // 创建将数据写到Channel的Runnable
        Runnable writer = new Runnable() {
            @Override
            public void run() {
                channel.writeAndFlush(buf.duplicate());
            }
        };
        //获取到线程池Executor 的引用
        Executor executor = Executors.newCachedThreadPool();

        //递交写任务给线程池以便在某个线程中执行
        // write in one thread
        executor.execute(writer);

        //递交另一个写任务以便在另一个线程中执行
        // write in another thread
        executor.execute(writer);
        //...
    }
}
