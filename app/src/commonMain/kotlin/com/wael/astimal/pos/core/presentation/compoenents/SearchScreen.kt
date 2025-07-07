package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.util.convertToString
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.create
import pos.app.generated.resources.delete
import pos.app.generated.resources.last_modified_date_not_available
import pos.app.generated.resources.open_new
import pos.app.generated.resources.search
import pos.app.generated.resources.update
import pos.app.generated.resources.update_at

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    isSearchActive: Boolean,
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
    canSave: Boolean = true,
    searchResults: @Composable () -> Unit,
    mainContent: LazyStaggeredGridScope.() -> Unit,
) {
    val openNewString = stringResource(Res.string.open_new)
    val deleteString = stringResource(Res.string.delete)
    val createString = stringResource(Res.string.create)
    val updateString = stringResource(Res.string.update)

    val fabActions =
        remember(isNew, canEdit, openNewString, deleteString, createString, updateString) {
            buildList {
                if (isNew.not()) {
                    add(
                        FabAction(
                            icon = Icons.Default.FileCopy,
                            label = openNewString,
                            onClick = onNew
                        )
                    )
                    if (canEdit) {
                        add(
                            FabAction(
                                icon = Icons.Default.Delete,
                                label = deleteString,
                                onClick = onDelete
                            )
                        )
                    }
                }
                if (canEdit) {
                    add(
                        FabAction(
                            icon = Icons.Default.Check,
                            label = if (isNew) createString else updateString,
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

    Screen(
        topBar = {
            DockedSearchBar(
                modifier = modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp, start = 16.dp)
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
                            placeholder = { Text(stringResource(Res.string.search)) },
                            trailingIcon = {
                                IconButton(onClick = { onSearch(query) }) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                            },
                            modifier = Modifier.height(OutlinedTextFieldDefaults.MinHeight)
                                .weight(1f),
                        )
                    }
                },
                expanded = isSearchActive,
                onExpandedChange = onSearchActiveChange,
            ) {
                Box(Modifier.padding(8.dp)) {
                    searchResults()
                }
            }
        },
        floatingActionButton = {
            MultiActionFab(
                actions = fabActions, enabled = canEdit && canSave
            )
        },
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            mainContent()
            item(
                span = StaggeredGridItemSpan.FullLine,
            ) {
                AnimatedVisibility(
                    visible = !isNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = stringResource(Res.string.update_at),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = lastModifiedDate?.convertToString()
                                ?: stringResource(Res.string.last_modified_date_not_available),
                        )
                    }
                }
            }
            item { Box(Modifier.padding(FloatingActionButtonDefaults.LargeIconSize)) }
        }
    }
}