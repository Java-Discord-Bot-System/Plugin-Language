package com.almightyalpaca.discord.bot.plugin.language;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.almightyalpaca.discord.bot.system.command.AbstractCommand;
import com.almightyalpaca.discord.bot.system.command.annotation.Command;
import com.almightyalpaca.discord.bot.system.command.arguments.special.Rest;
import com.almightyalpaca.discord.bot.system.events.CommandEvent;
import com.almightyalpaca.discord.bot.system.exception.PluginLoadingException;
import com.almightyalpaca.discord.bot.system.exception.PluginUnloadingException;
import com.almightyalpaca.discord.bot.system.plugins.Plugin;
import com.almightyalpaca.discord.bot.system.plugins.PluginInfo;
import com.detectlanguage.DetectLanguage;
import com.detectlanguage.errors.APIError;

import net.dv8tion.jda.MessageBuilder;

public class LanguagePlugin extends Plugin {

	class LanguageCommand extends AbstractCommand {

		public LanguageCommand() {
			super("language", "Tells you the language", "language [text]");
		}

		@Command(dm = true, guild = true, async = true)
		public void onCommand(final CommandEvent event, final Rest text) {
			final MessageBuilder builder = new MessageBuilder();

			String lang = null;

			try {
				lang = DetectLanguage.simpleDetect(text.getString());
			} catch (final APIError e) {
				e.printStackTrace();
			}

			final String language = LanguagePlugin.this.getLanguageName(lang);

			if (lang == null) {
				builder.appendString("An unexpected error occured!");
			} else {
				builder.appendString("The language is: " + language);
			}

			event.sendMessage(builder.build());
		}

	}

	private static final PluginInfo	INFO	= new PluginInfo("com.almightyalpaca.discord.bot.plugin.language", "1.0.0", "Almighty Alpaca", "Language Plugin", "Detects the language of a text.");

	private Map<String, String>		languages;

	public LanguagePlugin() {
		super(LanguagePlugin.INFO);
	}

	private String getLanguageName(final String lang) {
		return this.languages.get(lang);
	}

	@Override
	public void load() throws PluginLoadingException {

		this.setupLanguageTable();

		DetectLanguage.apiKey = this.getBridge().getSecureConfig("detectlanguage").getString("API_KEY");

		this.registerCommand(new LanguageCommand());
	}

	private void setupLanguageTable() {
		this.languages = new HashMap<>();
		try {
			final CSVParser p = CSVParser.parse(new URL("https://detectlanguage.com/languages.csv"), Charset.forName("UTF-8"), CSVFormat.newFormat(','));
			for (final CSVRecord r : p) {
				this.languages.put(r.get(0), r.get(1));
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unload() throws PluginUnloadingException {}
}
