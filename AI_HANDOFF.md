# AI_HANDOFF.md — حالة مشروع Atlas

## 1. تعريف المشروع

* اسم المشروع المستهدف: **Atlas**
* الأساس: **TachiyomiSY 1.13.2**
* المستودع الحالي: `C:\Users\REDAKING\StudioProjects\TachiyomiSY`
* الفرع الحالي: `atlas-new`
* Commit الأساس:
  `96f96c644` — `Release 1.13.2`
* الفرع القديم الذي يحتوي على العمل السابق:
  `backup-atlas-old`
* Commit العمل القديم:
  `015ee9faf`
* الفرع القديم مرفوع إلى GitHub.
* **لا تحذف `backup-atlas-old` حتى يستقر Atlas الجديد.**

---

## 2. الهدف الحالي

إعادة بناء Atlas فوق أحدث أساس مستقر من TachiyomiSY، ثم إعادة تطبيق التخصيصات المطلوبة تدريجيًا وبشكل آمن.

الهدف النهائي لهوية التطبيق:

* Application ID:
  `com.reda6dev.atlas`
* اسم التطبيق:
  `Atlas`

لكن **هوية Atlas لم تُطبّق بعد**.

الحالة الحالية ما زالت تستخدم هوية TachiyomiSY الأصلية.

---

## 3. حالة مشكلة Extensions / Index

كانت هناك مشكلة أساسية في نظام الإضافات:

* مستودع Keiyoushi الحديث يستخدم:
  `index.pb`
* النسخة القديمة من Atlas كانت تعتمد على:
  `index.min.json`
* النظام القديم لم يكن متوافقًا بشكل كامل مع نظام ExtensionStore الحديث.
* محاولة ترقيع الفرع القديم لم تكن كافية بسبب اختلافات `extensions-lib` و`tachiyomix`.

### القرار

تم إنشاء `atlas-new` انطلاقًا من أحدث TachiyomiSY الرسمي بدل الاستمرار في ترقيع النظام القديم.

### النتيجة

تم بناء التطبيق واختباره فعليًا.

تم اختبار:

* مستودع Keiyoushi باستخدام `index.pb`
* تحميل الإضافات
* فتح عدة مصادر
* استخدام المصادر

**المشكلة الأساسية الخاصة بـ index.pb وExtensions أصبحت محلولة.**

لا تعُد إلى نظام `index.min.json` القديم إلا إذا ظهر سبب تقني واضح ومدعوم باختبار.

---

## 4. التعديلات الحالية المقصودة

### `gradle/libs.versions.toml`

تم استبدال FlexibleAdapter القديم الذي كان يشير إلى JitPack:

```text
com.github.arkon.FlexibleAdapter:c8013533
```

بـ:

```text
eu.davidea:flexible-adapter:5.1.0
```

السبب:

نسخة JitPack القديمة أصبحت ترجع 404 أثناء البناء.

هذا التعديل **مقصود ولا يجب التراجع عنه تلقائيًا**.

---

### `gradle.properties`

تمت إضافة:

```text
-Duser.language=en -Duser.country=US
```

إلى `org.gradle.jvmargs`.

القيمة الحالية المقصودة:

```text
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US
```

السبب:

SQLDelight كان يولّد أسماء Kotlin تحتوي على أرقام عربية-هندية بسبب إعدادات اللغة/الـlocale، مما تسبب في مئات أخطاء Kotlin.

بعد إضافة إعدادات اللغة الإنجليزية نجح:

```text
:data:compileDebugKotlin
```

ثم نجح بناء التطبيق كاملًا.

**لا تحذف هذه الإضافة دون اختبار بديل.**

---

## 5. PROJECT_MAP.md

تم تحديث:

```text
PROJECT_MAP.md
```

اعتمادًا على بنية المشروع الحالية فعليًا.

الخريطة الحالية توثق:

* Modules
* Clean Architecture
* App / Domain / Data / Presentation
* ExtensionStore
* Extensions
* UI
* Database
* SQLDelight
* WorkManager
* `exh/`
* Gradle build logic
* Version catalogs
* Themes
* Caching
* Sync
* Backup/Restore
* Localization
* علاقات الأنظمة الرئيسية

استخدم `PROJECT_MAP.md` لتقليل استكشاف المشروع، لكن **تحقق دائمًا من الكود الحالي قبل تعديل أي شيء**.

الكود وGit هما مصدر الحقيقة النهائي.

---

## 6. الهوية الحالية

### Application ID الحالي

```text
eu.kanade.tachiyomi.sy
```

### المطلوب لاحقًا

```text
com.reda6dev.atlas
```

### اسم التطبيق الحالي

```text
TachiyomiSY
```

### المطلوب لاحقًا

```text
Atlas
```

### Namespace

يبقى حاليًا:

```text
eu.kanade.tachiyomi
```

**لا تغيّر namespace أو package إلى Atlas.**

المطلوب حاليًا هو تغيير هوية التطبيق فقط، وليس إعادة تسمية كامل الكود.

