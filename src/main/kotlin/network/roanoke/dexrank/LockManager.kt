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
import java.util.*
import kotlin.collections.ArrayList

class LockManager {
    var lockedUUIDS = ArrayList<String>()
    val configDir = FabricLoader.getInstance().configDir.toFile()
    val saveFile = File(configDir, "level_lock_data.json")

    init {
        if (!saveFile.createNewFile()) {
            lockedUUIDS = loadLevelLocks(saveFile)
        }
    }

    fun loadLevelLocks(saveFile: File): ArrayList<String> {
        val gson = Gson()
        val reader = FileReader(saveFile)

        // Read the JSON file into a map
        val mapType = object : TypeToken<ArrayList<String>>() {}.type
        val jsonMap: ArrayList<String> = gson.fromJson(reader, mapType)

        reader.close()
        return jsonMap
    }

    fun isPokemonLevelLocked(pokemonUUID: UUID): Boolean {
        return lockedUUIDS.contains(pokemonUUID.toString())
    }

    fun addLock(pokemonUUID: UUID): Boolean {
        when (lockedUUIDS.contains(pokemonUUID.toString())) {
            true -> return false
            false -> {
                lockedUUIDS.add(pokemonUUID.toString())
                saveLevelLocks()
                return true
            }
        }
    }

    fun removeLock(pokemonUUID: UUID): Boolean {
        when (lockedUUIDS.contains(pokemonUUID.toString())) {
            false -> return false;
            true -> {
                lockedUUIDS.remove(pokemonUUID.toString())
                saveLevelLocks()
                return true
            }
        }
    }

    fun saveLevelLocks() {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        var fileWriter = FileWriter(saveFile)
        val jsonString: String = gson.toJson(lockedUUIDS)
        fileWriter.use {
            it.write(jsonString)
        }
    }
}