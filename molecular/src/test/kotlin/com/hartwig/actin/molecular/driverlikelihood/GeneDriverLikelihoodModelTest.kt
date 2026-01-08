package com.hartwig.actin.molecular.driverlikelihood

import com.hartwig.actin.configuration.MolecularConfiguration
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

private const val GENE = "gene"

private val ENTRY = DndsDatabaseEntry(0.1, 0.5)

private const val DRIVER_LIKELIHOOD = 0.181
private const val HIGHER_DRIVER_LIKELIHOOD = 0.526

class GeneDriverLikelihoodModelTest {

    private val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(mockk())
    private val configuration = MolecularConfiguration()

    @Test
    fun `Should load proper dnds database and return expected likelihood for BRAF VUS`() {
        val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(
            DndsModel.create(
                DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV),
                tumorMutationalBurden = null
            )
        )
        val result = geneDriverLikelihoodModel.evaluate(
            "BRAF",
            GeneRole.ONCO,
            listOf(createVariant(VariantType.SNV, CodingEffect.MISSENSE)),
            configuration.variantPathogenicityIsConfirmed
        )
        assertThat(result).isEqualTo(0.504, Offset.offset(0.001))
    }

    @Test
    fun `Should return driver likelihood of 1 for BRAF VUS when variant pathogenicity is confirmed`() {
        val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(
            DndsModel.create(
                DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV),
                tumorMutationalBurden = null
            )
        )
        val result = geneDriverLikelihoodModel.evaluate(
            "BRAF",
            GeneRole.ONCO,
            listOf(createVariant(VariantType.SNV, CodingEffect.MISSENSE)),
            true
        )
        assertThat(result).isEqualTo(1.0)
    }

    @Test
    fun `Should load proper dnds database and return expected likelihood for ARID1B VUS (multi-hit)`() {
        val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(
            DndsModel.create(
                DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV),
                tumorMutationalBurden = null
            )
        )
        val result = geneDriverLikelihoodModel.evaluate(
            "ARID1B",
            GeneRole.TSG,
            listOf(
                createVariant(VariantType.SNV, CodingEffect.MISSENSE),
                createVariant(VariantType.SNV, CodingEffect.NONSENSE_OR_FRAMESHIFT)
            ),
            configuration.variantPathogenicityIsConfirmed
        )
        assertThat(result).isEqualTo(0.967, Offset.offset(0.001))
    }

    @Test
    fun `Should assign driver likelihood of high when variant is already marked as a cancer-associated variant`() {
        val result = geneDriverLikelihoodModel.evaluate(
            GENE,
            GeneRole.ONCO,
            listOf(TestVariantFactory.createMinimal().copy(isCancerAssociatedVariant = true)),
            configuration.variantPathogenicityIsConfirmed
        )
        assertThat(result).isEqualTo(1.0)
    }

    @Test
    fun `Should assign driver likelihood of high when biallelic splice variant in TSG`() {
        val biallelicVariant = TestVariantFactory.createMinimal().copy(
            isBiallelic = true,
            canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(
                codingEffect = CodingEffect.SPLICE
            )
        )
        val result = geneDriverLikelihoodModel.evaluate(
            GENE,
            GeneRole.TSG,
            listOf(biallelicVariant),
            configuration.variantPathogenicityIsConfirmed
        )
        assertThat(result).isEqualTo(1.0)
    }

    @Test
    fun `Should assign null driver likelihood when gene role is unknown`() {
        val result = geneDriverLikelihoodModel.evaluate(
            GENE,
            GeneRole.UNKNOWN,
            listOf(TestVariantFactory.createMinimal()),
            configuration.variantPathogenicityIsConfirmed
        )
        assertThat(result).isNull()
    }

    @Test
    fun `Should evaluate single VUS nonsense in onco and tsg gene`() {
        val dndsModel = mockk<DndsModel> {
            every { find(GENE, GeneRole.ONCO, DndsDriverType.NONSENSE) } returns ENTRY
            every { find(GENE, GeneRole.TSG, DndsDriverType.NONSENSE) } returns ENTRY
        }
        val model = GeneDriverLikelihoodModel(dndsModel)
        evaluateAndAssertVUS(
            model, GeneRole.ONCO, DRIVER_LIKELIHOOD, createVariant(
                VariantType.SNV,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            )
        )
        evaluateAndAssertVUS(
            model, GeneRole.TSG, DRIVER_LIKELIHOOD, createVariant(
                VariantType.SNV,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            )
        )
    }

    @Test
    fun `Should evaluate single VUS missense in onco and tsg gene`() {
        val dndsModel = mockk<DndsModel> {
            every { find(GENE, GeneRole.ONCO, DndsDriverType.MISSENSE) } returns ENTRY
            every { find(GENE, GeneRole.TSG, DndsDriverType.MISSENSE) } returns ENTRY
        }
        val model = GeneDriverLikelihoodModel(dndsModel)
        evaluateAndAssertVUS(
            model, GeneRole.ONCO, DRIVER_LIKELIHOOD, createVariant(
                VariantType.SNV,
                CodingEffect.MISSENSE
            )
        )
        evaluateAndAssertVUS(
            model, GeneRole.TSG, DRIVER_LIKELIHOOD, createVariant(
                VariantType.SNV,
                CodingEffect.MISSENSE
            )
        )
    }

    @Test
    fun `Should evaluate single VUS indel in onco and tsg gene`() {
        val dndsModel = mockk<DndsModel> {
            every { find(GENE, GeneRole.ONCO, DndsDriverType.INDEL) } returns ENTRY
            every { find(GENE, GeneRole.TSG, DndsDriverType.INDEL) } returns ENTRY
        }
        val model = GeneDriverLikelihoodModel(dndsModel)
        evaluateAndAssertVUS(
            model, GeneRole.ONCO, DRIVER_LIKELIHOOD, createVariant(
                VariantType.INSERT,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            )
        )
        evaluateAndAssertVUS(
            model, GeneRole.TSG, DRIVER_LIKELIHOOD, createVariant(
                VariantType.DELETE,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            )
        )
    }

    @Test
    fun `Should evaluate single VUS splice in onco and tsg gene`() {
        val dndsModel = mockk<DndsModel> {
            every { find(GENE, GeneRole.ONCO, DndsDriverType.SPLICE) } returns ENTRY
            every { find(GENE, GeneRole.TSG, DndsDriverType.SPLICE) } returns ENTRY
        }
        val model = GeneDriverLikelihoodModel(dndsModel)
        evaluateAndAssertVUS(
            model, GeneRole.ONCO, DRIVER_LIKELIHOOD, createVariant(
                VariantType.DELETE,
                CodingEffect.SPLICE
            )
        )
        evaluateAndAssertVUS(
            model, GeneRole.TSG, DRIVER_LIKELIHOOD, createVariant(
                VariantType.DELETE,
                CodingEffect.SPLICE
            )
        )
    }

    @Test
    fun `Should evaluate single VUS in genes with both roles`() {
        val dndsModel = mockk<DndsModel> {
            every { find(GENE, GeneRole.ONCO, DndsDriverType.NONSENSE) } returns ENTRY
            every { find(GENE, GeneRole.TSG, DndsDriverType.NONSENSE) } returns ENTRY.copy(probabilityVariantNonDriver = 0.1)
        }
        val model = GeneDriverLikelihoodModel(dndsModel)
        evaluateAndAssertVUS(
            model, GeneRole.BOTH, HIGHER_DRIVER_LIKELIHOOD, createVariant(
                VariantType.SNV,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            )
        )
    }

    @Test
    fun `Should evaluate multi VUS in onco genes by taking max`() {
        val dndsModel = mockk<DndsModel> {
            every { find(GENE, GeneRole.ONCO, DndsDriverType.NONSENSE) } returns ENTRY
            every { find(GENE, GeneRole.ONCO, DndsDriverType.MISSENSE) } returns ENTRY.copy(probabilityVariantNonDriver = 0.1)
        }
        val model = GeneDriverLikelihoodModel(dndsModel)
        evaluateAndAssertVUS(
            model, GeneRole.ONCO, HIGHER_DRIVER_LIKELIHOOD, createVariant(
                VariantType.SNV,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            ), createVariant(VariantType.SNV, CodingEffect.MISSENSE)
        )
    }

    @Test
    fun `Should evaluate multi VUS in tsg genes by taking joint probability of 2 most impactful variants`() {
        val dndsModel = mockk<DndsModel> {
            every { find(GENE, GeneRole.TSG, DndsDriverType.NONSENSE) } returns ENTRY.copy(
                probabilityVariantNonDriver = 0.5,
                driversPerSample = 0.7
            )
            every { find(GENE, GeneRole.TSG, DndsDriverType.MISSENSE) } returns ENTRY.copy(
                probabilityVariantNonDriver = 0.01,
                driversPerSample = 0.6
            )
            every { find(GENE, GeneRole.TSG, DndsDriverType.INDEL) } returns ENTRY.copy(
                probabilityVariantNonDriver = 0.1,
                driversPerSample = 0.5
            )
        }
        val model = GeneDriverLikelihoodModel(dndsModel)
        evaluateAndAssertVUS(
            model, GeneRole.TSG, 0.979, createVariant(
                VariantType.SNV,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            ), createVariant(VariantType.SNV, CodingEffect.MISSENSE), createVariant(VariantType.INSERT, CodingEffect.NONSENSE_OR_FRAMESHIFT)
        )
    }

    @Test
    fun `Should evaluate variant without coding effect`() {
        val dndsModel = mockk<DndsModel> {
            every { find(GENE, GeneRole.ONCO, DndsDriverType.NONSENSE) } returns ENTRY.copy(
                probabilityVariantNonDriver = 0.5,
                driversPerSample = 0.7
            )
            every { find(GENE, GeneRole.ONCO, DndsDriverType.MISSENSE) } returns ENTRY.copy(
                probabilityVariantNonDriver = 0.01,
                driversPerSample = 0.6
            )
            every { find(GENE, GeneRole.ONCO, DndsDriverType.INDEL) } returns ENTRY.copy(
                probabilityVariantNonDriver = 0.1,
                driversPerSample = 0.5
            )
        }
        val model = GeneDriverLikelihoodModel(dndsModel)
        evaluateAndAssertVUS(
            model, GeneRole.BOTH, null,
            createVariant(VariantType.SNV, CodingEffect.NONE)
        )
    }

    private fun evaluateAndAssertVUS(
        model: GeneDriverLikelihoodModel,
        geneRole: GeneRole,
        expectedLikelihood: Double?,
        vararg variants: Variant
    ) {
        val result = model.evaluate(GENE, geneRole, variants.toList(), configuration.variantPathogenicityIsConfirmed)

        if (expectedLikelihood == null) {
            assertThat(result).isNull()
        } else {
            assertThat(result).isEqualTo(expectedLikelihood, Offset.offset(0.001))
        }
    }

    private fun createVariant(variantType: VariantType, codingEffect: CodingEffect) = TestVariantFactory.createMinimal().copy(
        type = variantType,
        canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = codingEffect)
    )
}