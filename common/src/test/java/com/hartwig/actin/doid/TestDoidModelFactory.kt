package com.hartwig.actin.doid;

import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.hartwig.actin.doid.config.DoidManualConfig;
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory;

import org.jetbrains.annotations.NotNull;

public final class TestDoidModelFactory {

    private TestDoidModelFactory() {
    }

    @NotNull
    public static DoidModel createMinimalTestDoidModel() {
        return create(ArrayListMultimap.create(),
                Maps.newHashMap(),
                Maps.newHashMap(),
                TestDoidManualConfigFactory.createMinimalTestDoidManualConfig());
    }

    @NotNull
    public static DoidModel createWithOneParentChild(@NotNull String parent, @NotNull String child) {
        Multimap<String, String> childToParentsMap = ArrayListMultimap.create();
        childToParentsMap.put(child, parent);

        return createWithChildToParentsMap(childToParentsMap);
    }

    @NotNull
    public static DoidModel createWithChildToParentMap(@NotNull Map<String, String> childToParentMap) {
        Multimap<String, String> childToParentsMap = ArrayListMultimap.create();
        for (Map.Entry<String, String> entry : childToParentMap.entrySet()) {
            childToParentsMap.put(entry.getKey(), entry.getValue());
        }
        return createWithChildToParentsMap(childToParentsMap);
    }

    @NotNull
    public static DoidModel createWithMainCancerTypeAndChildToParentMap(@NotNull String mainCancerDoid,
            @NotNull Map<String, String> childToParentMap) {
        Multimap<String, String> childToParentsMap = ArrayListMultimap.create();
        for (Map.Entry<String, String> entry : childToParentMap.entrySet()) {
            childToParentsMap.put(entry.getKey(), entry.getValue());
        }

        return create(childToParentsMap,
                Maps.newHashMap(),
                Maps.newHashMap(),
                TestDoidManualConfigFactory.createWithOneMainCancerDoid(mainCancerDoid));
    }

    @NotNull
    public static DoidModel createWithOneDoidAndTerm(@NotNull String doid, @NotNull String term) {
        Map<String, String> termPerDoidMap = Maps.newHashMap();
        termPerDoidMap.put(doid, term);

        Map<String, String> doidPerLowerCaseTermMap = Maps.newHashMap();
        doidPerLowerCaseTermMap.put(term.toLowerCase(), doid);

        return create(ArrayListMultimap.create(),
                termPerDoidMap,
                doidPerLowerCaseTermMap,
                TestDoidManualConfigFactory.createMinimalTestDoidManualConfig());
    }

    @NotNull
    public static DoidModel createWithDoidManualConfig(@NotNull DoidManualConfig config) {
        return create(ArrayListMultimap.create(), Maps.newHashMap(), Maps.newHashMap(), config);
    }

    @NotNull
    private static DoidModel createWithChildToParentsMap(@NotNull Multimap<String, String> childToParentsMap) {
        return create(childToParentsMap,
                Maps.newHashMap(),
                Maps.newHashMap(),
                TestDoidManualConfigFactory.createMinimalTestDoidManualConfig());
    }

    @NotNull
    private static DoidModel create(@NotNull Multimap<String, String> childToParentsMap, @NotNull Map<String, String> termPerDoidMap,
            @NotNull Map<String, String> doidPerLowerCaseTermMap, @NotNull DoidManualConfig config) {
        return new DoidModel(childToParentsMap, termPerDoidMap, doidPerLowerCaseTermMap, config);
    }
}
