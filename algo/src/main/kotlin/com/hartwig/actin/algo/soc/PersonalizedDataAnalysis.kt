package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.molecular.GeneHasActivatingMutation
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.Measurement
import com.hartwig.actin.personalization.datamodel.MeasurementType
import com.hartwig.actin.personalization.datamodel.SubPopulationAnalysis
import com.hartwig.actin.personalization.datamodel.TreatmentMeasurementCollection
import com.hartwig.actin.personalization.similarity.PersonalizedDataInterpreter
import com.hartwig.actin.personalization.similarity.population.ALL_PATIENTS_SUB_POPULATION_NAME
import com.hartwig.actin.personalization.similarity.population.Measurement as PopulationMeasurement
import com.hartwig.actin.personalization.similarity.population.MeasurementType as PopulationMeasurementType
import com.hartwig.actin.personalization.similarity.population.SubPopulationAnalysis as PopulationAnalysis

class PersonalizedDataAnalysis(private val analyses: List<PopulationAnalysis>, private val treatmentDatabase: TreatmentDatabase) {

    fun pfsByTreatmentName(): Map<String, Measurement> {
        return analyses.single { it.name == ALL_PATIENTS_SUB_POPULATION_NAME }
            .treatmentMeasurements[PopulationMeasurementType.PROGRESSION_FREE_SURVIVAL]!!.measurementsByTreatment
            .mapNotNull { (measuredTreatment, measurement) ->
                treatmentDatabase.findTreatmentByName(measuredTreatment.display)?.let { treatment ->
                    treatment.name.lowercase() to convertMeasurement(measurement)
                }
            }
            .toMap()
    }

    fun analysis(): List<SubPopulationAnalysis> {
        // Convert to ACTIN datamodel:
        val treatmentLookup = analyses.first().treatments.associateWith { treatmentDatabase.findTreatmentByName(it.display) }
        return analyses.map { analysis ->
            val treatments = analysis.treatments.mapNotNull(treatmentLookup::get)
            val treatmentMeasurements = analysis.treatmentMeasurements.map { (type, measurementCollection) ->
                MeasurementType.valueOf(type.name) to TreatmentMeasurementCollection(
                    measurementCollection.measurementsByTreatment.mapNotNull { (treatment, measurement) ->
                        treatmentLookup[treatment]?.let { it to convertMeasurement(measurement) }
                    }.toMap(),
                    measurementCollection.numPatients
                )
            }.toMap()
            SubPopulationAnalysis(analysis.name, treatmentMeasurements, treatments)
        }
    }

    private fun convertMeasurement(measurement: PopulationMeasurement) =
        with(measurement) { Measurement(value, numPatients, min, max, iqr) }

    companion object {
        fun create(
            personalizationDataPath: String, patient: PatientRecord, treatmentDatabase: TreatmentDatabase
        ): PersonalizedDataAnalysis {
            val personalizedDataInterpreter = PersonalizedDataInterpreter.createFromFile(personalizationDataPath)
            val hasKrasMutation = GeneHasActivatingMutation("KRAS", null).evaluate(patient.molecularHistory).result == EvaluationResult.PASS
            val metastasisLocationGroups = with(patient.tumor) {
                sequenceOf(
                    hasBrainLesions to LocationGroup.BRAIN,
                    hasLungLesions to LocationGroup.BRONCHUS_AND_LUNG,
                    // colon?
                    hasLymphNodeLesions to LocationGroup.LYMPH_NODES,
                    hasLiverLesions to LocationGroup.LIVER_AND_INTRAHEPATIC_BILE_DUCTS,
                    otherLesions?.any { it.contains("periton", ignoreCase = true) } to LocationGroup.RETROPERITONEUM_AND_PERITONEUM,
                ).filter { it.first == true }.map { it.second }.toSet()
            }

            val analyses = personalizedDataInterpreter.analyzePatient(
                patient.patient.registrationDate.year - patient.patient.birthYear,
                patient.clinicalStatus.who!!,
                hasKrasMutation,
                metastasisLocationGroups
            )
            return PersonalizedDataAnalysis(analyses, treatmentDatabase)
        }
    }
}