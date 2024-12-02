package com.hartwig.actin.clinical

import com.hartwig.actin.datamodel.clinical.DrugInteraction
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DrugInteractionsDatabaseTest {

    private val database: DrugInteractionsDatabase =
        DrugInteractionsDatabase.read(ResourceLocator.resourceOnClasspath("medication/drug_interactions.tsv"))

    @Test
    fun `Should return DrugInteraction from medication name`() {
        assertThat(database.curateMedicationTransporterInteractions("paracetamol")).isEqualTo(
            listOf(
                DrugInteraction(
                    DrugInteraction.Type.SUBSTRATE,
                    DrugInteraction.Strength.UNKNOWN,
                    DrugInteraction.Group.TRANSPORTER,
                    "BCRP"
                )
            )
        )
        assertThat(database.curateMedicationCypInteractions("paracetamol")).isEqualTo(
            listOf(
                DrugInteraction(
                    DrugInteraction.Type.INHIBITOR,
                    DrugInteraction.Strength.MODERATE,
                    DrugInteraction.Group.CYP,
                    "2D6"
                ),
                DrugInteraction(
                    DrugInteraction.Type.SUBSTRATE,
                    DrugInteraction.Strength.MODERATE_SENSITIVE,
                    DrugInteraction.Group.CYP,
                    "3A4"
                )
            )
        )
    }
}