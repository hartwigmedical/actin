package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class MedicationToTreatmentConverterTest {

    private val oncologicalHistory = listOf(
        createTreatmentHistoryEntry("Chemotherapy", TreatmentCategory.CHEMOTHERAPY, DrugType.ALKYLATING_AGENT, 2022, 2022),
        createTreatmentHistoryEntry("Bevacizumab", TreatmentCategory.TARGETED_THERAPY, DrugType.VEGF_ANTIBODY, 2022, 2022),
        createTreatmentHistoryEntry("Doxorubicin", TreatmentCategory.CHEMOTHERAPY, DrugType.ANTHRACYCLINE, 2021, 2022),
        createTreatmentHistoryEntry("Oxaliplatin", TreatmentCategory.CHEMOTHERAPY, DrugType.PLATINUM_COMPOUND, 2021, 2022)
    )

    private val medications = listOf(
        createMedication("Pembrolizumab", TreatmentCategory.IMMUNOTHERAPY, DrugType.PD_1_PD_L1_ANTIBODY, LocalDate.of(2023, 11, 1)),
        createMedication("Pembrolizumab", TreatmentCategory.IMMUNOTHERAPY, DrugType.PD_1_PD_L1_ANTIBODY, LocalDate.of(2023, 12, 1)),
        createMedication("Bevacizumab", TreatmentCategory.TARGETED_THERAPY, DrugType.VEGF_ANTIBODY, LocalDate.of(2022, 1, 1)),
        createMedication("Doxorubicin", TreatmentCategory.CHEMOTHERAPY, DrugType.ANTHRACYCLINE, LocalDate.of(2022, 1, 1)),
        createMedication("Oxaliplatin", TreatmentCategory.CHEMOTHERAPY, DrugType.PLATINUM_COMPOUND, LocalDate.of(2021, 12, 1))
    )

    @Test
    fun `Should convert medications not already present in treatment history to a treatment history`() {
        val medicationsToAdd = MedicationToTreatmentConverter.convert(medications, oncologicalHistory)
        assertThat(medicationsToAdd.size).isEqualTo(1)
        assertThat(medicationsToAdd.first()).isEqualTo(
            createTreatmentHistoryEntry("Pembrolizumab", TreatmentCategory.IMMUNOTHERAPY, DrugType.PD_1_PD_L1_ANTIBODY, 2023, 2023).copy(
                treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2023, stopMonth = 12),
                startYear = 2023,
                startMonth = 11,
                intents = null,
            )
        )
    }

    private fun createTreatmentHistoryEntry(
        name: String,
        category: TreatmentCategory,
        drugType: DrugType,
        startYear: Int,
        stopYear: Int,
        startMonth: Int? = null
    ): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.drugTreatment(
                    name,
                    category,
                    setOf(drugType)
                )
            ), startYear = startYear, startMonth = startMonth, stopYear = stopYear
        )
    }

    private fun createMedication(name: String, category: TreatmentCategory, drugType: DrugType, date: LocalDate): Medication {
        return TestMedicationFactory.createMinimal().copy(
            drug = Drug(
                name = name,
                category = category,
                drugTypes = setOf(drugType)
            ), name = name, atc = AtcClassification(
                anatomicalMainGroup = AtcLevel(name = "", code = ""),
                chemicalSubGroup = AtcLevel(name = "", code = ""),
                chemicalSubstance = AtcLevel(name = "", code = ""),
                pharmacologicalSubGroup = AtcLevel(name = "", code = ""),
                therapeuticSubGroup = AtcLevel(name = "", code = "")
            ), startDate = date, stopDate = date
        )
    }

}