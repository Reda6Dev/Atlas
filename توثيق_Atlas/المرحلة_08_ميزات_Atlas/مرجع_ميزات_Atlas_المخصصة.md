# مرجع المرحلة 08 — ميزات Atlas المخصصة (Atlas-Specific Features Reference)

> **المرحلة:** Phase 8 — Atlas-Specific Features
> **الغرض:** المرجع الهندسي للميزات الحصرية الفريدة المضافة والمخطط إضافتها لمشروع Atlas.
> **الحالة الحالية:** تتضمن ميزات موروثة من SY وتعديلات Atlas الهيكلية الحالية.

---

## 1. الميزات الحصرية القائمة حالياً في الكود (Current Existing SY/Atlas Features)

تتركز الميزات الحصرية الحالية داخل المجلد `app/src/main/java/exh/` والملفات التابعة لها في `:domain` و `:data`:

1. **المصدر المدمج (Merged Source):**
   * دمج المانغا وفصولها من عدة مصادر مختلفة في صفحة واحدة.
   * الكود: `app/src/main/java/eu/kanade/tachiyomi/source/online/all/MergedSource.kt` & `merged.sq`.
2. **محرك البحث الذكي والمطابقة (Smart Library Search):**
   * مطابقة الأعمال والبحث التلقائي عبر عدة مصادر.
   * الكود: `app/src/main/java/exh/smartsearch/SmartLibrarySearchEngine.kt`.
3. **التغذية الشاملة والأبحاث المحفوظة (Feed & Saved Searches):**
   * عرض نتائج أبحاث المصادر المتعددة في تبويب التغذية الشاملة.
   * الكود: `app/src/main/java/eu/kanade/presentation/browse/FeedScreen.kt` & `feed_saved_search.sq`.
4. **تكاملات E-Hentai/ExHentai و NHentai و MangaDex:**
   * محركات جلب الميتا، المعاينة السريعة قبل القراءة (`pagepreview/`)، والبحث المتقدم بالوسوم (`search_metadata.sq`, `search_tags.sq`).

---

## 2. المخطط الهندسي للميزات المخصصة القادمة (Planned Atlas Features)

1. **نظام التوصيات والمقترحات الذكي الداخلي:**
   * تطوير خوارزمية توصية القراءة المبنية على محتوى المكتبة والتفضيلات دون الحاجة لخادم خارجي.
2. **تتبع التقدم وإحصائيات القراءة المتقدمة:**
   * رسوم بيانية تفاعلية تحلل وقت القراءة اليومي، السرعة، والأنواع المفضلة.
3. **أدوات إدارة التخزين الذكية (Smart Storage Cleaner):**
   * أداة للتعرف التلقائي على الفصول المنزلة المقروءة وتنظيف الذاكرة المؤقتة الذكي.
