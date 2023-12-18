package com.hartwig.actin.doid

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Maps
import com.google.common.collect.Multimap
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory.createMinimalTestDoidManualConfig
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory.createWithOneMainCancerDoid
import java.util.*

object TestDoidModelFactory {
    fun createMinimalTestDoidModel(): DoidModel {
        return create(
            ArrayListMultimap.create(),
            Maps.newHashMap(),
            Maps.newHashMap(),
            createMinimalTestDoidManualConfig()
        )
    }

    fun createWithOneParentChild(parent: String, child: String): DoidModel {
        val childToParentsMap: Multimap<String, String> = ArrayListMultimap.create()
        childToParentsMap.put(child, parent)
        return createWithChildToParentsMap(childToParentsMap)
    }

    fun createWithChildToParentMap(childToParentMap: Map<String, String>): DoidModel {
        val childToParentsMap: Multimap<String, String> = ArrayListMultimap.create()
        for ((key, value) in childToParentMap) {
            childToParentsMap.put(key, value)
        }
        return createWithChildToParentsMap(childToParentsMap)
    }

    fun createWithMainCancerTypeAndChildToParentMap(
        mainCancerDoid: String,
        childToParentMap: Map<String, String>
    ): DoidModel {
        val childToParentsMap: Multimap<String, String> = ArrayListMultimap.create()
        for ((key, value) in childToParentMap) {
            childToParentsMap.put(key, value)
        }
        return create(
            childToParentsMap,
            Maps.newHashMap(),
            Maps.newHashMap(),
            createWithOneMainCancerDoid(mainCancerDoid)
        )
    }

    fun createWithOneDoidAndTerm(doid: String, term: String): DoidModel {
        val termPerDoidMap: MutableMap<String, String> = Maps.newHashMap()
        termPerDoidMap[doid] = term
        val doidPerLowerCaseTermMap: MutableMap<String, String> = Maps.newHashMap()
        doidPerLowerCaseTermMap[term.lowercase(Locale.getDefault())] = doid
        return create(
            ArrayListMultimap.create(),
            termPerDoidMap,
            doidPerLowerCaseTermMap,
            createMinimalTestDoidManualConfig()
        )
    }

    fun createWithDoidManualConfig(config: DoidManualConfig): DoidModel {
        return create(ArrayListMultimap.create(), Maps.newHashMap(), Maps.newHashMap(), config)
    }

    private fun createWithChildToParentsMap(childToParentsMap: Multimap<String, String>): DoidModel {
        return create(
            childToParentsMap,
            Maps.newHashMap(),
            Maps.newHashMap(),
            createMinimalTestDoidManualConfig()
        )
    }

    private fun create(
        childToParentsMap: Multimap<String, String>, termPerDoidMap: Map<String, String>,
        doidPerLowerCaseTermMap: Map<String, String>, config: DoidManualConfig
    ): DoidModel {
        return DoidModel(childToParentsMap, termPerDoidMap, doidPerLowerCaseTermMap, config)
    }
}
