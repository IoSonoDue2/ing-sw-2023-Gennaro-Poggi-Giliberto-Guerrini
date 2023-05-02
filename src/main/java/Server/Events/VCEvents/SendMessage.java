package Server.Events.VCEvents;

import Server.Model.Chat.Message;

public class SendMessage extends VCEvent{
    private final String secondaryType = "SendMessage";
    private final String methodname;
    private final Message message;

    public SendMessage(Message message){
        this.message = message;
        this.methodname = "onSendMessage";
    }

    public String getMethodName(){
        return this.methodname;
    }
    public Object getValue(){
        return this.message;
    }

    public String getType(){
        return "SendMessage";
    }

}
