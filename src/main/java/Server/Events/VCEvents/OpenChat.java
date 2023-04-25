package Server.Events.VCEvents;

public class OpenChat extends VCEvent{
    private final String methodname;

    public OpenChat(){
        this.methodname = "onOpenChat";
    }

    public String getMethodName(){
        return this.methodname;
    }

    public Object getValue(){
        return null;
    }
}