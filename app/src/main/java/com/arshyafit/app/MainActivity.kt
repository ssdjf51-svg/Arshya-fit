package com.arshyafit.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF080D18)
private val Panel = Color(0xFF111C2E)
private val Blue = Color(0xFF258BFF)
private val Muted = Color(0xFF9BAAC0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8,13,24)
        window.navigationBarColor = android.graphics.Color.rgb(8,13,24)
        setContent { ArshyaFitApp() }
    }
}

data class UserProfile(
    val name: String = "",
    val username: String = "",
    val age: Int = 18,
    val height: Int = 170,
    val weight: Int = 65,
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

data class Exercise(val name: String, val muscle: String, val sets: Int, val reps: String, val rest: Int, val note: String)

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
    val ageSafe = p.age < 18
    val base = when (p.style) {
        "بدنسازی" -> listOf(0,1,3,4,6,7)
        "کالیستنیکس" -> listOf(0,1,2,3,4,5)
        else -> listOf(0,1,2,3,4,6)
    }
    return base.map { exerciseBank[it] }.map {
        if (ageSafe) it.copy(sets = minOf(it.sets, 2), rest = maxOf(it.rest, 90))
        else it
    }
}

private fun loadProfile(ctx: Context): UserProfile? {
    val raw = ctx.getSharedPreferences("arshya_fit", Context.MODE_PRIVATE).getString("profile", null) ?: return null
    return runCatching {
        val j = JSONObject(raw)
        UserProfile(
            name=j.optString("name"), username=j.optString("username"), age=j.optInt("age",18),
            height=j.optInt("height",170), weight=j.optInt("weight",65), gender=j.optString("gender","پسر"),
            experience=j.optString("experience","مبتدی"), goal=j.optString("goal","عضله‌سازی"),
            style=j.optString("style","ترکیبی"), place=j.optString("place","خانه"),
            equipment=j.optString("equipment","وزن بدن"), days=j.optInt("days",3),
            minutes=j.optInt("minutes",45), limitations=j.optString("limitations")
        )
    }.getOrNull()
}
private fun saveProfile(ctx: Context, p: UserProfile) {
    val j = JSONObject().apply {
        put("name",p.name); put("username",p.username); put("age",p.age); put("height",p.height)
        put("weight",p.weight); put("gender",p.gender); put("experience",p.experience); put("goal",p.goal)
        put("style",p.style); put("place",p.place); put("equipment",p.equipment); put("days",p.days)
        put("minutes",p.minutes); put("limitations",p.limitations)
    }
    ctx.getSharedPreferences("arshya_fit", Context.MODE_PRIVATE).edit().putString("profile",j.toString()).apply()
}

@Composable
fun ArshyaFitApp() {
    val ctx = LocalContext.current
    var profile by remember { mutableStateOf(loadProfile(ctx)) }
    var tab by remember { mutableStateOf("خانه") }
    var done by remember { mutableStateOf(ctx.getSharedPreferences("arshya_fit",0).getInt("done",0)) }
    var sessions by remember { mutableStateOf(ctx.getSharedPreferences("arshya_fit",0).getInt("sessions",0)) }
    var updateMessage by remember { mutableStateOf("برای بررسی نسخه جدید، دکمه بررسی را بزن.") }
    var checking by remember { mutableStateOf(false) }
    MaterialTheme(colorScheme = darkColorScheme(
        primary=Blue, background=Navy, surface=Panel, onBackground=Color.White,
        onSurface=Color.White, secondary=Color(0xFF62B0FF)
    )) {
        Surface(Modifier.fillMaxSize(), color=Navy) {
            if (profile == null) {
                Onboarding { p -> saveProfile(ctx,p); profile=p }
            } else {
                Column(Modifier.fillMaxSize()) {
                    TopHeader(profile!!)
                    Box(Modifier.weight(1f)) {
                        when(tab) {
                            "خانه" -> HomeScreen(profile!!, done, sessions, onStart={tab="تمرین"})
                            "تمرین" -> WorkoutScreen(profile!!, done, onDone={
                                done += 1
                                ctx.getSharedPreferences("arshya_fit",0).edit().putInt("done",done).apply()
                                sessions += 1
                                ctx.getSharedPreferences("arshya_fit",0).edit().putInt("sessions",sessions).apply()
                                tab="آمار"
                            })
                            "حرکات" -> ExerciseScreen()
                            "آمار" -> StatsScreen(done,sessions)
                            "پروفایل" -> ProfileScreen(profile!!, onSave={p->saveProfile(ctx,p);profile=p}, updateMessage, checking, onCheck={
                                checking=true
                            }, onOpenGitHub={
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/YOUR_GITHUB_USERNAME/ARSHYA-FIT/releases")))
                            }, onOpenMyket={
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir")))
                            })
                        }
                    }
                    NavigationBar(containerColor=Panel) {
                        listOf("خانه","تمرین","حرکات","آمار","پروفایل").forEach { label ->
                            NavigationBarItem(
                                selected=tab==label, onClick={tab=label},
                                icon={ Icon(when(label) {
                                    "خانه"->Icons.Default.Home
                                    "تمرین"->Icons.Default.FitnessCenter
                                    "حرکات"->Icons.Default.List
                                    "آمار"->Icons.Default.BarChart
                                    else->Icons.Default.Person
                                }, contentDescription=label) },
                                label={Text(label)}
                            )
                        }
                    }
                }
                LaunchedEffect(checking) {
                    if(checking) {
                        updateMessage = withContext(Dispatchers.IO) { checkGithubUpdate() }
                        checking=false
                    }
                }
            }
        }
    }
}

