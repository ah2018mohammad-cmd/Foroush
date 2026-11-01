package ir.foroosh.calc.ui.facilities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.foroosh.calc.data.TitlesRepo
import ir.foroosh.calc.ui.nikvam.NikVamScreen
import ir.foroosh.calc.ui.shayan.ShayanScreen
import ir.foroosh.calc.ui.theme.Green600
import ir.foroosh.calc.ui.theme.Red600
import kotlinx.coroutines.delay

/**
 * تب «تسهیلات» — شامل دو محاسبه‌گر «شایان» و «نیک وام» (از نسخه ۱.۳.۰ داخل تب تسهیلات).
 * هنگام جابه‌جایی، عنوان انتخاب‌شده به‌صورت آلارم چشمک‌زن یک‌ثانیه‌ای
 * (قرمز برای تسهیلات اول / سبز برای تسهیلات دوم) فعال می‌شود.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FacilitiesScreen() {
    var sub by remember { mutableIntStateOf(0) }        // ۰ = تسهیلات اول (شایان)، ۱ = تسهیلات دوم (نیک وام)
    var flashOn by remember { mutableStateOf(false) }   // فاز روشن/خاموش چشمک
    var prevSub by remember { mutableIntStateOf(-1) }   // عدم اجرای چشمک در اولین نمایش

    // آلارم چشمک‌زن یک ثانیه‌ای هنگام انتخاب هر کدام از دو تسهیلات
    LaunchedEffect(sub) {
        if (prevSub != -1) {
            repeat(4) {              // ۴ بار روشن/خاموش × ۲۵۰ میلی‌ثانیه = یک ثانیه
                flashOn = true
                delay(125)
                flashOn = false
                delay(125)
            }
        }
        prevSub = sub
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // انتخابگر تسهیلات — عنوان‌ها از بخش «ویرایش تسهیلات و طرح‌های سپرده گذاری»
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = sub == 0,
                onClick = { sub = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text(
                    TitlesRepo.shayan,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (sub == 0) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (sub == 0 && flashOn) Red600 else Color.Unspecified
                )
            }
            SegmentedButton(
                selected = sub == 1,
                onClick = { sub = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text(
                    TitlesRepo.nikvam,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (sub == 1) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (sub == 1 && flashOn) Green600 else Color.Unspecified
                )
            }
        }

        if (sub == 0) ShayanScreen() else NikVamScreen()
    }
}
