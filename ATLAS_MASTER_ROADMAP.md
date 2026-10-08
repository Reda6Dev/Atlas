# خارطة الطريق الرئيسية لـ Atlas (Atlas Master Roadmap)

> الوثيقة الرئيسية لخريطة تطوير Atlas.
> تُقرأ مع:
> - `presentation-core/ATLAS_AI_HANDOFF_PROMPT.md`
> - `PROJECT_MAP.md`
> - `AI_HANDOFF.md`
> - `ATLAS_CHANGELOG.md`
> - `توثيق_Atlas/واجهة_المستخدم/` (مرجع مرحلة UX/UI)

## 0. قواعد المشروع (Project Rules)

- Atlas هو المشروع المستقل الذي نطوره فوق قاعدة TachiyomiSY الحالية.
- لا نعيد بناء نظام المصادر/الإضافات بالكامل في البداية.
- نستمر حاليًا باستخدام التكامل الحديث الموجود مع Keiyoushi والمصادر الحالية (`index.pb`).
- لا نكسر تكامل المصادر من أجل إعادة تصميم مبكرة.
- الهدف طويل المدى هو عزل نواة Atlas (Atlas Core) عن تغيّرات المصادر الخارجية عبر طبقة تجريد وتكامل (Integration/Abstraction Layer).
- الميزات التي تحتاج خادمًا خلفيًا (Backend) وحسابات وبنية تحتية تؤجل إلى مراحل متقدمة.
- لا نغيّر مساحة الأسماء/الحزمة في Kotlin `eu.kanade.tachiyomi` لمجرد الاستقلالية.
- لا نغيّر `MdConstants.kt` أو `tachiyomisy://mangadex-auth` دون سبب تقني موثق.
- لا نستخدم أوامر Git التدميرية دون موافقة صريحة.

---

# الخريطة الكبرى (Master Map)