private suspend fun checkGithubUpdate(): String = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/YOUR_GITHUB_USERNAME/ARSHYA-FIT/releases/latest")
        val c = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout=7000; readTimeout=7000
            setRequestProperty("Accept","application/vnd.github+json")
        }
        val code=c.responseCode
        if(code !in 200..299) return@withContext "فعلاً امکان بررسی نسخه وجود ندارد. مخزن GitHub را تنظیم کن."
        val body=c.inputStream.bufferedReader().use { it.readText() }
        val j=JSONObject(body)
        val tag=j.optString("tag_name","نسخه نامشخص")
        val notes=j.optString("body","")
        "آخرین نسخه منتشرشده: $tag\n\n$notes\n\nبرای نصب، صفحه GitHub Releases را باز کن."
    } catch(e: Exception) { "بررسی آپدیت ناموفق بود؛ اتصال اینترنت یا آدرس مخزن را بررسی کن." }
}

@Composable
fun TopHeader(p: UserProfile) {
    Row(Modifier.fillMaxWidth().background(Panel).padding(18.dp), verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("ARSHYA FIT", color=Blue, fontWeight=FontWeight.Black, fontSize=21.sp)
            Text("تمرین هوشمند، پیشرفت واقعی", color=Muted, fontSize=11.sp)
        }
        Text("سلام ${p.name.ifBlank { "ورزشکار" }} 👋", color=Color.White)
    }
}

@Composable
fun Onboarding(onFinish:(UserProfile)->Unit) {
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("18") }
    var height by remember { mutableStateOf("170") }
    var weight by remember { mutableStateOf("65") }
    var gender by remember { mutableStateOf("پسر") }
    var exp by remember { mutableStateOf("مبتدی") }
    var goal by remember { mutableStateOf("عضله‌سازی") }
    var style by remember { mutableStateOf("ترکیبی") }
    var place by remember { mutableStateOf("خانه") }
    var equipment by remember { mutableStateOf("وزن بدن") }
    var days by remember { mutableStateOf("3") }
    var minutes by remember { mutableStateOf("45") }
    var limits by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("ARSHYA FIT", color=Blue, fontSize=28.sp, fontWeight=FontWeight.Black)
        Text("پروفایل ورزشی خودت رو بساز • مرحله ${page+1} از 3", color=Muted)
        LinearProgressIndicator(progress={(page+1)/3f}, modifier=Modifier.fillMaxWidth())
        when(page) {
            0 -> {
                Text("اطلاعات پایه", fontSize=22.sp, fontWeight=FontWeight.Bold)
                Field("نام",name,{name=it})
                Field("نام کاربری",username,{username=it})
                Field("سن",age,{age=it},true)
                Field("قد (سانتی‌متر)",height,{height=it},true)
                Field("وزن (کیلوگرم)",weight,{weight=it},true)
                Choice("جنسیت",listOf("پسر","دختر"),gender){gender=it}
            }
            1 -> {
                Text("سابقه و هدف", fontSize=22.sp, fontWeight=FontWeight.Bold)
                Choice("سابقه تمرین",listOf("بدون سابقه","کمتر از ۶ ماه","۶ تا ۱۲ ماه","۱ تا ۲ سال","بیشتر"),exp){exp=it}
                Choice("سطح",listOf("مبتدی","متوسط","پیشرفته"),exp){exp=it}
                Choice("هدف اصلی",listOf("عضله‌سازی","افزایش قدرت","استقامت","تناسب اندام","مهارت کالیستنیکس"),goal){goal=it}
                Choice("نوع تمرین",listOf("بدنسازی","کالیستنیکس","ترکیبی"),style){style=it}
                Choice("محل تمرین",listOf("خانه","باشگاه","فضای باز"),place){place=it}
                Choice("تجهیزات",listOf("وزن بدن","دمبل","کش","تجهیزات باشگاه","متنوع"),equipment){equipment=it}
            }
            else -> {
                Text("برنامه و محدودیت‌ها", fontSize=22.sp, fontWeight=FontWeight.Bold)
                Choice("روز تمرین در هفته",listOf("1","2","3","4","5","6","7"),days){days=it}
                Choice("مدت هر جلسه",listOf("15","30","45","60","90","120"),minutes){minutes=it}
                Field("درد یا محدودیت حرکتی (اختیاری)",limits,{limits=it})
                Text("اگر هنگام تمرین درد داری، آن حرکت را انجام نده و از مربی یا متخصص کمک بگیر.",color=Muted,fontSize=12.sp)
                Text("خلاصه: ${name.ifBlank{"ورزشکار"}} • $age سال • $goal • $style",color=Blue)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            if(page>0) OutlinedButton(onClick={page--}, modifier=Modifier.weight(1f)){Text("قبلی")}
            Button(onClick={
                if(page<2) page++ else {
                    val p=UserProfile(name,username,age.toIntOrNull()?.coerceIn(7,100)?:18,
                        height.toIntOrNull()?.coerceIn(100,230)?:170,weight.toIntOrNull()?.coerceIn(20,250)?:65,
                        gender,exp,goal,style,place,equipment,days.toIntOrNull()?:3,minutes.toIntOrNull()?:45,limits)
                    onFinish(p)
                }
            }, modifier=Modifier.weight(1f)){Text(if(page==2)"ساخت برنامه من" else "ادامه")}
        }
    }
}

