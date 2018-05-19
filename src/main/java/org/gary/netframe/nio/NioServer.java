package org.gary.netframe.nio;

import org.gary.netframe.eventhandler.EventHandler;
import org.gary.netframe.eventloop.EventLoopGroup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

//考虑eventloop自动扩容
//考虑将ByteBuffer暴露给用户
//处理连接中断后用户的写行为
//重写群发功能
//给用于预置序列化功能，所以需要重载方法

public class NioServer {

    private EventLoopGroup eventLoopGroup;

    public NioServer(EventHandler eventHandler) {
        eventLoopGroup = new EventLoopGroup(eventHandler);
    }

    public NioServer startup(final int port) {
        new Thread(() -> {
            ServerSocketChannel serverSocketChannel = null;
            Selector selector = null;
            try {
                selector = Selector.open();
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(port));
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                while (!Thread.interrupted()) {
                    selector.select();
                    Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                    while (selectionKeyIterator.hasNext()) {
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        if(!selectionKey.isValid())
                            continue;
                        if(selectionKey.isAcceptable()){
                            handleAccept(selectionKey);
                        }else if(selectionKey.isReadable() || selectionKey.isWritable()){
                            eventLoopGroup.dispatch(selectionKey);
                        }
                        selectionKeyIterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (selector != null) {
                    try {
                        selector.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (serverSocketChannel != null) {
                    try {
                        serverSocketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return this;
    }

    private void handleAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        Socket socket=socketChannel.socket();
        System.out.println("与客户端建立连接"+socket.getInetAddress()+":"+socket.getPort());
    }

    public void writeToAll(byte[] content){

    }

}
