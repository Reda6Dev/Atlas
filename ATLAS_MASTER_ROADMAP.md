# Atlas Master Roadmap

> الوثيقة الرئيسية لخريطة تطوير Atlas.
> تُقرأ مع:
> - `presentation-core/ATLAS_AI_HANDOFF_PROMPT.md`
> - `PROJECT_MAP.md`
> - `AI_HANDOFF.md`

## 0. قواعد المشروع

- Atlas هو المشروع المستقل الذي نطوره فوق قاعدة TachiyomiSY الحالية.
- لا نعيد بناء نظام المصادر/الإضافات بالكامل في البداية.
- نستمر حاليًا باستخدام التكامل الحديث الموجود مع Keiyoushi والمصادر الحالية.
- لا نكسر تكامل المصادر من أجل إعادة تصميم مبكرة.
- الهدف طويل المدى هو عزل Atlas Core عن تغيّرات المصادر الخارجية عبر Integration/Abstraction Layer.
- الميزات التي تحتاج Backend وحسابات وبنية تحتية تؤجل إلى مراحل متقدمة.
- لا نغيّر Kotlin namespace/package `eu.kanade.tachiyomi` لمجرد الاستقلالية.
- لا نغيّر `MdConstants.kt` أو `tachiyomisy://mangadex-auth` دون سبب تقني موثق.
- لا نستخدم أوامر Git التدميرية دون موافقة صريحة.

---

# الخريطة الكبرى

