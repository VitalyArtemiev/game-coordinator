import java.util.ArrayList;
import java.util.List;

public class MessagingDB {
    static private volatile List<Message> queue = new ArrayList();

    static public /* just in case */  synchronized /* just in case */ List<Message> findMessages(long to)
    {
        List<Message> returnList = new ArrayList();
        for (Message a: queue) {
            if (a.getTo() == to)
            {
                queue.remove(a);
                returnList.add(a);
            }
        }
        return returnList;
    }

    static synchronized void addElement(Message a)
    {
        queue.add(a);
    }
}
