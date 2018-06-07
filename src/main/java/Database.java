import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import javax.print.DocFlavor;
import javax.xml.bind.annotation.XmlType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;


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
        this.name = name;
        players = new ArrayList<>();

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
            players.remove(Long.valueOf(id));
            return true;
        }
        return false;
    }

    long ID;
    String name;
    int playerCount;
    int playerLimit;
    ArrayList<Long> players;
}

enum TaskType {tInvalid, tRegister, tRoomList, tNewRoom, tEnter, tLeave, tParse, tMessage}

class DBTask {
    private static final String MESSAGE_TASK_FAIL = "fail";
    private static final String MESSAGE_TASK_SUCCESS = "success";
    private static final String MESSAGE_REGISTER = "register";
    private static final String MESSAGE_ROOM_ENTER = "enterRoom";
    private static final String MESSAGE_ROOM_EXIT = "exitRoom";
    private static final String MESSAGE_ROOM_CREATE = "newRoom";
    private static final String MESSAGE_ROOMLIST = "roomList";
    private static final String MESSAGE_SEND_TO = "communicate";

    private static final String STRING_NOT_FOUND = "error: no string";
    private static final String STRING_DEFAULT = "string not needed";
    private static final int INT_NOT_FOUND = -2;
    private static final int INT_DEFAULT = -1;

    TaskType t;
    String input;
    Channel ch;
    Player p;
    Player rp;
    Room r;
    String commMessage;

    DBTask(Channel ch, String input) {
        t = TaskType.tParse;
        this.ch = ch;
        this.input = input;
    }

    public void respond(boolean success, Object response) {
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode rootNode = objectMapper.createObjectNode();
        ObjectNode message = objectMapper.createObjectNode();
        ObjectNode responseNode = objectMapper.createObjectNode();

        switch (t) {
            case tMessage: {
                responseNode.put("status", "message is added");
                responseNode.put("message", (String) response);
                break;
            }
            case tRegister: {
                responseNode.put("task", MESSAGE_REGISTER);
                responseNode.put("id", (long) response);
                break;
            }
            case tRoomList: {
                responseNode.put("task", MESSAGE_ROOMLIST);

                ArrayNode rooms = (ArrayNode) response;

                responseNode.putPOJO("room", rooms);
                break;
            }
            case tNewRoom: {
                responseNode.put("task", MESSAGE_ROOM_CREATE);
                responseNode.put("roomId", (long) response);
                break;
            }
            case tEnter: {
                responseNode.put("task", MESSAGE_ROOM_ENTER);
                //todo: return player list
                break;
            }
            case tLeave: {
                responseNode.put("task", MESSAGE_ROOM_EXIT);
                break;
            }
            case tInvalid: {
                responseNode.put("task", (String) response);
            }
        }

        if (success) {
            responseNode.put("status", MESSAGE_TASK_SUCCESS);
        }
        else {
            responseNode.put("status", MESSAGE_TASK_FAIL);
        }

        message.putPOJO("response", responseNode);

        rootNode.putPOJO("message", message);

        String result = null;
        try {
            result = objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        ch.writeAndFlush(new TextWebSocketFrame(result));
    }

    public void parse() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(input);
        } catch (IOException e) {
            t = TaskType.tInvalid;
            throw e;
        }

        JsonNode message = rootNode.path("message");

        String request = message.path("request").asText(STRING_NOT_FOUND);

        switch (request) {
            // messages client-to-client
            case MESSAGE_SEND_TO: {
                t = TaskType.tMessage;
                p = new Player(STRING_DEFAULT, message.path("sender").asLong(INT_NOT_FOUND));
                rp = new Player(STRING_DEFAULT, message.path("reciever").asLong(INT_NOT_FOUND));
                r = new Room(message.path("roomId").asLong(INT_NOT_FOUND), STRING_DEFAULT, INT_DEFAULT);
                commMessage = message.path("commMessage").asText(STRING_NOT_FOUND);
                break;
            }
            case MESSAGE_REGISTER: {
                t = TaskType.tRegister;
                p = new Player(message.path("name").asText(STRING_NOT_FOUND), INT_DEFAULT);
                break;
            }
            case MESSAGE_ROOMLIST: {
                t = TaskType.tRoomList;
                p = new Player(STRING_DEFAULT, message.path("id").asLong(INT_NOT_FOUND));
                break;
            }
            case MESSAGE_ROOM_CREATE: {
                t = TaskType.tNewRoom;
                p = new Player(STRING_DEFAULT, message.path("id").asLong(INT_NOT_FOUND));
                r = new Room(INT_DEFAULT, message.path("roomName").asText(STRING_NOT_FOUND), message.path("playerLimit").asInt(INT_NOT_FOUND));
                break;
            }
            case MESSAGE_ROOM_ENTER: {
                t = TaskType.tEnter;
                p = new Player(STRING_DEFAULT, message.path("id").asLong(INT_NOT_FOUND));
                r = new Room(message.path("roomId").asLong(INT_NOT_FOUND), STRING_DEFAULT, INT_DEFAULT);
                break;
            }
            case MESSAGE_ROOM_EXIT: {
                t = TaskType.tLeave;
                p = new Player(STRING_DEFAULT, message.path("id").asLong(INT_NOT_FOUND));
                r = new Room(message.path("roomId").asLong(INT_NOT_FOUND), STRING_DEFAULT, INT_DEFAULT);
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
                t = TaskType.tInvalid;
                //response = "Command not supported: '" + request + '\'';
            }
        }

