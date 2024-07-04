package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.molecular.GeneHasActivatingMutation
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalization.datamodel.Measurement
import com.hartwig.actin.personalization.datamodel.MeasurementType
import com.hartwig.actin.personalization.datamodel.PersonalizedDataAnalysis
import com.hartwig.actin.personalization.datamodel.SubPopulation
import com.hartwig.actin.personalization.datamodel.TreatmentAnalysis
import com.hartwig.actin.personalization.datamodel.TreatmentGroup
import com.hartwig.actin.personalization.similarity.PersonalizedDataInterpreter
import com.hartwig.actin.personalization.similarity.population.Measurement as PopulationMeasurement
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis as ActinPersonalizationAnalysis

class PersonalizedDataInterpreter(private val analysis: ActinPersonalizationAnalysis) {

    fun interpret(): PersonalizedDataAnalysis {
        return PersonalizedDataAnalysis(treatmentAnalyses(), subPopulations())
    }

    private fun treatmentAnalyses(): List<TreatmentAnalysis> {
        return analysis.treatmentAnalyses.map { (treatmentGroup, measurements) ->
            val treatmentMeasurements = measurements.entries.map { (type, measurementsByPopulation) ->
                MeasurementType.valueOf(type.name) to measurementsByPopulation.mapValues { (_, measurement) ->
                    convertMeasurement(measurement)
                }
            }.toMap()
            TreatmentAnalysis(TreatmentGroup.valueOf(treatmentGroup.name), treatmentMeasurements)
        }
    }

    private fun subPopulations(): List<SubPopulation> {
        return analysis.subPopulations.map { subPopulation ->
            SubPopulation(
                subPopulation.name,
                subPopulation.patientsByMeasurementType.entries.map { (measurementType, patients) ->
                    MeasurementType.valueOf(measurementType.name) to patients.size
                }.toMap()
            )
        }
    }

    private fun convertMeasurement(measurement: PopulationMeasurement) =
        with(measurement) { Measurement(value, numPatients, min, max, iqr) }

    companion object {
        fun create(personalizationDataPath: String, patient: PatientRecord): com.hartwig.actin.algo.soc.PersonalizedDataInterpreter {
            val personalizedDataInterpreter = PersonalizedDataInterpreter.createFromFile(personalizationDataPath)
            val hasKrasMutation = GeneHasActivatingMutation("KRAS", null).evaluate(patient).result == EvaluationResult.PASS
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

            val analysis = personalizedDataInterpreter.analyzePatient(
                patient.patient.registrationDate.year - patient.patient.birthYear,
                patient.clinicalStatus.who!!,
                hasKrasMutation,
                metastasisLocationGroups
            )
            return PersonalizedDataInterpreter(analysis)
        }
    }
}