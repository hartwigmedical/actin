package com.hartwig.actin.molecular.driverlikelihood

import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

val TEST_ONCO_DNDS_TSV = System.getProperty("user.dir") + "/src/test/resources/interpretation/dnds_driver_likelihood_onco.tsv"
val TEST_TSG_DNDS_TSV = System.getProperty("user.dir") + "/src/test/resources/interpretation/dnds_driver_likelihood_tsg.tsv"

class DndsModelTest {

    private val database = DndsModel.create(DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV), tumorMutationalBurden = null)

    @Test
    fun `Should load database from onco and tsg dnd TSVs and calculate probability of non-driver with a default TMB of 10`() {
        assertThat(database.find("BRAF", GeneRole.ONCO, DndsDriverType.INDEL)).isEqualTo(DndsDatabaseEntry(0.0, 7.660332542758219E-5))
        assertThat(database.find("BRAF", GeneRole.ONCO, DndsDriverType.NONSENSE)).isEqualTo(
            DndsDatabaseEntry(
                9.26041369655672E-5,
                3.776596213239669E-4
            )
        )
        assertThat(database.find("BRAF", GeneRole.ONCO, DndsDriverType.MISSENSE)).isEqualTo(
            DndsDatabaseEntry(
                0.00524550351936585,
                0.005181857483946728
            )
        )
        assertThat(database.find("BRAF", GeneRole.ONCO, DndsDriverType.SPLICE)).isEqualTo(
            DndsDatabaseEntry(
                1.38906205448351E-4,
                5.664359435727517E-4
            )
        )
    }

    @Test
    fun `Should use estimated variant counts based on TMB when given`() {
        val database = DndsModel.create(
            DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV), tumorMutationalBurden = TumorMutationalBurden(
                6.4, false, ClinicalEvidence(emptySet(), emptySet(), emptySet())
            )
        )
        assertThat(database.find("BRAF", GeneRole.ONCO, DndsDriverType.INDEL)).isEqualTo(DndsDatabaseEntry(0.0, 4.9020157660617514E-5))
    }

    @Test
    fun `Should return null for unknown genes`() {
        assertThat(database.find("EGFR", GeneRole.TSG, DndsDriverType.NONSENSE)).isNull()
    }

    @Test
    fun `Should throw for gene role BOTH`() {
        assertThatThrownBy {
            database.find(
                "BRAF",
                GeneRole.BOTH,
                DndsDriverType.NONSENSE
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}