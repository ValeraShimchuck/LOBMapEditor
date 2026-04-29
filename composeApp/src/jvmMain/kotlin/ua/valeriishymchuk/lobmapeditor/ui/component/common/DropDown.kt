package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.ComboBox
import org.jetbrains.jewel.ui.component.PopupManager
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer

@ExperimentalFoundationApi
@ExperimentalJewelApi
@Composable
fun <T> DropDown(
    currentValue: T,
    options: List<T>,
    elementText: (Int?, T) -> String,
    onClick: (Int, T) -> Unit,
    modifier: Modifier = Modifier//.widthIn(30.dp, 200.dp)
) {
    val popupManager = remember { PopupManager() }
    ComboBox(
        modifier = modifier,
        labelText = elementText(null, currentValue),
        popupManager = popupManager,
        popupContent = {
            VerticallyScrollableContainer {
                Column {
                    options.withIndex().forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp).onClick {
                                onClick(item.index, item.value)
                                popupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = elementText(item.index, item.value),
                            )
                        }
                    }
                }
            }
        }
    )
}

@ExperimentalFoundationApi
@ExperimentalJewelApi
@Composable
fun <T> DropDownNullable(
    currentValue: T?,
    options: List<T>,
    elementText: (Int?, T) -> String,
    onClick: (Int, T?) -> Unit,
    modifier: Modifier =  Modifier//.widthIn(30.dp, 200.dp)
) {
    DropDown(
        currentValue,
        listOf(
            listOf(null),
            options
        ).flatten(),
        { idx, value ->
            if (value == null) "None"
            else elementText(idx?.minus(1), value)
        },
        { idx, value ->
            onClick(idx - 1, value)
        },
        modifier
    )
}