@Composable
fun Field(label:String,value:String,onChange:(String)->Unit,numeric:Boolean=false) {
    OutlinedTextField(value=value,onValueChange=onChange,label={Text(label)},singleLine=true,
        keyboardOptions=if(numeric) KeyboardOptions(keyboardType=KeyboardType.Number) else KeyboardOptions.Default,
        modifier=Modifier.fillMaxWidth(), colors=OutlinedTextFieldDefaults.colors(
            focusedBorderColor=Blue, focusedLabelColor=Blue, unfocusedBorderColor=Muted))
}
@Composable
fun Choice(label:String,options:List<String>,selected:String,onSelect:(String)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(5.dp)) {
        Text(label,color=Muted,fontSize=13.sp)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            options.take(3).forEach { item ->
                FilterChip(selected=selected==item,onClick={onSelect(item)},label={Text(item,fontSize=11.sp)})
            }
        }
        if(options.size>3) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                options.drop(3).forEach { item ->
                    FilterChip(selected=selected==item,onClick={onSelect(item)},label={Text(item,fontSize=11.sp)})
                }
            }
        }
    }
}

@Composable
fun HomeScreen(p:UserProfile,done:Int,sessions:Int,onStart:()->Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item {
            Text("داشبورد",fontSize=25.sp,fontWeight=FontWeight.Bold)
            Text("امروز برای پیشرفت آماده‌ای؟",color=Muted)
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text("برنامه امروز",color=Blue,fontWeight=FontWeight.Bold)
                    Text("${p.goal} • ${p.style}",fontSize=20.sp,fontWeight=FontWeight.Bold)
                    Text("${p.minutes} دقیقه | ${p.days} روز در هفته",color=Muted)
                    Button(onClick=onStart,modifier=Modifier.fillMaxWidth()){Text("شروع تمرین")}
                }
            }
        }
        item { Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            StatCard("جلسات ثبت‌شده",sessions.toString(),Modifier.weight(1f))
            StatCard("حرکات تکمیل‌شده",done.toString(),Modifier.weight(1f))
        }}
        item { Card(colors=CardDefaults.cardColors(containerColor=Panel)) {
            Column(Modifier.padding(16.dp)) {
                Text("یادآوری ایمنی",color=Blue,fontWeight=FontWeight.Bold)
                Text("کیفیت اجرای حرکت مهم‌تر از تعداد تکرار است. درد تیز یا مداوم نشانه توقف تمرین است.",color=Muted)
            }
        }}
    }
}
@Composable
fun StatCard(title:String,value:String,modifier:Modifier=Modifier) {
    Card(modifier,colors=CardDefaults.cardColors(containerColor=Panel)) {
        Column(Modifier.padding(16.dp)) { Text(value,fontSize=25.sp,color=Blue,fontWeight=FontWeight.Bold); Text(title,color=Muted,fontSize=12.sp) }
    }
}

