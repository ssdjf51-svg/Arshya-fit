package com.arshyafit.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val AppBackground = Color(0xFFF5F7FB)
private val CardWhite = Color(0xFFFFFFFF)
private val PrimaryBlue = Color(0xFF0878E8)
private val SoftBlue = Color(0xFFE5F1FF)
private val MainText = Color(0xFF171A21)
private val SecondaryText = Color(0xFF8993A3)
private val SoftGray = Color(0xFFF0F2F6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(245, 247, 251)
        window.navigationBarColor = android.graphics.Color.rgb(245, 247, 251)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            window.decorView.systemUiVisibility =
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        setContent { ArshyaFitApp() }
    }
}

data class UserProfile(
    val name: String = "",
    val username: String = "",
    val age: Int = 15,
    val height: Int = 173,
    val weight: Int = 43,
    val gender: String = "پسر",
    val experience: String = "مبتدی",
    val goal: String = "عضله‌سازی",
    val style: String = "ترکیبی",
    val place: String = "خانه",
    val equipment: String = "وزن بدن",
    val days: Int = 3,
    val minutes: Int = 45,
    val limitations: String = ""
)

data class Exercise(
    val name: String,
    val muscle: String,
    val sets: Int,
    val reps: String,
    val rest: Int,
    val note: String
)

private val exerciseBank = listOf(
    Exercise("شنا سوئدی", "سینه، پشت بازو", 3, "6 تا 12", 90, "بدن در یک خط؛ اگر فرم به‌هم خورد حرکت را آسان‌تر کن."),
    Exercise("اسکوات وزن بدن", "پا و باسن", 3, "8 تا 12", 90, "زانوها هم‌جهت پنجه پا باشند."),
    Exercise("بارفیکس کمکی", "پشت و جلو بازو", 3, "3 تا 8", 120, "بدون تاب‌دادن بدن و با دامنه کنترل‌شده."),
    Exercise("پل باسن", "باسن و میان‌تنه", 2, "10 تا 15", 60, "کمر را بیش از حد گود نکن."),
    Exercise("پلانک", "میان‌تنه", 2, "15 تا 30 ثانیه", 60, "شکم را منقبض و تنفس را حفظ کن."),
    Exercise("لانج درجا", "پا", 2, "6 تا 10 هر پا", 90, "در صورت درد زانو دامنه را کم یا حرکت را حذف کن."),
    Exercise("پارویی با کش", "پشت", 3, "8 تا 12", 90, "کتف‌ها را کنترل‌شده به عقب بکش."),
    Exercise("پرس سرشانه دمبل سبک", "سرشانه", 2, "8 تا 12", 90, "فقط با وزنه‌ای که کنترل کامل داری.")
)

private fun makePlan(p: UserProfile): List<Exercise> {
    val indexes = when (p.style) {
        "بدنسازی" -> listOf(0, 1, 3, 4, 6, 7)
        "کالیستنیکس" -> listOf(0, 1, 2, 3, 4, 5)
        else -> listOf(0, 1, 2, 3, 4, 6)
    }
    return indexes.map { exerciseBank[it] }.map {
        if (p.age < 18) it.copy(sets = minOf(it.sets, 2), rest = maxOf(it.rest, 90))
        else it
    }
}

private fun loadProfile(ctx: Context): UserProfile? {
    val raw = ctx.getSharedPreferences("arshya_fit", Context.MODE_PRIVATE)
        .getString("profile", null) ?: return null
    return runCatching {
        val j = JSONObject(raw)
        UserProfile(
            name = j.optString("name"),
            username = j.optString("username"),
            age = j.optInt("age", 15),
            height = j.optInt("height", 173),
            weight = j.optInt("weight", 43),
            gender = j.optString("gender", "پسر"),
            experience = j.optString("experience", "مبتدی"),
            goal = j.optString("goal", "عضله‌سازی"),
            style = j.optString("style", "ترکیبی"),
            place = j.optString("place", "خانه"),
            equipment = j.optString("equipment", "وزن بدن"),
            days = j.optInt("days", 3),
            minutes = j.optInt("minutes", 45),
            limitations = j.optString("limitations")
        )
    }.getOrNull()
}

