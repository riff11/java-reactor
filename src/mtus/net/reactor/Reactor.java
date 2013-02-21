package mtus.net.reactor;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * @author Martin Tuskevicius
 */
public class Reactor {

    private static boolean started = false;

    /**
     * Starts the reactor. This begins by initializing the dispatcher selector.
     * Next, the acceptor thread is started. Finally, the dispatcher is created
     * using the provided {@code EventHandler} instance and a thread is created
     * and started to process that dispatcher. The reactor is designed to be
     * started only once. However, multiple acceptor threads are supported. If
     * this method is invoked after the reactor is started, an
     * {@code IllegalStateException} is thrown.
     * 
     * @param acceptor
     * @param eventHandler
     * @throws IOException
     * @throws IllegalStateException
     */
    public static void start(Acceptor acceptor, EventHandler eventHandler)
	    throws IOException {
	if (started) {
	    throw new IllegalStateException("The reactor is already started!");
	}
	Dispatcher.selector = Selector.open();
	new Thread(acceptor).start();
	new Thread(eventHandler == null ? new Dispatcher() : new Dispatcher(
		eventHandler)).start();
	started = true;
    }

    /**
     * Starts the reactor using a standard implementation of the
     * {@code EventHandler} class.
     * 
     * @param acceptor
     * @throws IOException
     * @see {@link #start(Acceptor, EventHandler)}
     */
    public static void start(Acceptor acceptor) throws IOException {
	start(acceptor, null);
    }
}
