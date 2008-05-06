package org.cs3.pl.prolog.internal.lifecycle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;

import org.cs3.pl.prolog.PrologInterface;
import org.cs3.pl.prolog.PrologInterfaceException;
import org.cs3.pl.prolog.PrologSession;

public class ShutdownState extends AbstractState {

	private PrologInterfaceException error;

	public ShutdownState(LifeCycle context) {
		super(context);
	
	}

	
	
	public void enter() {
		
		HashSet<LifeCycleHookWrapper> done = new HashSet<LifeCycleHookWrapper>();
		HashMap<String, LifeCycleHookWrapper> hooks = context.getHooks();
		
		PrologInterface pif = context.getPrologInterface();
		context.clearWorkQueue(); //there may be afterINit hooks left. dump them.
		
		for (LifeCycleHookWrapper w : context.getHooks().values()) {			
			w.beforeShutdown(done);
		}
		
		context.enqueueWork(new NamedWorkRunnable("shutdown_server") {
			
			public void run() throws PrologInterfaceException {
				try {
					context.disposeSessions();
					context.stopServer();
					context.workDone();
				} catch (Throwable e) {
					throw new PrologInterfaceException(e);					
				}
				
			}
		});
	}

	

	
	public State workDone() {	
		return new DownState(context); //reset when hooks are through.
	}

	

}
