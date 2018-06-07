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
            }
        }
        for (int i = 0, j = 0; i < queue.size(); i++)
        {
            if (queue.get(i).getRoom() == returnList.get(j).getRoom() &&
                    queue.get(i).getTo() == returnList.get(j).getTo())
            {
                queue.remove(i);
                j++;
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
