package com.gym.manager.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gym.manager.domain.MemberUi
import com.gym.manager.ui.methodLabel
import com.gym.manager.ui.statusColor
import com.gym.manager.ui.statusLabel
import com.gym.manager.util.PaymentMethods

/** Screens apply insets themselves, so their Scaffolds use this. */
val NoInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)

@Composable
fun rememberAnimatedInt(target: Int): Int {
    val v by animateIntAsState(target, tween(700), label = "count")
    return v
}

/** Picture header that fades into the screen background, so text stays readable. */
@Composable
fun HeroHeader(
    @DrawableRes image: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    height: Dp = 240.dp
) {
    Box(modifier.fillMaxWidth().height(height)) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.9f
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0x66000000), Color.Transparent, MaterialTheme.colorScheme.background)
                )
            )
        )
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StatusChip(ui: MemberUi, modifier: Modifier = Modifier) {
    val c = statusColor(ui.status)
    Surface(color = c.copy(alpha = 0.16f), shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            statusLabel(ui.status, ui.daysRemaining),
            color = c,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MemberRow(ui: MemberUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier.fillMaxWidth().clip(shape).clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(statusColor(ui.status).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    ui.m.name.trim().take(1),
                    color = statusColor(ui.status),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ui.m.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    "${ui.m.memberId}  •  ${ui.m.phone}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(ui)
        }
    }
}

@Composable
fun MethodChips(selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PaymentMethods.all.forEach { m ->
            FilterChip(
                selected = selected == m,
                onClick = { onSelect(m) },
                label = { Text(methodLabel(m)) }
            )
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    danger: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmText,
                    color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
