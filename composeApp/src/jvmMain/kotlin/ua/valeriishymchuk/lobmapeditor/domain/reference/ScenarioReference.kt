package ua.valeriishymchuk.lobmapeditor.domain.reference

import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.property.DomainProperty
import kotlin.reflect.KClass

interface ScenarioReference {


    fun dereference(scenario: GameScenario<*>): DomainProperty<*>

    private fun <T: DomainProperty<*>> ensureArgumentsValidity(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>
    ){
        if (references.isEmpty()) throw IllegalArgumentException("Received empty list")
        if (!clazz.isInstance(dereference(scenario)))
            throw IllegalArgumentException("${this::class} doesn't implement $clazz")
        if (!references.all { this::class.isInstance(it) })
            throw IllegalArgumentException("References should share the same type as the caller object")
    }

    fun <T: DomainProperty<*>> duplicate0(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>
    ): Pair<Command<*>, Set<ScenarioReference>>

    fun <T: DomainProperty<*>> duplicate(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>
    ): Pair<Command<*>, Set<ScenarioReference>>{
        ensureArgumentsValidity(clazz, references, scenario)
        return duplicate0(clazz,references, scenario)
    }

    fun <T: DomainProperty<*>> delete0(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>
    ): Command<*>

    fun <T: DomainProperty<*>> delete(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>
    ): Command<*> {
        ensureArgumentsValidity(clazz, references, scenario)
        return delete0(clazz,references, scenario)
    }


    fun <T: DomainProperty<*>> update0(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>,
        updater: (T) -> T
    ): Command<*>

    fun <T: DomainProperty<*>> update(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>,
        updater: (T) -> T
    ): Command<*> {
        ensureArgumentsValidity(clazz, references, scenario)
        return update0(clazz,references, scenario, updater)
    }



    interface Preset: ScenarioReference {
        fun dereferencePreset(scenario: GameScenario.Preset): DomainProperty<*>

        private fun ensureScenario(scenario: GameScenario<*>): GameScenario.Preset {
            if (scenario !is GameScenario.Preset) throw IllegalArgumentException("This reference supports only preset.")
            return scenario
        }

        override fun dereference(scenario: GameScenario<*>): DomainProperty<*> {
            return dereferencePreset(ensureScenario(scenario))
        }

        fun <T: DomainProperty<*>> updatePreset(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario.Preset,
            updater: (T) -> T
        ): Command<*>

        override fun <T : DomainProperty<*>> update0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>,
            updater: (T) -> T
        ): Command<*> {
            return updatePreset(clazz,references,ensureScenario(scenario), updater)
        }

        fun <T: DomainProperty<*>> duplicatePreset(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario.Preset
        ): Pair<Command<*>, Set<ScenarioReference>>

        override fun <T : DomainProperty<*>> duplicate0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>
        ): Pair<Command<*>, Set<ScenarioReference>>  {
            return duplicatePreset(clazz, references, ensureScenario(scenario))
        }

        fun <T: DomainProperty<*>> deletePreset(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario.Preset
        ): Command<*>

        override fun <T : DomainProperty<*>> delete0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>
        ): Command<*> {
            return deletePreset(clazz, references, ensureScenario(scenario))
        }

    }

    interface Hybrid: ScenarioReference {
        fun dereferenceHybrid(scenario: GameScenario.Hybrid): DomainProperty<*>

        private fun ensureScenario(scenario: GameScenario<*>): GameScenario.Hybrid {
            if (scenario !is GameScenario.Hybrid) throw IllegalArgumentException("This reference supports only hybrid.")
            return scenario
        }

        override fun dereference(scenario: GameScenario<*>): DomainProperty<*> {
            return dereferenceHybrid(ensureScenario(scenario))
        }

        fun <T: DomainProperty<*>> updateHybrid(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario.Hybrid,
            updater: (T) -> T
        ): Command<*>

        override fun <T : DomainProperty<*>> update0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>,
            updater: (T) -> T
        ): Command<*> {
            return updateHybrid(clazz, references,ensureScenario(scenario), updater)
        }

        fun <T: DomainProperty<*>> duplicateHybrid(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario.Hybrid
        ): Pair<Command<*>, Set<ScenarioReference>>

        override fun <T : DomainProperty<*>> duplicate0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>
        ): Pair<Command<*>, Set<ScenarioReference>> {
            return duplicateHybrid(clazz, references, ensureScenario(scenario))
        }

        fun <T: DomainProperty<*>> deleteHybrid(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario.Hybrid
        ): Command<*>

        override fun <T : DomainProperty<*>> delete0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>
        ): Command<*> {
            return deleteHybrid(clazz, references, ensureScenario(scenario))
        }

    }

    companion object {
        fun <T: DomainProperty<*>> updateList(
            clazz: KClass<T>,
            references: Collection<ScenarioReference>,
            scenario: GameScenario<*>,
            updater: (T) -> T
        ): List<Command<*>> {
            return references.groupBy { it::class }.values.map { group ->
                val first = group.first()
                first.update(clazz, group, scenario, updater)
            }
        }

        fun duplicateList(
            references: Collection<ScenarioReference>,
            scenario: GameScenario<*>,
        ): Pair<List<Command<*>>, Set<ScenarioReference>> {
            val newReferences = mutableSetOf<ScenarioReference>()
            val commands = references.groupBy { it::class }.values.map { group ->
                val first = group.first()
                val res = first.duplicate(DomainProperty::class, group, scenario)
                newReferences.addAll(res.second)
                res.first
            }
            return commands to newReferences
        }

        fun deleteList(
            references: Collection<ScenarioReference>,
            scenario: GameScenario<*>,
        ): List<Command<*>> {
            return references.groupBy { it::class }.values.map { group ->
                val first = group.first()
                first.delete(DomainProperty::class, group, scenario)
            }
        }

    }

}