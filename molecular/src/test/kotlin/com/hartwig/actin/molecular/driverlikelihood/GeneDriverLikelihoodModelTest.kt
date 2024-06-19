package com.hartwig.actin.molecular.driverlikelihood

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val GENE = "gene"

class GeneDriverLikelihoodModelTest {

    private val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(DndsDatabase(mapOf(GENE to DndsDatabaseEntry())))

    @Test
    fun `Should assign driver likelihood of high and hotspot true when gene has loss of function or gain of function (and predicted)`() {
        evaluateAndAssert(geneDriverLikelihoodModel, ProteinEffect.LOSS_OF_FUNCTION)
        evaluateAndAssert(geneDriverLikelihoodModel, ProteinEffect.LOSS_OF_FUNCTION_PREDICTED)
        evaluateAndAssert(geneDriverLikelihoodModel, ProteinEffect.LOSS_OF_FUNCTION)
        evaluateAndAssert(geneDriverLikelihoodModel, ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
    }

    @Test
    fun `Should assign null driver likelihood when gene is unknown to serve`() {
        evaluateAndAssertGene(GENE)
        evaluateAndAssertGene("another_gene")
    }

    @Test
    fun `Should discard VUS`() {
        evaluateAndAssertGene(GENE)
        evaluateAndAssertGene("another_gene")
    }

    private fun evaluateAndAssertGene(gene: String) {
        val annotatedVariants = geneDriverLikelihoodModel.evaluate(
            gene,
            listOf(TestVariantFactory.createMinimal())
        )
        assertThat(annotatedVariants.isHotspot).isFalse()
        assertThat(annotatedVariants.driverLikelihood).isNull()
    }

    private fun evaluateAndAssert(geneDriverLikelihoodModel: GeneDriverLikelihoodModel, proteinEffect: ProteinEffect) {
        val annotatedVariants = geneDriverLikelihoodModel.evaluate(
            GENE,
            listOf(TestVariantFactory.createMinimal().copy(proteinEffect = proteinEffect))
        )
        assertThat(annotatedVariants.isHotspot).isTrue()
        assertThat(annotatedVariants.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
    }

}