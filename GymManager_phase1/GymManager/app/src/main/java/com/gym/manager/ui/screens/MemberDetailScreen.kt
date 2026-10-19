package com.gym.manager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.manager.data.db.PaymentEntity
import com.gym.manager.domain.MemberRules
import com.gym.manager.domain.MemberStatus
import com.gym.manager.domain.MemberUi
import com.gym.manager.ui.components.ConfirmDialog
import com.gym.manager.ui.components.MethodChips
import com.gym.manager.ui.components.NoInsets
import com.gym.manager.ui.components.StatusChip
import com.gym.manager.ui.methodLabel
import com.gym.manager.ui.paymentTypeLabel
import com.gym.manager.util.DateUtils
import com.gym.manager.util.Validators
import com.gym.manager.util.asMoney
import com.gym.manager.viewmodel.Factories
import com.gym.manager.viewmodel.MemberDetailViewModel
import com.gym.manager.viewmodel.UiEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    vm: MemberDetailViewModel = viewModel(factory = Factories.memberDetail(memberId))
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    var showRenew by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    val member = state.member

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            when (e) {
                is UiEvent.Message -> launch { snack.showSnackbar(e.text) }
                UiEvent.Renewed -> showRenew = false
                UiEvent.Deleted -> Unit // the screen closes itself when the member disappears
                UiEvent.Done -> Unit
            }
        }
    }
    // The member disappeared (e.g. deleted) — leave the screen.
    LaunchedEffect(state.loaded, member) {
        if (state.loaded && member == null) onBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = NoInsets,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text(member?.m?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(memberId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "تعديل")
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { pad ->
        if (member != null) {
            LazyColumn(
                modifier = Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { InfoCard(member) }
                item {
                    Button(
                        onClick = { showRenew = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Autorenew, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("تجديد الاشتراك")
                    }
                }
                item {
                    Text(
                        "سجل المدفوعات",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                val payments = member.full.payments.sortedWith(
                    compareByDescending<PaymentEntity> { it.date }.thenByDescending { it.id }
                )
                items(payments, key = { it.id }) { PaymentRow(it) }
            }
        }
    }

    if (showRenew && member != null) {
        RenewDialog(
            ui = member,
            onDismiss = { showRenew = false },
            onConfirm = { d, p, m -> vm.renew(d, p, m) }
        )
    }
    if (showDelete && member != null) {
        ConfirmDialog(
            title = "حذف العضو",
            message = "سيتم حذف «${member.m.name}» وكل مدفوعاته وبياناته نهائيًا. لا يمكن التراجع.",
            confirmText = "حذف",
            danger = true,
            onConfirm = { showDelete = false; vm.delete() },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun InfoCard(ui: MemberUi) {
    val remaining = when {
        ui.status == MemberStatus.FROZEN -> "مجمّد"
        ui.daysRemaining < 0 -> "تجاوز ${-ui.daysRemaining} يوم"
        ui.daysRemaining == 0 -> "ينتهي اليوم"
        else -> "${ui.daysRemaining} يوم"
    }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(ui.m.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${ui.m.memberId}  •  ${ui.m.phone}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(ui)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            InfoRow("تاريخ بداية الاشتراك", ui.m.subscribeDate)
            InfoRow("تاريخ الانتهاء", DateUtils.format(ui.endDate))
            InfoRow("المتبقي", remaining)
            InfoRow("مدة الباقة", "${ui.m.packageDays} يوم")
            InfoRow("سعر الباقة", ui.m.price.asMoney())
            InfoRow("إجمالي المدفوع", ui.totalPaid.asMoney())
            InfoRow("طريقة الدفع المفضلة", methodLabel(ui.m.preferredMethod))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun PaymentRow(p: PaymentEntity) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${paymentTypeLabel(p.type)}  •  ${methodLabel(p.method)}", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${p.date}  •  ${p.days} يوم",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(p.amount.asMoney(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun RenewDialog(
    ui: MemberUi,
    onDismiss: () -> Unit,
    onConfirm: (days: String, price: String, method: String) -> Unit
) {
    var days by remember { mutableStateOf(ui.m.packageDays.toString()) }
    var price by remember { mutableStateOf(ui.m.price.toString()) }
    var method by remember { mutableStateOf(ui.m.preferredMethod) }
    val d = Validators.parseInt(days)
    val newEnd = if (d != null && Validators.isValidDays(d)) MemberRules.renewalEnd(ui.full, d) else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تجديد اشتراك ${ui.m.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = days, onValueChange = { days = it }, singleLine = true,
                    label = { Text("عدد الأيام") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = price, onValueChange = { price = it }, singleLine = true,
                    label = { Text("السعر") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                MethodChips(selected = method, onSelect = { method = it })
                Text(
                    if (newEnd != null) "ينتهي الاشتراك الجديد في ${DateUtils.format(newEnd)}"
                    else "أدخل عدد أيام صحيحًا",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(days, price, method) }) { Text("تأكيد التجديد") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