```text
ATLAS
│
├── PHASE 1 — IDENTITY & FOUNDATION
│   ├── 1.1 الاسم وهوية التطبيق
│   ├── 1.2 Application ID
│   ├── 1.3 Logo / App Icon
│   ├── 1.4 Splash Screen
│   ├── 1.5 About / Branding
│   ├── 1.6 Attribution / Licenses
│   └── 1.7 تنظيف بقايا الهوية القديمة دون حذف الحقوق
│
├── PHASE 2 — ATLAS VISUAL SYSTEM
│   ├── 2.1 الألوان
│   ├── 2.2 Light Theme
│   ├── 2.3 Dark Theme
│   ├── 2.4 Typography
│   ├── 2.5 Iconography
│   ├── 2.6 Buttons / Controls
│   └── 2.7 Visual Design Language
│
├── PHASE 3 — UX/UI
│   ├── 3.1 Home
│   ├── 3.2 Library
│   ├── 3.3 Search
│   ├── 3.4 Extensions / Sources
│   ├── 3.5 Reader
│   ├── 3.6 Downloads
│   ├── 3.7 Settings
│   └── 3.8 Navigation & Interaction Improvements
│
├── PHASE 4 — STABILITY & QUALITY
│   ├── 4.1 جمع المشاكل المعروفة
│   ├── 4.2 Reproduction
│   ├── 4.3 Bug Fixing
│   ├── 4.4 Regression Testing
│   ├── 4.5 Performance
│   └── 4.6 Release Stability
│
├── PHASE 5 — LIBRARY & READING EXPERIENCE
│   ├── 5.1 Smart Collections
│   ├── 5.2 Custom Lists
│   ├── 5.3 Advanced Filters / Tags
│   ├── 5.4 Reading Statistics
│   ├── 5.5 Reader Improvements
│   ├── 5.6 Download Manager Improvements
│   └── 5.7 Backup / Sync Improvements
│
├── PHASE 6 — ATLAS CHARACTERS
│   ├── 6.1 Character Data Model
│   ├── 6.2 Character Provider Abstraction
│   ├── 6.3 Character Images
│   ├── 6.4 Favorite Characters
│   ├── 6.5 Character Details
│   ├── 6.6 Link Characters ↔ Works
│   └── 6.7 Local Cache / Fallback Providers
│
├── PHASE 7 — SOURCE SUPPORT & USER RESPECT
│   ├── 7.1 Source Information
│   ├── 7.2 Official Website Links
│   ├── 7.3 Support / Subscription Links
│   ├── 7.4 Login Links where supported
│   ├── 7.5 Clear Source Attribution
│   └── 7.6 Do not intentionally bypass source monetization/support mechanisms
│
├── PHASE 8 — ATLAS-SPECIFIC FEATURES
│   ├── 8.1 Feature backlog
│   ├── 8.2 Prioritize by user value and technical dependency
│   ├── 8.3 Build features that do not require backend first
│   └── 8.4 Integrate mature features into Atlas Core
│
├── PHASE 9 — TRANSLATION & OCR
│   ├── 9.1 Translation Architecture
│   ├── 9.2 Source/Target Language Model
│   ├── 9.3 Text Extraction
│   ├── 9.4 OCR
│   ├── 9.5 Text Detection
│   ├── 9.6 Original Text Removal / Masking
│   ├── 9.7 Translated Text Rendering
│   ├── 9.8 Manga/Manhwa Image Processing
│   └── 9.9 Offline / Online Strategy
│
├── PHASE 10 — AI & AUTOMATION
│   ├── 10.1 AI Provider Abstraction
│   ├── 10.2 Translation Assistance
│   ├── 10.3 OCR Assistance
│   ├── 10.4 Reading Assistant
│   ├── 10.5 Summaries / Explanations
│   └── 10.6 Optional Automation
│
├── PHASE 11 — ATLAS ACCOUNTS
│   ├── 11.1 Backend Architecture
│   ├── 11.2 Authentication
│   ├── 11.3 User Profiles
│   ├── 11.4 Session / Security
│   └── 11.5 Privacy / Account Management
│
├── PHASE 12 — COMMENTS & COMMUNITY
│   ├── 12.1 Manga Reviews
│   ├── 12.2 Chapter Comments
│   ├── 12.3 Ratings
│   ├── 12.4 Replies
│   ├── 12.5 Reports
│   ├── 12.6 Moderation
│   ├── 12.7 Anti-spam / Rate Limiting
│   └── 12.8 Notifications
│
├── PHASE 13 — SOURCE ABSTRACTION
│   ├── 13.1 Atlas Source Interface
│   ├── 13.2 Integration Layer
│   ├── 13.3 Isolate Keiyoushi Integration
│   ├── 13.4 Support Multiple Repositories
│   ├── 13.5 Compatibility Layer
│   └── 13.6 Prevent external changes from leaking into Atlas Core
│
├── PHASE 14 — URL-BASED SOURCE SUPPORT
│   ├── 14.1 User enters website URL
│   ├── 14.2 Website capability analysis
│   ├── 14.3 Supported-site templates
│   ├── 14.4 Source definition generation
│   ├── 14.5 AI-assisted analysis (optional)
│   ├── 14.6 Authentication / Cloudflare limitations
│   └── 14.7 Validation and safety controls
│
└── PHASE 15 — ATLAS INDEPENDENCE & RELEASE
    ├── 15.1 Independent infrastructure
    ├── 15.2 Release pipeline
    ├── 15.3 Documentation
    ├── 15.4 Privacy / Terms
    ├── 15.5 Performance & scalability
    ├── 15.6 Backup / disaster recovery
    └── 15.7 Stable Atlas release
```

## Priority order

| Priority | Phase | Purpose | Status |
|---|---|---|---|
| 1 | Identity & Foundation | تحويل المشروع إلى Atlas فعليًا | Started |
| 2 | Visual System | هوية بصرية مستقلة | Next |
| 3 | UX/UI | تحسين تجربة الاستخدام | Planned |
| 4 | Stability & Quality | الثبات وإصلاح المشاكل | Planned |
| 5 | Library & Reading | جعل تجربة القراءة والمكتبة أقوى | Planned |
| 6 | Characters | الشخصيات المفضلة ونظام الشخصيات | Planned |
| 7 | Source Support | دعم المصادر واحترامها | Planned |
| 8 | Atlas Features | ميزات خاصة بـAtlas | Planned |
| 9 | Translation & OCR | نظام الترجمة المتقدم | Future |
| 10 | AI & Automation | الذكاء الاصطناعي والأتمتة | Future |
| 11 | Accounts | البنية الحسابية | Future |
| 12 | Comments & Community | التعليقات والمراجعات والمجتمع | Future |
| 13 | Source Abstraction | عزل Atlas عن تغيّر المصادر | Advanced |
| 14 | URL Sources | إضافة/تحليل مصادر من URL | Advanced |
| 15 | Independence & Release | البنية المستقلة والإصدار | Final |

