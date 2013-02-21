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

    /**
     * Creates a new acceptor. The provided port number is the port from which
     * clients will be accepted.
     * 
     * @param port
     * @throws IOException
     */
    public Acceptor(int port) throws IOException {
	ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
	serverSocketChannel.configureBlocking(true);
	serverSocketChannel.socket().bind(new InetSocketAddress(port));
	this.serverSocketChannel = serverSocketChannel;
    }

    /**
     * Accepts all incoming connections. Connections are accepted in a blocking
     * manner, meaning that this thread will block until a client connects. Once
     * the connection is accepted, the channel is configured to be non-blocking
     * and registered with the dispatcher's selector with an interest in a read
     * operation. A separate thread for accepting clients provides the quickest
     * and most reliable service; the selector does not need to be awakened nor
     * does the request need to wait to be processed. This thread is idle the
     * vast majority of the time, so it has a minimal impact on the processor
     * usage.
     */
    public final void run() {
	while (true) {
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

    /**
     * 
     * @param selectionKey
     * @return
     */
    public abstract Client createClient(SelectionKey selectionKey);
}
