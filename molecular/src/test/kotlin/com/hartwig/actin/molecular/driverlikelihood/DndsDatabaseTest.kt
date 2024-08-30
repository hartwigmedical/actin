package com.hartwig.actin.molecular.driverlikelihood

import com.hartwig.actin.datamodel.molecular.GeneRole
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

val TEST_ONCO_DNDS_TSV = System.getProperty("user.dir") + "/src/test/resources/interpretation/dnds_driver_likelihood_onco.tsv"
val TEST_TSG_DNDS_TSV = System.getProperty("user.dir") + "/src/test/resources/interpretation/dnds_driver_likelihood_tsg.tsv"

class DndsDatabaseTest {

    private val database = DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV)

    @Test
    fun `Should load database from onco and tsg dnd TSVs and calculate probability of non-driver`() {
        assertThat(database.find("BRAF", GeneRole.ONCO, DndsDriverType.INDEL)).isEqualTo(DndsDatabaseEntry(0.0, 8.308359783759656E-5))
        assertThat(database.find("BRAF", GeneRole.ONCO, DndsDriverType.NONSENSE)).isEqualTo(
            DndsDatabaseEntry(
                9.26041369655672E-5,
                4.0949897767694754E-4
            )
        )
        assertThat(database.find("BRAF", GeneRole.ONCO, DndsDriverType.MISSENSE)).isEqualTo(
            DndsDatabaseEntry(
                0.00524550351936585,
                0.005617584377687335
            )
        )
        assertThat(database.find("BRAF", GeneRole.ONCO, DndsDriverType.SPLICE)).isEqualTo(
            DndsDatabaseEntry(
                1.38906205448351E-4,
                6.141855786931938E-4
            )
        )
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