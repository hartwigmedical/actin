package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.util.GeneConstants
import org.junit.jupiter.api.Test

class IsHomologousRecombinationDeficientTest {
    
    private val function = IsHomologousRecombinationDeficient()
    private val hrGene = GeneConstants.HR_GENES.first()

    @Test
    fun canEvaluate() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withVariant(hrdVariant())),
            "Unknown HRD status"
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withVariant(hrdVariant(isReportable = true, isBiallelic = true))),
            "Unknown HRD status but biallelic driver event(s) in HR gene(s) (BRCA1) detected"
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withVariant(hrdVariant(isReportable = true, isBiallelic = false))),
            "Unknown HRD status but non-biallelic driver event(s) in HR gene(s) (BRCA1) detected"
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(isReportable = true, gene = hrGene)
                )
            ),
            "Unknown HRD status but driver event(s) in HR gene(s) (BRCA1) detected"
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(true, hrdVariant(isReportable = true, isBiallelic = false))
            ),
            "Tumor is HRD but with only non-biallelic driver event(s) in HR gene(s) (BRCA1)"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(true, hrdVariant(isReportable = true, isBiallelic = true))
            ),
            "Tumor is HRD with biallelic driver event(s) in HR gene(s) (BRCA1)"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndDeletion(
                    true,
                    TestCopyNumberFactory.createMinimal().copy(
                        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_DEL),
                        gene = hrGene
                    )
                )
            ),
            "Tumor is HRD with biallelic driver event(s) in HR gene(s) (BRCA1)"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndHomozygousDisruption(
                    true, TestHomozygousDisruptionFactory.createMinimal().copy(gene = hrGene)
                )
            ),
            "Tumor is HRD with biallelic driver event(s) in HR gene(s) (BRCA1)"
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndDisruption(
                    true, TestDisruptionFactory.createMinimal().copy(gene = hrGene)
                )
            ),
            "Tumor is HRD but without driver event(s) in HR gene(s)"
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHomologousRecombinationAndVariant(true, hrdVariant(isReportable = false))),
            "Tumor is HRD but without driver event(s) in HR gene(s)"
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true,
                    TestVariantFactory.createMinimal().copy(
                        gene = "other gene",
                        isReportable = true,
                        isBiallelic = false
                    )
                )
            ),
            "Tumor is HRD but without driver event(s) in HR gene(s)"
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHomologousRecombinationAndVariant(false, hrdVariant(isReportable = true))),
            "Tumor is not HRD"
        )
    }

    private fun hrdVariant(isReportable: Boolean = false, isBiallelic: Boolean = false): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = hrGene,
            isReportable = isReportable,
            isBiallelic = isBiallelic
        )
    }
}