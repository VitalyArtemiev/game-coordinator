import java.util.ArrayList;
import java.util.List;

public class MessagingDB {
    static private volatile List<Message> queue = new ArrayList();

    static public /* just in case */  synchronized /* just in case */ List<Message> findMessages(long to, long room)
    {
        List<Message> returnList = new ArrayList();
        for (Message a: queue) {
            if (a.getTo() == to && a.getRoom() == room)
            {
                returnList.add(a);
                queue.remove(a);
            }
        }
        return returnList;
    }

    static synchronized void addElement(Message a)
    {
        queue.add(a);
        System.out.print(queue.get(0).getMessage());
    }
}