        if (p != null) {
            if (p.ID == INT_NOT_FOUND || p.nickName == STRING_NOT_FOUND) {
                throw new IOException("Invalid player parameters");
            }
        }

        if (r != null) {
            if (r.ID == INT_NOT_FOUND || r.name == STRING_NOT_FOUND || r.playerLimit == INT_NOT_FOUND) {
                throw new IOException("Invalid room parameters");
            }
        }

        //input = ""; todo: uncomment for release
    }
}


public class Database {
    private static final int SERVICE_THREAD_COUNT = 2;

    private static final String DEFAULT_DB_NAME = "PlayerDB.json";

    private String DBFileName = DEFAULT_DB_NAME;

    final ArrayList<Player> players;
    final ArrayList<Room> rooms;

    public ConcurrentLinkedQueue<DBTask> tasks;


    class ServiceThread implements Runnable {
        private volatile boolean shutdown;

        @Override
        public void run() {
            tasks = new ConcurrentLinkedQueue<>();
            while (!shutdown) {
                DBTask task = null;
                try {
                    try {
                        task = tasks.remove();
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

                    switch (task.t) {
                        case tParse: {
                            task.parse();
                            tasks.add(task);
                            break; //don't remove, fallthrough is buggy
                        }
                        case tRegister: {
                            long id = registerPlayer(task.p.nickName);
                            task.respond(true, id);
                            break;
                        }
                        case tRoomList: {
                            /*Room room  = new Room(1, "hello", 8);
                            rooms.add(room);
                            room = new Room(2, "hello1", 6);
                            rooms.add(room);
                            room = new Room(3, "hello2", 5);
                            rooms.add(room);
                            room = new Room(20, "hello3", 4);
                            rooms.add(room);*/

                            JsonNode response = getRoomList();
                            task.respond(true, response);
                            break;
                        }
                        case tNewRoom: {
                            long id = createNewRoom(task.r.name, task.r.playerLimit);
                            task.respond(true, id);
                            break;
                        }
                        case tEnter: {
                            enterPlayerIntoRoom(task.r.ID, task.p.ID);
                            task.respond(true,null);
                            break;
                        }
                        case tLeave: {
                            removePlayerFromRoom(task.r.ID, task.p.ID);
                            task.respond(true,null);
                            break;
                        }
                        case tInvalid: {
                            task.respond(false, "invalid");
                            break;
                        }
                        case tMessage: {
                            // TODO: add proper actions
                            Message mg = new Message(task.p.ID, task.rp.ID, task.r.ID, task.commMessage);
                            MessagingDB.addElement(mg);
                            task.respond(true, task.commMessage);
                        }
                        default: {

                        }
                    }
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

    private final ServiceThread st;

    public Database(String filename) {
        if (filename != "") {
            DBFileName = filename;
        }

        players = new ArrayList<>();
        rooms = new ArrayList<>();


        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        st = new ServiceThread();

        for (int i = 0; i < SERVICE_THREAD_COUNT; i++) {
            new Thread(st).start();
        }
    }

    private void save() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode playersNode = objectMapper.createArrayNode();

        for (Player p: players) {
            ObjectNode pNode = objectMapper.createObjectNode();
            pNode.put("id", p.ID);
            pNode.put("name", p.nickName);
            playersNode.add(pNode);
        }

        ObjectNode temp = objectMapper.createObjectNode();
        temp.putPOJO("player", playersNode);

        rootNode.putPOJO("PlayerDB", temp);

        String result = objectMapper.writeValueAsString(rootNode);

        byte[] jsonData = objectMapper.writeValueAsBytes(rootNode);

        Files.write(Paths.get(DBFileName), jsonData);
    }

    private void load() throws IOException {
        long maxid = 0;
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get(DBFileName));

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(jsonData);
            JsonNode playersNode = rootNode.path("PlayerDB").path("player");
            if (playersNode.isArray()) {
                for (final JsonNode pNode : playersNode) {
                    Player p = new Player(pNode.get("name").asText("error"), pNode.get("id").asLong(-1));
                    players.add(p);
                }
            }



            for (Player p : players) {
                if (p.ID > maxid) {
                    maxid = p.ID;
                }
            }
        }
        finally {

            lastPlayerId = new AtomicLong(maxid);
            lastRoomId = new AtomicLong(1);
        }
    }

    AtomicLong lastRoomId;
    AtomicLong lastPlayerId;

    public long registerPlayer(String nickName) throws Exception {
        long newID = lastPlayerId.incrementAndGet();

        players.add(new Player(nickName, newID));

        return newID;
    }

    public ArrayNode getRoomList() {
        ObjectMapper objectMapper = new ObjectMapper();

        ArrayNode roomsNode = objectMapper.createArrayNode();

        for (Room r: rooms) {
            ObjectNode rNode = objectMapper.createObjectNode();
            rNode.put("id", r.ID);
            rNode.put("name", r.name);
            rNode.put("playerCount", r.playerCount);
            rNode.put("playerLimit", r.playerLimit);
            roomsNode.add(rNode);
        }

        return roomsNode;
    }

    public long createNewRoom(String name, int playerLimit) throws Exception {
        long newID = lastRoomId.incrementAndGet();

        synchronized (rooms) {
            for (Room r : rooms) {
                if (r.name == name) {
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
        throw new NoSuchElementException("Room with ID " + roomid + " not found");
    }

    public void removePlayerFromRoom(long roomid, long playerid) throws NoSuchElementException {
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
        throw new NoSuchElementException("Room with ID " + roomid + " not found");
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
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
