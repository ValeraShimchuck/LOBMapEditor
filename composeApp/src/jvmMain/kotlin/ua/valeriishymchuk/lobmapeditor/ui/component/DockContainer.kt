package ua.valeriishymchuk.lobmapeditor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
public fun DockContainer(
    startComponent: @Composable RowScope.(Modifier) -> Unit = {},
    endComponent: @Composable RowScope.(Modifier) -> Unit = {},
    content: @Composable ColumnScope.(Modifier) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
    ) {
        Row(
            modifier = Modifier.background(
                JewelTheme.globalColors.borders.disabled
            )
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)

            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            startComponent( Modifier.weight(0.9f))
            endComponent(Modifier)
        }
        Spacer(Modifier.height(8.dp))
        content(Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
    }
}