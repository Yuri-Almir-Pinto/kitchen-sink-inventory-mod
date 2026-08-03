package com.yipeekiyaay.unslotted.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.yipeekiyaay.unslotted.command.commands.ClearCommand;
import com.yipeekiyaay.unslotted.command.commands.FillCommand;
import com.yipeekiyaay.unslotted.command.commands.SizeCommand;
import com.yipeekiyaay.unslotted.command.commands.SyncCommand;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class UnslottedCommands {
    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, selection) -> dispatcher.register(
                CommandManager.literal("unslotted")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("fill")
                                .then(CommandManager.argument("type amount", IntegerArgumentType.integer(1))
                                        .executes(context -> FillCommand.execute(
                                                context,
                                                context.getArgument("type amount", Integer.class),
                                                1,
                                                0,
                                                ""
                                        ))
                                        .then(CommandManager.argument("max amount", IntegerArgumentType.integer(1))
                                                .executes((context) -> FillCommand.execute(
                                                        context,
                                                        context.getArgument("type amount", Integer.class),
                                                        context.getArgument("max amount", Integer.class),
                                                        0,
                                                        ""
                                                ))
                                                .then(CommandManager.argument("byte increase per item", IntegerArgumentType.integer(0))
                                                        .executes(context -> FillCommand.execute(
                                                                context,
                                                                context.getArgument("type amount", Integer.class),
                                                                context.getArgument("max amount", Integer.class),
                                                                context.getArgument("byte increase per item", Integer.class),
                                                                ""
                                                        ))
                                                        .then(CommandManager.argument("player", StringArgumentType.word())
                                                                .suggests(UnslottedCommands::suggestPlayerNames)
                                                                .executes(context -> FillCommand.execute(
                                                                        context,
                                                                        context.getArgument("type amount", Integer.class),
                                                                        context.getArgument("max amount", Integer.class),
                                                                        context.getArgument("byte increase per item", Integer.class),
                                                                        context.getArgument("player", String.class)
                                                                ))
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(CommandManager.literal("size")
                                .executes(context -> SizeCommand.execute(
                                        context,
                                        ""
                                ))
                                .then(CommandManager.argument("player", StringArgumentType.word())
                                        .suggests(UnslottedCommands::suggestPlayerNames)
                                        .executes(context -> SizeCommand.execute(
                                                context,
                                                context.getArgument("player", String.class)
                                        ))
                                )
                        )
                        .then(CommandManager.literal("clear")
                                .executes(context -> ClearCommand.execute(
                                        context,
                                        ""
                                ))
                                .then(CommandManager.argument("player", StringArgumentType.word())
                                        .suggests(UnslottedCommands::suggestPlayerNames)
                                        .executes(context -> ClearCommand.execute(
                                                context,
                                                context.getArgument("player", String.class)
                                        ))
                                )
                        )
                        .then(CommandManager.literal("sync")
                                .executes(context -> SyncCommand.execute(
                                        context,
                                        getSourceName(context)
                                ))
                                .then(CommandManager.argument("player", StringArgumentType.word())
                                        .suggests(UnslottedCommands::suggestPlayerNames)
                                        .executes(context -> SyncCommand.execute(
                                                context,
                                                context.getArgument("player", String.class)
                                        ))
                                )
                        )
        ));
    }

    public static @Nullable String getSourceName(CommandContext<ServerCommandSource> context) {
        var player = context.getSource().getPlayer();

        if (player == null) return null;

        return player.getName().getString();
    }

    public static CompletableFuture<Suggestions> suggestPlayerNames(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(context.getSource().getServer().getPlayerNames(), builder);
    }
}
