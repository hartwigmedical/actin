package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.ClinicalSummaryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalDetailsChapterTest {

    private val proper = TestReportFactory.createProperTestReport()
    
    @Test
    fun `Should provide all clinical details tables when details are provided`() {
        val tables = ClinicalDetailsChapter(proper).createClinicalDetailGenerators()
        
        assertThat(tables.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            PatientCurrentDetailsGenerator::class,
            TumorDetailsGenerator::class,
            MedicationGenerator::class,
            BloodTransfusionGenerator::class
        )
    }

    @Test
    fun `Should omit medication and bloodTransfusion tables when data not provided`() {
        val report = proper.copy(
            patientRecord = proper.patientRecord.copy(
                medications = null,
                bloodTransfusions = emptyList()
            )
        )
        val tables = ClinicalDetailsChapter(report).createClinicalDetailGenerators()
        
        assertThat(tables.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            PatientCurrentDetailsGenerator::class,
            TumorDetailsGenerator::class
        )
    }
}