private fun saveProfile(ctx: Context, p: UserProfile) {
    val j = JSONObject().apply {
        put("name", p.name)
        put("username", p.username)
        put("age", p.age)
        put("height", p.height)
        put("weight", p.weight)
        put("gender", p.gender)
        put("experience", p.experience)
        put("goal", p.goal)
        put("style", p.style)
        put("place", p.place)
        put("equipment", p.equipment)
        put("days", p.days)
        put("minutes", p.minutes)
        put("limitations", p.limitations)
    }
    ctx.getSharedPreferences("arshya_fit", Context.MODE_PRIVATE)
        .edit().putString("profile", j.toString()).apply()
}

@Composable
fun ArshyaFitApp() {
    val ctx = LocalContext.current
    var profile by remember { mutableStateOf(loadProfile(ctx)) }
    var tab by remember { mutableStateOf("خانه") }
    var done by remember { mutableIntStateOf(ctx.getSharedPreferences("arshya_fit", 0).getInt("done", 0)) }
    var sessions by remember { mutableIntStateOf(ctx.getSharedPreferences("arshya_fit", 0).getInt("sessions", 0)) }
    var updateMessage by remember { mutableStateOf("برای بررسی نسخه جدید، دکمه بررسی را بزن.") }
    var checking by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PrimaryBlue,
            onPrimary = Color.White,
            background = AppBackground,
            surface = CardWhite,
            onBackground = MainText,
            onSurface = MainText,
            secondary = PrimaryBlue
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = AppBackground) {
            if (profile == null) {
                Onboarding { p ->
                    saveProfile(ctx, p)
                    profile = p
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    TopHeader(profile!!)
                    Box(Modifier.weight(1f)) {
                        when (tab) {
                            "خانه" -> HomeScreen(profile!!, done, sessions) { tab = "تمرین" }
                            "تمرین" -> WorkoutScreen(profile!!, done) {
                                done += 1
                                sessions += 1
                                ctx.getSharedPreferences("arshya_fit", 0).edit()
                                    .putInt("done", done).putInt("sessions", sessions).apply()
                                tab = "آمار"
                            }
                            "حرکات" -> ExerciseScreen()
                            "آمار" -> StatsScreen(done, sessions)
                            "پروفایل" -> ProfileScreen(
                                profile!!,
                                onSave = { p -> saveProfile(ctx, p); profile = p },
                                updateMessage = updateMessage,
                                checking = checking,
                                onCheck = { checking = true },
                                onOpenGitHub = {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ssdjf51-svg/Arshya-fit/releases")))
                                },
                                onOpenMyket = {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir")))
                                },
                                onRestartSetup = {
                                    ctx.getSharedPreferences("arshya_fit", 0).edit().remove("profile").apply()
                                    profile = null
                                }
                            )
                        }
                    }
                    NavigationBar(containerColor = CardWhite) {
                        listOf("خانه", "تمرین", "حرکات", "آمار", "پروفایل").forEach { label ->
                            NavigationBarItem(
                                selected = tab == label,
                                onClick = { tab = label },
                                icon = {
                                    Icon(
                                        when (label) {
                                            "خانه" -> Icons.Default.Home
                                            "تمرین" -> Icons.Default.FitnessCenter
                                            "حرکات" -> Icons.Default.List
                                            "آمار" -> Icons.Default.BarChart
                                            else -> Icons.Default.Person
                                        },
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = SoftBlue
                                )
                            )
                        }
                    }
                }
                LaunchedEffect(checking) {
                    if (checking) {
                        updateMessage = checkGithubUpdate()
                        checking = false
                    }
                }
            }
        }
    }
}

private suspend fun checkGithubUpdate(): String = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/ssdjf51-svg/Arshya-fit/releases/latest")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 7000
            readTimeout = 7000
            setRequestProperty("Accept", "application/vnd.github+json")
        }
        val code = connection.responseCode
        if (code !in 200..299) return@withContext "هنوز نسخه‌ای در بخش Releases منتشر نشده یا دسترسی ممکن نیست."
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(body)
        val tag = json.optString("tag_name", "نسخه نامشخص")
        val notes = json.optString("body", "")
        "آخرین نسخه منتشرشده: $tag\n\n$notes\n\nبرای دریافت، صفحه GitHub Releases را باز کن."
    } catch (_: Exception) {
        "بررسی آپدیت ناموفق بود؛ اتصال اینترنت را بررسی کن."
    }
}

