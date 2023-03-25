package network.roanoke.dexrank

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
import com.cobblemon.mod.common.pokemon.Pokemon
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.util.Identifier
import network.roanoke.dexrank.Commands.DexSave
import network.roanoke.dexrank.Commands.DexTop

class DexRank : ModInitializer {

    companion object {
        @JvmField
        val rankManager = RankManager()
    }

    override fun onInitialize() {
        PlayerDataExtensionRegistry.register(PokedexDataExtension.NAME_KEY, PokedexDataExtension::class.java)
        DexTop()
        DexSave()
        // Events that trigger the Pokédex to update
        CobblemonEvents.POKEMON_CAPTURED.subscribe {
            rankManager.updateDex(it.player, it.pokemon.species)
        }

        CobblemonEvents.STARTER_CHOSEN.subscribe {
            rankManager.updateDex(it.player, it.pokemon.species)
        }

        CobblemonEvents.EVOLUTION_COMPLETE.subscribe {
            val owner = it.pokemon.getOwnerPlayer()
            if (owner != null) {
                rankManager.updateDex(owner, it.pokemon.species)
            }
        }

        // Create Pokédex data & fill in PC if it doesn't exist
        CobblemonEvents.PLAYER_JOIN.subscribe {
            val playerData = Cobblemon.playerData.get(it)
            if (playerData.extraData[PokedexDataExtension.NAME_KEY] == null) {
                playerData.extraData[PokedexDataExtension.NAME_KEY] = PokedexDataExtension(hashSetOf())
                val iter: Iterator<Pokemon> = Cobblemon.storage.getPC(it.uuid).iterator()
                while (iter.hasNext()) {
                    val mon = iter.next()
                    rankManager.updateDex(it, mon.species)
                }
                val iterParty: Iterator<Pokemon> = Cobblemon.storage.getParty(it.uuid).iterator()
                while (iterParty.hasNext()) {
                    val mon = iterParty.next()
                    rankManager.updateDex(it, mon.species)
                }
            }
        }

        // Placeholders
        Placeholders.register(Identifier("pokedex", "caught")) { ctx, _ ->
            if (!ctx.hasPlayer()) {
                return@register PlaceholderResult.invalid("No player!")
            }

            val playerData = Cobblemon.playerData.get(ctx.player!!)
            val pokedexData = playerData.extraData[PokedexDataExtension.NAME_KEY] as? PokedexDataExtension
                ?: return@register PlaceholderResult.value("Missing Pokedex data!")

            PlaceholderResult.value(pokedexData.caughtSpecies.size.toString())
        }

        Placeholders.register(Identifier("pokedex", "total")) { _, _ ->
            return@register PlaceholderResult.value(PokemonSpecies.species.size.toString())
        }

        Placeholders.register(Identifier("pokedex", "implemented_total")) { _, _ ->
            return@register PlaceholderResult.value(PokemonSpecies.implemented.size.toString())
        }
    }

}
