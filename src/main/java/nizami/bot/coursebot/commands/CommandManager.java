package nizami.bot.coursebot.commands;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import nizami.bot.coursebot.ActivePetitions;

public class CommandManager extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		String command = event.getName();
		ActivePetitions petitions = ActivePetitions.getInstance();
		if (command.equals("newcourse")) {
			// Reads user input for the "course" option and parses it to proper channel name formatting
			OptionMapping messageOption = event.getOption("course");
			String userInput = messageOption.getAsString();
			String courseName = userInput.toLowerCase().replace(" ", "-");
			// Checks if the channel the user is attempting to create a petition for already has a petiton,
			// or already exists
			try {
				Scanner pScanner = new Scanner(new File("petitions.txt"));
				while (pScanner.hasNextLine()) {
					if (pScanner.findInLine(courseName) != null) {
						event.reply("Error: The Channel **" + courseName + "** already has a petition!").setEphemeral(true).queue();
						return;
					}
					pScanner.nextLine();
				}
				if (!event.getGuild().getTextChannelsByName(courseName, true).isEmpty()) {
					event.reply("Error: The Channel **" + courseName + "** already exists!").setEphemeral(true).queue();
					return;
				}
				pScanner.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			MessageChannel channel = event.getGuild().getTextChannelsByName("course-request", true).get(0);
			String userTag = event.getUser().getAsTag();
			// Prepares Bot's embed message response
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Course Channel Petition: " + userInput);
			eb.setColor(Color.CYAN);
			eb.setDescription("**" + userTag + "** would like to add the course \"" + userInput + "\" as a channel.");
			// gets the petition instance and stores the current petition into memory
			HashMap<String, Integer> courseMap = new HashMap<>();
			courseMap.put(courseName, 0);
			// Queues the message to be sent and stores it against it's unique vote count upon the message sending
			channel.sendMessageEmbeds(eb.build()).queue(message -> {
				message.addReaction(Emoji.fromUnicode("U+2705")).queue();	
				petitions.petitionMap.put(message.getIdLong(), courseMap);
				// Saves the data to the .txt file
				try {
					BufferedWriter pOut = new BufferedWriter(new FileWriter("petitions.txt", true));
					pOut.append(message.getIdLong() + ":" + courseName + ",0");
					pOut.newLine();
					pOut.flush();
					pOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			
			
			
			event.reply("Created petition to add the **" + courseName + "** course as a channel!").setEphemeral(true)
					.queue();
		}
	}

	/*
	 * Registers the command with the given options Course: Name of the course which
	 * the user intends to create as a channel.
	 */

	@Override
	public void onGuildReady(@NotNull GuildReadyEvent event) {
		List<CommandData> commandData = new ArrayList<>();

		// Command: /newcourse
		OptionData option2 = new OptionData(OptionType.STRING, "course", "Name of the course", true);
		commandData.add(Commands.slash("newcourse", "Creates a petition to add a course as a channel to the server.")
				.addOptions(option2));
		event.getGuild().updateCommands().addCommands(commandData).queue();
	}
}