@Composable
fun TopHeader(p: UserProfile) {
    Row(
        Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("ARSHYA FIT", color = PrimaryBlue, fontWeight = FontWeight.Black, fontSize = 21.sp)
            Text("تمرین هوشمند، پیشرفت واقعی", color = SecondaryText, fontSize = 11.sp)
        }
        Text("سلام ${p.name.ifBlank { "ورزشکار" }} 👋", color = MainText)
    }
}

@Composable
fun Onboarding(onFinish: (UserProfile) -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("15") }
    var height by remember { mutableStateOf("173") }
    var weight by remember { mutableStateOf("43") }
    var gender by remember { mutableStateOf("پسر") }
    var exp by remember { mutableStateOf("مبتدی") }
    var goal by remember { mutableStateOf("عضله‌سازی") }
    var style by remember { mutableStateOf("ترکیبی") }
    var place by remember { mutableStateOf("خانه") }
    var equipment by remember { mutableStateOf("وزن بدن") }
    var days by remember { mutableStateOf("3") }
    var minutes by remember { mutableStateOf("45") }
    var limits by remember { mutableStateOf("") }

    val total = 14
    val titles = listOf(
        "اسمت چیه؟", "یک نام کاربری انتخاب کن", "چند سالته؟", "قدت چقدره؟",
        "وزنت چقدره؟", "جنسیتت چیه؟", "چقدر تجربه داری؟", "هدفت چیه؟",
        "چه نوع تمرینی دوست داری؟", "کجا تمرین می‌کنی؟", "چه تجهیزاتی داری؟",
        "هفته‌ای چند روز تمرین می‌کنی؟", "هر جلسه چقدر وقت داری؟",
        "محدودیت یا آسیب‌دیدگی داری؟"
    )
    val descriptions = listOf(
        "برای شخصی‌سازی تجربه ARSHYA FIT", "این نام در پروفایلت نمایش داده می‌شود",
        "برای تنظیم مناسب‌تر برنامه تمرینی", "قدت را به سانتی‌متر وارد کن",
        "وزنت را به کیلوگرم وارد کن", "یک گزینه را انتخاب کن",
        "سطح تجربه خودت را مشخص کن", "می‌خواهی به چه چیزی برسی؟",
        "سبک مورد علاقه‌ات را انتخاب کن", "محل تمرینت را مشخص کن",
        "وسایلی که در اختیار داری را انتخاب کن", "تعداد روزهای تمرین را مشخص کن",
        "زمان مناسب هر جلسه را انتخاب کن", "این سؤال اختیاری است"
    )
    val canContinue = when (page) {
        0 -> name.isNotBlank()
        1 -> username.isNotBlank()
        2 -> (age.toIntOrNull() ?: 0) in 7..100
        3 -> (height.toIntOrNull() ?: 0) in 100..230
        4 -> (weight.toIntOrNull() ?: 0) in 20..250
        else -> true
    }

    Column(
        Modifier.fillMaxSize().background(AppBackground).padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ARSHYA FIT", color = PrimaryBlue, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("TRAIN • PROGRESS • BE STRONGER", color = SecondaryText, fontSize = 9.sp)
            }
            Text("${page + 1}/$total", color = PrimaryBlue, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { (page + 1) / total.toFloat() },
            Modifier.fillMaxWidth().height(6.dp),
            color = PrimaryBlue,
            trackColor = Color(0xFFE1E5EC)
        )
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("درباره تو", color = SecondaryText, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Text(
                titles[page],
                color = MainText,
                fontSize = 27.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(descriptions[page], color = SecondaryText, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            Text(
                when (page) {
                    0, 1 -> "👋"
                    2, 3, 4 -> "💪"
                    5 -> "🙂"
                    6 -> "🏋️"
                    7 -> "🎯"
                    8, 9, 10 -> "🔥"
                    11, 12 -> "📅"
                    else -> "🩹"
                },
                fontSize = 82.sp
            )
            Spacer(Modifier.height(22.dp))
            when (page) {
                0 -> Field("نام", name, { name = it })
                1 -> Field("نام کاربری", username, { username = it })
                2 -> Field("سن به سال", age, { age = it }, true)
                3 -> Field("قد به سانتی‌متر", height, { height = it }, true)
                4 -> Field("وزن به کیلوگرم", weight, { weight = it }, true)
                5 -> QuestionOptions(listOf("پسر", "دختر"), gender) { gender = it }
                6 -> QuestionOptions(listOf("تازه‌کار", "مبتدی", "متوسط", "حرفه‌ای"), exp) { exp = it }
                7 -> QuestionOptions(listOf("عضله‌سازی", "افزایش قدرت", "استقامت", "تناسب اندام", "مهارت کالیستنیکس"), goal) { goal = it }
                8 -> QuestionOptions(listOf("بدنسازی", "کالیستنیکس", "ترکیبی"), style) { style = it }
                9 -> QuestionOptions(listOf("خانه", "باشگاه", "فضای باز"), place) { place = it }
                10 -> QuestionOptions(listOf("وزن بدن", "دمبل", "کش", "تجهیزات باشگاه", "متنوع"), equipment) { equipment = it }
                11 -> QuestionOptions(listOf("1", "2", "3", "4", "5", "6", "7"), days) { days = it }
                12 -> QuestionOptions(listOf("15", "30", "45", "60", "90", "120"), minutes) { minutes = it }
                13 -> Field("توضیحات اختیاری", limits, { limits = it })
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (page > 0) {
                OutlinedButton(
                    onClick = { page-- },
                    Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(30.dp)
                ) { Text("سؤال قبلی") }
            }
            Button(
                onClick = {
                    if (page < total - 1) page++
                    else onFinish(
                        UserProfile(
                            name = name, username = username,
                            age = age.toIntOrNull() ?: 15,
                            height = height.toIntOrNull() ?: 173,
                            weight = weight.toIntOrNull() ?: 43,
                            gender = gender, experience = exp, goal = goal,
                            style = style, place = place, equipment = equipment,
                            days = days.toIntOrNull() ?: 3,
                            minutes = minutes.toIntOrNull() ?: 45,
                            limitations = limits
                        )
                    )
                },
                enabled = canContinue,
                Modifier.weight(2f).height(56.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(if (page == total - 1) "ساخت برنامه من" else "ادامه", fontSize = 17.sp)
            }
        }
    }
}

@Composable
fun QuestionOptions(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    val active = selected == item
                    Card(
                        onClick = { onSelect(item) },
                        Modifier.weight(1f).height(58.dp),
                        shape = RoundedCornerShape(30.dp),
                        border = if (active) androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue) else null,
                        colors = CardDefaults.cardColors(containerColor = if (active) SoftBlue else SoftGray)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                item,
                                color = if (active) PrimaryBlue else MainText,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, numeric: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            focusedLabelColor = PrimaryBlue,
            unfocusedBorderColor = Color(0xFFD6DCE5),
            focusedTextColor = MainText,
            unfocusedTextColor = MainText
        )
    )
}

