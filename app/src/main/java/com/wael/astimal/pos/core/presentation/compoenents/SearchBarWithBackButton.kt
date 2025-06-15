package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.wael.astimal.pos.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithBackButton(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
) {
    Surface(
        shape = SearchBarDefaults.dockedShape,
        color = SearchBarDefaults.colors().containerColor,
        contentColor = contentColorFor(SearchBarDefaults.colors().containerColor),
        tonalElevation = SearchBarDefaults.TonalElevation,
        shadowElevation = SearchBarDefaults.ShadowElevation,
        modifier = Modifier.Companion
            .padding(16.dp)
            .zIndex(1f)
            .width(360.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
        ) {
            BackButton(onClick = onBack)
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                expanded = true,
                onExpandedChange = {},
                placeholder = { Text(stringResource(R.string.search)) },
                trailingIcon = {
                    IconButton(onClick = { onQueryChange(query) }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                modifier = Modifier.Companion.weight(1f),
            )
        }
    }
}