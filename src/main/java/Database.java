import java.util.ArrayList;

class Player {
    public Player(String name, int id) {
        nickName = name;
        ID= id;
    }
    public static int ID = 1;
    public String nickName;
}


class Room {
    public Room(int id, String name, int pl) {
        ID = id;
        Name = name;
        playerCount = 0;
        playerLimit = pl;
    }

    public void addPlayer(int id) {
        if (playerCount < playerLimit) {
            //players.add(); //Todo: implement Room methods
            playerCount++;
        }

    }

    public void removePlayer(int id) {

    }

    public void attachPlayer(String nickName) {


    }

    int ID;
    String Name;
    int playerCount;
    int playerLimit;
    ArrayList<Player> players;
}


public class Database {
    ArrayList<Player> players;
    ArrayList<Room> rooms;
    public Database(String filename) {
        players = new ArrayList<>();
        rooms = new ArrayList<>();

        //Todo: load DB file
    }

    public int registerPlayer(String nickName) throws Exception {
        int newID = 1;
        for (Player p: players) {
            if (p.nickName == nickName) {
                throw new Exception("Name already exists");
            }
            if (Player.ID == newID) {
                newID++;
            }
        }
        players.add(new Player(nickName, newID));

        return newID;
    }

    public int createNewRoom(String name, int playerLimit) throws Exception {
        int newID = 1;
        for (Room r: rooms) {
            if (r.Name == name) {
                throw new Exception("Room with such name already exists");
            }
            if (r.ID == newID) {
                newID++;
            }
        }
        rooms.add(new Room(newID, name, playerLimit));

        return newID;
    }

    public void cleanupRooms() {
        for (Room r : rooms) {
            if (r.playerCount == 0) {
                rooms.remove(r);
            }
        }
    }
}