```text
ATLAS
│
├── PHASE 1 — IDENTITY & FOUNDATION (المرحلة 1 — الهوية والأساس) [مكتملة]
│   ├── 1.1 الاسم وهوية التطبيق (App Name = Atlas)
│   ├── 1.2 معرّف التطبيق (Application ID = com.reda6dev.atlas)
│   ├── 1.3 حفظ مساحة الأسماء الداخلية (Namespace = eu.kanade.tachiyomi)
│   ├── 1.4 شاشة البدء التأسيسية (Splash Screen)
│   ├── 1.5 شاشة حول التطبيق التأسيسية (About / Branding)
│   ├── 1.6 الإسناد والتراخيص (Attribution / Licenses)
│   └── 1.7 تنظيف بقايا الهوية القديمة مع حفظ التوافق
│
├── PHASE 2 — ATLAS VISUAL SYSTEM (المرحلة 2 — النظام البصري) [مكتملة بالكامل]
│   ├── 2.1 الألوان والموارد المرئية (Colors & Color Resources)
│   ├── 2.2 السمة الفاتحة (Light Theme)
│   ├── 2.3 السمة الداكنة (Dark Theme) ووضع الأسود المطلق (AMOLED Pure Black)
│   ├── 2.4 الشعار وأيقونة التطبيق (App Launcher Icons - WebP)
│   ├── 2.5 شعار شاشة More وإصلاح AAPT2 Crash (LogoHeader Vector Fix)
│   ├── 2.6 ضبط مقاسات الشعار لشاشة البدء وشاشة حول التطبيق (110dp Splash / 74dp LogoHeader)
│   └── 2.7 الاختبار البصري النهائي والتحقق على جهاز فعلي (Honor CLK-LX2)
│
├── PHASE 3 — UX/UI (المرحلة 3 — واجهة وتجربة المستخدم) [المرحلة النشطة الحالية]
│   ├── 3.1 الشاشة الرئيسية (Home)
│   ├── 3.2 المكتبة (Library)
│   ├── 3.3 البحث (Search)
│   ├── 3.4 الإضافات والمصادر (Extensions / Sources)
│   ├── 3.5 القارئ (Reader)
│   ├── 3.6 التنزيلات (Downloads)
│   ├── 3.7 الإعدادات (Settings)
│   └── 3.8 تحسينات التنقل والتفاعل (Navigation & Interaction Improvements)
│
├── PHASE 4 — STABILITY & QUALITY (المرحلة 4 — الاستقرار والجودة)
│   ├── 4.1 جمع المشاكل المعروفة (Known Issues Gathering)
│   ├── 4.2 إعادة إنتاج الأخطاء (Bug Reproduction)
│   ├── 4.3 إصلاح الأخطاء (Bug Fixing)
│   ├── 4.4 اختبار الانتكاس والتراجع (Regression Testing)
│   ├── 4.5 الأداء (Performance)
│   └── 4.6 استقرار الإصدار (Release Stability)
│
├── PHASE 5 — LIBRARY & READING EXPERIENCE (المرحلة 5 — تجربة القراءة والمكتبة)
│   ├── 5.1 المجموعات الذكية (Smart Collections)
│   ├── 5.2 القوائم المخصصة (Custom Lists)
│   ├── 5.3 التصفية المتقدمة والوسوم (Advanced Filters / Tags)
│   ├── 5.4 إحصائيات القراءة (Reading Statistics)
│   ├── 5.5 تحسينات القارئ (Reader Improvements)
│   ├── 5.6 تحسينات مدير التنزيلات (Download Manager Improvements)
│   └── 5.7 تحسينات النسخ الاحتياطي والمزامنة (Backup / Sync Improvements)
│
├── PHASE 6 — ATLAS CHARACTERS (المرحلة 6 — شخصيات Atlas)
│   ├── 6.1 نموذج بيانات الشخصية (Character Data Model)
│   ├── 6.2 تجريد مزود بيانات الشخصيات (Character Provider Abstraction)
│   ├── 6.3 صور الشخصيات (Character Images)
│   ├── 6.4 الشخصيات المفضلة (Favorite Characters)
│   ├── 6.5 تفاصيل الشخصية (Character Details)
│   ├── 6.6 ربط الشخصيات بالأعمال (Link Characters ↔ Works)
│   └── 6.7 التخزين المؤقت المحلي ومزودات الاحتياط (Local Cache / Fallback Providers)
│
├── PHASE 7 — SOURCE SUPPORT & USER RESPECT (المرحلة 7 — دعم المصادر واحترام المستخدم)
│   ├── 7.1 معلومات المصدر (Source Information)
│   ├── 7.2 روابط المواقع الرسمية (Official Website Links)
│   ├── 7.3 روابط الدعم والاشتراك (Support / Subscription Links)
│   ├── 7.4 روابط تسجيل الدخول حيثما تُدعم (Login Links where supported)
│   ├── 7.5 الإسناد الواضح للمصدر (Clear Source Attribution)
│   └── 7.6 عدم تجاوز آليات تحقيق الدخل/الدعم للمصدر قصدًا (Respect Source Monetization)
│
├── PHASE 8 — ATLAS-SPECIFIC FEATURES (المرحلة 8 — ميزات Atlas الخاصة)
│   ├── 8.1 سجل الميزات المتراكمة (Feature Backlog)
│   ├── 8.2 تحديد الأولويات حسب القيمة للمستخدم والتبعية التقنية (Prioritize by Value & Dependency)
│   ├── 8.3 بناء الميزات التي لا تتطلب خادمًا أولًا (Build Non-Backend Features First)
│   └── 8.4 دمج الميزات المكتملة في نواة Atlas (Integrate into Atlas Core)
│
├── PHASE 9 — TRANSLATION & OCR (المرحلة 9 — الترجمة والتعرف الضوئي على الحروف)
│   ├── 9.1 بنية الترجمة (Translation Architecture)
│   ├── 9.2 نموذج لغة المصدر/الهدف (Source/Target Language Model)
│   ├── 9.3 استخراج النص (Text Extraction)
│   ├── 9.4 التعرف الضوئي على الحروف (OCR)
│   ├── 9.5 اكتشاف النصوص (Text Detection)
│   ├── 9.6 إزالة/تغطية النص الأصلي (Original Text Removal / Masking)
│   ├── 9.7 رسم وعرض النص المترجم (Translated Text Rendering)
│   ├── 9.8 معالجة صور المانغا/المانهوا (Manga/Manhwa Image Processing)
│   └── 9.9 استراتيجية العمل دون اتصال/عبر الإنترنت (Offline / Online Strategy)
│
├── PHASE 10 — AI & AUTOMATION (المرحلة 10 — الذكاء الاصطناعي والأتمتة)
│   ├── 10.1 تجريد مزود الذكاء الاصطناعي (AI Provider Abstraction)
│   ├── 10.2 المساعدة في الترجمة (Translation Assistance)
│   ├── 10.3 المساعدة في التعرف الضوئي (OCR Assistance)
│   ├── 10.4 مساعد القراءة (Reading Assistant)
│   ├── 10.5 الملخصات والشروحات (Summaries / Explanations)
│   └── 10.6 الأتمتة الاختيارية (Optional Automation)
│
├── PHASE 11 — ATLAS ACCOUNTS (المرحلة 11 — حسابات Atlas)
│   ├── 11.1 بنية الخادم والخلفية (Backend Architecture)
│   ├── 11.2 المصادقة والتحقق من الهوية (Authentication)
│   ├── 11.3 ملفات المستخدمين (User Profiles)
│   ├── 11.4 الجلسات والأمان (Session / Security)
│   └── 11.5 الخصوصية وإدارة الحسابات (Privacy / Account Management)
│
├── PHASE 12 — COMMENTS & COMMUNITY (المرحلة 12 — التعليقات والمجتمع)
│   ├── 12.1 مراجعات المانغا (Manga Reviews)
│   ├── 12.2 تعليقات الفصول (Chapter Comments)
│   ├── 12.3 التقييمات (Ratings)
│   ├── 12.4 الردود (Replies)
│   ├── 12.5 بلاغات الإساءة (Reports)
│   ├── 12.6 الإشراف والمراجعة (Moderation)
│   ├── 12.7 مكافحة البريد العشوائي والحد من معدل الطلبات (Anti-spam / Rate Limiting)
│   └── 12.8 الإشعارات (Notifications)
│
├── PHASE 13 — SOURCE ABSTRACTION (المرحلة 13 — تجريد المصادر)
│   ├── 13.1 واجهة مصدر Atlas (Atlas Source Interface)
│   ├── 13.2 طبقة التكامل (Integration Layer)
│   ├── 13.3 عزل تكامل Keiyoushi (Isolate Keiyoushi Integration)
│   ├── 13.4 دعم مستودعات متعددة (Support Multiple Repositories)
│   ├── 13.5 طبقة التوافق (Compatibility Layer)
│   └── 13.6 منع التغييرات الخارجية من التسرب إلى نواة Atlas (Prevent External Leaks into Atlas Core)
│
├── PHASE 14 — URL-BASED SOURCE SUPPORT (المرحلة 14 — دعم المصادر المستندة إلى الروابط)
│   ├── 14.1 إدخال رابط الموقع من المستخدم (User Enters Website URL)
│   ├── 14.2 تحليل قدرات الموقع (Website Capability Analysis)
│   ├── 14.3 قوالب المواقع المدعومة (Supported-Site Templates)
│   ├── 14.4 إنشاء تعريف المصدر (Source Definition Generation)
│   ├── 14.5 التحليل بمساعدة الذكاء الاصطناعي - اختياري (AI-Assisted Analysis)
│   ├── 14.6 حدود المصادقة وحماية Cloudflare (Authentication / Cloudflare Limitations)
│   └── 14.7 أدوات التحقق وضوابط الأمان (Validation and Safety Controls)
│
└── PHASE 15 — ATLAS INDEPENDENCE & RELEASE (المرحلة 15 — استقلالية وإصدار Atlas)
    ├── 15.1 البنية التحتية المستقلة (Independent Infrastructure)
    ├── 15.2 خط أنابيب الإصدار (Release Pipeline)
    ├── 15.3 التوثيق (Documentation)
    ├── 15.4 الخصوصية والشروط (Privacy / Terms)
    ├── 15.5 الأداء وقابلية التوسع (Performance & Scalability)
    ├── 15.6 النسخ الاحتياطي والتعافي من الكوارث (Backup / Disaster Recovery)
    └── 15.7 إصدار Atlas المستقر (Stable Atlas Release)
```

