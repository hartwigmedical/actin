package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.molecular.filter.MolecularTestFilter
import com.hartwig.actin.molecular.util.GeneConstants
import java.time.LocalDate

val EXAMPLE_DATE = LocalDate.of(1900,1,1)

abstract class MolecularEvaluationFunction(
    maxTestAge: LocalDate? = null,
    useInsufficientQualityRecords: Boolean = false,
    open val gene: String? = null,
    val targetCoveragePredicate: TargetCoveragePredicate = any(),
) : EvaluationFunction {

    private val molecularTestFilter = MolecularTestFilter(maxTestAge, useInsufficientQualityRecords)

    override fun evaluate(record: PatientRecord): Evaluation {
        val recentMolecularTests = molecularTestFilter.apply(record.molecularTests)
        val relevantIhcTests = if (gene in GeneConstants.IHC_EVALUABLE_GENES) {
            gene?.let { IhcTestEvaluation.create(item = GeneConstants.returnProteinForGene(it), ihcTests = record.ihcTests).filteredTests }
        } else null

        return if (recentMolecularTests.isEmpty() && relevantIhcTests.isNullOrEmpty()) {
            noMolecularTestEvaluation() ?: EvaluationFactory.undetermined(
                "No molecular results of sufficient quality",
                isMissingMolecularResultForEvaluation = true
            )
        } else {
            if (relevantIhcTests.isNullOrEmpty() && gene?.let { g ->
                    recentMolecularTests.any { t ->
                        t.testsGene(
                            g,
                            targetCoveragePredicate
                        )
                    }
                } == false)
                return Evaluation(
                    recoverable = false,
                    result = EvaluationResult.UNDETERMINED,
                    undeterminedMessages = setOf(targetCoveragePredicate.message(gene!!)),
                    isMissingMolecularResultForEvaluation = true
                )

            val testEvaluation =
                recentMolecularTests.mapNotNull { evaluate(it, record.ihcTests)?.let { eval -> MolecularEvaluation(it, eval) } }
            val onlyIhcEvaluation = evaluate(createEmptyMolecularTest(), record.ihcTests)

            return when {
                testEvaluation.isNotEmpty() -> MolecularEvaluation.combine(testEvaluation, evaluationPrecedence())
                onlyIhcEvaluation != null -> onlyIhcEvaluation
                else -> {
                    noMolecularTestEvaluation() ?: EvaluationFactory.undetermined(
                        "Insufficient molecular data",
                        isMissingMolecularResultForEvaluation = true
                    )
                }
            }
        }
    }

    open fun noMolecularTestEvaluation(): Evaluation? = null
    open fun evaluate(test: MolecularTest): Evaluation? = null
    open fun evaluate(test: MolecularTest, ihcTests: List<IhcTest>): Evaluation? = evaluate(test)

    open fun evaluationPrecedence(): (Map<EvaluationResult, List<MolecularEvaluation>>) -> List<MolecularEvaluation>? =
        { MolecularEvaluation.defaultEvaluationPrecedence(it) }

    private fun createEmptyMolecularTest(): MolecularTest {
        return MolecularTest(
            date = EXAMPLE_DATE,
            sampleId = null,
            reportHash = null,
            experimentType = ExperimentType.IHC,
            testTypeDisplay = ExperimentType.IHC.display(),
            targetSpecification = null,
            refGenomeVersion = RefGenomeVersion.V37,
            containsTumorCells = true,
            hasSufficientPurity = true,
            hasSufficientQuality = true,
            isContaminated = false,
            drivers = Drivers(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
            characteristics = MolecularCharacteristics(null, null, null, null, null, null, null),
            immunology = null,
            pharmaco = emptySet(),
            evidenceSource = "",
            externalTrialSource = ""
        )
    }
}