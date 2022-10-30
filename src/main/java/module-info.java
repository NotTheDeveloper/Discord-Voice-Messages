/**
 * A Discord bot for sending voice messages.
 */
module discordvoicemessages {
    uses org.slf4j.spi.SLF4JServiceProvider;

    requires java.desktop;

    requires jdk.incubator.concurrent;

    requires net.dv8tion.jda;
    requires jdacommons;
    requires tixte4j;
    requires org.slf4j;

    requires org.jetbrains.annotations;

    exports dev.blocky.discord;
    exports dev.blocky.discord.listener;
    exports dev.blocky.discord.secrets;
}