## ترتيب الأولويات (Priority Order)

| الأولوية (Priority) | المرحلة (Phase) | الغرض (Purpose) | الحالة (Status) |
|---|---|---|---|
| 1 | الهوية والأساس (Identity & Foundation) | تحويل اسم التطبيق والمُعرّف إلى Atlas | مكتملة (Completed) |
| 2 | النظام البصري (Visual System) | الشعار والأيقونات والألوان لشاشة البدء وMore | مكتملة بالكامل (Completed) |
| 3 | واجهة وتجربة المستخدم (UX/UI) | تحسينات الواجهة والشاشة الرئيسية | المرحلة النشطة الحالية (Active) |
| 4 | الاستقرار والجودة (Stability & Quality) | الثبات وإصلاح المشاكل | مخطط لها (Planned) |
| 5 | تجربة القراءة والمكتبة (Library & Reading) | جعل تجربة القراءة والمكتبة أقوى | مخطط لها (Planned) |
| 6 | الشخصيات (Characters) | الشخصيات المفضلة ونظام الشخصيات | مخطط لها (Planned) |
| 7 | دعم المصادر (Source Support) | دعم المصادر واحترامها | مخطط لها (Planned) |
| 8 | ميزات Atlas (Atlas Features) | ميزات خاصة بـAtlas | مخطط لها (Planned) |
| 9 | الترجمة والتعرف الضوئي (Translation & OCR) | نظام الترجمة المتقدم | مستقبلية (Future) |
| 10 | الذكاء الاصطناعي والأتمتة (AI & Automation) | الذكاء الاصطناعي والأتمتة | مستقبلية (Future) |
| 11 | الحسابات (Accounts) | البنية الحسابية | مستقبلية (Future) |
| 12 | التعليقات والمجتمع (Comments & Community) | التعليقات والمراجعات والمجتمع | مستقبلية (Future) |
| 13 | تجريد المصادر (Source Abstraction) | عزل Atlas عن تغيّرات المصادر | متقدمة (Advanced) |
| 14 | مصادر الروابط (URL Sources) | إضافة/تحليل مصادر من رابط URL | متقدمة (Advanced) |
| 15 | الاستقلالية والإصدار (Independence & Release) | البنية المستقلة والإصدار النهائي | نهائية (Final) |

