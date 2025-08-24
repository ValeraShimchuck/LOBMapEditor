package ua.valeriishymchuk.lobmapeditor.commands

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player

@Deprecated("Not sure whether should be used at all. UpdatePlayerListCommand exists already.",level = DeprecationLevel.ERROR)
class UpdatePlayerCommand(
    val playerId: Int,
    val oldPlayer: Player,
    val newPlayer: Player
): Command.Preset {
    override fun execute(input: GameScenario.Preset): GameScenario.Preset {
        val list = input.players.toMutableList()
        list[playerId] = newPlayer
        return input.copy(players = list)
    }

    override fun undo(input: GameScenario.Preset): GameScenario.Preset {
        val list = input.players.toMutableList()
        list[playerId] = oldPlayer
        return input.copy(players = list)
    }


}