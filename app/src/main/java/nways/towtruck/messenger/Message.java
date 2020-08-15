package nways.towtruck.messenger;

public class Message {
    private String from_email, to_email, message;
    private long data;

    Message (String from_email, String to_email, String message, long data){
        this.from_email = from_email;
        this.to_email = to_email;
        this.message = message;
        this.data = data;
    }

    public void setFrom_email(String from_email) {
        this.from_email = from_email;
    }

    public void setTo_email(String to_email){
        this.to_email = to_email;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setData(Long data){
        this.data = data;
    }

    public String getFrom_email(){
        return from_email;
    }

    public String getTo_email(){
        return to_email;
    }

    public String getMessage(){
        return message;
    }

    public Long getData(){
        return data;
    }
}
