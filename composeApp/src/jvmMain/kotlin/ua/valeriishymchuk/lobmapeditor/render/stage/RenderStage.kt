package ua.valeriishymchuk.lobmapeditor.render.stage

import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext

interface RenderStage {

    fun RenderContext.draw0()

    fun draw(renderContext: RenderContext) {
        renderContext.draw0()
    }

}