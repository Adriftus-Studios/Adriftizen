package Adriftizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class ChunkEnterEvent extends BukkitScriptEvent {


    public ChunkEnterEvent() {
        instance = this;
    }

    public static ChunkEnterEvent instance;

    public PlayerTag currentPlayer;
    public ChunkTag to;
    public ChunkTag from;
    public Boolean entering;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player chunk change")) {
            return false;
        }
        return true;
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("new_chunk") && entering) {
            return to;
        } else if (name.equals("old_chunk") && from != null) {
            return from;
        }
        return super.getContext(name);
    }

    @Override
    public String getName() {
        return "ChunkEntryExit";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(currentPlayer, null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        from = new ChunkTag(event.getPlayer().getLocation().getChunk());
        entering = false;
        fire(event);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        entering = true;
        to = new ChunkTag(event.getPlayer().getLocation().getChunk());
        fire(event);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getChunk() == event.getTo().getChunk()) {
            return;
        }
        from = new ChunkTag(event.getFrom().getChunk());
        entering = true;
        to = new ChunkTag(event.getTo().getChunk());
        fire(event);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getChunk() == event.getTo().getChunk()) {
            return;
        }
        entering = true;
        from = new ChunkTag(event.getFrom().getChunk());
        to = new ChunkTag(event.getTo().getChunk());
        fire(event);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        entering = true;
        to = new ChunkTag(event.getPlayer().getLocation().getChunk());
        fire(event);
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getFrom().getChunk() == event.getTo().getChunk()) {
            return;
        }
        for (Entity entity : event.getVehicle().getPassengers()) {
            if (EntityTag.isPlayer(entity)) {
                entering = true;
                from = new ChunkTag(event.getFrom().getChunk());
                to = new ChunkTag(event.getTo().getChunk());
                fire(event);
            }
        }
    }
}
