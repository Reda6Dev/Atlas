# Atlas Documentation Index

> نقطة الدخول الرسمية لوثائق مشروع **Atlas**.
>
> اقرأ هذا الملف أولًا عند بدء العمل على المشروع أو عند نقل المشروع إلى AI جديد.

---

## 1. الوثائق الأساسية

Atlas يعتمد على أربع وثائق رئيسية، بالإضافة إلى هذا الفهرس:

| الملف                                                                          | الوظيفة                                              |
| ------------------------------------------------------------------------------ | ---------------------------------------------------- |
| [`ATLAS_AI_HANDOFF_PROMPT.md`](./presentation-core/ATLAS_AI_HANDOFF_PROMPT.md) | قواعد وتعليمات عمل الـAI على مشروع Atlas             |
| [`PROJECT_MAP.md`](./PROJECT_MAP.md)                                           | الخريطة التقنية لبنية المشروع ومكوناته               |
| [`AI_HANDOFF.md`](./AI_HANDOFF.md)                                             | الحالة الحالية للمشروع وما تم إنجازه وما يجب متابعته |
| [`ATLAS_MASTER_ROADMAP.md`](./ATLAS_MASTER_ROADMAP.md)                         | خارطة الطريق المستقبلية ومراحل تطوير Atlas           |

---

# 2. ترتيب القراءة

عند إعطاء المشروع إلى AI جديد، يكون ترتيب القراءة الموصى به:

```text
ATLAS_INDEX.md
      ↓
ATLAS_AI_HANDOFF_PROMPT.md
      ↓
PROJECT_MAP.md
      ↓
AI_HANDOFF.md
      ↓
ATLAS_MASTER_ROADMAP.md
```

### 1. ATLAS_AI_HANDOFF_PROMPT.md

يشرح **كيف يجب على الـAI أن يعمل على المشروع**.

يشمل قواعد مثل:

* فحص الكود قبل التعديل.
* عدم إجراء تغييرات عشوائية.
* الحفاظ على قرارات Atlas الحالية.
* Build بعد التغييرات المهمة.
* اختبار الميزات عند الحاجة.
* مراجعة Git diff.
* تجنب أوامر Git التدميرية دون موافقة صريحة.

[فتح الملف](./presentation-core/ATLAS_AI_HANDOFF_PROMPT.md)

---

### 2. PROJECT_MAP.md

يشرح **كيف بُني المشروع وأين توجد أجزاؤه**.

يشمل:

* بنية المشروع.
* Modules.
* Clean Architecture.
* Presentation / Domain / Data / App.
* Extension Store.
* Sources.
* Reader.
* Library.
* Downloads.
* Settings.
* Dependency Injection.
* WorkManager.
* SQLDelight.
* `exh`.
* Gradle Build Logic.

[فتح الملف](./PROJECT_MAP.md)

---

### 3. AI_HANDOFF.md

يشرح **أين وصل المشروع حاليًا**.

يشمل:

* الفرع الحالي.
* آخر التغييرات المهمة.
* ما تم إنجازه.
* ما تم اختباره.
* المشاكل المعروفة.
* القرارات التي يجب الحفاظ عليها.
* المهمة الحالية أو التالية.
* التحذيرات المهمة.

[فتح الملف](./AI_HANDOFF.md)

---

### 4. ATLAS_MASTER_ROADMAP.md

يشرح **إلى أين نريد أن يصل Atlas مستقبلًا**.

يشمل مراحل المشروع من الأساسيات إلى الأنظمة المتقدمة:

```text
Identity
    ↓
Visual System
    ↓
UX/UI
    ↓
Stability
    ↓
Library / Reading
    ↓
Characters
    ↓
Atlas Features
    ↓
Translation / OCR
    ↓
AI
    ↓
Accounts
    ↓
Comments / Community
    ↓
Source Abstraction
    ↓
URL-Based Sources
    ↓
Atlas Independence
```

[فتح الملف](./ATLAS_MASTER_ROADMAP.md)

---

# 3. وظيفة كل وثيقة باختصار

```text
ATLAS_INDEX.md
    = أين أبدأ؟ وما هي الوثائق؟

ATLAS_AI_HANDOFF_PROMPT.md
    = كيف يجب أن أعمل على Atlas؟

PROJECT_MAP.md
    = كيف بُني Atlas وأين توجد أجزاؤه؟

AI_HANDOFF.md
    = أين وصل Atlas الآن؟

ATLAS_MASTER_ROADMAP.md
    = إلى أين نريد أن يصل Atlas؟
```

