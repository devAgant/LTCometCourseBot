package nizami.bot.coursebot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import nizami.bot.coursebot.commands.CommandManager;
import nizami.bot.coursebot.listeners.ReactionListener;

public class BotBuilder {
	private final ShardManager shardManager;

	public BotBuilder() throws LoginException, IllegalArgumentException, IOException {
		// Load the bot
		String token = "MTAxMTM2NTU2MDQzNDU1NjkyOA.GoJ50l.p2dky71AIPOpF-6-bT_kJ02GmEyFaESZD5x97s";
		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
		builder.setStatus(OnlineStatus.ONLINE);
		builder.enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.GUILD_MEMBERS);
		shardManager = builder.build();
		// Load the bot's data
		File petitionsFile = new File("petitions.txt");
		if (petitionsFile.exists() && !petitionsFile.isDirectory()) {
			Scanner pScanner = new Scanner(new File("petitions.txt"));
			ActivePetitions petitions = ActivePetitions.getInstance();
			while (pScanner.hasNextLine()) {
				String currentLine = pScanner.nextLine();
				HashMap<String, Integer> courseMap = new HashMap<>();
				courseMap.put(currentLine.substring(currentLine.indexOf(":") + 1, currentLine.indexOf(",")),
						Integer.parseInt(currentLine.substring(currentLine.indexOf(",") + 1)));
				petitions.petitionMap.put(Long.parseLong(currentLine.substring(0, currentLine.indexOf(":"))),
						courseMap);
			}
			
		} else {
			petitionsFile.createNewFile();
		}
		// Register event listeners
		shardManager.addEventListener(new CommandManager());
		shardManager.addEventListener(new ReactionListener());
	}

	public ShardManager getShardManager() {
		return shardManager;
	}
}
