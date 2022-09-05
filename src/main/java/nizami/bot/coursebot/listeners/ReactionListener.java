package nizami.bot.coursebot.listeners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nizami.bot.coursebot.ActivePetitions;

public class ReactionListener extends ListenerAdapter {
	// How many votes needed for a petition to create a channel
	private static final int VOTE_COUNT = 2;

	@Override
	public void onReady(ReadyEvent event) {
		// Checks to see if there is a mismatch in reactions between the file and
		// messages while the bot was offline, and updates the counter if there is

		ActivePetitions petitions = ActivePetitions.getInstance();
		List<Guild> guilds = event.getJDA().getGuilds();
		
		// Iterates through all servers the bot has joined in order to look for the bot's messages
		for (Guild guild : guilds) {
			MessageChannel channel = guild.getTextChannelsByName("course-request", true).get(0);

			// Iterates through all active petitions the bot is managing to see if those petitions
			// Exist on the current server (in iteration)
			for (long messageID : petitions.petitionMap.keySet()) {
				channel.retrieveMessageById(messageID).queue(message -> {
					// Retrieves the new vote count and compares it to the old vote count
					int voteCount = message.getReaction(Emoji.fromUnicode("U+2705")).getCount();
					String courseName = (String) petitions.petitionMap.get(messageID).keySet().toArray()[0];
					int oldVoteCount = petitions.petitionMap.get(messageID).get(courseName);
					if (voteCount != oldVoteCount) {
						// Update the vote count if there is a mismatch
						try {
							// Updates the vote count stored in the file as well as the vote count stored
							// in memory
							updateFile(messageID + ":" + courseName + "," + oldVoteCount, voteCount);
							petitions.petitionMap.get(messageID).put(courseName, voteCount);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				});
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		// checks if the message being reacted to is by the bot and is also the correct
		// emoji
		if (event.getEmoji().equals(Emoji.fromUnicode("U+2705")) && event.getJDA().getSelfUser()
				.equals(event.getChannel().retrieveMessageById(event.getMessageId()).complete().getAuthor())) {
			// gets the list of active petitions and checks if the message reacted to by the
			// user is on this list.
			ActivePetitions petitions = ActivePetitions.getInstance();
			HashMap<String, Integer> courseMap = petitions.petitionMap.get(event.getMessageIdLong());
			String courseName = (String) courseMap.keySet().toArray()[0];
			if (courseMap != null) {

				// add one vote to the vote counter for the current petition and update the file
				courseMap.replace(courseName, courseMap.get(courseName) + 1);
				// if the vote count = VOTE_COUNT, create the channel for the course
				if (courseMap.get(courseName) == VOTE_COUNT) {
					event.getChannel().retrieveMessageById(event.getMessageId()).complete().delete().queue();
					event.getGuild()
							.createTextChannel(courseName, event.getGuild().getCategoriesByName("Courses", true).get(0))
							.complete();
					// clear the bot message from memory
					petitions.petitionMap.remove(event.getMessageIdLong());
					event.getChannel().sendMessage("The course **" + courseName
							+ "** has obtained enough votes, and a channel has been created!").queue();
				}
				// Update the file storing the bot message voteCounts
				try {
					updateFile(event.getMessageIdLong() + ":" + courseName + "," + (courseMap.get(courseName) - 1),
							courseMap.get(courseName));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		// checks if the message being reacted to is by the bot and is also the correct
		// emoji
		if (event.getEmoji().equals(Emoji.fromUnicode("U+2705")) && event.getJDA().getSelfUser()
				.equals(event.getChannel().retrieveMessageById(event.getMessageId()).complete().getAuthor())) {
			// gets the list of active petitions and checks if the message reacted to by the
			// user is on this list.
			ActivePetitions petitions = ActivePetitions.getInstance();
			HashMap<String, Integer> courseMap = petitions.petitionMap.get(event.getMessageIdLong());
			String courseName = (String) courseMap.keySet().toArray()[0];
			if (courseMap != null) {

				// removes one vote to the vote counter for the current petition and update the
				// file
				courseMap.replace(courseName, courseMap.get(courseName) - 1);
				// Update the file storing the bot message voteCounts
				try {
					updateFile(event.getMessageIdLong() + ":" + courseName + "," + (courseMap.get(courseName) + 1),
							courseMap.get(courseName));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void updateFile(String lineToUpdate, int voteCount) throws IOException {
		File inputFile = new File("petitions.txt");
		File tempFile = new File("tempPetitions.txt");
		// Create reader and writer to copy current file to a new one without the old
		// line
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

		String currentLine;
		// Reads a line until there are no more lines
		while ((currentLine = reader.readLine()) != null) {
			// trim newline when comparing with lineToRemove
			String trimmedLine = currentLine.trim();
			// Gets the vote count associated with the current line
			if (trimmedLine.equals(lineToUpdate)) {
				// If the vote count on record is equal to the votes required remove the line
				// from storage
				if (voteCount == VOTE_COUNT)
					continue;
				// Else add back the line with an incremented vote count
				String updatedLine = currentLine.substring(0, currentLine.indexOf(',') + 1) + voteCount;
				writer.write(updatedLine + System.getProperty("line.separator"));
				continue;
			}
			// Else just rewrite the line
			writer.write(currentLine + System.getProperty("line.separator"));
		}
		writer.close();
		reader.close();
		inputFile.delete();
		boolean successful = tempFile.renameTo(inputFile);
	}

}
