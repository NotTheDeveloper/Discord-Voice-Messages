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
package dev.blocky.discord.listener;

import dev.blocky.discord.DiscordBot;
import dev.blocky.library.jda.entities.voice.VoiceRecorder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

/**
 * A listener that records voice chat.
 *
 * @author BlockyDotJar
 * @version v1.0.0
 * @since v1.0.0
 */
public class VoiceRecordListener extends ListenerAdapter
{
    private static final Logger logger = JDALogger.getLog(VoiceRecorder.class);
    private int i = 0;

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event)
    {
        final Guild guild = event.getGuild();
        // The channel, where the messages shall be sent.
        final TextChannel channel = guild.getTextChannelById(876766868864647191L);
        final Member member = event.getMember();

        // The file, the voice shall be recorded to.
        final File voiceFile = new File("VoiceMessage.mp3");

        assert channel != null;

        if (event.getChannelJoined() != null)
        {
            try
            {
                // Checks if the id of the joined channel is equal to voice message channel.
                if (event.getChannelJoined().getIdLong() != 1030865498407182377L)
                {
                    return;
                }

                // The JDA audio system initializing is kinda bugged, because of this we must rejoin the channel, that the bot can receive audio.
                if (i == 0)
                {
                    i += 1;
                    channel.sendMessage("> Initializing... Please rejoin the channel.").queue();
                }

                // Checks if the bot has the permission to join the VoiceChannel.
                if (!guild.getSelfMember().hasPermission(Permission.VOICE_CONNECT))
                {
                    logger.error("Cannot connect to VoiceChannel, missing permission 'VOICE_CONNECT'.", new IllegalStateException());
                }

                if (!member.getUser().isBot() && guild.getSelfMember().hasPermission(Permission.VOICE_CONNECT))
                {
                    // Joins a VoiceChannel.
                    guild.getAudioManager().openAudioConnection(
                            // This requires the GUILD_VOICE_STATES intent and the VOICE_STATE CacheFlag.
                            member.getVoiceState().getChannel()
                    );
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (event.getChannelLeft() != null)
        {
            // Checks if the bot can receive audio from the user.
            // This requires the GUILD_VOICE_STATES intent and the VOICE_STATE CacheFlag.
            if (guild.getSelfMember().getVoiceState().isGuildDeafened())
            {
                logger.warn("Deafened, cannot record.");
            }

            // This requires the GUILD_VOICE_STATES intent and the VOICE_STATE CacheFlag.
            if (!member.getUser().isBot() && !guild.getSelfMember().getVoiceState().isGuildDeafened())
            {
                try
                {
                    // Leaves from the VoiceChannel.
                    guild.getAudioManager().closeAudioConnection();

                    // The same reason as I said above.
                    if (i == 1)
                    {
                        i += 1;
                        return;
                    }

                    DiscordBot.getRecorder()
                            // Sets the volume, how loud the audio should be. (this cannot be negative)
                            // If this is not set it will set to 1.0d.
                            .setVolume(1.5)
                            // Sets the receiving handler, which is required to receive the audio.
                            .setReceivingHandler(guild)
                            // Starts recording the audio.
                            // PLEASE NOTE: Only use this feature to record conversations with the consent of all users in the channel.
                            // Recording a conversation against other users' consent is illegal, and you are violating the Discord Terms of Service.
                            .createRecording(voiceFile);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
