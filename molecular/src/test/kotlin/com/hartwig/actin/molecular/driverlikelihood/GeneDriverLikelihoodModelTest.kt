package com.hartwig.actin.molecular.driverlikelihood

import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

private const val GENE = "gene"

private val ENTRY = DndsDatabaseEntry(0.1, 0.5)

private const val DRIVER_LIKELIHOOD = 0.181
private const val HIGHER_DRIVER_LIKELIHOOD = 0.526

class GeneDriverLikelihoodModelTest {

    private val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(
        DndsDatabase(
            mapOf(GENE to mapOf(DndsDriverType.NONSENSE to DndsDatabaseEntry())),
            mapOf(GENE to mapOf(DndsDriverType.NONSENSE to DndsDatabaseEntry()))
        )
    )

    @Test
    fun `Should load proper dnds database and return expected likelihood for BRAF VUS`() {
        val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV))
        val result = geneDriverLikelihoodModel.evaluate(
            "BRAF",
            GeneRole.ONCO,
            listOf(createVariant(VariantType.SNV, CodingEffect.MISSENSE))
        )
        assertThat(result).isEqualTo(0.484, Offset.offset(0.001))
    }

    @Test
    fun `Should load proper dnds database and return expected likelihood for ARID1B VUS (multi-hit)`() {
        val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV))
        val result = geneDriverLikelihoodModel.evaluate(
            "ARID1B",
            GeneRole.TSG,
            listOf(
                createVariant(VariantType.SNV, CodingEffect.MISSENSE),
                createVariant(VariantType.SNV, CodingEffect.NONSENSE_OR_FRAMESHIFT)
            )
        )
        assertThat(result).isEqualTo(0.961, Offset.offset(0.001))
    }

    @Test
    fun `Should assign driver likelihood of high and hotspot true when gene has loss of function or gain of function (and predicted)`() {
        evaluateAndAssert(geneDriverLikelihoodModel, ProteinEffect.LOSS_OF_FUNCTION)
        evaluateAndAssert(geneDriverLikelihoodModel, ProteinEffect.LOSS_OF_FUNCTION_PREDICTED)
        evaluateAndAssert(geneDriverLikelihoodModel, ProteinEffect.GAIN_OF_FUNCTION)
        evaluateAndAssert(geneDriverLikelihoodModel, ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
    }

    @Test
    fun `Should assign driver likelihood of high when variant is already marked as hotspot`() {
        val result = geneDriverLikelihoodModel.evaluate(
            GENE,
            GeneRole.ONCO,
            listOf(TestVariantFactory.createMinimal().copy(isHotspot = true))
        )
        assertThat(result).isEqualTo(1.0)
    }

    @Test
    fun `Should assign null driver likelihood when gene role is unknown`() {
        val result = geneDriverLikelihoodModel.evaluate(
            GENE,
            GeneRole.UNKNOWN,
            listOf(TestVariantFactory.createMinimal())
        )
        assertThat(result).isNull()
    }

    @Test
    fun `Should evaluate single VUS nonsense in onco and tsg gene`() {
        val model = GeneDriverLikelihoodModel(DndsDatabase(dndsMap(DndsDriverType.NONSENSE), dndsMap(DndsDriverType.NONSENSE)))
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
        val model = GeneDriverLikelihoodModel(DndsDatabase(dndsMap(DndsDriverType.MISSENSE), dndsMap(DndsDriverType.MISSENSE)))
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
        val model = GeneDriverLikelihoodModel(DndsDatabase(dndsMap(DndsDriverType.INDEL), dndsMap(DndsDriverType.INDEL)))
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
        val model = GeneDriverLikelihoodModel(DndsDatabase(dndsMap(DndsDriverType.SPLICE), dndsMap(DndsDriverType.SPLICE)))
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
        val model = GeneDriverLikelihoodModel(
            DndsDatabase(
                dndsMap(DndsDriverType.NONSENSE),
                mapOf(GENE to mapOf(DndsDriverType.NONSENSE to ENTRY.copy(probabilityVariantNonDriver = 0.1)))
            )
        )
        evaluateAndAssertVUS(
            model, GeneRole.BOTH, HIGHER_DRIVER_LIKELIHOOD, createVariant(
                VariantType.SNV,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            )
        )
    }

    @Test
    fun `Should evaluate multi VUS in onco genes by taking max`() {
        val model = GeneDriverLikelihoodModel(
            DndsDatabase(
                dndsMap(DndsDriverType.NONSENSE) + mapOf(GENE to mapOf(DndsDriverType.MISSENSE to ENTRY.copy(probabilityVariantNonDriver = 0.1))),
                emptyMap()
            )
        )
        evaluateAndAssertVUS(
            model, GeneRole.ONCO, HIGHER_DRIVER_LIKELIHOOD, createVariant(
                VariantType.SNV,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            ), createVariant(VariantType.SNV, CodingEffect.MISSENSE)
        )
    }

    @Test
    fun `Should evaluate multi VUS in tsg genes by taking joint probability of 2 most impactful variants`() {
        val model = GeneDriverLikelihoodModel(
            DndsDatabase(
                emptyMap(),
                mapOf(
                    GENE to mapOf(
                        DndsDriverType.NONSENSE to ENTRY.copy(probabilityVariantNonDriver = 0.5, driversPerSample = 0.7),
                        DndsDriverType.MISSENSE to ENTRY.copy(probabilityVariantNonDriver = 0.01, driversPerSample = 0.6),
                        DndsDriverType.INDEL to ENTRY.copy(probabilityVariantNonDriver = 0.1, driversPerSample = 0.5),
                    )
                )
            )
        )
        evaluateAndAssertVUS(
            model, GeneRole.TSG, 0.979, createVariant(
                VariantType.SNV,
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            ), createVariant(VariantType.SNV, CodingEffect.MISSENSE), createVariant(VariantType.INSERT, CodingEffect.NONSENSE_OR_FRAMESHIFT)
        )
    }

    @Test
    fun `Should evaluate variant without coding effect`() {
        val model = GeneDriverLikelihoodModel(
            DndsDatabase(
                emptyMap(),
                mapOf(
                    GENE to mapOf(
                        DndsDriverType.NONSENSE to ENTRY.copy(probabilityVariantNonDriver = 0.5, driversPerSample = 0.7),
                        DndsDriverType.MISSENSE to ENTRY.copy(probabilityVariantNonDriver = 0.01, driversPerSample = 0.6),
                        DndsDriverType.INDEL to ENTRY.copy(probabilityVariantNonDriver = 0.1, driversPerSample = 0.5),
                    )
                )
            )
        )
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
        val result = model.evaluate(GENE, geneRole, variants.toList())

        if (expectedLikelihood == null) {
            assertThat(result).isNull()
        } else {
            assertThat(result).isEqualTo(expectedLikelihood, Offset.offset(0.001))
        }
    }

    private fun createVariant(variantType: VariantType, codingEffect: CodingEffect) = TestVariantFactory.createMinimal().copy(
        type = variantType,
        canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(codingEffect = codingEffect)
    )

    private fun dndsMap(dndsDriverType: DndsDriverType) = mapOf(GENE to mapOf(dndsDriverType to ENTRY))

    private fun evaluateAndAssert(geneDriverLikelihoodModel: GeneDriverLikelihoodModel, proteinEffect: ProteinEffect) {
        val result = geneDriverLikelihoodModel.evaluate(
            GENE,
            GeneRole.ONCO,
            listOf(TestVariantFactory.createMinimal().copy(proteinEffect = proteinEffect))
        )
        assertThat(result).isEqualTo(1.0)
    }
}