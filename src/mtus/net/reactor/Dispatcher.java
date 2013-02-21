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

    /**
     * The thread pool is a fixed thread pool with the number of threads set as
     * the number of available processors.
     */
    private static final ExecutorService threadPool = Executors
	    .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final Object guardLock = new Object();
    private static Selector selector;
    private final EventHandler eventHandler;

    /**
     * Creates a new dispatcher using the provided event handler.
     * 
     * @param eventHandler
     */
    public Dispatcher(EventHandler eventHandler) {
	this.eventHandler = eventHandler;
    }

    /**
     * Creates a new dispatcher using a standard event handler.
     */
    public Dispatcher() {
	this(new EventHandler());
    }

    /**
     * 
     */
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
		threadPool.execute(new Runnable() {
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

    /**
     * Registers a channel with the dispatcher's selector and interests the
     * channel in the provided operation. This method is thread-safe, meaning
     * that it can be invoked safely from any thread, at any time. This is
     * achieved by using a "guard lock." A guard lock is a vanilla
     * {@code Object} upon which threads, including the dispatcher thread,
     * synchronize. This method synchronizes upon the guard lock, wakes up the
     * selector, and finally registers the channel. At the beginning of every
     * dispatcher cycle, it waits until it owns the guard lock's monitor before
     * continuing. This ensures that any channel registrations from different
     * threads are accounted for in the upcoming select because acquiring the
     * lock forces the dispatcher to wait until all of the registrations have
     * finished. The selector is awakened by this registration operation to
     * ensure that the channel is incorporated as soon as possible.
     * 
     * @param selectableChannel
     * @param operation
     * @return
     * @throws ClosedChannelException
     * @see {@link SelectableChannel#register(Selector, int)}
     */
    public static SelectionKey registerChannel(
	    SelectableChannel selectableChannel, int operation)
	    throws ClosedChannelException {
	synchronized (guardLock) {
	    selector.wakeup();
	    return selectableChannel.register(selector, operation);
	}
    }
}
