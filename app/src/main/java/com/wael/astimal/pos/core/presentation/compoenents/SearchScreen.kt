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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.util.convertToString

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
    searchResults: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
) {
    BackHandler {
        if (isSearchActive) onSearchActiveChange(false)
        else onBack()
    }
    Box(
        modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .fillMaxSize()
            .semantics { isTraversalGroup = true }) {
        DockedSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
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
        Column(
            modifier = Modifier
                .padding(top = 64.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            mainContent()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(visible = !isNew && !loading) {
                    ElevatedButton(
                        onClick = { onNew() },
                    ) {
                        Text(stringResource(R.string.new_))
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = { onDelete() },
                        enabled = !isNew && !loading,
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                    Button(
                        onClick = {
                            if (isNew) {
                                onCreate()
                            } else {
                                onUpdate()
                            }
                        },
                        enabled = !loading,
                    ) {
                        Text(
                            stringResource(
                                if (isNew) R.string.create
                                else R.string.update
                            )
                        )
                    }
                }
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