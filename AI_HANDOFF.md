# AI_HANDOFF.md — حالة تسليم مشروع Atlas (Atlas Project State)

## 1. تعريف المشروع (Project Definition)

* اسم المشروع المستهدف (Target App Name): **Atlas**
* معرّف التطبيق المستهدف (Target Application ID): `com.reda6dev.atlas`
* مساحة الأسماء الداخلية (Namespace/Package): `eu.kanade.tachiyomi` (مُحفوظة للتوافق الكامل مع قاعدة البيانات والإضافات والتنزيلات)
* الأساس المستقر (Base Version): **TachiyomiSY 1.13.2**
* المسار المحلي للمستودع (Local Repository): `C:\Users\REDAKING\StudioProjects\TachiyomiSY`
* الفرع الحالي (Current Branch): `atlas-new`
* الالتزام البرمجي الحالي (Current Commit): `07197ff79` (`Add Atlas documentation index and roadmap`)
* الفرع القديم الذي يحتوي على العمل السابق: `backup-atlas-old` (التزام `015ee9faf` - مرفوع إلى GitHub ولا يُحذف حتى يستقر Atlas الجديد)
* سجل التغييرات التاريخي التفصيلي: `ATLAS_CHANGELOG.md`
* مرجع توثيق واجهة المستخدم التفصيلي: `توثيق_Atlas/واجهة_المستخدم/`

---

## 2. أين وصل Atlas الآن؟ (Current State Summary)

تمت إعادة بناء Atlas بنجاح فوق أحدث أساس مستقر من TachiyomiSY (إصدار 1.13.2) عبر الفرع `atlas-new` بدلاً من استمرار ترقيع النسخة القديمة.

### ما تم إنجازه فعليًا ومتحقق منه:

1. **تطبيق هوية التطبيق الأساسية (Identity - مكتملة):**
   * اسم التطبيق للمستخدم: `Atlas` (معرّف في `i18n/src/commonMain/moko-resources/base/strings.xml`).
   * معرّف التطبيق: `applicationId = "com.reda6dev.atlas"` (معرّف في `app/build.gradle.kts`).
   * إبقاء `namespace = "eu.kanade.tachiyomi"` كما هو دون تغيير لحفظ التوافقية مع قاعدة البيانات والتنزيلات والإضافات.

2. **دعم الإضافات والمصادر الحديثة (Extensions System - مكتمل ومتحقق):**
   * يدعم بشكل كامل وقائم قراءة مستودع Keiyoushi الحديث عبر `index.pb`.
   * عرض قائمة الإضافات وتثبيتها وتحديثها يعمل بطلاقة.
   * فتح المصادر المحلية وتصفح محتواها وقراءته يعمل بشكل سليم تمامًا.
   * تم اختبار هذه الدورة عمليًا على جهاز فعلي (**Honor CLK-LX2**).

3. **إصلاحات البناء والـ Build Logic (مكتملة ومتحققة):**
   * **FlexibleAdapter:** تم استبدال مكتبة JitPack التالفة بـ `eu.davidea:flexible-adapter:5.1.0` في `gradle/libs.versions.toml`.
   * **SQLDelight & Locale:** تم إجبار بيئة JVM في Gradle على اللغة الإنجليزية عبر `-Duser.language=en -Duser.country=US` في `gradle.properties` لمنع توليد أسماء أرقام عربية-هندية كان يتسبب في مئات أخطاء الترجمة.

4. **مرحلة الهوية البصرية (Visual Identity - مكتملة بالكامل):**
   * تحديث أيقونات التطبيق التكيفية (`ic_launcher.webp`, `ic_launcher_round.webp`, `ic_launcher_foreground.webp`) في جميع كثافات `mipmap-*dpi`.
   * إزالة المجلد `app/src/debug/res/` المتضمن لأيقونات مضللة لتوحيد الهوية بين كافة بيئات البناء.
   * تحديث شاشة البدء (Splash Screen) بالمورد المتجه `ic_tachi_splash.xml` وضبط ارتفاع الشعار إلى `110dp` ليتناسب مع شاشة بدء Android.
   * تحديث شعار Atlas في شاشة `More` عبر `LogoHeader.kt` بزيادة الحجم إلى `74dp` مع ضبط الهوامش.
   * مراجعة وتأكيد الألوان الأساسية وأنماط الرؤية في الوضع الفاتح (Light Mode)، الداكن (Dark Mode)، والأسود المطلق (AMOLED Pure Black).

5. **حل مشكلة الانهيار (Crash) عند فتح شاشة More وشعار Atlas:**
   * **المشكلة:** عند فتح شاشة `More` كان يحدث انهيار (Crash) بالخطأ `java.lang.IllegalArgumentException: Unknown command for: R` في `LogoHeader.kt` أثناء تحميل المورد `R.drawable.ic_tachi` (`app/src/main/res/drawable/ic_tachi.xml`).
   * **السبب التقني:** حجم وتعقيد `pathData` داخل ملف المتجه `ic_tachi.xml` تجاوز حد 64KB الخاص بمجمع النصوص في AAPT2 أثناء معالجة الموارد (`processDebugResources`)، مما أدى إلى اقتطاع النص وكتابة نصوص تالفة تسببت في فشل محول المسارات (PathParser) عند تشغيل Compose.
   * **الإصلاح:** تم إعادة تحويل وتبسيط الشعار باستخدام أدوات Android Studio الرسمية، وتنظيف المسارات الزائدة، وإزالة الطبقة الخفية السوداء، وتوحيد الألوان لتصبح أبيض خالص (`#ffffff`) خلفية شفافة، ليقل حجم الملف من >64KB إلى ~21KB.
   * **النتيجة:** اختفى الانهيار تمامًا، وأصبحت شاشة `More` تفتح بسلاسة وتعرِض شعار Atlas بدون أي مشاكل.