---

# 4. العلاقة بين الوثائق

الوثائق تكمل بعضها ولا تستبدل بعضها:

```text
                    ATLAS_INDEX
                         │
                         ▼
              AI HANDOFF PROMPT
                 "كيف أعمل؟"
                         │
                         ▼
                   PROJECT MAP
                 "كيف بُني؟"
                         │
                         ▼
                   AI HANDOFF
                 "أين وصلنا؟"
                         │
                         ▼
                MASTER ROADMAP
                "إلى أين نذهب؟"
```

---

# 5. قاعدة مهمة عند بدء جلسة جديدة

عند العمل على Atlas مع AI جديد:

1. اقرأ `ATLAS_INDEX.md`.
2. اقرأ `ATLAS_AI_HANDOFF_PROMPT.md`.
3. اقرأ `PROJECT_MAP.md`.
4. اقرأ `AI_HANDOFF.md`.
5. اقرأ `ATLAS_MASTER_ROADMAP.md`.
6. بعد ذلك افحص الكود الفعلي المتعلق بالمهمة.
7. لا تبدأ تعديل الكود اعتمادًا على الوثائق وحدها.

الوثائق تساعد على فهم المشروع، لكن **الكود الفعلي وGit history هما المرجع عند التحقق من الحالة التقنية الحالية**.

---

# 6. الفرق بين AI_HANDOFF و MASTER_ROADMAP

لا تخلط بينهما.

### `AI_HANDOFF.md`

يجيب عن:

> ماذا أنجزنا حتى الآن؟

وهو ملف يتغير باستمرار مع تقدم المشروع.

### `ATLAS_MASTER_ROADMAP.md`

يجيب عن:

> ماذا نريد أن نبني مستقبلًا؟

ولا يحتاج إلى تعديل عند كل تغيير صغير.

يتم تحديث الـRoadmap عندما يحدث تغيير كبير مثل:

* إضافة مرحلة جديدة.
* تغيير أولوية ميزة.
* اتخاذ قرار معماري طويل المدى.
* إنهاء مرحلة رئيسية.
* تغيير اتجاه المشروع.

---

# 7. عند انتهاء مهمة تطويرية

المسار المعتاد:

```text
Plan
  ↓
Inspect existing code
  ↓
Make the smallest safe change
  ↓
Build
  ↓
Test
  ↓
Review Git diff
  ↓
Commit
  ↓
Update AI_HANDOFF.md
```

أما `ATLAS_MASTER_ROADMAP.md` فلا يتم تحديثه إلا إذا كان التغيير يؤثر على الخطة العامة للمشروع.

---

# 8. ترتيب الأولوية عند وجود تعارض

إذا وجدت معلومات مختلفة بين الوثائق:

### الحالة التقنية الحالية

```text
الكود الفعلي
    ↓
Git history / commits
    ↓
AI_HANDOFF.md
```

### بنية المشروع

```text
الكود الفعلي
    ↓
PROJECT_MAP.md
```

### الخطة المستقبلية

```text
القرار الأحدث المعتمد
    ↓
ATLAS_MASTER_ROADMAP.md
```

### قواعد عمل الـAI

```text
ATLAS_AI_HANDOFF_PROMPT.md
```

---

# 9. البنية النهائية للوثائق

يفضل أن تكون الملفات بهذا الشكل:

```text
Atlas/
│
├── ATLAS_INDEX.md
├── PROJECT_MAP.md
├── AI_HANDOFF.md
├── ATLAS_MASTER_ROADMAP.md
│
└── presentation-core/
    └── ATLAS_AI_HANDOFF_PROMPT.md
```

إذا تغير مكان أي ملف، يجب تحديث الرابط النسبي الخاص به داخل `ATLAS_INDEX.md`.

---

# 10. القاعدة الأساسية

> **افهم كيف تعمل → افهم كيف بُني المشروع → اعرف أين وصلنا → اعرف إلى أين نريد الوصول → ثم عدّل الكود.**

ولا تبدأ بتغيير الكود لمجرد أن المهمة تبدو بسيطة قبل فحص السياق والكود الفعلي.

---

## الوثائق

* [AI Handoff Prompt](./presentation-core/ATLAS_AI_HANDOFF_PROMPT.md)
* [Project Map](./PROJECT_MAP.md)
* [Current AI Handoff](./AI_HANDOFF.md)
* [Atlas Master Roadmap](./ATLAS_MASTER_ROADMAP.md)
