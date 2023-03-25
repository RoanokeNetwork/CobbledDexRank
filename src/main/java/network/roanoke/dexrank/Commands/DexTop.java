package network.roanoke.dexrank.Commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import network.roanoke.dexrank.DexRank;


import java.util.*;

import static net.minecraft.server.command.CommandManager.literal;

public class DexTop {
    public DexTop() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("dextop")
                            .requires(Permissions.require("dexrank.dextop", 2))
                            .executes(this::executeDexTop)
            );
        });
    }

    private int executeDexTop(CommandContext<ServerCommandSource> ctx) {
        if (ctx.getSource().getPlayer() != null) {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            MinecraftServer server = ctx.getSource().getServer();
            HashMap<String, Integer> dexMap = DexRank.rankManager.getDexMap();

            List<Map.Entry<String, Integer>> list = new ArrayList<>(dexMap.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            player.sendMessage(Text.literal("= Roanoke PokeDex Leaderboard =").formatted(Formatting.GOLD));
            for (int i = 0; i < Math.min(8, list.size()); i++) {
                Map.Entry<String, Integer> entry = list.get(i);
                Optional<GameProfile> gf = server.getUserCache().getByUuid(UUID.fromString(entry.getKey()));
                String rank = "#" + (i+1) + " ";
                if (gf.isPresent()) {
                    player.sendMessage(Text.literal(rank + gf.get().getName() + ": " + entry.getValue()));
                } else {
                    player.sendMessage(Text.literal(rank + entry.getKey() + ": " + entry.getValue()));
                }
            }

        }
        return 1;
    }
}
