package com.hartwig.actin

import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Test

class TreatmentDatabaseTest {

    @Test
    fun `Should find existing treatment by name`() {
        assertThat(treatmentDatabase().findTreatmentByName("nonexistent")).isNull()

        val treatment = treatmentDatabase().findTreatmentByName("Capecitabine+Oxaliplatin")
        assertThat(treatment).isNotNull()
        assertThat(treatment!!.categories()).containsExactly(TreatmentCategory.CHEMOTHERAPY)
        assertThat(treatment.isSystemic).isTrue()
        assertThat((treatment as DrugTreatment).drugs).extracting(Drug::name, Drug::drugTypes).containsExactlyInAnyOrder(
            tuple("CAPECITABINE", setOf(DrugType.ANTIMETABOLITE)),
            tuple("OXALIPLATIN", setOf(DrugType.PLATINUM_COMPOUND))
        )
    }

    @Test
    fun `Should equate spaces and underscores in treatment lookups`() {
        val treatment = OtherTreatment(name = "MULTIWORD_NAME", isSystemic = false, categories = emptySet())
        val treatmentDatabase = TreatmentDatabase(emptyMap(), mapOf(treatment.name.lowercase() to treatment))
        assertThat(treatmentDatabase.findTreatmentByName("Multiword name")).isEqualTo(treatment)
    }

    @Test
    fun `Should find existing drug by name`() {
        assertThat<Drug>(treatmentDatabase().findDrugByName("nonexistent")).isNull()
        val drug = treatmentDatabase().findDrugByName("Capecitabine")
        assertThat<Drug>(drug).isNotNull()
        assertThat(drug!!.name).isEqualTo("CAPECITABINE")
        assertThat(drug.drugTypes).containsExactly(DrugType.ANTIMETABOLITE)
    }

    @Test
    fun `Should equate spaces and underscores in drug lookups`() {
        val drug = Drug(name = "MULTIWORD_NAME", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = emptySet())
        val treatmentDatabase = TreatmentDatabase(mapOf(drug.name.lowercase() to drug), emptyMap())
        assertThat(treatmentDatabase.findDrugByName("Multiword name")).isEqualTo(drug)
    }

    @Test
    fun `Should find drug by ATC code`() {
        val capecitabine = treatmentDatabase().findDrugByAtcName("Capecitabine")
        assertThat<Drug>(capecitabine).isNotNull()
        assertThat(capecitabine!!.name).isEqualTo("CAPECITABINE")
        assertThat(capecitabine.drugTypes).containsExactly(DrugType.ANTIMETABOLITE)

        val trifluridine = treatmentDatabase().findDrugByAtcName("Trifluridine, combinations")
        assertThat<Drug>(trifluridine).isNotNull()
        assertThat(trifluridine!!.name).isEqualTo("TRIFLURIDINE")
        assertThat(trifluridine.drugTypes).containsExactly(DrugType.ANTIMETABOLITE)
    }

    @Test
    fun `Should return null for unknown ATC codes`() {
        assertThat<Drug>(treatmentDatabase().findDrugByAtcName("paracetamol")).isNull()
        assertThat<Drug>(treatmentDatabase().findDrugByAtcName("temoporfin")).isNull()
    }

    @Test
    fun `Should find drug by ATC code for drugs with AND in ATC name `() {
        val pertuzumabAndTrastuzumab = treatmentDatabase().findDrugByAtcName("PERTUZUMAB AND TRASTUZUMAB")
        assertThat<Drug>(pertuzumabAndTrastuzumab).isNotNull()
        assertThat(pertuzumabAndTrastuzumab!!.name).isEqualTo("PERTUZUMAB_TRASTUZUMAB")
    }

    private fun treatmentDatabase(): TreatmentDatabase {
        return TreatmentDatabaseFactory.createFromPath(resourceOnClasspath("clinical/treatment_db"))
    }
}