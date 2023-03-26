package network.roanoke.dexrank

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokemon.Species
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Path

class RankManager {
    var dexMap: HashMap<String, Int> = HashMap<String, Int>()
    val configDir = FabricLoader.getInstance().configDir.toFile()
    val saveFile = File(configDir, "dexrank_data.json")
    var updateCounter = 0

    init {
        if (!saveFile.createNewFile()) {
            dexMap = loadDexData(saveFile)
        }
    }

    fun loadDexData(saveFile: File): HashMap<String, Int> {
        val gson = Gson()
        val reader = FileReader(saveFile)

            // Read the JSON file into a map
        val mapType = object : TypeToken<HashMap<String, Int>>() {}.type
        val jsonMap: HashMap<String, Int> = gson.fromJson(reader, mapType)

        reader.close()
        return jsonMap
    }

    fun updateDex(player: ServerPlayerEntity, species: Species) {
        val playerData = Cobblemon.playerData.get(player)
        val pokedexData = playerData.extraData[PokedexDataExtension.NAME_KEY] as? PokedexDataExtension

        // Extra safety check, shouldn't be possible
        if (pokedexData == null) {
            playerData.extraData[PokedexDataExtension.NAME_KEY] =
                PokedexDataExtension(hashSetOf(species.resourceIdentifier))
            if (!dexMap.containsKey(player.uuidAsString)) {
                dexMap[player.uuidAsString] = 1;
            }
        } else {
            pokedexData.caughtSpecies.add(species.resourceIdentifier)
            dexMap[player.uuidAsString] = pokedexData.caughtSpecies.size;
            updateCounter += 1
            when (updateCounter % 10) {
                0 -> {
                    updateCounter = 0
                    saveDexData()
                }
            }
        }
    }

    fun saveDexData() {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        var fileWriter = FileWriter(saveFile)
        val jsonString: String = gson.toJson(dexMap)
        fileWriter.use {
            it.write(jsonString)
        }
    }
}