package ir.foroosh.calc.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.ui.theme.Vazir
import ir.foroosh.calc.ui.theme.Yellow500
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// ---------- قالب‌بندی اعداد (مطابق سبک اکسل: ارقام لاتین با جداکننده هزارگان) ----------
private val moneyFmt = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US)).apply {
    roundingMode = java.math.RoundingMode.HALF_UP
}

fun fmtMoney(v: Double): String = moneyFmt.format(v)

fun fmtPct(frac: Double): String {
    val p = kotlin.math.round(frac * 10000.0) / 100.0
    return if (p == Math.floor(p)) "${p.toLong()}%"
    else String.format(Locale.US, "%.2f", p).trimEnd('0').trimEnd('.') + "%"
}

fun digitsOnly(s: String): String = s.filter { it.isDigit() }

fun groupedDigits(s: String): String =
    if (s.isEmpty()) "" else moneyFmt.format(s.toLongOrNull() ?: 0L)

fun parseMoney(s: String): Double = s.toLongOrNull()?.toDouble() ?: 0.0

// ---------- فیلد مبلغ با جداکننده زنده ----------
@Composable
fun MoneyField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = groupedDigits(value),
        onValueChange = { raw -> onChange(digitsOnly(raw).take(15)) },
        label = { Text(label, fontFamily = Vazir) },
        supportingText = if (hint != null) {
            { Text(hint, fontFamily = Vazir, fontSize = MaterialTheme.typography.bodySmall.fontSize) }
        } else null,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            fontFamily = Vazir, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        ),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = inputColors(),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.secondary,
)

// ---------- فیلد قفل‌شده (فقط نمایش، غیرقابل ویرایش) ----------
@Composable
fun LockedField(label: String, value: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label, fontFamily = Vazir) },
        textStyle = MaterialTheme.typography.titleLarge.copy(
            fontFamily = Vazir, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// ---------- ردیف انتخاب (چیپ‌ها) ----------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceChips(
    label: String,
    options: List<T>,
    selected: T,
    format: (T) -> String = { it.toString() },
    onSelect: (T) -> Unit
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { op ->
                FilterChip(
                    selected = op == selected,
                    onClick = { onSelect(op) },
                    label = {
                        Text(
                            format(op),
                            fontFamily = Vazir,
                            fontWeight = if (op == selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
            }
        }
    }
}

// ---------- منوی بازشو ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = options.getOrNull(selectedIndex) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = Vazir) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp),
            colors = inputColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            options.forEachIndexed { i, op ->
                DropdownMenuItem(
                    text = { Text(op, fontFamily = Vazir) },
                    onClick = { onSelect(i); expanded = false }
                )
            }
        }
    }
}

// ---------- کارت بخش با هدر رنگی (سبک برگرفته از هدرهای اکسل) ----------
@Composable
fun SectionCard(
    title: String,
    accentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(width = 5.dp, height = 18.dp)
                        .background(accentColor, RoundedCornerShape(3.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ---------- ردیف نتیجه ----------
@Composable
fun ResultRow(
    label: String,
    value: String?,
    key: Boolean = false,
    highlight: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = if (key) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else null,
            fontFamily = Vazir,
            color = if (highlight) Yellow500 else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value ?: "—",
            style = if (key) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = Vazir,
            color = if (highlight) Yellow500
            else if (key) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------- کارت نتیجه اصلی ----------
@Composable
fun BigResult(label: String, value: String, useSecondary: Boolean = false) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (useSecondary) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = if (useSecondary) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = Vazir,
                fontWeight = FontWeight.ExtraBold,
                color = if (useSecondary) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
