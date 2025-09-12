package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.search

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithBackButton(
    screenTitle: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        Surface(
            shape = SearchBarDefaults.dockedShape,
            color = SearchBarDefaults.colors().containerColor,
            contentColor = contentColorFor(SearchBarDefaults.colors().containerColor),
            tonalElevation = SearchBarDefaults.TonalElevation,
            shadowElevation = SearchBarDefaults.ShadowElevation,
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .zIndex(1f)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
            ) {
                BackButton(onClick = onBack)
                SearchBarDefaults.InputField(
                    modifier = modifier.height(OutlinedTextFieldDefaults.MinHeight).weight(1f),
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    expanded = true,
                    onExpandedChange = {},
                    placeholder = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.search))
                            Text(screenTitle)
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = { onQueryChange(query) }) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    },
                )
            }
        }
    }
}