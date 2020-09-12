package Adriftizen;

import Adriftizen.commands.AsyncCommand;
import Adriftizen.events.ChunkEnterEvent;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.events.ScriptEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Adriftizen extends JavaPlugin {

    @Override
    public void onEnable() {
        ScriptEvent.registerScriptEvent(new ChunkEnterEvent());
        DenizenAPI.getCurrentInstance().getCommandRegistry().registerCommand(AsyncCommand.class);
    }
}
