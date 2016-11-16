package discordbot.command.economy;

import discordbot.core.AbstractCommand;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

public class CookieCommand extends AbstractCommand {
	public CookieCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Ask for a cookie";
	}

	@Override
	public String getCommand() {
		return "cookie";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"cookie             //gives you a cookie"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{
				"candy",
		};
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		return "Here have a \uD83C\uDF6A!";
	}
}
