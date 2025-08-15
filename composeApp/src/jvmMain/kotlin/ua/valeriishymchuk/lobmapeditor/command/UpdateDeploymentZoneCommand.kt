package ua.valeriishymchuk.lobmapeditor.command

import ua.valeriishymchuk.lobmapeditor.domain.DeploymentZone
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

class UpdateDeploymentZoneCommand(
    val deploymentZoneIndex: Int,
    val oldDeploymentZone: DeploymentZone,
    val newDeploymentZone: DeploymentZone
): Command.Hybrid {
    override fun execute(input: GameScenario.Hybrid): GameScenario.Hybrid {
        val list = input.deploymentZones.toMutableList()
        list[deploymentZoneIndex] = newDeploymentZone
        return input.copy(deploymentZones = list)
    }

    override fun undo(input: GameScenario.Hybrid): GameScenario.Hybrid {
        val list = input.deploymentZones.toMutableList()
        list[deploymentZoneIndex] = oldDeploymentZone
        return input.copy(deploymentZones = list)
    }
}