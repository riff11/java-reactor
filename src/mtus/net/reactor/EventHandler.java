package mtus.net.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import mtus.net.reactor.model.Client;

/**
 * @author Martin Tuskevicius
 */
public class EventHandler {

    public void onConnect(SelectionKey selectionKey) throws Exception {

    }

    public void onRead(SelectionKey selectionKey) throws Exception {
	SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
	Client client = (Client) selectionKey.attachment();
	ByteBuffer readBuffer = client.getReadBuffer();
	int bytesRead;
	try {
	    bytesRead = socketChannel.read(readBuffer);
	} catch (IOException e) {
	    e.printStackTrace();
	    client.disconnect();
	    return;
	}
	if (bytesRead == 0) {
	    return;
	} else if (bytesRead == -1) {
	    client.disconnect();
	    return;
	}
	readBuffer.flip();
	try {
	    client.handleData();
	} finally {
	    if (readBuffer.hasRemaining()) {
		readBuffer.compact();
	    } else {
		readBuffer.clear();
	    }
	}
    }

    public void onWrite(SelectionKey selectionKey) throws Exception {
	SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
	Client client = (Client) selectionKey.attachment();
	ByteBuffer writeBuffer = client.getWriteBuffer();
	writeBuffer.flip();
	try {
	    socketChannel.write(writeBuffer);
	} catch (IOException e) {
	    e.printStackTrace();
	    client.disconnect();
	    return;
	}
	if (writeBuffer.hasRemaining()) {
	    writeBuffer.compact();
	} else {
	    writeBuffer.clear();
	    selectionKey.interestOps(SelectionKey.OP_READ);
	}
    }
}
