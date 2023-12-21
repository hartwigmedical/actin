package com.hartwig.actin.clinical.serialization

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.ImmutableMedication
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.ImmutableToxicityEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.ObservedToxicity
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicityEvaluation
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentClass
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentStage
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage
import com.hartwig.actin.util.json.Json.integer
import com.hartwig.actin.util.json.Json.string
import java.lang.reflect.Type
import java.time.LocalDate
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

object ClinicalGsonDeserializer {
    fun create(): Gson {
        return gsonBuilder().registerTypeAdapter(
            object : TypeToken<ImmutableSet<Drug?>?>() {}.type, ImmutableSetAdapter<Drug>(
                ImmutableDrug::class.java
            )
        ).create()
    }

    fun createWithDrugMap(drugsByName: Map<String, Drug>): Gson {
        return gsonBuilder().registerTypeAdapter(object : TypeToken<ImmutableSet<Drug?>?>() {}.type, DrugNameSetAdapter(drugsByName))
            .create()
    }

    private fun gsonBuilder(): GsonBuilder {
        return GsonBuilder().serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(PatientDetails::class.java, AbstractClassAdapter<PatientDetails>(ImmutablePatientDetails::class.java))
            .registerTypeAdapter(TumorDetails::class.java, AbstractClassAdapter<TumorDetails>(ImmutableTumorDetails::class.java))
            .registerTypeAdapter(ClinicalStatus::class.java, AbstractClassAdapter<ClinicalStatus>(ImmutableClinicalStatus::class.java))
            .registerTypeAdapter(InfectionStatus::class.java, AbstractClassAdapter<InfectionStatus>(ImmutableInfectionStatus::class.java))
            .registerTypeAdapter(ECG::class.java, AbstractClassAdapter<ECG>(ImmutableECG::class.java))
            .registerTypeAdapter(
                AtcClassification::class.java,
                AbstractClassAdapter<AtcClassification>(ImmutableAtcClassification::class.java)
            )
            .registerTypeAdapter(AtcLevel::class.java, AbstractClassAdapter<AtcLevel>(ImmutableAtcLevel::class.java))
            .registerTypeAdapter(Dosage::class.java, AbstractClassAdapter<Dosage>(ImmutableDosage::class.java))
            .registerTypeAdapter(Drug::class.java, AbstractClassAdapter<Drug>(ImmutableDrug::class.java))
            .registerTypeAdapter(DrugTreatment::class.java, AbstractClassAdapter<DrugTreatment>(ImmutableDrugTreatment::class.java))
            .registerTypeAdapter(OtherTreatment::class.java, AbstractClassAdapter<OtherTreatment>(ImmutableOtherTreatment::class.java))
            .registerTypeAdapter(Radiotherapy::class.java, AbstractClassAdapter<Radiotherapy>(ImmutableRadiotherapy::class.java))
            .registerTypeAdapter(TreatmentStage::class.java, AbstractClassAdapter<TreatmentStage>(ImmutableTreatmentStage::class.java))
            .registerTypeAdapter(Treatment::class.java, TreatmentAdapter())
            .registerTypeAdapter(
                TreatmentHistoryDetails::class.java,
                AbstractClassAdapter<TreatmentHistoryDetails>(ImmutableTreatmentHistoryDetails::class.java)
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<String?>?>() {}.type, ImmutableListAdapter<String>(
                    String::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<TreatmentHistoryEntry?>?>() {}.type, ImmutableListAdapter<TreatmentHistoryEntry>(
                    ImmutableTreatmentHistoryEntry::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<TreatmentStage?>?>() {}.type, ImmutableListAdapter<TreatmentStage>(
                    ImmutableTreatmentStage::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<PriorSecondPrimary?>?>() {}.type, ImmutableListAdapter<PriorSecondPrimary>(
                    ImmutablePriorSecondPrimary::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<PriorOtherCondition?>?>() {}.type, ImmutableListAdapter<PriorOtherCondition>(
                    ImmutablePriorOtherCondition::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<PriorMolecularTest?>?>() {}.type, ImmutableListAdapter<PriorMolecularTest>(
                    ImmutablePriorMolecularTest::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<Complication?>?>() {}.type, ImmutableListAdapter<Complication>(
                    ImmutableComplication::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<LabValue?>?>() {}.type, ImmutableListAdapter<LabValue>(
                    ImmutableLabValue::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<Toxicity?>?>() {}.type, ImmutableListAdapter<Toxicity>(
                    ImmutableToxicity::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<ToxicityEvaluation?>?>() {}.type, ImmutableListAdapter<ToxicityEvaluation>(
                    ImmutableToxicityEvaluation::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<Intolerance?>?>() {}.type, ImmutableListAdapter<Intolerance>(
                    ImmutableIntolerance::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<Surgery?>?>() {}.type, ImmutableListAdapter<Surgery>(
                    ImmutableSurgery::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<BodyWeight?>?>() {}.type, ImmutableListAdapter<BodyWeight>(
                    ImmutableBodyWeight::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<VitalFunction?>?>() {}.type, ImmutableListAdapter<VitalFunction>(
                    ImmutableVitalFunction::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<BloodTransfusion?>?>() {}.type, ImmutableListAdapter<BloodTransfusion>(
                    ImmutableBloodTransfusion::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<Medication?>?>() {}.type, ImmutableListAdapter<Medication>(
                    ImmutableMedication::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableSet<String?>?>() {}.type, ImmutableSetAdapter<String>(
                    String::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableSet<TreatmentCategory?>?>() {}.type, ImmutableSetAdapter<TreatmentCategory>(
                    TreatmentCategory::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableSet<Treatment?>?>() {}.type, ImmutableSetAdapter<Treatment>(
                    Treatment::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableSet<TreatmentType?>?>() {}.type, ImmutableSetAdapter<TreatmentType>(
                    OtherTreatmentType::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableSet<BodyLocationCategory?>?>() {}.type, ImmutableSetAdapter<BodyLocationCategory>(
                    BodyLocationCategory::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableSet<DrugType?>?>() {}.type, ImmutableSetAdapter<DrugType>(
                    DrugType::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableSet<Intent?>?>() {}.type, ImmutableSetAdapter<Intent>(
                    Intent::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableSet<ObservedToxicity?>?>() {}.type, ImmutableSetAdapter<ObservedToxicity>(
                    ImmutableObservedToxicity::class.java
                )
            )
            .registerTypeAdapter(
                object : TypeToken<ImmutableList<CypInteraction?>?>() {}.type, ImmutableListAdapter<CypInteraction>(
                    ImmutableCypInteraction::class.java
                )
            )
    }

    private fun <T> deserializeJsonCollection(
        jsonElement: JsonElement, context: JsonDeserializationContext,
        type: Type
    ): Stream<T> {
        return jsonElement.asJsonArray.asList().stream().map { listElement: JsonElement ->
            val deserialized = context.deserialize<T>(listElement, type) ?: throw RuntimeException("Unable to deserialize $listElement")
            deserialized
        }
    }

    private class LocalDateAdapter : JsonDeserializer<LocalDate?> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            jsonElement: JsonElement, type: Type,
            jsonDeserializationContext: JsonDeserializationContext
        ): LocalDate? {
            return if (jsonElement.isJsonNull) {
                null
            } else {
                val dateObject = jsonElement.asJsonObject
                LocalDate.of(integer(dateObject, "year"), integer(dateObject, "month"), integer(dateObject, "day"))
            }
        }
    }

    private class AbstractClassAdapter<T>(private val concreteType: Type) : JsonDeserializer<T> {
        @Throws(JsonParseException::class)
        override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): T {
            return context.deserialize(jsonElement, concreteType)
        }
    }

    private class ImmutableListAdapter<T>(private val concreteType: Type) : JsonDeserializer<ImmutableList<T>?> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            jsonElement: JsonElement, type: Type,
            context: JsonDeserializationContext
        ): ImmutableList<T>? {
            return if (jsonElement.isJsonNull) null else ImmutableList.copyOf(
                deserializeJsonCollection<Any>(jsonElement, context, concreteType).collect(
                    Collectors.toList()
                )
            ) as ImmutableList<T>
        }
    }

    private class ImmutableSetAdapter<T>(private val concreteType: Type) : JsonDeserializer<ImmutableSet<T>?> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            jsonElement: JsonElement, type: Type,
            context: JsonDeserializationContext
        ): ImmutableSet<T>? {
            return if (jsonElement.isJsonNull) null else ImmutableSet.copyOf(
                deserializeJsonCollection<Any>(
                    jsonElement,
                    context,
                    concreteType
                ).collect(Collectors.toSet())
            ) as ImmutableSet<T>
        }
    }

    private class DrugNameSetAdapter(private val drugsByName: Map<String, Drug>) : JsonDeserializer<ImmutableSet<Drug>?> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): ImmutableSet<Drug>? {
            return if (json.isJsonNull) null else ImmutableSet.copyOf(
                json.asJsonArray.asList().stream().map { listElement: JsonElement -> getDrug(listElement) }
                    .collect(Collectors.toSet()))
        }

        private fun getDrug(listElement: JsonElement): Drug {
            return drugsByName[listElement.asString.lowercase(Locale.getDefault())]
                ?: throw JsonParseException("Failed to resolve: $listElement")
        }
    }

    private class TreatmentAdapter : JsonDeserializer<Treatment?> {
        @Throws(JsonParseException::class)
        override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): Treatment? {
            return try {
                if (jsonElement.isJsonNull) null else context.deserialize<Any>(
                    jsonElement,
                    TreatmentClass.valueOf(string(jsonElement.asJsonObject, "treatmentClass")).treatmentClass()
                ) as Treatment
            } catch (e: Exception) {
                throw JsonParseException("Failed to deserialize: $jsonElement", e)
            }
        }
    }
}
