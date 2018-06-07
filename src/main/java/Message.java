public class Message {
    public Message(long from, long to, long room, String message) {
        this.from = from;
        this.to = to;
        this.room = room;
        this.message = message;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public long getRoom() {
        return room;
    }

    public void setRoom(long room) {
        this.room = room;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private long from;
    private long to;



    private long room;
    private String message;
}