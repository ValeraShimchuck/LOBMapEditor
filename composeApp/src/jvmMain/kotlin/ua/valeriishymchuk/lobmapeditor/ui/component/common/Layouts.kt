package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun CenteredRow(content: @Composable RowScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, content = content)
}

@Composable
fun CenteredColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, content = content)
}