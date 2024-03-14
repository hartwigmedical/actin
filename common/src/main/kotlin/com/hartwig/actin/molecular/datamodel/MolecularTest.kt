package com.hartwig.actin.molecular.datamodel

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

interface MolecularTest {
    val type: ExperimentType
    val date: LocalDate?
    val result: Any
}

data class WGSMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: MolecularRecord
) : MolecularTest {

    companion object {
        fun fromMolecularRecord(result: MolecularRecord): WGSMolecularTest {
            return WGSMolecularTest(result.type, result.date, result)
        }
    }
}

data class IHCMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: PriorMolecularTest
) : MolecularTest {

    companion object {
        fun fromPriorMolecularTest(result: PriorMolecularTest): IHCMolecularTest {
            return IHCMolecularTest(ExperimentType.IHC, date = null, result)
        }

        fun fromPriorMolecularTests(priorMolecularTests: List<PriorMolecularTest>): List<IHCMolecularTest> {
            return priorMolecularTests.map { IHCMolecularTest.fromPriorMolecularTest(it) }
        }
    }
}

class MolecularTestAdapter : TypeAdapter<MolecularTest>() {
    override fun write(out: JsonWriter, value: MolecularTest?) {
        val jsonObject = JsonObject()
        when (value) {
            is WGSMolecularTest -> {
                jsonObject.addProperty("data_type", "WGSMolecularTest")
                jsonObject.add("data", Gson().toJsonTree(value))
            }

            is IHCMolecularTest -> {
                jsonObject.addProperty("data_type", "IHCMolecularTest")
                jsonObject.add("data", Gson().toJsonTree(value))
            }

            else -> throw IllegalArgumentException("Unknown molecular test type: $value")
        }
        Gson().toJson(jsonObject, out)
    }

    override fun read(input: JsonReader): MolecularTest? {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        val type = jsonObject.get("data_type").asString
        val data = jsonObject.get("data")

        return when (type) {
            "WGSMolecularTest" -> Gson().fromJson(data, WGSMolecularTest::class.java)
            "IHCMolecularTest" -> Gson().fromJson(data, IHCMolecularTest::class.java)
            else -> throw IllegalArgumentException("Unknown molecular test type: $type")
        }
    }
}