## لماذا هذا الترتيب؟

1. **الهوية أولًا:** نثبت ما هو Atlas قبل بناء ميزات كثيرة.
2. **التصميم قبل الميزات الكبيرة:** حتى لا نبني ميزات جديدة ثم نعيد تصميم واجهتها.
3. **الاستقرار مبكرًا:** كل مرحلة لاحقة يجب أن تبنى فوق أساس قابل للاختبار.
4. **الشخصيات بعد المكتبة:** لأنها تستفيد من ربط الشخصيات بالأعمال التي يتابعها المستخدم.
5. **التعليقات بعد الحسابات:** التعليقات تحتاج هوية مستخدم وخادمًا خلفيًا (Backend) وإشرافًا (Moderation).
6. **الترجمة والذكاء الاصطناعي (AI) لاحقًا:** لأنها أنظمة ثقيلة ويمكن أن تعتمد على خدمات ونماذج وموارد كبيرة.
7. **استقلال المصادر في مرحلة متقدمة:** لا نعيد بناء نظام المصادر الآن ما دام التكامل الحالي يعمل.
8. **المصادر المستندة إلى الروابط (URL → Source) في النهاية:** الرابط وحده لا يصف طريقة البحث والفصول والصور والمصادقة والحماية، لذلك هذه ميزة متقدمة.
9. **الاستقلال النهائي:** بعد أن نعرف فعليًا أي أجزاء Atlas تحتاج بنية مستقلة.

## المبدأ المعماري طويل المدى (Long-Term Architectural Principle)

```text
                    ATLAS
                      │
              ┌───────┴────────┐
              │   ATLAS CORE   │
              │                │
              │ Library        │
              │ Reader         │
              │ Downloads      │
              │ Characters     │
              │ Statistics     │
              │ AI             │
              └───────┬────────┘
                      │
             Integration Layer
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
   Extensions     Characters     Accounts
   / Sources      Providers      / Backend
```

