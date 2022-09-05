package nizami.bot.coursebot;

import java.util.HashMap;

public class ActivePetitions {
	private static ActivePetitions petitions = null;
	public HashMap<Long, HashMap<String, Integer>> petitionMap;
	
	private ActivePetitions() {
		petitionMap = new HashMap<>();
	}
	
	public static ActivePetitions getInstance() {
		if (petitions == null) {
			petitions = new ActivePetitions();
		}
		return petitions;
	}
	
}
