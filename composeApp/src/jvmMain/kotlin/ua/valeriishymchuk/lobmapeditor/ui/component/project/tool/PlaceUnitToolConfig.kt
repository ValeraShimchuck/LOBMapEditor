package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import ua.valeriishymchuk.lobmapeditor.services.project.tool.PlaceUnitTool
import ua.valeriishymchuk.lobmapeditor.ui.component.common.UnitPrototypeProperties

@OptIn(ExperimentalFoundationApi::class, ExperimentalJewelApi::class)
@Composable
fun PlaceUnitToolConfig() {
    val unit by PlaceUnitTool.currentUnit.collectAsState()
    UnitPrototypeProperties(unit, {
        PlaceUnitTool.currentUnit.value = it
    })
}