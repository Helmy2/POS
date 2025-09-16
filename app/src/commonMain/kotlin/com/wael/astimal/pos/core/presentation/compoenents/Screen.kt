package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Screen(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    floatingActionButton: @Composable () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = topBar,
        floatingActionButton = floatingActionButton
    ) {
        AnimatedContent(loading, modifier = Modifier.padding(it)) {
            if (it) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            } else {
                Column(
                    modifier = modifier
                        .imePadding()
                        .fillMaxSize(),
                    verticalArrangement = verticalArrangement,
                    horizontalAlignment = horizontalAlignment
                ) {
                    content()
                }
            }
        }
    }
}