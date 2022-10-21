/**
 * Copyright 2022 Dominic R. (aka. BlockyDotJar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.blocky.discord;

import dev.blocky.discord.listener.VoiceRecordListener;
import dev.blocky.discord.secrets.DONOTOPEN;
import dev.blocky.library.jda.entities.member.SelfMember;
import dev.blocky.library.jda.entities.voice.VoiceRecorder;
import dev.blocky.library.tixte.api.MyFiles;
import dev.blocky.library.tixte.api.TixteClient;
import dev.blocky.library.tixte.api.TixteClientBuilder;
import dev.blocky.library.tixte.api.enums.CachePolicy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.EnumSet;

/**
 * This is the main class of the Discord-Bot.
 *
 * @author BlockyDotJar
 * @version v1.0.0
 * @since v1.0.0
 */
public class DiscordBot extends ListenerAdapter
{
    private static final Logger logger = JDALogger.getLog(DiscordBot.class);
    private static final VoiceRecordListener recordListener = new VoiceRecordListener();
    private static final VoiceRecorder recorder = VoiceRecorder.newRecorder();
    private static TixteClient client;
    private static JDA jda;

    /**
     * Constructs a <b>new</b> {@link DiscordBot}.
     */
    public DiscordBot() throws IOException
    {
        jda = JDABuilder
                .createDefault(DONOTOPEN.getBotToken(),
                        EnumSet.of(
                                GatewayIntent.GUILD_MEMBERS,
                                GatewayIntent.GUILD_MESSAGES,
                                // This intent is needed for the VoiceRecordListener.
                                GatewayIntent.GUILD_VOICE_STATES
                        )
                )
                // This cache flag is needed for the TextInVoiceCommand and for the VoiceRecordListener.
                .enableCache(CacheFlag.VOICE_STATE)
                .disableCache(
                        EnumSet.of(
                                // Disables all not needed cache flags.
                                CacheFlag.CLIENT_STATUS,
                                CacheFlag.ACTIVITY,
                                CacheFlag.EMOJI,
                                CacheFlag.STICKER
                        )
                )
                // Policy which decides whether a member (and respective user) should be kept in cache.
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                // Since v5.0.0-alpha.13 we now can use a ChunkingFilter *AND* MemberCachePolicy at the same time.
                .setChunkingFilter(ChunkingFilter.ALL)
                // Adds all provided listeners to the list of listeners that will be used to populate the JDA object.
                .addEventListeners(recordListener, this)
                // Sets the activity to 'Playing with the voice recorder'.
                .setActivity(Activity.playing("with the voice recorder"))
                // Sets the status of the bot to 'Do not disturb'.
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build();

        // Creates a *new* TixteClientBuilder instance.
        TixteClientBuilder builder = new TixteClientBuilder()
                // Sets the API-key, which is required for most of the methods.
                // This method also sets the cache policy. I really recommend to set this to ALL.
                // If this is equal to null or not set, this will be automatically set to NONE.
                // If you use this method like here, you don't have to set it via setCachePolicy(@Nullable CachePolicy).
                .create(DONOTOPEN.getAPIKey(), CachePolicy.ALL)
                // Sets the session-token. (Optional but recommended)
                .setSessionToken(DONOTOPEN.getSessionToken())
                // Sets a default domain. (Optional)
                .setDefaultDomain(DONOTOPEN.getDefaultDomain());

        // Builds a *new* TixteClient instance and uses the provided API-key and session-token to start the login process.
        client = builder.build();

        // Initializes the shutdown method.
        shutdown();
    }

    /**
     * This is the main method of the Discord-Bot.
     *
     * @param args An array of string arguments.
     *
     */
    public static void main(@NotNull String[] args) throws IOException
    {
        new DiscordBot();
    }

    /**
     * The {@link DiscordBot#recorder} instance from the {@link DiscordBot} class.
     *
     * @return {@link DiscordBot#recorder}.
     */
    @NotNull
    public static VoiceRecorder getRecorder()
    {
        return recorder;
    }

    /**
     * Shutdowns the system and the {@link DiscordBot#jda} instance after five seconds, when the exit
     * command was typed.
     */
    private static void shutdown()
    {
        new Thread(() ->
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try
            {
                String line = reader.readLine();
                while (line != null)
                {
                    if (line.equalsIgnoreCase("exit") && jda != null)
                    {
                        for (int i = 5; i > 0; i--)
                        {
                            if (i != 1)
                            {
                                logger.info("Bot stops in " + i + " seconds.");
                            }
                            else
                            {
                                logger.info("Bot stops in " + i + " second.");
                            }
                            Thread.sleep(1000);
                        }
                        // Sets the presence of the bot to OFFLINE.
                        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
                        // Shuts this JDA instance down and closes all its connections.
                        jda.shutdown();

                        // This is needed, because the cache could my something corrupt
                        if (client.getHttpClient().cache() != null)
                        {
                            client.pruneCache();
                        }

                        client.cancelRequests();

                        // Terminates the currently running JVM.
                        System.exit(0);
                    }
                }
            }
            catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        ).start(); // Creates a thread to execute a task and schedules it to execute.
    }

    @Override
    public void onReady(@NotNull ReadyEvent event)
    {
        // If the bot is ready to use, this message will be logged in the console.
        logger.info("{} successfully connected to the Discord network and finally logged in.",
                event.getJDA().getSelfUser().getAsTag());
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event)
    {
        // If the bot is getting a shutdown, this message will be logged in the console.
        logger.info("{} successfully disconnected from the Discord network and finally logged out.",
                event.getJDA().getSelfUser().getAsTag());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getMember().getUser().isBot())
        {
            return;
        }

        final Guild guild = event.getGuild();
        // The channel, where the messages shall be sent.
        final TextChannel channel = guild.getTextChannelById(876766868864647191L);
        final SelfMember selfMember = new SelfMember(event.getGuild());
        final MyFiles myFiles = new MyFiles();

        // The file, the voice shall be recorded to.
        final File voiceFile = new File("VoiceMessage.mp3");

        assert channel != null;

        if (selfMember.isMentioned(event.getMessage()))
        {
            try
            {
                // You cannot upload more than 100MB at once and there is a complete limit of 15GB.
                if (Files.size(voiceFile.toPath()) >= myFiles.getRemainingSize() || Files.size(voiceFile.toPath()) > 100000000)
                {
                    channel.sendMessage("> Too big file size!").queue();
                    return;
                }

                // Uploads the file to Tixte.
                myFiles.uploadFile(voiceFile);

                // If the file upload fails, there will be sent the default domain.
                final String url = myFiles.getURL().orElse("https://" + client.getDefaultDomain().get() + "/");

                channel.sendMessage(url).queue();

                // Deletes the file to not waste memory.
                voiceFile.delete();
            }
            catch (@NotNull Exception e)
            {
                // There is a small chance, that the okhttp reconnect fails.
                if (e instanceof SocketTimeoutException)
                {
                    channel.sendMessage("> " + event.getMember().getAsMention() + " you are uploading to much voice messages!").queue();
                }
                e.printStackTrace();
            }
        }
    }
}
