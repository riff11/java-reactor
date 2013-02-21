package mtus.net.reactor.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author Martin Tuskevicius
 */
public abstract class Client {

    protected final SelectionKey selectionKey;

    public Client(SelectionKey selectionKey) {
	this.selectionKey = selectionKey;
    }

    /**
     * 
     */
    public abstract void handleData();

    /**
     * 
     * @return
     */
    public abstract ByteBuffer getReadBuffer();

    /**
     * 
     * @return
     */
    public abstract ByteBuffer getWriteBuffer();

    /**
     * 
     * @param buffer
     * @throws IOException
     */
    public final void write(ByteBuffer buffer) throws IOException {
	ByteBuffer writeBuffer = getWriteBuffer();
	if (writeBuffer.position() > 0) {
	    writeBuffer.put(buffer);
	} else {
	    ((SocketChannel) selectionKey.channel()).write(buffer);
	    if (buffer.hasRemaining()) {
		writeBuffer.put(buffer);
		selectionKey.interestOps(SelectionKey.OP_WRITE);
	    }
	}
    }

    /**
     * 
     * @throws IOException
     */
    public final void disconnect() throws IOException {
	((SocketChannel) selectionKey.channel()).close();
	selectionKey.attach(null);
	remove();
    }

    /**
     * 
     */
    protected abstract void remove();

    /**
     * 
     * @return
     */
    public final SelectionKey getSelectionKey() {
	return selectionKey;
    }
}
