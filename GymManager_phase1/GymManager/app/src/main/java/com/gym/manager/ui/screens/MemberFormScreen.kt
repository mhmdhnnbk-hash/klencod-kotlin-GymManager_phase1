package com.gym.manager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.manager.ui.components.ConfirmDialog
import com.gym.manager.ui.components.MethodChips
import com.gym.manager.ui.components.NoInsets
import com.gym.manager.util.DateUtils
import com.gym.manager.viewmodel.Factories
import com.gym.manager.viewmodel.MemberFormViewModel
import com.gym.manager.viewmodel.UiEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberFormScreen(
    editId: String?,
    onBack: () -> Unit,
    vm: MemberFormViewModel = viewModel(factory = Factories.memberForm(editId))
) {
    val s by vm.state.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }
    val isEdit = editId != null

    LaunchedEffect(Unit) {
        vm.events.collect { if (it == UiEvent.Done) onBack() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = NoInsets,
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "تعديل بيانات العضو" else "إضافة عضو") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = s.name, onValueChange = { v -> vm.update { it.copy(name = v) } },
                label = { Text("الاسم") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.memberId, onValueChange = { v -> vm.update { it.copy(memberId = v) } },
                label = { Text("رقم العضوية (حروف إنجليزية وأرقام)") }, singleLine = true,
                enabled = !isEdit, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )
            OutlinedTextField(
                value = s.phone, onValueChange = { v -> vm.update { it.copy(phone = v) } },
                label = { Text("رقم الهاتف") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            if (!isEdit) {
                OutlinedTextField(
                    value = s.days, onValueChange = { v -> vm.update { it.copy(days = v) } },
                    label = { Text("مدة الاشتراك (أيام)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = s.price, onValueChange = { v -> vm.update { it.copy(price = v) } },
                    label = { Text("السعر") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = DateUtils.format(s.date), onValueChange = {}, readOnly = true,
                    label = { Text("تاريخ الاشتراك") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showPicker = true }) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = "اختيار التاريخ")
                        }
                    }
                )
                Text("طريقة الدفع", style = MaterialTheme.typography.labelLarge)
                MethodChips(selected = s.method, onSelect = { m -> vm.update { it.copy(method = m) } })
            }

            s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { vm.submit(false) },
                enabled = !s.busy && s.loaded,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(if (isEdit) "حفظ التعديلات" else "حفظ العضو") }
        }
    }

    if (showPicker) {
        val endOfToday = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() - 1
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = s.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                // a subscription cannot start in the future
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= endOfToday
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        val picked = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                        vm.update { it.copy(date = picked) }
                    }
                    showPicker = false
                }) { Text("اختيار") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("إلغاء") } }
        ) { DatePicker(state = pickerState) }
    }

    s.confirm?.let { msg ->
        ConfirmDialog(
            title = "رقم هاتف مكرر",
            message = msg,
            confirmText = "متابعة",
            onConfirm = { vm.submit(true) },
            onDismiss = vm::dismissConfirm
        )
    }
}