## لماذا هذا الترتيب؟

1. **الهوية أولًا:** نثبت ما هو Atlas قبل بناء ميزات كثيرة.
2. **التصميم قبل الميزات الكبيرة:** حتى لا نبني ميزات جديدة ثم نعيد تصميم واجهتها.
3. **الاستقرار مبكرًا:** كل مرحلة لاحقة يجب أن تبنى فوق أساس قابل للاختبار.
4. **الشخصيات بعد المكتبة:** لأنها تستفيد من ربط الشخصيات بالأعمال التي يتابعها المستخدم.
5. **التعليقات بعد الحسابات:** التعليقات تحتاج هوية مستخدم وBackend وModeration.
6. **الترجمة وAI لاحقًا:** لأنها أنظمة ثقيلة ويمكن أن تعتمد على خدمات ونماذج وموارد كبيرة.
7. **استقلال المصادر في مرحلة متقدمة:** لا نعيد بناء نظام المصادر الآن ما دام التكامل الحالي يعمل.
8. **URL → Source في النهاية:** الرابط وحده لا يصف طريقة البحث والفصول والصور والمصادقة والحماية، لذلك هذه ميزة متقدمة.
9. **الاستقلال النهائي:** بعد أن نعرف فعليًا أي أجزاء Atlas تحتاج بنية مستقلة.

## المبدأ المعماري طويل المدى

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

## حالة الميزات الحالية

### منجز
- اسم التطبيق: Atlas
- `applicationId = com.reda6dev.atlas`
- إبقاء namespace/package الداخلي كما هو
- دعم النظام الحديث للإضافات/المصادر
- اختبار Keiyoushi والمصادر على جهاز فعلي
- إصلاح FlexibleAdapter
- تثبيت JVM locale لمنع مشاكل SQLDelight مع الأرقام العربية
- إنشاء وثائق التسليم الأساسية

### لم يُنفذ بعد في Atlas الجديد
- Icon النهائي
- Splash النهائي
- About/branding النهائي
- الألوان والثيم
- إعادة تصميم الأيقونات
- إعادة تصميم الواجهة
- الشخصيات المفضلة
- التعليقات
- الحسابات
- الترجمة/OCR
- AI
- Source Abstraction
- URL-based sources

## قاعدة التنفيذ

في كل مرحلة:

```text
Plan
  ↓
Inspect existing code
  ↓
Make smallest safe change
  ↓
Build
  ↓
Test on device when relevant
  ↓
Review git diff
  ↓
Commit
  ↓
Update AI_HANDOFF.md
```

ولا ننتقل إلى المرحلة التالية لمجرد أن الكود "يبدو صحيحًا". يجب أن يكون هناك Build/Test مناسب للمرحلة.

## العلاقة بين ملفات التسليم الأربعة

`ATLAS_AI_HANDOFF_PROMPT.md`
→ كيف يجب أن يتصرف أي AI يعمل على المشروع.

`PROJECT_MAP.md`
→ أين توجد أجزاء المشروع وكيف تعمل.

`AI_HANDOFF.md`
→ أين وصل العمل الآن وما الذي تم تنفيذه.

`ATLAS_MASTER_ROADMAP.md`
→ إلى أين نريد أن نصل وما ترتيب الطريق.

هذه الملفات الأربعة تكمل بعضها ولا تستبدل بعضها.
