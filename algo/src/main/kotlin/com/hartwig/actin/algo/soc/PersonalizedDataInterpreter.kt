package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.molecular.GeneHasActivatingMutation
import com.hartwig.actin.personalization.datamodel.LocationGroup
import com.hartwig.actin.personalized.datamodel.Measurement
import com.hartwig.actin.personalized.datamodel.MeasurementType
import com.hartwig.actin.personalized.datamodel.PersonalizedDataAnalysis
import com.hartwig.actin.personalized.datamodel.Population
import com.hartwig.actin.personalized.datamodel.TreatmentAnalysis
import com.hartwig.actin.personalized.datamodel.TreatmentGroup
import com.hartwig.actin.personalization.similarity.PersonalizedDataInterpreter as PersonalizedDataAnalyzer
import com.hartwig.actin.personalization.similarity.population.Measurement as PopulationMeasurement
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis as PersonalAnalysis

class PersonalizedDataInterpreter(private val analyzer: PersonalizedDataAnalyzer) {

    fun interpret(patient: PatientRecord): PersonalizedDataAnalysis {
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

        val analysis = analyzer.analyzePatient(
            patient.patient.registrationDate.year - patient.patient.birthYear,
            patient.clinicalStatus.who!!,
            hasKrasMutation,
            metastasisLocationGroups
        )
        return PersonalizedDataAnalysis(extractTreatmentAnalyses(analysis), extractPopulations(analysis))
    }

    private fun extractTreatmentAnalyses(analysis: PersonalAnalysis): List<TreatmentAnalysis> {
        return analysis.treatmentAnalyses.map { (treatmentGroup, measurements) ->
            val treatmentMeasurements = measurements.entries.associate { (type, measurementsByPopulation) ->
                MeasurementType.valueOf(type.name) to measurementsByPopulation.mapValues { (_, measurement) ->
                    convertMeasurement(measurement)
                }
            }
            TreatmentAnalysis(TreatmentGroup.valueOf(treatmentGroup.name), treatmentMeasurements)
        }
    }

    private fun extractPopulations(analysis: PersonalAnalysis): List<Population> {
        return analysis.subPopulations.map { population ->
            Population(
                population.name,
                population.patientsByMeasurementType.entries.map { (measurementType, patients) ->
                    MeasurementType.valueOf(measurementType.name) to patients.size
                }.toMap()
            )
        }
    }

    private fun convertMeasurement(measurement: PopulationMeasurement) =
        with(measurement) { Measurement(value, numPatients, min, max, iqr) }

    companion object {
        fun create(personalizationDataPath: String): PersonalizedDataInterpreter {
            return PersonalizedDataInterpreter(PersonalizedDataAnalyzer.createFromFile(personalizationDataPath))
        }
    }
}