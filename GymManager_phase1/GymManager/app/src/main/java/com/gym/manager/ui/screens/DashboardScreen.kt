package com.gym.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gym.manager.ui.components.HeroHeader
import com.gym.manager.ui.components.MemberRow
import com.gym.manager.ui.components.StatCard
import com.gym.manager.ui.components.rememberAnimatedInt
import com.gym.manager.ui.theme.GymImages
import com.gym.manager.ui.theme.StatusActive
import com.gym.manager.ui.theme.StatusExpired
import com.gym.manager.ui.theme.StatusFrozen
import com.gym.manager.ui.theme.StatusSoon
import com.gym.manager.util.asMoney
import com.gym.manager.viewmodel.DashboardViewModel
import com.gym.manager.viewmodel.Factories

@Composable
fun DashboardScreen(
    onAddMember: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenMember: (String) -> Unit,
    vm: DashboardViewModel = viewModel(factory = Factories.dashboard)
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { HeroHeader(GymImages.dashboardHero, "لوحة التحكم", "نظرة سريعة على الجيم اليوم") }

        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("الأعضاء", rememberAnimatedInt(s.total).toString(), Icons.Filled.Groups, primary, Modifier.weight(1f))
                    StatCard("النشطون", rememberAnimatedInt(s.active).toString(), Icons.Filled.CheckCircle, StatusActive, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("ينتهي قريبًا", rememberAnimatedInt(s.endingSoon).toString(), Icons.Filled.HourglassBottom, StatusSoon, Modifier.weight(1f))
                    StatCard("المنتهون", rememberAnimatedInt(s.expired).toString(), Icons.Filled.EventBusy, StatusExpired, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("المجمّدون", rememberAnimatedInt(s.frozen).toString(), Icons.Filled.AcUnit, StatusFrozen, Modifier.weight(1f))
                    StatCard("إيراد الشهر", s.monthIncome.asMoney(), Icons.Filled.Payments, primary, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onAddMember,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("إضافة عضو")
                    }
                    OutlinedButton(
                        onClick = onOpenMembers,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("كل الأعضاء") }
                }
            }
        }

        item {
            Text(
                "اشتراكات تنتهي قريبًا",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
            )
        }

        if (s.soonList.isEmpty()) {
            item {
                Text(
                    "لا توجد اشتراكات قريبة من الانتهاء",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(s.soonList.take(5), key = { it.m.memberId }) { ui ->
                MemberRow(
                    ui = ui,
                    onClick = { onOpenMember(ui.m.memberId) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}
