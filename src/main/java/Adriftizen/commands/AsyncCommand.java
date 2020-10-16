package Adriftizen.commands;

import Adriftizen.Adriftizen;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.commands.BracedCommand;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsyncCommand extends BracedCommand {

	public AsyncCommand() {
		setName("async");
		setSyntax("async");
		setRequiredArguments(0, 0);
	}
	boolean hasDefinitions = true;
	@Override
	public void parseArgs(ScriptEntry e) throws InvalidArgumentsException {
		for (Argument arg : e.getProcessedArgs()) {
			if (!e.hasObject("player")
					&& arg.matchesPrefix("player")) {
				e.addObject("player", PlayerTag.valueOf(arg.getValue(), null));
			}
			else if (!e.hasObject("npc")
					&& arg.matchesPrefix("npc")) {
				e.addObject("npc", NPCTag.valueOf(arg.getValue(), null));
			}
			else if (arg.matches("no_definitions")) {
				hasDefinitions = false;
			}
		}
	}

	@Override
	public void execute(ScriptEntry e) {
		List<String> commands = new ArrayList<>();
		for (ScriptEntry entry : e.getBracedSet().get(0).value) {
			commands.add(stringify(entry));
		}
		Map<String,String> defs = new HashMap<>();
		for (Map.Entry<String, ObjectTag> def : e.getResidingQueue().getAllDefinitions().entrySet()) {
			defs.put(def.getKey(), def.getValue().toString());
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Adriftizen.instance, new Runnable() {
			@Override
			public void run() {
				List<Object> rawEntries = new ArrayList<>();
				for (String cmd : commands) {
					if (cmd.length() > 0) {
						rawEntries.add(parseCommandsBack(unescape(cmd)));
					}
				}
				ScriptEntryData data = new BukkitScriptEntryData(e.hasObject("player") ? e.getObjectTag("player") : null, e.hasObject("npc") ? e.getObjectTag("npc") : null);
				List<ScriptEntry> entries = ScriptBuilder.buildScriptEntries(rawEntries, e.getScript().getContainer(), data);
				if (entries.isEmpty()) {
					return;
				}
				if (e.shouldDebug()) {
					for (ScriptEntry entry : entries) {
						entry.shouldDebugBool = false;
					}
				}
				ScriptQueue queue = new InstantQueue("ASYNC_").addEntries(entries);
				for (Map.Entry<String,String> entry : defs.entrySet()) {
					queue.addDefinition(entry.getKey(), entry.getValue());
					Debug.echoDebug(entries.get(0), "Adding definition '" + entry.getKey() + "' as " + entry.getValue());
				}
				queue.start();
			}
		});
	}

	public static String escape(String text) {
		return text.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
	}

	public static String unescape(String text) {
		return text.replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\");
	}

	public static Object parseCommandsBack(String value) {
		List<String> cmds = CoreUtilities.split(value, '\r');
		if (cmds.size() == 1) {
			return cmds.get(0);
		}
		List<Object> resultSubList = new ArrayList<>();
		for (int i = 1; i < cmds.size(); i++) {
			if (cmds.get(i).length() > 0) {
				resultSubList.add(parseCommandsBack(unescape(cmds.get(i))));
			}
		}
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(cmds.get(0), resultSubList);
		return resultMap;
	}

	public static String stringify(Object obj) {
		if (obj instanceof ScriptEntry) {
			ScriptEntry entry = (ScriptEntry) obj;
			if (entry.internal.insideList == null) {
				return escape(entry.internal.originalLine);
			}
			StringBuilder result = new StringBuilder();
			result.append(escape(entry.internal.originalLine)).append("\r");
			for (Object subCommand : entry.internal.insideList) {
				result.append(stringify(subCommand)).append("\r");
			}
			return result.toString();
		}
		else if (obj instanceof Map) {
			Map<Object, Object> valueMap = (Map<Object, Object>) obj;
			StringBuilder result = new StringBuilder();
			Object cmdLine = valueMap.keySet().toArray()[0];
			result.append(escape(cmdLine.toString())).append("\r");
			List<Object> inside = (List<Object>) valueMap.get(cmdLine);
			for (Object subCommand : inside) {
				result.append(stringify(subCommand)).append("\r");
			}
			return result.toString();
		}
		else {
			return escape(obj.toString());
		}
	}
}
