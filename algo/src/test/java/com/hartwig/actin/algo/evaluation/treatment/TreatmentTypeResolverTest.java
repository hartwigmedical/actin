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
                .transplantType("transplant");

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
        assertFalse(TreatmentTypeResolver.isOfType(radio, TreatmentCategory.RADIOTHERAPY, "transplant"));

        PriorTumorTreatment transplant = builder.categories(Lists.newArrayList(TreatmentCategory.TRANSPLANTATION)).build();
        assertTrue(TreatmentTypeResolver.isOfType(transplant, TreatmentCategory.TRANSPLANTATION, "transplant"));
        assertFalse(TreatmentTypeResolver.isOfType(transplant, TreatmentCategory.TRANSPLANTATION, "chemo"));
    }

    @Test
    public void canHandleCategoryWithoutType() {
        PriorTumorTreatment vaccine = TreatmentTestFactory.builder().categories(Lists.newArrayList(TreatmentCategory.VACCINE)).build();

        assertFalse(TreatmentTypeResolver.isOfType(vaccine, TreatmentCategory.VACCINE, "vaccine"));
    }
}