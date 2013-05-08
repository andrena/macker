package de.andrena.tools.macker.event;

import org.apache.commons.lang.exception.NestableException;

public class ListenerException
    extends NestableException
    {
    public ListenerException(MackerEventListener listener, String message)
        {
        super(createMessage(listener, message));
        this.listener = listener;
        }
    
    public ListenerException(MackerEventListener listener, String message, Throwable cause)
        {
        super(createMessage(listener, message), cause);
        this.listener = listener;
        }
    
    public MackerEventListener getListener()
        { return listener; }
    
    private static String createMessage(MackerEventListener listener, String message)
        { return "Aborted by " + listener + ": " + message; }
    
    private final MackerEventListener listener;
    }
