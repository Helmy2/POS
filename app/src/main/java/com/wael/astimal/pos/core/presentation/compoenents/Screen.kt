package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.wael.astimal.pos.core.presentation.snackbar.ObserveEffect
import com.wael.astimal.pos.core.presentation.snackbar.SnackbarController
import com.wael.astimal.pos.core.presentation.snackbar.SnackbarEvent
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.util.sharePdf
import kotlinx.coroutines.flow.Flow


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Screen(
    loading: Boolean,
    eventFlow: Flow<UiEvent>,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {

    val context = LocalContext.current

    ObserveEffect(eventFlow, eventFlow) {
        when (it) {
            is UiEvent.ShowSnackbar -> {
                SnackbarController.sendEvent(
                    event = SnackbarEvent(
                        message = context.getString(it.message)
                    )
                )
            }

            is UiEvent.ShareFile -> {
                sharePdf(
                    context = context,
                    uri = it.fileUri,
                    title = context.getString(it.fileTitle)
                )
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        floatingActionButton = floatingActionButton
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            AnimatedContent(loading) { it ->
                if (it) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    content()
                }
            }
        }
    }
}