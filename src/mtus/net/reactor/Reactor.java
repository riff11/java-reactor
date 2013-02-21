package mtus.net.reactor;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * @author Martin Tuskevicius
 */
public class Reactor {

    private static boolean started = false;

    public static void start(Acceptor acceptor, EventHandler eventHandler)
	    throws IOException {
	if (started) {
	    throw new IllegalStateException("The reactor is already started!");
	}
	Dispatcher.selector = Selector.open();
	new Thread(acceptor).start();
	new Thread(new Dispatcher(eventHandler)).start();
	started = true;
    }

    public static void start(Acceptor acceptor) throws IOException {
	start(acceptor, new EventHandler());
    }
}
