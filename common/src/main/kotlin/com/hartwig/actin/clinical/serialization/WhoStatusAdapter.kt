import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import com.hartwig.actin.util.json.Json
import java.lang.reflect.Type
import java.time.LocalDate

class WhoStatusAdapter : JsonDeserializer<WhoStatus> {

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): WhoStatus {

        return try {
            val jsonObject = jsonElement.asJsonObject
            val date = context.deserialize<LocalDate>(jsonObject.get("date"), LocalDate::class.java)
            val status = Json.integer(jsonObject, "status")

            val precision = if (jsonObject.has("precision") && !jsonObject.get("precision").isJsonNull) {
                WhoStatusPrecision.valueOf(Json.string(jsonObject, "precision"))
            } else {
                WhoStatusPrecision.EXACT
            }

            WhoStatus(date = date, status = status, precision = precision)
        } catch (e: Exception) {
            throw JsonParseException("Failed to deserialize: $jsonElement", e)
        }
    }
}
