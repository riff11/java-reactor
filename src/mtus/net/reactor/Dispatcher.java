package mtus.net.reactor;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Martin Tuskevicius
 */
public final class Dispatcher implements Runnable {

    private static final ExecutorService workerPool = Executors
	    .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final Object guardLock = new Object();
    protected static Selector selector;
    private final EventHandler eventHandler;

    protected Dispatcher(EventHandler eventHandler) {
	this.eventHandler = eventHandler;
    }

    public void run() {
	int amountSelected;
	while (true) {
	    synchronized (guardLock) {
	    }
	    try {
		amountSelected = selector.select();
	    } catch (IOException e) {
		e.printStackTrace();
		continue;
	    }
	    if (amountSelected == 0) {
		continue;
	    }
	    Iterator<SelectionKey> selectedKeys = selector.selectedKeys()
		    .iterator();
	    while (selectedKeys.hasNext()) {
		final SelectionKey selectionKey = selectedKeys.next();
		selectedKeys.remove();
		if (!selectionKey.isValid()) {
		    continue;
		}
		workerPool.execute(new Runnable() {
		    public void run() {
			try {
			    if (selectionKey.isConnectable()) {
				eventHandler.onConnect(selectionKey);
			    }
			    if (selectionKey.isReadable()) {
				eventHandler.onRead(selectionKey);
			    }
			    if (selectionKey.isWritable()) {
				eventHandler.onWrite(selectionKey);
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }
		});
	    }
	}
    }

    public static SelectionKey registerChannel(
	    SelectableChannel selectableChannel, int operation)
	    throws ClosedChannelException {
	synchronized (guardLock) {
	    selector.wakeup();
	    return selectableChannel.register(selector, operation);
	}
    }
}
