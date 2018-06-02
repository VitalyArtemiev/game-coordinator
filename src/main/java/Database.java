import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

class Player {
    public Player(String name, long id) {
        nickName = name;
        ID= id;
    }
    public  long ID;
    public String nickName;
}


class Room {
    public Room(long id, String name, int pl) {
        ID = id;
        Name = name;
        playerCount = 0;
        playerLimit = pl;
    }

    public boolean addPlayer(long id) {
        if (playerCount < playerLimit) {
            players.add(id);
            playerCount++;
            return true;
        }
        return false;
    }

    public boolean removePlayer(long id) {
        if (players.contains(id)) {
            players.remove((Long) id);
            return true;
        }
        return false;
    }

    long ID;
    String Name;
    int playerCount;
    int playerLimit;
    ArrayList<Long> players;
}

enum TaskType {tRegister, tRoomList, tNewRoom, tEnter, tLeave, tParse};

class DBTask {
    private static final String MESSAGE_TASK_FAIL = "task fail\n"; //todo: make a proper response
    private static final String MESSAGE_TASK_SUCCESS = "task success\n";
    private static final String MESSAGE_REGISTER = "registered\n";
    private static final String MESSAGE_ROOM_ENTER = "room entered\n";
    private static final String MESSAGE_ROOM_EXIT = "room exited\n";
    private static final String MESSAGE_ROOM_CREATE = "room created\n";
    private static final String MESSAGE_ROOMLIST = "roomList\n";

    TaskType t;
    String input;
    Channel ch;
    Player p;
    Room r;

    DBTask(String input) {
        t = TaskType.tParse;
        this.input = input;
    }

    public void respond(boolean success, String response) {//todo: make a proper json
        String result = "";

        switch (t) {
            case tRegister: result = MESSAGE_REGISTER + response;
                break;
            case tRoomList: result = MESSAGE_ROOMLIST + response;
                break;
            case tNewRoom: result = MESSAGE_ROOM_CREATE + response;
                break;
            case tEnter: result = MESSAGE_ROOM_ENTER + response;
                break;
            case tLeave: result = MESSAGE_ROOM_EXIT + response;
                break;
        }

        if (success) {
            result += MESSAGE_TASK_SUCCESS;
        }
        else {
            result += MESSAGE_TASK_FAIL;
        }

        ch.writeAndFlush(new TextWebSocketFrame(result));
    }

    public void parse() {

        switch (input) {
            case "getRoomList\n": {

                t = TaskType.tRoomList;
                break;
            }
            case "close": {
                ch.close();
                //ch.
                break;
            }
            case "shutdown": {
                ch.parent().close();
                break;
            }
            default: {
                //response = "Command not supported: '" + request + '\'';
            }
        }

        //input = ""; todo: uncomment for release
    }
}


public class Database {
    private static final int SERVICE_THREAD_COUNT = 2;

    private static final String DEFAULT_DB_NAME = "PlayerDB.json";

    private String DBFileName = DEFAULT_DB_NAME;

    ArrayList<Player> players;
    ArrayList<Room> rooms;

    public ConcurrentLinkedQueue<DBTask> tasks;


    class ServiceThread implements Runnable {
        private volatile boolean shutdown;

        @Override
        public void run() {
            tasks = new ConcurrentLinkedQueue<>();
            while (!shutdown) {
                DBTask task = null;
                try {
                    task = tasks.remove();
                    switch (task.t) {
                        case tParse: {
                            task.parse();
                            tasks.add(task);
                        }
                        case tRegister: {
                            long id = registerPlayer(task.p.nickName);
                            task.respond(true, Long.toString(id));
                            break;
                        }
                        case tRoomList: { //todo: implement getroomlist
                            String response = "4\n";
                            response += "1 room1 2 8\n";
                            response += "2 room2 3 10\n";
                            response += "3 room3 5 6\n";
                            response += "4 room4 1 4\n";
                            task.respond(true, response);
                            break;
                        }
                        case tNewRoom: {
                            long id = createNewRoom(task.r.Name, task.r.playerLimit);
                            task.respond(true,Long.toString(id));
                            break;
                        }
                        case tEnter: {
                            enterPlayerIntoRoom(task.r.ID, task.p.ID);
                            task.respond(true,"");
                            break;
                        }
                        case tLeave: {
                            removePlayerFromRoom(task.r.ID, task.p.ID);
                            task.respond(true,"");
                            break;
                        }
                        default: {

                        }
                    }
                }
                catch (NoSuchElementException e) {
                    /*
                    try {
                        sleep(10);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    */

                    continue;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (task != null) {
                        task.respond(false, e.getMessage());
                    }
                    continue;
                }
                //todo: add 1 hr timer to cleanup rooms
            }
        }

        public void shutdown() {
            shutdown = true;
        }
    }

    private ServiceThread st;

    public Database(String filename) {
        if (filename != "") {
            DBFileName = filename;
        }
        players = new ArrayList<>();
        rooms = new ArrayList<>();

        load();

        st = new ServiceThread();

        for (int i = 0; i < SERVICE_THREAD_COUNT; i++) {
            new Thread(st).start();
        }
    }

    private void save() {
        ObjectMapper mapper = new ObjectMapper();
        for (Player p: players) {
            //JsonNode node = mapper.
        }

    }

    private void load() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        //rootNode.

        long maxid = 0;

        for (Player p : players) {
            if (p.ID > maxid) {
                maxid = p.ID;
            }
        }

        lastPlayerId = new AtomicLong(maxid);
        lastRoomId = new AtomicLong(1);//todo: possibly save rooms? nah.
    }

    AtomicLong lastRoomId;
    AtomicLong lastPlayerId;

    public long registerPlayer(String nickName) throws Exception {
        long newID = lastPlayerId.incrementAndGet();

        players.add(new Player(nickName, newID));

        return newID;
    }

    public long createNewRoom(String name, int playerLimit) throws Exception {
        long newID = lastRoomId.incrementAndGet();

        synchronized (rooms) {
            for (Room r : rooms) {
                if (r.Name == name) {
                    throw new Exception("Room with such name already exists, ID " + r.ID);
                }
            }
            rooms.add(new Room(newID, name, playerLimit));
        }

        return newID;
    }

    public void enterPlayerIntoRoom(long roomid, long playerid) throws Exception {
        for (Room r: rooms) {
            if (r.ID == roomid) {
                synchronized (r) {
                    if (!r.addPlayer(playerid)) {
                        throw new Exception("Room " + roomid + " is full");
                    }
                }
                return;
            }
        }
    }

    public void removePlayerFromRoom(long roomid, long playerid) throws Exception {
        for (Room r: rooms) {
            if (r.ID == roomid) {
                synchronized (r) {
                    if (!r.removePlayer(playerid)) {
                        throw new NoSuchElementException("Player with ID " + playerid + " not found in room with ID " + roomid);
                    }
                }
                return;
            }
        }
    }

    public void cleanupRooms() {
        for (Room r : rooms) {
            if (r.playerCount == 0) {
                rooms.remove(r);
            }
        }
    }

    public void shutdown(){
        st.shutdown();
        save();
    }
}
