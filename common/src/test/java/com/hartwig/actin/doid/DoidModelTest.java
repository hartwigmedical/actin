package com.hartwig.actin.doid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DoidModelTest {

    @Test
    public void canIncludeParents() {
        DoidModel model = createTestModel();

        Set<String> withParents200 = model.doidWithParents("200");
        assertEquals(3, withParents200.size());
        assertTrue(withParents200.contains("200"));
        assertTrue(withParents200.contains("300"));
        assertTrue(withParents200.contains("400"));

        Set<String> withParents300 = model.doidWithParents("300");
        assertEquals(2, withParents300.size());
        assertFalse(withParents300.contains("200"));
        assertTrue(withParents300.contains("300"));
        assertTrue(withParents300.contains("400"));

        Set<String> withParents400 = model.doidWithParents("400");
        assertEquals(1, withParents400.size());
        assertFalse(withParents400.contains("200"));
        assertFalse(withParents400.contains("300"));
        assertTrue(withParents400.contains("400"));

        assertEquals(1, model.doidWithParents("500").size());
    }

    @Test
    public void canResolveMainCancerTypes() {
        String firstMainCancerType = DoidMainCancerTypesConfig.MAIN_CANCER_TYPES.iterator().next();
        String child = "012345";
        assertFalse(DoidMainCancerTypesConfig.MAIN_CANCER_TYPES.contains(child));

        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(firstMainCancerType, child);

        Set<String> mainCancerTypes = doidModel.mainCancerTypes(child);
        assertFalse(mainCancerTypes.isEmpty());
        assertTrue(mainCancerTypes.contains(firstMainCancerType));

        assertTrue(doidModel.mainCancerTypes("does not exist").isEmpty());
    }

    @Test
    public void canResolveTerms() {
        DoidModel model = createTestModel();

        assertEquals("tumor a", model.resolveTermForDoid("200"));
        assertNull(model.resolveTermForDoid("300"));
    }

    @Test
    public void canResolveDoids() {
        DoidModel model = createTestModel();

        assertEquals("200", model.resolveDoidForTerm("tumor a"));
        assertEquals("200", model.resolveDoidForTerm("tumor A"));
        assertEquals("200", model.resolveDoidForTerm("Tumor A"));
        assertNull(model.resolveDoidForTerm("tumor B"));
    }

    @NotNull
    private static DoidModel createTestModel() {
        Multimap<String, String> relations = ArrayListMultimap.create();
        relations.put("200", "300");
        relations.put("300", "400");

        Map<String, String> termPerDoidMap = Maps.newHashMap();
        termPerDoidMap.put("200", "tumor a");

        Map<String, String> doidPerTermMap = Maps.newHashMap();
        doidPerTermMap.put("tumor a", "200");

        return new DoidModel(relations, termPerDoidMap, doidPerTermMap);
    }
}