6. **توثيق واجهة المستخدم التفصيلي (UI Documentation - مكتمل):**
   * تم إنشاء مرجع متخصص تفصيلي بـ 4 ملفات داخل المجلد `توثيق_Atlas/واجهة_المستخدم/`:
     * `01_خريطة_الواجهة.md` (خريطة الشاشات والتدفقات)
     * `02_نظام_التصميم.md` (الثيمات والألوان والخطوط)
     * `03_مكونات_الواجهة.md` (المكونات القابلة لإعادة الاستخدام)
     * `04_موارد_الواجهة.md` (الأيقونات والموارد واللغات)

---

## 3. المشاكل السابقة وكيف تم حلها (Resolved Issues)

| المشكلة | السبب المباشر | الحل المطبق | الحالة |
| --- | --- | --- | --- |
| فشل تحميل الإضافات | اعتماد النسخة القديمة على `index.min.json` عدم توافقها مع Keiyoushi | الانتقال إلى أساس TachiyomiSY 1.13.2 المستقر الداعم لـ `index.pb` | منجزة ومتحققة |
| خطأ 404 في FlexibleAdapter | رابط JitPack القديم لم يعد متوفرًا | التحديث إلى `eu.davidea:flexible-adapter:5.1.0` | منجزة ومتحققة |
| أخطاء بناء SQLDelight | توليد أرقام عربية-هندية بسبب Locale النظام | إضافة `-Duser.language=en -Duser.country=US` لـ JVM في `gradle.properties` | منجزة ومتحققة |
| انهيار شاشة More (`Unknown command for: R`) | تجاوز `ic_tachi.xml` حد 64KB في AAPT2 string pool | إعادة تحويل وتحسين الشعار بـ Android Studio وتبسيطه إلى ~21KB | منجزة ومتحققة |
| تعارض أيقونات Debug | وجود ملفات قديمة في `app/src/debug/res/` | إزالة الملفات الزائدة في مجلد Debug لتوحيد الهوية | منجزة ومتحققة |

*لمزيد من التفاصيل التقنية والتاريخية السابقة، راجع `ATLAS_CHANGELOG.md`.*

---

## 4. ما تم اختباره فعليًا على جهاز (Verified On Physical Device)

تم الاختبار والتأكد عمليًا على جهاز **Honor CLK-LX2**:

* [x] بناء التطبيق الكامل (`:app:assembleDebug`).
* [x] تثبيت التطبيق وتشغيله بنجاح.
* [x] ظهور اسم التطبيق `Atlas` وأيقونة Atlas الجديدة على الشاشة الرئيسية للجهاز.
* [x] قراءة مستودع Keiyoushi الحديث وتحميل الإضافات وتثبيتها.
* [x] فتح المصادر وتصفح المانغا والقراءة.
* [x] فتح شاشة `More` والتأكد من ظهور الشعار المحدث بحجمه المضبوط بدون أي انهيار (Crash).

---

## 5. المرحلة التالية النشطة (Current Active Phase)

انتقل مشروع Atlas رسمياً إلى **المرحلة 3 — واجهة وتجربة المستخدم (UX/UI)** وفقاً لـ `ATLAS_MASTER_ROADMAP.md`.

* المرجع الرئيسي لتطوير مرحلة UX/UI هو المجلد: `توثيق_Atlas/واجهة_المستخدم/`.

---

## 6. قيود صارمة يجب الالتزام بها (Strict Constraints)

* **لا تغيّر مساحة الأسماء/الحزمة:** `eu.kanade.tachiyomi` يجب أن تبقى كما هي.
* **لا تعدّل ملفات الثوابت الحساسة:** مثل `MdConstants.kt` أو `tachiyomisy://mangadex-auth`.
* **لا تقم بأي عمليات Git تدميرية:** يمنع استخدام `git reset`, `git clean`, `git restore`, `git stash`, `rebase`, `force push` دون إذن صريح.
* **لا تعدّل ملفات الموارد المتجهة المعقدة يدويًا:** عند الحاجة لتغيير متجه، استخدم أدوات Android Studio الرسمية لتفادي أخطاء AAPT2.

---

## نقطة التوقف الحالية (Current Checkpoint)

> **الحالة:** اكتملت مرحلة الهوية الأساسية ومرحلة الهوية البصرية بالكامل. التطبيق يعمل بإنتاجية عالية مع دعم Keiyoushi `index.pb` وإصلاح انهيار `LogoHeader.kt`. تم توثيق الواجهة بالكامل في `توثيق_Atlas/واجهة_المستخدم/`. المشروع جاهز للبدء في **مرحلة UX/UI**.
>
> **على أي AI قادم:** اقرأ `PROJECT_MAP.md` و`توثيق_Atlas/واجهة_المستخدم/` وفحص الكود وGit قبل القيام بأي تعديل جديد. لا تقم بإعادة تسمية `eu.kanade.tachiyomi` ولا تقم بأي عمليات Git تدميرية.
