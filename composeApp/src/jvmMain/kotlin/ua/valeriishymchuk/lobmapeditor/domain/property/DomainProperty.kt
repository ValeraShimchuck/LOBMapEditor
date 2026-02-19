package ua.valeriishymchuk.lobmapeditor.domain.property

import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import kotlin.reflect.KClass

// requirements for DomainProperties
// should be directly be implemented from domain class for easier access and reduce black-magic
// every DomainProperty should have its compose-component in companion object
// need a way to update itself

// what I want to do with DomainProperties:
// access



interface DomainProperty<SELF: DomainProperty<SELF>> {




}