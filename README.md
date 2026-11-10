# ARSHYA FIT

نسخه اولیه اپلیکیشن اندرویدی فارسی برای برنامه‌ریزی تمرین بدنسازی و کالیستنیکس.

## ساخت APK با GitHub Actions
1. یک Repository جدید در GitHub بساز.
2. تمام فایل‌های این پروژه را در ریشه مخزن قرار بده.
3. به تب Actions برو.
4. Workflow با نام `Build ARSHYA FIT APK` را انتخاب کن.
5. روی `Run workflow` بزن.
6. پس از پایان موفق، در صفحه اجرای Workflow بخش Artifacts فایل `ARSHYA-FIT-debug-APK` را دانلود کن.
7. ZIP دانلودشده را باز کن و `app-debug.apk` را روی گوشی نصب کن.

## تنظیم بررسی آپدیت
در `app/src/main/java/com/arshyafit/app/MainActivity.kt` این آدرس را پیدا کن:
`https://api.github.com/repos/YOUR_GITHUB_USERNAME/ARSHYA-FIT/releases/latest`
به جای `YOUR_GITHUB_USERNAME` نام کاربری GitHub خودت را قرار بده.
سپس در `ProfileScreen` لینک Releases را هم با آدرس مخزن خودت جایگزین کن.
لینک مایکت را بعد از انتشار واقعی برنامه با آدرس صفحه محصول خودت عوض کن.

## نسخه و انتشار
برای نسخه بعدی در `app/build.gradle.kts` مقدارهای `versionCode` و `versionName` را افزایش بده.
برای انتشار عمومی باید APK را با کلید امضای Release خودت امضا کنی و کلید خصوصی را هرگز در مخزن عمومی قرار نده.

## توجه
این پروژه یک MVP قابل توسعه است، نه پیاده‌سازی نهایی تمام ۲۵۱ قابلیت. برنامه‌ساز فعلی نمونه و قانون‌محور است؛ قبل از استفاده عمومی باید بانک حرکات و قواعد تمرینی با مربی واجد صلاحیت بازبینی شوند. اطلاعات پروفایل فعلاً در SharedPreferences دستگاه ذخیره می‌شود.
