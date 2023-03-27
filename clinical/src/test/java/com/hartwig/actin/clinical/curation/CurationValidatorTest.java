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
        DoidModel doidModel =
                TestDoidModelFactory.createWithOneParentChild(CurationValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID, "child");
        CurationValidator curationValidator = new CurationValidator(doidModel);

        Set<String> valid = Sets.newHashSet("child");
        assertTrue(curationValidator.isValidCancerDoidSet(valid));

        Set<String> generic = Sets.newHashSet(CurationValidator.DISEASE_DOID);
        assertFalse(curationValidator.isValidCancerDoidSet(generic));

        Set<String> notAllValid = Sets.newHashSet("child", "other");
        assertFalse(curationValidator.isValidCancerDoidSet(notAllValid));
    }

    @Test
    public void shouldIdentifyInvalidDiseaseDoidSets() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationValidator.DISEASE_DOID, "child");
        CurationValidator curationValidator = new CurationValidator(doidModel);

        Set<String> valid = Sets.newHashSet("child");
        assertTrue(curationValidator.isValidDiseaseDoidSet(valid));

        Set<String> cancer = Sets.newHashSet(CurationValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID);
        assertFalse(curationValidator.isValidDiseaseDoidSet(cancer));

        Set<String> notAllValid = Sets.newHashSet("child", "other");
        assertFalse(curationValidator.isValidDiseaseDoidSet(notAllValid));
    }

    @Test
    public void emptyDoidsIsInvalid() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        assertFalse(CurationValidator.hasValidDoids(Sets.newHashSet(), doidModel, "parent"));
    }
}