---

## 7. قيود مهمة جدًا

لا تقم بهذه التغييرات بدون طلب صريح:

### لا تغيّر

```text
eu.kanade.tachiyomi
```

كـ namespace/package.

### لا تعدّل

```text
MdConstants.kt
```

### لا تغيّر

```text
tachiyomisy://mangadex-auth
```

### لا تعدّل ملفات build الناتجة

مثل:

```text
app/build/
data/build/
```

وأي ملفات generated.

### لا تستخدم عمليات Git تدميرية بدون موافقة صريحة

خصوصًا:

```text
git reset --hard
git clean -fd
git checkout -- .
git restore .
git stash
rebase
force push
```

إذا كان هناك سبب حقيقي لاستخدام واحدة منها، اشرح السبب وانتظر موافقة المستخدم.

---

## 8. الاختبار السابق

بعد إصلاح Gradle وSQLDelight:

* تم بناء المشروع بنجاح.
* البناء الكامل نجح.
* تم تثبيت التطبيق على جهاز Honor CLK-LX2.
* تم تشغيل التطبيق.
* تم اختبار مستودع Keiyoushi الحديث.
* تم اختبار عدة Extensions/Sources.
* المصادر عملت بشكل صحيح.

هذه النتائج تم التحقق منها عمليًا، وليست مجرد تقرير من AI.

---

## 9. Git Status الحالي

آخر حالة معروفة:

```text
AM PROJECT_MAP.md
 M gradle.properties
 M gradle/libs.versions.toml
?? gradle.properties.bak
```

المعاني المهمة:

* `PROJECT_MAP.md` تم تحديثه.
* `gradle.properties` يحتوي على تعديل locale المقصود.
* `gradle/libs.versions.toml` يحتوي على إصلاح FlexibleAdapter المقصود.
* `gradle.properties.bak` ملف backup غير متتبع.

**لا تحذف `gradle.properties.bak` تلقائيًا.**

قبل أي Commit جديد، افحص:

```powershell
git status --short
git diff
git diff --cached
```

ولا تفترض أن حالة Git الحالية ما زالت نفسها إذا تغيرت لاحقًا.

---

## 10. المهمة التالية

المهمة التالية المخطط لها:

### تطبيق هوية Atlas

تغيير:

```text
applicationId = "eu.kanade.tachiyomi.sy"
```

إلى:

```text
applicationId = "com.reda6dev.atlas"
```

وتغيير:

```xml
<string name="app_name" translatable="false">TachiyomiSY</string>
```

إلى:

```xml
<string name="app_name" translatable="false">Atlas</string>
```

مع الحفاظ على:

```text
namespace = "eu.kanade.tachiyomi"
```

ثم:

1. مراجعة `git diff`
2. بناء التطبيق
3. تثبيته
4. تشغيله
5. اختبار Extensions/Sources
6. التأكد من عدم كسر أي Deep Links أو وظائف موجودة
7. فحص Git status/diff

لا تنفذ تغيير الهوية تلقائيًا لمجرد قراءة هذا الملف؛ هذا الملف يصف الحالة والخطة فقط.

---

## 11. طريقة العمل المطلوبة لأي AI

قبل تعديل الكود:

1. اقرأ:

  * `ATLAS_AI_HANDOFF_PROMPT.md`
  * `PROJECT_MAP.md`
  * `AI_HANDOFF.md`

2. افحص Git:

```powershell
git status --short
git branch --show-current
git log -1 --oneline
git remote -v
```

3. افحص الملفات الفعلية المتعلقة بالمهمة.

4. قارن الوثائق مع الكود الحالي.

5. لا تعتمد على تقرير AI سابق باعتباره حقيقة نهائية.

6. نفّذ أقل تغيير ممكن لتحقيق المطلوب.

7. اعرض `git diff` بعد التعديل.

8. ابنِ المشروع واختبره إذا كان التغيير يؤثر على البناء أو التشغيل.

9. حدّث `AI_HANDOFF.md` فقط إذا تغيرت حالة المشروع بشكل مهم.

---

## 12. مصدر الحقيقة

ترتيب الثقة عند وجود تعارض:

1. الكود الحالي في Git
2. حالة Git الحالية
3. نتائج البناء والاختبارات الفعلية
4. `PROJECT_MAP.md`
5. `AI_HANDOFF.md`
6. تقارير AI السابقة

إذا تعارضت الوثائق مع الكود، **الكود هو المرجع** ويجب الإبلاغ عن التعارض بدل افتراض صحة الوثيقة.

---

## 13. مبدأ المشروع

Atlas يتم تطويره تدريجيًا.

لا نحاول تغيير كل شيء دفعة واحدة.

الأولوية:

```text
استقرار
↓
توافق
↓
اختبار
↓
تغيير صغير
↓
اختبار
↓
توثيق
```

أي تغيير يجب أن يكون قابلًا للتتبع ويمكن معرفة سببه ونتيجته.
