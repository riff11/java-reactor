package mtus.net.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import mtus.net.reactor.model.Client;

/**
 * @author Martin Tuskevicius
 */
public abstract class Acceptor implements Runnable {

    private final ServerSocketChannel serverSocketChannel;

    public Acceptor(int port) throws IOException {
	ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
	serverSocketChannel.configureBlocking(true);
	serverSocketChannel.socket().bind(new InetSocketAddress(port));
	this.serverSocketChannel = serverSocketChannel;
    }

    public final void run() {
	while (serverSocketChannel.isOpen()) {
	    try {
		SocketChannel socketChannel = serverSocketChannel.accept();
		if (socketChannel == null) {
		    continue;
		}
		socketChannel.configureBlocking(false);
		SelectionKey selectionKey = Dispatcher.registerChannel(
			socketChannel, SelectionKey.OP_READ);
		selectionKey.attach(createClient(selectionKey));
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public final void stop() throws IOException {
	serverSocketChannel.close();
    }

    public abstract Client createClient(SelectionKey selectionKey);
}
