package network.roanoke.dexrank.Commands;

import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import network.roanoke.dexrank.DexRank;

import static net.minecraft.server.command.CommandManager.literal;

public class DexSave {
    public DexSave() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("dexsave")
                            .requires(Permissions.require("dexrank.dexsave", 4))
                            .executes(this::executeDexSave)
            );
        });
    }

    private int executeDexSave(CommandContext<ServerCommandSource> ctx) {
        DexRank.rankManager.saveDexData();
        return 1;
    }
}