@Composable
fun WorkoutScreen(p:UserProfile,done:Int,onDone:()->Unit) {
    val plan= remember(p) { makePlan(p) }
    var checked by remember { mutableStateOf(setOf<Int>()) }
    var seconds by remember { mutableIntStateOf(0) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Text("جلسه تمرینی",fontSize=24.sp,fontWeight=FontWeight.Bold); Text("حرکت‌ها را با فرم صحیح انجام بده.",color=Muted) }
        items(plan.size) { i ->
            val ex=plan[i]
            Card(colors=CardDefaults.cardColors(containerColor=Panel)) {
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(ex.name,fontWeight=FontWeight.Bold); Text(ex.muscle,color=Muted,fontSize=12.sp) }
                        Checkbox(checked=i in checked,onCheckedChange={v->checked=if(v)checked+i else checked-i})
                    }
                    Text("${ex.sets} ست • ${ex.reps} تکرار • استراحت ${ex.rest} ثانیه",color=Blue)
                    Text(ex.note,color=Muted,fontSize=12.sp)
                }
            }
        }
        item {
            Text("تکمیل ${checked.size} از ${plan.size}",color=Blue)
            LinearProgressIndicator(progress={checked.size.toFloat()/plan.size},modifier=Modifier.fillMaxWidth())
            Button(onClick=onDone,enabled=checked.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("ثبت پایان جلسه")}
        }
    }
}

@Composable
fun ExerciseScreen() {
    var query by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Text("بانک حرکات",fontSize=24.sp,fontWeight=FontWeight.Bold); Field("جست‌وجوی حرکت",query,{query=it}) }
        items(exerciseBank.filter { it.name.contains(query,true)||it.muscle.contains(query,true) }) { ex ->
            Card(colors=CardDefaults.cardColors(containerColor=Panel)) {
                Column(Modifier.padding(14.dp)) {
                    Text(ex.name,fontWeight=FontWeight.Bold); Text(ex.muscle,color=Blue)
                    Text("${ex.sets} ست • ${ex.reps}",color=Muted)
                    Text(ex.note,color=Muted,fontSize=12.sp)
                }
            }
        }
    }
}

@Composable
fun StatsScreen(done:Int,sessions:Int) {
    Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text("آمار و پیشرفت",fontSize=25.sp,fontWeight=FontWeight.Bold)
        StatCard("جلسات تکمیل‌شده",sessions.toString(),Modifier.fillMaxWidth())
        StatCard("حرکات ثبت‌شده",done.toString(),Modifier.fillMaxWidth())
        Text("آمار از زمان نصب این نسخه روی همین گوشی ذخیره می‌شود.",color=Muted)
    }
}

@Composable
fun ProfileScreen(p:UserProfile,onSave:(UserProfile)->Unit,updateMessage:String,checking:Boolean,onCheck:()->Unit,onOpenGitHub:()->Unit,onOpenMyket:()->Unit) {
    var name by remember(p) { mutableStateOf(p.name) }
    var age by remember(p) { mutableStateOf(p.age.toString()) }
    var height by remember(p) { mutableStateOf(p.height.toString()) }
    var weight by remember(p) { mutableStateOf(p.weight.toString()) }
    var goal by remember(p) { mutableStateOf(p.goal) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Text("پروفایل",fontSize=25.sp,fontWeight=FontWeight.Bold) }
        item { Field("نام",name,{name=it}); Field("سن",age,{age=it},true); Field("قد",height,{height=it},true); Field("وزن",weight,{weight=it},true) }
        item { Choice("هدف",listOf("عضله‌سازی","افزایش قدرت","استقامت","تناسب اندام"),goal){goal=it} }
        item { Button(onClick={onSave(p.copy(name=name,age=age.toIntOrNull()?:p.age,height=height.toIntOrNull()?:p.height,weight=weight.toIntOrNull()?:p.weight,goal=goal))},modifier=Modifier.fillMaxWidth()){Text("ذخیره تغییرات")} }
        item { Divider(color=Muted) }
        item { Text("به‌روزرسانی برنامه",fontSize=20.sp,fontWeight=FontWeight.Bold) }
        item { Text("نسخه نصب‌شده: 1.0.0",color=Blue) }
        item { Button(onClick=onCheck,enabled=!checking,modifier=Modifier.fillMaxWidth()){Text(if(checking)"در حال بررسی..." else "بررسی نسخه جدید")} }
        item { Text(updateMessage,color=Muted,fontSize=12.sp) }
        item { OutlinedButton(onClick=onOpenGitHub,modifier=Modifier.fillMaxWidth()){Text("دانلود از GitHub Releases")} }
        item { OutlinedButton(onClick=onOpenMyket,modifier=Modifier.fillMaxWidth()){Text("مشاهده در مایکت")} }
    }
}