@Composable
fun Choice(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = SecondaryText, fontSize = 13.sp)
        options.chunked(3).forEach { group ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                group.forEach { item ->
                    FilterChip(
                        selected = selected == item,
                        onClick = { onSelect(item) },
                        label = { Text(item, fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(p: UserProfile, done: Int, sessions: Int, onStart: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("داشبورد", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = MainText)
            Text("امروز برای پیشرفت آماده‌ای؟", color = SecondaryText)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("برنامه امروز", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    Text("${p.goal} • ${p.style}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MainText)
                    Text("${p.minutes} دقیقه | ${p.days} روز در هفته", color = SecondaryText)
                    Button(onClick = onStart, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Text("شروع تمرین")
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("جلسات ثبت‌شده", sessions.toString(), Modifier.weight(1f))
                StatCard("حرکات تکمیل‌شده", done.toString(), Modifier.weight(1f))
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(Modifier.padding(16.dp)) {
                    Text("یادآوری ایمنی", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    Text("کیفیت اجرای حرکت مهم‌تر از تعداد تکرار است. درد تیز یا مداوم نشانه توقف تمرین است.", color = SecondaryText)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = CardWhite), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(value, fontSize = 25.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            Text(title, color = SecondaryText, fontSize = 12.sp)
        }
    }
}

@Composable
fun WorkoutScreen(p: UserProfile, done: Int, onDone: () -> Unit) {
    val plan = remember(p) { makePlan(p) }
    var checked by remember { mutableStateOf(setOf<Int>()) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("جلسه تمرینی", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MainText)
            Text("حرکت‌ها را با فرم صحیح انجام بده.", color = SecondaryText)
        }
        items(plan.size) { i ->
            val ex = plan[i]
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ex.name, fontWeight = FontWeight.Bold, color = MainText)
                            Text(ex.muscle, color = SecondaryText, fontSize = 12.sp)
                        }
                        Checkbox(checked = i in checked, onCheckedChange = { value ->
                            checked = if (value) checked + i else checked - i
                        })
                    }
                    Text("${ex.sets} ست • ${ex.reps} تکرار • استراحت ${ex.rest} ثانیه", color = PrimaryBlue)
                    Text(ex.note, color = SecondaryText, fontSize = 12.sp)
                }
            }
        }
        item {
            Text("تکمیل ${checked.size} از ${plan.size}", color = PrimaryBlue)
            LinearProgressIndicator(
                progress = { if (plan.isEmpty()) 0f else checked.size.toFloat() / plan.size },
                Modifier.fillMaxWidth()
            )
            Button(
                onClick = onDone,
                enabled = checked.isNotEmpty(),
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("ثبت پایان جلسه") }
        }
    }
}

@Composable
fun ExerciseScreen() {
    var query by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("بانک حرکات", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MainText)
            Field("جست‌وجوی حرکت", query, { query = it })
        }
        items(exerciseBank.filter { it.name.contains(query, true) || it.muscle.contains(query, true) }) { ex ->
            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(ex.name, fontWeight = FontWeight.Bold, color = MainText)
                    Text(ex.muscle, color = PrimaryBlue)
                    Text("${ex.sets} ست • ${ex.reps}", color = SecondaryText)
                    Text(ex.note, color = SecondaryText, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatsScreen(done: Int, sessions: Int) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("آمار و پیشرفت", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = MainText)
        StatCard("جلسات تکمیل‌شده", sessions.toString(), Modifier.fillMaxWidth())
        StatCard("حرکات ثبت‌شده", done.toString(), Modifier.fillMaxWidth())
        Text("آمار از زمان نصب این نسخه روی همین گوشی ذخیره می‌شود.", color = SecondaryText)
    }
}

@Composable
fun ProfileScreen(
    p: UserProfile,
    onSave: (UserProfile) -> Unit,
    updateMessage: String,
    checking: Boolean,
    onCheck: () -> Unit,
    onOpenGitHub: () -> Unit,
    onOpenMyket: () -> Unit,
    onRestartSetup: () -> Unit
) {
    var name by remember(p) { mutableStateOf(p.name) }
    var age by remember(p) { mutableStateOf(p.age.toString()) }
    var height by remember(p) { mutableStateOf(p.height.toString()) }
    var weight by remember(p) { mutableStateOf(p.weight.toString()) }
    var goal by remember(p) { mutableStateOf(p.goal) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("پروفایل", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = MainText) }
        item {
            Field("نام", name, { name = it })
            Field("سن", age, { age = it }, true)
            Field("قد", height, { height = it }, true)
            Field("وزن", weight, { weight = it }, true)
        }
        item { Choice("هدف", listOf("عضله‌سازی", "افزایش قدرت", "استقامت", "تناسب اندام"), goal) { goal = it } }
        item {
            Button(
                onClick = {
                    onSave(
                        p.copy(
                            name = name,
                            age = age.toIntOrNull() ?: p.age,
                            height = height.toIntOrNull() ?: p.height,
                            weight = weight.toIntOrNull() ?: p.weight,
                            goal = goal
                        )
                    )
                },
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("ذخیره تغییرات") }
        }
        item { HorizontalDivider(color = Color(0xFFE0E5EC)) }
        item { Text("به‌روزرسانی برنامه", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MainText) }
        item { Text("نسخه نصب‌شده: 1.0.0", color = PrimaryBlue) }
        item {
            Button(onClick = onCheck, enabled = !checking, Modifier.fillMaxWidth()) {
                Text(if (checking) "در حال بررسی..." else "بررسی نسخه جدید")
            }
        }
        item { Text(updateMessage, color = SecondaryText, fontSize = 12.sp) }
        item { OutlinedButton(onClick = onOpenGitHub, Modifier.fillMaxWidth()) { Text("دانلود از GitHub Releases") } }
        item { OutlinedButton(onClick = onOpenMyket, Modifier.fillMaxWidth()) { Text("مشاهده در مایکت") } }
        item {
            OutlinedButton(onClick = onRestartSetup, Modifier.fillMaxWidth()) {
                Text("شروع دوباره پرسش‌نامه")
            }
        }
    }
}
