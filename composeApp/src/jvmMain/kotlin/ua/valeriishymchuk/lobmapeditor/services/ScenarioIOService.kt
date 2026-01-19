package ua.valeriishymchuk.lobmapeditor.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import java.io.File
import java.io.FileReader

class ScenarioIOService(override val di: DI) : DIAware {
    private val gson: Gson by instance()

    class InvalidScenarioFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

    suspend fun load(file: File) = withContext(Dispatchers.IO) {

        val jsonObject = gson.fromJson(file.reader(), JsonObject::class.java)

        return@withContext GameScenario.deserialize(jsonObject)
    }

    fun isHybrid(file: File): Boolean {
        return getType(file) == "hybrid"
    }

    fun getType(file: File): String {
        JsonReader(FileReader(file)).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) {
                val name = reader.nextName()
                if (name == "type") {
                    return reader.nextString()
                } else {
                    reader.skipValue()
                }
            }
            reader.endObject()
            throw MalformedJsonException("Can't find 'type' key in json ${file.absolutePath}")
        }

    }

    suspend fun save(scenario: GameScenario<*>, file: File) = withContext(Dispatchers.IO) {
        val jsonElement = scenario.serialize()
        val jsonString = try {
            gson.toJson(jsonElement)
        } catch (e: Exception) {
            throw Exception("Error saving json file scenario", e)
        }
        file.writeText(jsonString)
    }
}