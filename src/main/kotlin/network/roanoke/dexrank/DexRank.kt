package network.roanoke.dexrank

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import net.fabricmc.api.ModInitializer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class DexRank : ModInitializer {

    override fun onInitialize() {
        PlayerDataExtensionRegistry.register(PokedexDataExtension.NAME_KEY, PokedexDataExtension::class.java)

        // Events that trigger the Pokédex to update
        CobblemonEvents.POKEMON_CAPTURED.subscribe {
            this.handleGenericEvent(it.player, it.pokemon.species)
        }

        CobblemonEvents.STARTER_CHOSEN.subscribe {
            this.handleGenericEvent(it.player, it.pokemon.species)
        }

        CobblemonEvents.EVOLUTION_COMPLETE.subscribe {
            val owner = it.pokemon.getOwnerPlayer()
            if (owner != null) {
                this.handleGenericEvent(owner, it.pokemon.species)
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
                    this.handleGenericEvent(it, mon.species)
                }
                val iterParty: Iterator<Pokemon> = Cobblemon.storage.getParty(it.uuid).iterator()
                while (iterParty.hasNext()) {
                    val mon = iterParty.next()
                    this.handleGenericEvent(it, mon.species)
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

    /**
     * Handles generic events that should trigger the Pokédex to update
     */
    private fun handleGenericEvent(player: ServerPlayerEntity, species: Species) {
        val playerData = Cobblemon.playerData.get(player)
        val pokedexData = playerData.extraData[PokedexDataExtension.NAME_KEY] as? PokedexDataExtension

        // Extra safety check, shouldn't be possible
        if (pokedexData == null) {
            playerData.extraData[PokedexDataExtension.NAME_KEY] =
                PokedexDataExtension(hashSetOf(species.resourceIdentifier))
        } else {
            pokedexData.caughtSpecies.add(species.resourceIdentifier)
        }
    }
}
