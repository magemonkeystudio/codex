package mc.promcteam.engine.testutil.reflection;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutorGroup;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestChannel implements Channel {
    @Override
    public ChannelId id() {
        return new ChannelId() {
            @Override
            public String asShortText() {
                return "MyChannel";
            }

            @Override
            public String asLongText() {
                return "MyChannel";
            }

            @Override
            public int compareTo(@NotNull ChannelId channelId) {
                return 0;
            }
        };
    }

    @Override
    public EventLoop eventLoop() {
        return null;
    }

    @Override
    public Channel parent() {
        return null;
    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isRegistered() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public ChannelMetadata metadata() {
        return null;
    }

    @Override
    public SocketAddress localAddress() {
        return null;
    }

    @Override
    public SocketAddress remoteAddress() {
        return null;
    }

    @Override
    public ChannelFuture closeFuture() {
        return null;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public long bytesBeforeUnwritable() {
        return 0;
    }

    @Override
    public long bytesBeforeWritable() {
        return 0;
    }

    @Override
    public Unsafe unsafe() {
        return null;
    }

    @Override
    public ChannelPipeline pipeline() {
        return new ChannelPipeline() {
            @Override
            public ChannelPipeline addFirst(String s, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelPipeline addFirst(EventExecutorGroup eventExecutorGroup, String s, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelPipeline addLast(String s, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelPipeline addLast(EventExecutorGroup eventExecutorGroup, String s, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelPipeline addBefore(String s, String s1, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelPipeline addBefore(EventExecutorGroup eventExecutorGroup, String s, String s1, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelPipeline addAfter(String s, String s1, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelPipeline addAfter(EventExecutorGroup eventExecutorGroup, String s, String s1, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelPipeline addFirst(ChannelHandler... channelHandlers) {
                return null;
            }

            @Override
            public ChannelPipeline addFirst(EventExecutorGroup eventExecutorGroup, ChannelHandler... channelHandlers) {
                return null;
            }

            @Override
            public ChannelPipeline addLast(ChannelHandler... channelHandlers) {
                return null;
            }

            @Override
            public ChannelPipeline addLast(EventExecutorGroup eventExecutorGroup, ChannelHandler... channelHandlers) {
                return null;
            }

            @Override
            public ChannelPipeline remove(ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelHandler remove(String s) {
                return null;
            }

            @Override
            public <T extends ChannelHandler> T remove(Class<T> aClass) {
                return null;
            }

            @Override
            public ChannelHandler removeFirst() {
                return null;
            }

            @Override
            public ChannelHandler removeLast() {
                return null;
            }

            @Override
            public ChannelPipeline replace(ChannelHandler channelHandler, String s, ChannelHandler channelHandler1) {
                return null;
            }

            @Override
            public ChannelHandler replace(String s, String s1, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public <T extends ChannelHandler> T replace(Class<T> aClass, String s, ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelHandler first() {
                return null;
            }

            @Override
            public ChannelHandlerContext firstContext() {
                return null;
            }

            @Override
            public ChannelHandler last() {
                return null;
            }

            @Override
            public ChannelHandlerContext lastContext() {
                return null;
            }

            @Override
            public ChannelHandler get(String s) {
                return null;
            }

            @Override
            public <T extends ChannelHandler> T get(Class<T> aClass) {
                return null;
            }

            @Override
            public ChannelHandlerContext context(ChannelHandler channelHandler) {
                return null;
            }

            @Override
            public ChannelHandlerContext context(String s) {
                return null;
            }

            @Override
            public ChannelHandlerContext context(Class<? extends ChannelHandler> aClass) {
                return null;
            }

            @Override
            public Channel channel() {
                return null;
            }

            @Override
            public List<String> names() {
                return null;
            }

            @Override
            public Map<String, ChannelHandler> toMap() {
                return null;
            }

            @Override
            public ChannelPipeline fireChannelRegistered() {
                return null;
            }

            @Override
            public ChannelPipeline fireChannelUnregistered() {
                return null;
            }

            @Override
            public ChannelPipeline fireChannelActive() {
                return null;
            }

            @Override
            public ChannelPipeline fireChannelInactive() {
                return null;
            }

            @Override
            public ChannelPipeline fireExceptionCaught(Throwable throwable) {
                return null;
            }

            @Override
            public ChannelPipeline fireUserEventTriggered(Object o) {
                return null;
            }

            @Override
            public ChannelPipeline fireChannelRead(Object o) {
                return null;
            }

            @Override
            public ChannelPipeline fireChannelReadComplete() {
                return null;
            }

            @Override
            public ChannelPipeline fireChannelWritabilityChanged() {
                return null;
            }

            @Override
            public ChannelPipeline flush() {
                return null;
            }

            @Override
            public ChannelFuture bind(SocketAddress socketAddress) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress socketAddress) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1) {
                return null;
            }

            @Override
            public ChannelFuture disconnect() {
                return null;
            }

            @Override
            public ChannelFuture close() {
                return null;
            }

            @Override
            public ChannelFuture deregister() {
                return null;
            }

            @Override
            public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture disconnect(ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture close(ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture deregister(ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelOutboundInvoker read() {
                return null;
            }

            @Override
            public ChannelFuture write(Object o) {
                return null;
            }

            @Override
            public ChannelFuture write(Object o, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture writeAndFlush(Object o) {
                return null;
            }

            @Override
            public ChannelPromise newPromise() {
                return null;
            }

            @Override
            public ChannelProgressivePromise newProgressivePromise() {
                return null;
            }

            @Override
            public ChannelFuture newSucceededFuture() {
                return null;
            }

            @Override
            public ChannelFuture newFailedFuture(Throwable throwable) {
                return null;
            }

            @Override
            public ChannelPromise voidPromise() {
                return null;
            }

            @NotNull
            @Override
            public Iterator<Map.Entry<String, ChannelHandler>> iterator() {
                return null;
            }
        };
    }

    @Override
    public ByteBufAllocator alloc() {
        return null;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress) {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress) {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1) {
        return null;
    }

    @Override
    public ChannelFuture disconnect() {
        return null;
    }

    @Override
    public ChannelFuture close() {
        return null;
    }

    @Override
    public ChannelFuture deregister() {
        return null;
    }

    @Override
    public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public ChannelFuture close(ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public ChannelFuture deregister(ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public Channel read() {
        return null;
    }

    @Override
    public ChannelFuture write(Object o) {
        return null;
    }

    @Override
    public ChannelFuture write(Object o, ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public Channel flush() {
        return null;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public ChannelFuture writeAndFlush(Object o) {
        return null;
    }

    @Override
    public ChannelPromise newPromise() {
        return null;
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return null;
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return null;
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable throwable) {
        return null;
    }

    @Override
    public ChannelPromise voidPromise() {
        return null;
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> attributeKey) {
        return null;
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> attributeKey) {
        return false;
    }

    @Override
    public int compareTo(@NotNull Channel channel) {
        return 0;
    }
}
