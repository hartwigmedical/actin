package com.hartwig.actin.clinical.sort

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createMinimalTestClinicalRecord
import org.junit.Assert
import org.junit.Test

class ClinicalRecordComparatorTest {
    @Test
    fun canSortClinicalRecords() {
        val record1 = withPatientId("1")
        val record2 = withPatientId("2")
        val record3 = withPatientId("3")
        val records: List<ClinicalRecord> = Lists.newArrayList(record2, record3, record1)
        records.sort(ClinicalRecordComparator())
        Assert.assertEquals(3, records.size.toLong())
        Assert.assertEquals(record1, records[0])
        Assert.assertEquals(record2, records[1])
        Assert.assertEquals(record3, records[2])
    }

    companion object {
        private fun withPatientId(patientId: String): ClinicalRecord {
            return ImmutableClinicalRecord.builder().from(createMinimalTestClinicalRecord()).patientId(patientId).build()
        }
    }
}