الهدف ليس التخلص من كل التكاملات الخارجية الآن، بل منعها من أن تصبح جزءًا لا يمكن فصله عن قلب Atlas.

## حالة الميزات الحالية (Current Feature Status)

### منجز بالكامل (Completed)
- اسم التطبيق: Atlas (`i18n/src/commonMain/moko-resources/base/strings.xml`)
- معرّف التطبيق: `applicationId = "com.reda6dev.atlas"` (`app/build.gradle.kts`)
- إبقاء مساحة الأسماء/الحزمة الداخلية كما هي: `namespace = "eu.kanade.tachiyomi"`
- دعم النظام الحديث للإضافات/المصادر (Keiyoushi مع `index.pb`)
- بناء واختبار التطبيق والتثبيت على جهاز فعلي (Honor CLK-LX2)
- إصلاح FlexibleAdapter واستبداله بـ `eu.davidea:flexible-adapter:5.1.0`
- تثبيت خيارات لغة JVM لمنع مشاكل SQLDelight مع الأرقام العربية
- أيقونات التطبيق التكيفية ببرمجة WebP
- إزالة ملفات Debug المطبقة عشوائيًا لتوحيد ألوان الشعار
- إصلاح انهيار شعار `LogoHeader.kt` وتبسيط المتجه `ic_tachi.xml` ليعمل بطلاقة بدون خطأ AAPT2
- ضبط أحجام الشعار في شاشة البدء (110dp) وشاشة `More` (74dp في `LogoHeader.kt`)
- الفحص النهائي والتأكيد للألوان في الوضع الداكن والفاتح وAMOLED
- إنشاء التوثيق التفصيلي الشامل لواجهة المستخدم في `توثيق_Atlas/واجهة_المستخدم/`

### المرحلة النشطة التالية (Current Active Phase)
- **المرحلة 3 — واجهة وتجربة المستخدم (UX/UI Redesign & Improvements)**

### لم يُنفذ بعد في Atlas الجديد (Not Implemented Yet)
- الشخصيات المفضلة (Favorite Characters)
- التعليقات والمجتمع (Comments & Community)
- الحسابات (Atlas Accounts)
- الترجمة والتعرف الضوئي على الحروف (Translation & OCR)
- مساعد الذكاء الاصطناعي (AI Assistant)
- تجريد المصادر (Source Abstraction)
- المصادر المستندة إلى الروابط (URL-based sources)

## قاعدة التنفيذ (Execution Rule)

في كل مرحلة:

```text
التخطيط (Plan)
  ↓
فحص توثيق الواجهة والكود الحالي (Inspect UI docs & code)
  ↓
إجراء أصغر تغيير آمن (Make smallest safe change)
  ↓
بناء المشروع (Build)
  ↓
اختبار التطبيق على جهاز عند الحاجة (Test on device when relevant)
  ↓
مراجعة فروقات Git (Review git diff)
  ↓
الالتزام البرمجي (Commit)
  ↓
تحديث AI_HANDOFF.md وتوثيق التغيير في ATLAS_CHANGELOG.md
```

ولا ننتقل إلى المرحلة التالية لمجرد أن الكود "يبدو صحيحًا". يجب أن يكون هناك بناء واختبار (Build/Test) مناسب للمرحلة.

## العلاقة بين ملفات التسليم

`ATLAS_AI_HANDOFF_PROMPT.md`
→ كيف يجب أن يتصرف أي ذكاء اصطناعي (AI) يعمل على المشروع والقواعد الصارمة.

`PROJECT_MAP.md`
→ أين توجد أجزاء المشروع وكيف تعمل والملفات الهامة.

`AI_HANDOFF.md`
→ أين وصل العمل الآن وما الذي تم تنفيذه واختباره والمشاكل المحلولة والنقطة الحالية.

`ATLAS_CHANGELOG.md`
→ السجل التاريخي المتراكم للتغييرات السابقة.

`ATLAS_MASTER_ROADMAP.md`
→ إلى أين نريد أن نصل وما ترتيب الطريق والمراحل الحالية.

`توثيق_Atlas/واجهة_المستخدم/`
→ المرجع الميداني التفصيلي لشاشات ومكونات وموارد الواجهة.
