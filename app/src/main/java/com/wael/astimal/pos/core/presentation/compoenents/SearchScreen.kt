package com.wael.astimal.pos.core.presentation.compoenents

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.ObserveEffect
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.core.util.convertToString
import com.wael.astimal.pos.core.util.sharePdf
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    isSearchActive: Boolean,
    loading: Boolean,
    isNew: Boolean,
    lastModifiedDate: Long?,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onCreate: () -> Unit,
    onUpdate: () -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
    canEdit: Boolean = true,
    searchResults: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    eventFlow: Flow<UiEvent>
) {

    val context = LocalContext.current

    ObserveEffect(eventFlow, eventFlow) {
        when (it) {
            is UiEvent.ShowSnackbar -> {
                SnackbarController.sendEvent(
                    event = SnackbarEvent(
                        message = StringResource.FromResource(it.message)
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

    val fabActions = remember(isNew, loading, canEdit) {
        buildList {
            if (isNew.not()) {
                add(
                    FabAction(
                        icon = Icons.Default.FileCopy,
                        label = context.getString(R.string.new_),
                        onClick = onNew
                    )
                )
                if (canEdit) {
                    add(
                        FabAction(
                            icon = Icons.Default.Delete,
                            label = context.getString(R.string.delete),
                            onClick = onDelete
                        )
                    )
                }
            }
            if (canEdit) {
                add(
                    FabAction(
                        icon = Icons.Default.Check,
                        label = context.getString(if (isNew) R.string.create else R.string.update),
                        onClick = if (isNew) onCreate else onUpdate
                    )
                )
            }
        }
    }

    BackHandler {
        if (isSearchActive) onSearchActiveChange(false)
        else onBack()
    }
    Scaffold(
        topBar = {
            DockedSearchBar(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .semantics { traversalIndex = 0f },
                inputField = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackButton(
                            onClick = {
                                if (isSearchActive) onSearchActiveChange(false)
                                else onBack()
                            },
                        )
                        SearchBarDefaults.InputField(
                            query = query,
                            onQueryChange = onQueryChange,
                            onSearch = onSearch,
                            expanded = isSearchActive,
                            onExpandedChange = onSearchActiveChange,
                            placeholder = { Text(stringResource(R.string.search)) },
                            trailingIcon = {
                                IconButton(onClick = { onSearch(query) }) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                },
                expanded = isSearchActive,
                onExpandedChange = onSearchActiveChange,
            ) {
                AnimatedContent(loading, modifier = Modifier.padding(8.dp)) { it ->
                    if (it) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        searchResults()
                    }
                }
            }
        },
        floatingActionButton = {
            MultiActionFab(
                actions = fabActions,
                enabled = canEdit
            )
        }
    ) {
        Box(
            modifier
                .padding(it)
                .fillMaxSize()
                .semantics { isTraversalGroup = true }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mainContent()
                AnimatedVisibility(visible = !isNew && !loading) {
                    Row {
                        Text(
                            text = stringResource(R.string.last_modification_date),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = lastModifiedDate?.convertToString()
                                ?: stringResource(R.string.last_modified_date_not_available),
                        )
                    }
                }
            }
        }
    }
}