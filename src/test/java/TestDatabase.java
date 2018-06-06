import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.*;

public class TestDatabase {
    Database db;
    @Test
    public void Test() {
        try {
            Files.copy(Paths.get("exampledb.json"), Paths.get("testdb.json"), REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        db = new Database("testdb.json");

        Player p = findPlayerById(0);
        assertNotNull("Player not found",p);
        assertEquals("DB not consistent", "VitalArt", p.nickName);

        p = findPlayerByName("endlessBorland");
        assertNotNull("Player not found", p);
        assertEquals("DB not consistent", 1, p.ID);

        String testname = "testplayer1";

        try {
            long roomid =db.createNewRoom("Room1", 8);
            assertEquals("Failed to create room", 1, db.rooms.size());

            int count = db.players.size();
            long id = db.registerPlayer(testname);
            assertEquals("Failed to register player", count + 1, db.players.size());

            System.out.println(db.rooms.get(0).playerCount);

            db.enterPlayerIntoRoom(roomid, id);
            assertTrue("Failed to add player to room", db.rooms.get(0).players.contains(id));

            System.out.println(db.rooms.get(0).playerCount);

            db.removePlayerFromRoom(roomid, id);
            assertFalse("Failed to remove player from room", db.rooms.get(0).players.contains(id));
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.shutdown();

        db = new Database("testdb.json");

        assertEquals("Failed to correctly save new data", testname, db.players.get(db.players.size()-1).nickName);

        try {
            Files.deleteIfExists( Paths.get("testdb.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Player findPlayerById(long id) {
        for (Player p: db.players) {
            if (p.ID == id) {
                return p;
            }

        }
        return null;
    }

    public Player findPlayerByName(String name) {
        for (Player p: db.players) {
            if (p.nickName.equals(name)) {
                return p;
            }

        }
        return null;
    }
}
