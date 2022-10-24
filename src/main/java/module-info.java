/**
 * A Discord bot for sending voice messages.
 */
module discordvoicemessages {
    requires java.datatransfer;
    requires java.desktop;

    requires jdk.incubator.concurrent;

    requires net.dv8tion.jda;
    requires jdacommons;
    requires tixte4j;
    requires org.slf4j;
    requires slf4jfbl;

    requires org.jetbrains.annotations;

    exports dev.blocky.discord;
    exports dev.blocky.discord.listener;
    exports dev.blocky.discord.secrets;
}
