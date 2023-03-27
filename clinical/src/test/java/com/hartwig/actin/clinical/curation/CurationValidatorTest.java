package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class CurationValidatorTest {

    @Test
    public void shouldIdentifyInvalidCancerDoidSets() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationValidator.CANCER_PARENT_DOID, "child");
        CurationValidator curationValidator = new CurationValidator(doidModel);

        Set<String> valid = Sets.newHashSet("child");
        assertTrue(curationValidator.isValidCancerDoidSet(valid));

        Set<String> generic = Sets.newHashSet(CurationValidator.GENERIC_PARENT_DOID);
        assertFalse(curationValidator.isValidCancerDoidSet(generic));

        Set<String> notAllValid = Sets.newHashSet("child", "other");
        assertFalse(curationValidator.isValidCancerDoidSet(notAllValid));
    }

    @Test
    public void shouldIdentifyInvalidGenericDoidSets() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationValidator.GENERIC_PARENT_DOID, "child");
        CurationValidator curationValidator = new CurationValidator(doidModel);

        Set<String> valid = Sets.newHashSet("child");
        assertTrue(curationValidator.isValidGenericDoidSet(valid));

        Set<String> cancer = Sets.newHashSet(CurationValidator.CANCER_PARENT_DOID);
        assertFalse(curationValidator.isValidGenericDoidSet(cancer));

        Set<String> notAllValid = Sets.newHashSet("child", "other");
        assertFalse(curationValidator.isValidGenericDoidSet(notAllValid));
    }

    @Test
    public void emptyDoidsIsInvalid() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        assertFalse(CurationValidator.hasValidDoids(Sets.newHashSet(), doidModel, "parent"));
    }
}