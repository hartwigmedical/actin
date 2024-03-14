package com.hartwig.actin.molecular.datamodel

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

private const val PD_L1 = "PD-L1"
private const val IHC = "IHC"

data class MolecularHistory(
    val molecularTests: List<MolecularTest>,
    val patientId: String = "TODO" // TODO (kz) remove default value and update call sites
) {
    // TODO (kz) do we want helpers like this or just use the list directly?
    fun allPDL1Tests(measureToFind: String): List<PriorMolecularTest> {
        return allIHCTests(allPriorMolecularTests()).filter { it.item == PD_L1 && measureToFind == it.measure }
    }

    fun allIHCTestsForProtein(protein: String): List<PriorMolecularTest> {
        return allIHCTests(allPriorMolecularTests()).filter { it.item == protein }
    }

    private fun allIHCTests(priorMolecularTests: List<PriorMolecularTest>): List<PriorMolecularTest> {
        return priorMolecularTests.filter { it.test == IHC }
    }

    fun allPriorMolecularTests(): List<PriorMolecularTest> {
        return molecularTests.filter { it.type == ExperimentType.IHC }
            .map { it.result as PriorMolecularTest }
    }

    fun mostRecentWGS(): MolecularRecord? {
        return molecularTests.filter { it.type == ExperimentType.WHOLE_GENOME || it.type == ExperimentType.TARGETED }
            .maxByOrNull { it.date ?: LocalDate.MIN }
            ?.result as MolecularRecord?
    }

    companion object {
        fun empty(): MolecularHistory {
            return MolecularHistory(emptyList())
        }

        // TODO (kz) convenience constructor, but change to make it Lists of specific
        //  molecular test subtypes? see at call sites for what looks better
        fun fromWGSandIHC(molecularRecord: MolecularRecord?, priorMolecularTests: List<PriorMolecularTest>): MolecularHistory {
            return MolecularHistory(
                (if (molecularRecord != null) {
                    listOf(WGSMolecularTest.fromMolecularRecord(molecularRecord))
                } else {
                    emptyList()
                }) + IHCMolecularTest.fromPriorMolecularTests(priorMolecularTests)
            )
        }
    }
}

class MolecularHistoryAdapter(private val gson: Gson) : TypeAdapter<MolecularHistory>() {

    override fun write(out: JsonWriter, value: MolecularHistory) {
        val jsonObject = gson.toJsonTree(value).asJsonObject
        val molecularTestsArray = JsonArray()
        value.molecularTests.forEach { molecularTest ->
            molecularTestsArray.add(MolecularTestAdapter().toJsonTree(molecularTest))
        }
        jsonObject.add("molecularTests", molecularTestsArray)

        gson.toJson(jsonObject, out)
    }

    override fun read(input: JsonReader): MolecularHistory {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        val molecularTestsJsonArray = jsonObject.getAsJsonArray("molecularTests")
        val molecularTests = molecularTestsJsonArray.map { element ->
            MolecularTestAdapter().fromJsonTree(element)
        }

        jsonObject.remove("molecularTests")
        val tempMolecularHistory = gson.fromJson(jsonObject, MolecularHistory::class.java)
        return MolecularHistory(molecularTests, tempMolecularHistory.patientId)
    }
}