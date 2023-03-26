package network.roanoke.dexrank.Commands;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import network.roanoke.dexrank.DexRank;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LevelLock {
    public LevelLock() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("levellock")
                            .requires(Permissions.require("dexrank.levellock", 2))
                            .then(argument("slot", IntegerArgumentType.integer(1, 6)).executes(this::execute))
            );
        });
    }

    public int execute(CommandContext<ServerCommandSource> ctx) {
        if (ctx.getSource().getPlayer() != null) {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            Integer slot = ctx.getArgument("slot", Integer.class);
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            Pokemon pokemon = party.get(slot - 1);
            if (pokemon != null) {
                // MutableText pokeName = pokemon.getSpecies().getTranslatedName().formatted(Formatting.BOLD);
                if (DexRank.lockManager.isPokemonLevelLocked(pokemon.getUuid())) {
                    ctx.getSource().sendMessage(Text.literal("Removing the Level Lock from your Pokemon in slot " + slot));
                    DexRank.lockManager.removeLock(pokemon.getUuid());
                } else {
                    ctx.getSource().sendMessage(Text.literal("Adding a Level Lock to your Pokemon in slot " + slot));
                    DexRank.lockManager.addLock(pokemon.getUuid());
                }
            } else {
                ctx.getSource().sendError(Text.literal("No Pokemon in slot."));
            }
        } else {
            ctx.getSource().sendError(Text.of("Sorry, this is only for players."));
        }
        return 1;
    }
}
