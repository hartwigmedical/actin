package com.hartwig.actin.molecular.evidence.curation

import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenericInhibitorFilteringTest {

    @Test
    fun `Should identify generic inhibitors`() {
        listOf(
            "ABL Inhibitor (pan)",
            "Aurka Inhibitors",
            "PI3KCA Inhibitor",
            "DDR2 inhibitor",
            "EGFR Inhibitor 2nd gen",
            "Gamma secretase inhibitor",
            "JAK1 Inhibitor - ATP competitive",
            "JAK Inhibitor (Pan) - ATP competitive",
            "PKC beta Inhibitor",
            "Tankyrase Inhibitor",
            "TrkA Receptor Inhibitor",
            "Trk Receptor Inhibitor (Pan)",
            "Immune Checkpoint Inhibitor"
        ).forEach { drugClass ->
            assertThat(GenericInhibitorFiltering.isGenericInhibitor(treatmentWithClass(drugClass)))
                .withFailMessage("Expected '%s' to be recognised as a generic inhibitor", drugClass)
                .isTrue
        }
    }

    @Test
    fun `Should not identify other therapies`() {
        listOf(
            "KRAS G12C Inhibitor",
            "FOLR1-targeted Therapy",
            "HER2 (ERBB2) Antibody",
            "HER2 (ERBB2) Immune Cell Therapy",
            "HER2 (ERBB2) Vaccine",
            "Hormone - Anti-progestins",
            "p53 Activator",
            "PD-L1/PD-1 antibody",
            "",
            null
        ).forEach { drugClass ->
            assertThat(GenericInhibitorFiltering.isGenericInhibitor(treatmentWithClass(drugClass)))
                .withFailMessage("Expected '%s' to be not be recognised as a generic inhibitor", drugClass)
                .isFalse
        }
    }

    private fun treatmentWithClass(drugClass: String?) = ImmutableTreatment.builder()
        .name("Test Treatment")
        .treatmentApproachesDrugClass(if (drugClass == null) emptyList() else listOf(drugClass))
        .build()
}
