package ua.valeriishymchuk.lobmapeditor.commands

data class ComposedCommand<T>(
    val commands: List<Command<T>>
): Command<T> {
    override fun execute(input: T): T {
        return commands.fold(input) { acc, element ->
            element.execute(acc)
        }
    }

    override fun undo(input: T): T {
        return commands.foldRight(input) { element, acc ->
            element.undo(acc)
        }
    }
}