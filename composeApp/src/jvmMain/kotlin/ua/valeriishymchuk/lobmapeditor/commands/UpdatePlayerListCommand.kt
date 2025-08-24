package ua.valeriishymchuk.lobmapeditor.commands

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player

class UpdatePlayerListCommand(
    val oldList: List<Player>,
    val newList: List<Player>,
): Command.Preset {
    override fun execute(input: GameScenario.Preset): GameScenario.Preset {
        return input.copy(players = newList)
    }

    override fun undo(input: GameScenario.Preset): GameScenario.Preset {
        return input.copy(players = oldList)
    }


}