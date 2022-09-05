

import java.io.IOException;

import javax.security.auth.login.LoginException;

public class CourseBot {

	public static void main(String[] args) {
		try {
			BotBuilder bot = new BotBuilder();
		} catch (LoginException | IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR: Provided bot token is invalid");
		}
		
	}

}
