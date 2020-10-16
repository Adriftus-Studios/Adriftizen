package Adriftizen;

import Adriftizen.commands.AsyncCommand;
import Adriftizen.events.ChunkEnterEvent;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.plugin.java.JavaPlugin;

public class Adriftizen extends JavaPlugin {

    public static Adriftizen instance;

    @Override
    public void onEnable() {
        instance = this;
        ScriptEvent.registerScriptEvent(new ChunkEnterEvent());
        DenizenAPI.getCurrentInstance().getCommandRegistry().registerCommand(AsyncCommand.class);

        // <--[tag]
        // @attribute <ElementTag.urls>
        // @returns ListTag
        // @plugin Adriftizen
        // @description
        // Returns a list of URLs in the element.
        // Returns empty list if no URLs are found.
        // -->
        ElementTag.registerTag("urls", (attribute, object) -> {
            ListTag list = new ListTag();
            for (String s : object.asString().split("\\s+")) {
                if (s.matches("(https?://)[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)")) {
                    list.addObject(new ElementTag(s));
                }
            }
            return list;
        });
    }
}
