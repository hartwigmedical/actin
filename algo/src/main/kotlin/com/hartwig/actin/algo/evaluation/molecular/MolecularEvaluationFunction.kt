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
import com.hartwig.actin.molecular.util.GeneConstants
import java.time.LocalDate

val EXAMPLE_DATE: LocalDate = LocalDate.of(1900, 1, 1)

const val NO_SUFFICIENT_QUALITY_MESSAGE = "No molecular results of sufficient quality"
const val INSUFFICIENT_MOLECULAR_DATA_MESSAGE = "Insufficient molecular data"

abstract class MolecularEvaluationFunction(
    private val useInsufficientQualityRecords: Boolean = false,
    open val gene: String? = null,
    val targetCoveragePredicate: TargetCoveragePredicate = any(),
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tests = record.molecularTests
        val recentMolecularTests = if (useInsufficientQualityRecords) tests else tests.filter { it.hasSufficientQuality }
        val relevantIhcTests = gene.takeIf { it in GeneConstants.IHC_EVALUABLE_GENES }
            ?.let { IhcTestEvaluation.create(item = GeneConstants.returnProteinForGene(it), ihcTests = record.ihcTests).filteredTests }

        return if (recentMolecularTests.isEmpty() && relevantIhcTests.isNullOrEmpty()) {
            noMolecularTestEvaluation() ?: EvaluationFactory.undetermined(
                NO_SUFFICIENT_QUALITY_MESSAGE,
                isMissingMolecularResultForEvaluation = true
            )
        } else {
            val geneIsNotTested = gene?.let { g -> recentMolecularTests.none { t -> t.testsGene(g, targetCoveragePredicate) } } == true
            if (geneIsNotTested && relevantIhcTests.isNullOrEmpty())
                return Evaluation(
                    recoverable = false,
                    result = EvaluationResult.UNDETERMINED,
                    undeterminedMessages = setOf(targetCoveragePredicate.message(gene!!)),
                    isMissingMolecularResultForEvaluation = true
                )

            val testEvaluation =
                recentMolecularTests.mapNotNull { evaluate(it, record.ihcTests)?.let { eval -> MolecularEvaluation(it, eval) } }
            val onlyIhcEvaluation = evaluate(createEmptyMolecularTest(), record.ihcTests)

            return if (testEvaluation.isNotEmpty()) {
                MolecularEvaluation.combine(testEvaluation, evaluationPrecedence())
            } else {
                onlyIhcEvaluation
                    ?: noMolecularTestEvaluation()
                    ?: EvaluationFactory.undetermined(INSUFFICIENT_MOLECULAR_DATA_MESSAGE, isMissingMolecularResultForEvaluation = true)
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