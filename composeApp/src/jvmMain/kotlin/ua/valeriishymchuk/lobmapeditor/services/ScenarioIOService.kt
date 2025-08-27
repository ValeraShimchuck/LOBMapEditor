package ua.valeriishymchuk.lobmapeditor.services

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import java.io.File

class ScenarioIOService(override val di: DI) : DIAware {
    private val gson: Gson by instance()

    class InvalidScenarioFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

    suspend fun load(file: File) = withContext(Dispatchers.IO) {

        val jsonObject = gson.fromJson(file.reader(), JsonObject::class.java)

        return@withContext GameScenario.deserialize(jsonObject)
    }

    suspend fun <T: GameScenario<T>> save(scenario: T, file: File) = withContext(Dispatchers.IO) {
        val jsonElement = scenario.serialize()
        val jsonString = try {
            gson.toJson(jsonElement)
        } catch (e: Exception) {
            throw Exception("Error saving json file scenario", e)
        }
        file.writeText(jsonString)
    }
}