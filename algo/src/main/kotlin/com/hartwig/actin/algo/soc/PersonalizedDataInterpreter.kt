package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.evaluation.molecular.GeneHasActivatingMutation
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.personalization.Measurement
import com.hartwig.actin.datamodel.personalization.MeasurementType
import com.hartwig.actin.datamodel.personalization.PersonalizedDataAnalysis
import com.hartwig.actin.datamodel.personalization.Population
import com.hartwig.actin.datamodel.personalization.TreatmentAnalysis
import com.hartwig.actin.datamodel.personalization.TreatmentGroup
import com.hartwig.actin.personalization.datamodel.diagnosis.LocationGroup
import com.hartwig.actin.personalization.similarity.PersonalizedDataInterpreter as PersonalizedDataAnalyzer
import com.hartwig.actin.personalization.similarity.population.Measurement as PopulationMeasurement
import com.hartwig.actin.personalization.similarity.population.PersonalizedDataAnalysis as PersonalAnalysis

class PersonalizedDataInterpreter(private val analyzer: PersonalizedDataAnalyzer) {

    private val measurementTypeLookup = MeasurementType.entries.associateBy { it.name }

    fun interpret(patient: PatientRecord): PersonalizedDataAnalysis {
        val hasRasMutation = listOf("KRAS", "NRAS", "HRAS").any { hasActivatingMutationInGene(patient, it) }
        val metastasisLocationGroups = with(patient.tumor) {
            sequenceOf(
                hasBrainLesions to LocationGroup.BRAIN,
                hasLungLesions to LocationGroup.BRONCHUS_AND_LUNG,
                hasLymphNodeLesions to LocationGroup.LYMPH_NODES,
                hasLiverLesions to LocationGroup.LIVER_AND_INTRAHEPATIC_BILE_DUCTS,
                TumorEvaluationFunctions.hasPeritonealMetastases(this) to LocationGroup.PERITONEUM,
            ).filter { it.first == true }.map { it.second }.toSet()
        }

        val analysis = analyzer.analyzePatient(
            patient.patient.registrationDate.year - patient.patient.birthYear,
            patient.clinicalStatus.who!!,
            hasRasMutation,
            metastasisLocationGroups
        )
        return PersonalizedDataAnalysis(extractTreatmentAnalyses(analysis), extractPopulations(analysis))
    }

    private fun extractTreatmentAnalyses(analysis: PersonalAnalysis): List<TreatmentAnalysis> {
        return analysis.treatmentAnalyses.map { (treatmentGroup, measurements) ->
            val treatmentMeasurements = measurements.entries.mapNotNull { (type, measurementsByPopulation) ->
                measurementTypeLookup[type.name]?.let { measurementType ->
                    measurementType to measurementsByPopulation.mapValues { (_, measurement) ->
                        convertMeasurement(measurement)
                    }
                }
            }.toMap()
            TreatmentAnalysis(TreatmentGroup.valueOf(treatmentGroup.name), treatmentMeasurements)
        }
    }

    private fun extractPopulations(analysis: PersonalAnalysis): List<Population> {
        return analysis.populations.map { population ->
            Population(
                population.name,
                population.entriesByMeasurementType.entries.mapNotNull { (type, patients) ->
                    measurementTypeLookup[type.name]?.let { it to patients.size }
                }.toMap()
            )
        }
    }

    private fun convertMeasurement(measurement: PopulationMeasurement) =
        with(measurement) { Measurement(value, numEntries, min, max, iqr) }

    private fun hasActivatingMutationInGene(patient: PatientRecord, gene: String): Boolean {
        return GeneHasActivatingMutation(gene, null).evaluate(patient).result == EvaluationResult.PASS
    }

    companion object {
        fun create(personalizationDataPath: String): PersonalizedDataInterpreter {
            return PersonalizedDataInterpreter(PersonalizedDataAnalyzer.createFromFile(personalizationDataPath))
        }
    }
}