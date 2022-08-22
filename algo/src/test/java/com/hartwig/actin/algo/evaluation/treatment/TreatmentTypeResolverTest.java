package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class TreatmentTypeResolverTest {

    @Test
    public void canResolveAllTypes() {
        ImmutablePriorTumorTreatment.Builder builder = TreatmentTestFactory.builder()
                .chemoType("chemo")
                .immunoType("immuno")
                .targetedType("targeted")
                .hormoneType("hormone")
                .radioType("radio")
                .carTType("car-t")
                .transplantType("transplant")
                .supportiveType("supportive");

        PriorTumorTreatment chemo = builder.categories(Lists.newArrayList(TreatmentCategory.CHEMOTHERAPY)).build();
        assertTrue(TreatmentTypeResolver.isOfType(chemo, TreatmentCategory.CHEMOTHERAPY, "chemo"));
        assertFalse(TreatmentTypeResolver.isOfType(chemo, TreatmentCategory.CHEMOTHERAPY, "immuno"));

        PriorTumorTreatment immuno = builder.categories(Lists.newArrayList(TreatmentCategory.IMMUNOTHERAPY)).build();
        assertTrue(TreatmentTypeResolver.isOfType(immuno, TreatmentCategory.IMMUNOTHERAPY, "immuno"));
        assertFalse(TreatmentTypeResolver.isOfType(immuno, TreatmentCategory.IMMUNOTHERAPY, "targeted"));

        PriorTumorTreatment targeted = builder.categories(Lists.newArrayList(TreatmentCategory.TARGETED_THERAPY)).build();
        assertTrue(TreatmentTypeResolver.isOfType(targeted, TreatmentCategory.TARGETED_THERAPY, "targeted"));
        assertFalse(TreatmentTypeResolver.isOfType(targeted, TreatmentCategory.TARGETED_THERAPY, "hormone"));

        PriorTumorTreatment hormone = builder.categories(Lists.newArrayList(TreatmentCategory.HORMONE_THERAPY)).build();
        assertTrue(TreatmentTypeResolver.isOfType(hormone, TreatmentCategory.HORMONE_THERAPY, "hormone"));
        assertFalse(TreatmentTypeResolver.isOfType(hormone, TreatmentCategory.HORMONE_THERAPY, "radio"));

        PriorTumorTreatment radio = builder.categories(Lists.newArrayList(TreatmentCategory.RADIOTHERAPY)).build();
        assertTrue(TreatmentTypeResolver.isOfType(radio, TreatmentCategory.RADIOTHERAPY, "radio"));
        assertFalse(TreatmentTypeResolver.isOfType(radio, TreatmentCategory.RADIOTHERAPY, "car-t"));

        PriorTumorTreatment carT = builder.categories(Lists.newArrayList(TreatmentCategory.CAR_T)).build();
        assertTrue(TreatmentTypeResolver.isOfType(carT, TreatmentCategory.CAR_T, "car-t"));
        assertFalse(TreatmentTypeResolver.isOfType(carT, TreatmentCategory.CAR_T, "transplant"));

        PriorTumorTreatment transplant = builder.categories(Lists.newArrayList(TreatmentCategory.TRANSPLANTATION)).build();
        assertTrue(TreatmentTypeResolver.isOfType(transplant, TreatmentCategory.TRANSPLANTATION, "transplant"));
        assertFalse(TreatmentTypeResolver.isOfType(transplant, TreatmentCategory.TRANSPLANTATION, "supportive"));

        PriorTumorTreatment supportive = builder.categories(Lists.newArrayList(TreatmentCategory.SUPPORTIVE_TREATMENT)).build();
        assertTrue(TreatmentTypeResolver.isOfType(supportive, TreatmentCategory.SUPPORTIVE_TREATMENT, "supportive"));
        assertFalse(TreatmentTypeResolver.isOfType(supportive, TreatmentCategory.SUPPORTIVE_TREATMENT, "chemo"));
    }

    @Test
    public void canRetrieveAcronymForTrial() {
        String acronym = "acronym";

        PriorTumorTreatment trial = TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).trialAcronym(acronym).build();

        assertTrue(TreatmentTypeResolver.isOfType(trial, TreatmentCategory.TRIAL, acronym));
    }

    @Test
    public void canHandleCategoryWithoutType() {
        PriorTumorTreatment vaccine = TreatmentTestFactory.builder().categories(Lists.newArrayList(TreatmentCategory.VACCINE)).build();

        assertFalse(TreatmentTypeResolver.isOfType(vaccine, TreatmentCategory.VACCINE, "vaccine"));
    }
}