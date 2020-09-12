package Adriftizen.commands;

import java.util.List;
import java.util.Map.Entry;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.BracedCommand;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;

public class AsyncCommand extends BracedCommand {

	public AsyncCommand() {
		setName("async");
		setSyntax("async");
		setRequiredArguments(0, 0);
	}

	@Override
	public void parseArgs(ScriptEntry e) throws InvalidArgumentsException {
	}

	@Override
	public void execute(ScriptEntry e) {
		ScriptQueue queue = (new InstantQueue(e.getScript().getName()));
		List<ScriptEntry> cmds = e.getBracedSet().get(0).value;
		queue.addEntries(cmds);
        for (Entry<String,ObjectTag> entry : e.getResidingQueue().getAllDefinitions().entrySet()) {
        	queue.addDefinition(entry.getKey(), entry.getValue());
        }
        if (e.entryData instanceof BukkitScriptEntryData) {
            BukkitScriptEntryData data = (BukkitScriptEntryData) e.entryData;
            if (data.hasPlayer()) {
            	for (ScriptEntry en : queue.script_entries) {
            		((BukkitScriptEntryData)en.entryData).setPlayer(data.getPlayer());
            	}
            }
            if (data.hasNPC()) {
            	for (ScriptEntry en : queue.script_entries) {
            		((BukkitScriptEntryData)en.entryData).setNPC(data.getNPC());
            	}
            }
        }
        queue.contextSource = e.getResidingQueue().contextSource;
        queue.start();
        e.setFinished(true);
	}

}
