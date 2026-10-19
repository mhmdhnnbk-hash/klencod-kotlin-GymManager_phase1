package com.gym.manager.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.manager.ui.components.MemberRow
import com.gym.manager.ui.components.NoInsets
import com.gym.manager.viewmodel.Factories
import com.gym.manager.viewmodel.MembersViewModel
import com.gym.manager.viewmodel.StatusFilter

@Composable
fun MembersScreen(
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    vm: MembersViewModel = viewModel(factory = Factories.members)
) {
    val members by vm.members.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = NoInsets,
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "إضافة عضو")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).statusBarsPadding()) {
            Text(
                "الأعضاء",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            OutlinedTextField(
                value = query,
                onValueChange = vm::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("ابحث بالاسم أو رقم العضوية أو الهاتف") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusFilter.entries.forEach { f ->
                    FilterChip(selected = filter == f, onClick = { vm.setFilter(f) }, label = { Text(f.label) })
                }
            }
            Text(
                "${members.size} عضو",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            if (members.isEmpty()) {
                Text(
                    if (query.isBlank() && filter == StatusFilter.ALL) "لا يوجد أعضاء بعد. اضغط + لإضافة أول عضو."
                    else "لا توجد نتائج مطابقة",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(members, key = { it.m.memberId }) { ui ->
                    MemberRow(ui = ui, onClick = { onOpen(ui.m.memberId) })
                }
            }
        }
    }
}
