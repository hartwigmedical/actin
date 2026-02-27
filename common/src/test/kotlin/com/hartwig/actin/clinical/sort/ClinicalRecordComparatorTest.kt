package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory.createMinimalTestClinicalRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalRecordComparatorTest {

    private val minimal = createMinimalTestClinicalRecord()
    
    @Test
    fun `Should sort clinical records`() {
        val record1 = withPatientId("1")
        val record2 = withPatientId("2")
        val record3 = withPatientId("3")
        val records = listOf(record2, record3, record1).sortedWith(ClinicalRecordComparator())
        assertThat(records).hasSize(3)
        assertThat(records).containsExactly(record1, record2, record3)
    }

    private fun withPatientId(patientId: String): ClinicalRecord {
        return minimal.copy(patientId = patientId)
    }
}