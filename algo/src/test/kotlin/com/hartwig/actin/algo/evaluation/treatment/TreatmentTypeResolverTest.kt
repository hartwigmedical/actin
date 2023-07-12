package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.TreatmentTypeResolver.isOfType
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TreatmentTypeResolverTest {

    @Test
    fun shouldBeAbleToResolveTypesForAllTreatmentCategoriesWithType() {
        val builder = TreatmentTestFactory.builder()
            .chemoType("chemo")
            .immunoType("immuno")
            .targetedType("targeted")
            .hormoneType("hormone")
            .radioType("radio")
            .carTType("car-t")
            .transplantType("transplant")
            .supportiveType("supportive")
            .ablationType("ablation")

        val chemo: PriorTumorTreatment = builder.categories(
            listOf(
                TreatmentCategory.CHEMOTHERAPY
            )
        ).build()
        assertTrue(isOfType(chemo, TreatmentCategory.CHEMOTHERAPY, "chemo"))
        assertFalse(isOfType(chemo, TreatmentCategory.CHEMOTHERAPY, "immuno"))

        val immuno: PriorTumorTreatment = builder.categories(
            listOf(
                TreatmentCategory.IMMUNOTHERAPY
            )
        ).build()
        assertTrue(isOfType(immuno, TreatmentCategory.IMMUNOTHERAPY, "immuno"))
        assertFalse(isOfType(immuno, TreatmentCategory.IMMUNOTHERAPY, "targeted"))

        val targeted: PriorTumorTreatment = builder.categories(
            listOf(
                TreatmentCategory.TARGETED_THERAPY
            )
        ).build()
        assertTrue(isOfType(targeted, TreatmentCategory.TARGETED_THERAPY, "targeted"))
        assertFalse(isOfType(targeted, TreatmentCategory.TARGETED_THERAPY, "hormone"))

        val hormone: PriorTumorTreatment = builder.categories(
            listOf(
                TreatmentCategory.HORMONE_THERAPY
            )
        ).build()
        assertTrue(isOfType(hormone, TreatmentCategory.HORMONE_THERAPY, "hormone"))
        assertFalse(isOfType(hormone, TreatmentCategory.HORMONE_THERAPY, "radio"))

        val radio: PriorTumorTreatment = builder.categories(
            listOf(
                TreatmentCategory.RADIOTHERAPY
            )
        ).build()
        assertTrue(isOfType(radio, TreatmentCategory.RADIOTHERAPY, "radio"))
        assertFalse(isOfType(radio, TreatmentCategory.RADIOTHERAPY, "car-t"))

        val carT: PriorTumorTreatment = builder.categories(
            listOf(
                TreatmentCategory.CAR_T
            )
        ).build()
        assertTrue(isOfType(carT, TreatmentCategory.CAR_T, "car-t"))
        assertFalse(isOfType(carT, TreatmentCategory.CAR_T, "transplant"))

        val transplant: PriorTumorTreatment = builder.categories(
            listOf(
                TreatmentCategory.TRANSPLANTATION
            )
        ).build()
        assertTrue(isOfType(transplant, TreatmentCategory.TRANSPLANTATION, "transplant"))
        assertFalse(isOfType(transplant, TreatmentCategory.TRANSPLANTATION, "supportive"))

        val supportive: PriorTumorTreatment = builder.categories(
            listOf(
                TreatmentCategory.SUPPORTIVE_TREATMENT
            )
        ).build()
        assertTrue(isOfType(supportive, TreatmentCategory.SUPPORTIVE_TREATMENT, "supportive"))
        assertFalse(isOfType(supportive, TreatmentCategory.SUPPORTIVE_TREATMENT, "ablation"))

        val ablation: PriorTumorTreatment = builder.categories(
            listOf(
                TreatmentCategory.ABLATION
            )
        ).build()
        assertTrue(isOfType(ablation, TreatmentCategory.ABLATION, "ablation"))
        assertFalse(isOfType(ablation, TreatmentCategory.ABLATION, "chemo"))
    }

    @Test
    fun shouldUseTrialAcronymForTrials() {
        val acronym = "acronym"
        val trial: PriorTumorTreatment = TreatmentTestFactory.builder().addCategories(
            TreatmentCategory.TRIAL
        ).trialAcronym(acronym).build()
        assertTrue(isOfType(trial, TreatmentCategory.TRIAL, acronym))
    }

    @Test
    fun shouldFunctionEvenWhenNoTypeIsConfiguredForTreatmentCategory() {
        val geneTherapy: PriorTumorTreatment =
            TreatmentTestFactory.builder().categories(listOf(TreatmentCategory.GENE_THERAPY)).build()
        assertFalse(isOfType(geneTherapy, TreatmentCategory.GENE_THERAPY, "gene therapy"))
    }

    @Test
    fun shouldReturnNullForTypeNotConfiguredWhenMatchingAgainstTypeCollection() {
        val treatment = TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).build()
        assertThat(TreatmentTypeResolver.matchesTypeFromCollection(treatment, TreatmentCategory.CHEMOTHERAPY, emptyList())).isNull()
    }

    @Test
    fun shouldReturnTrueForTypeThatMatchesTypeCollection() {
        val treatment = TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).chemoType("platinum").build()
        assertThat(TreatmentTypeResolver.matchesTypeFromCollection(treatment, TreatmentCategory.CHEMOTHERAPY, listOf("test", "platinum")))
            .isTrue
    }

    @Test
    fun shouldReturnFalseForTypeThatDoesNotMatchTypeCollection() {
        val treatment = TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).chemoType("platinum").build()
        assertThat(TreatmentTypeResolver.matchesTypeFromCollection(treatment, TreatmentCategory.CHEMOTHERAPY, listOf("test", "another")))
            .isFalse
    }
}