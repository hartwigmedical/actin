package com.hartwig.actin.molecular.datamodel

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

interface MolecularTest<T> {
    val type: ExperimentType
    val date: LocalDate?
    val result: T
}

class MolecularTestFactory {
    companion object {
        fun classify(result: PriorMolecularTest): ExperimentType {
            return when (result.test) {
                "IHC" -> ExperimentType.IHC

                "" -> if (result.item == "PD-L1") {
                    ExperimentType.IHC
                } else {
                    ExperimentType.OTHER
                }

                else -> ExperimentType.OTHER
            }
        }

        fun fromPriorMolecular(tests: List<PriorMolecularTest>): List<MolecularTest<*>> {
            return tests.groupBy { classify(it) }
                .flatMap { (type, results) ->
                    when (type) {
                        ExperimentType.IHC -> results.map { IHCMolecularTest.fromPriorMolecularTest(it) }
                        else -> results.map { OtherPriorMolecularTest.fromPriorMolecularTest(it) }
                    }
                }
        }
    }
}

data class WGSMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: MolecularRecord
) : MolecularTest<MolecularRecord> {

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
) : MolecularTest<PriorMolecularTest> {

    companion object {
        fun fromPriorMolecularTest(result: PriorMolecularTest): IHCMolecularTest {
            return IHCMolecularTest(ExperimentType.IHC, date = null, result)
        }
    }
}

data class OtherPriorMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: PriorMolecularTest
) : MolecularTest<PriorMolecularTest> {

    companion object {
        fun fromPriorMolecularTest(result: PriorMolecularTest): OtherPriorMolecularTest {
            return OtherPriorMolecularTest(ExperimentType.OTHER, date = null, result)
        }
    }
}


class MolecularTestAdapter(private val gson: Gson) : TypeAdapter<MolecularTest<*>>() {

    override fun write(out: JsonWriter, value: MolecularTest<*>?) {
        if (value == null) {
            out.nullValue()
            return
        }

        val jsonObject = gson.toJsonTree(value).asJsonObject
        gson.toJson(jsonObject, out)
    }

    override fun read(input: JsonReader): MolecularTest<*>? {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        val type = jsonObject.get("type").asString

        return when (type) {
            ExperimentType.WHOLE_GENOME.toString() -> gson.fromJson(jsonObject, WGSMolecularTest::class.java)
            ExperimentType.TARGETED.toString() -> gson.fromJson(jsonObject, WGSMolecularTest::class.java)
            ExperimentType.IHC.toString() -> gson.fromJson(jsonObject, IHCMolecularTest::class.java)
            ExperimentType.OTHER.toString() -> gson.fromJson(jsonObject, OtherPriorMolecularTest::class.java)
            else -> throw IllegalArgumentException("Unknown molecular test type: $type")
        }
    }
}