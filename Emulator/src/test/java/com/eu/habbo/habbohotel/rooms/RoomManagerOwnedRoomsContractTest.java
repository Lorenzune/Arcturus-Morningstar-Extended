package com.eu.habbo.habbohotel.rooms;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RoomManagerOwnedRoomsContractTest {

    @Test
    void ownedRoomLookupLoadsDatabaseRoomsBeforeReadingTheActiveCache() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/eu/habbo/habbohotel/rooms/RoomManager.java"));
        int method = source.indexOf("public List<Room> getRoomsForHabbo(Habbo habbo)");
        int nextMethod = source.indexOf("public List<Room> getRoomsForHabbo(String username)", method);
        String body = source.substring(method, nextMethod);

        assertTrue(body.indexOf("this.loadRoomsForHabbo(habbo);")
                < body.indexOf("this.activeRooms.values()"));
    }
}
