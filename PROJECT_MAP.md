# PROJECT_MAP.md — خريطة البنية الأساسية والتقنية لمشروع TachiyomiSY

---

## 1. نظرة عامة على التقنية والمعمارية (Architecture Overview)

يعتمد المشروع على معمارية **Clean Architecture** متعددة الوحدات (Multi-module)، مع فصل تام ومحدد بين منطق الأعمال (Business Logic)، طبقة البيانات (Data Layer)، وطبقة العرض (Presentation Layer):

1. **طبقة العرض (Presentation Layer)**:
   - تمثلها الوحدات `:presentation-core` (مكونات نظام التصميم الثابتة والثيمات M3 Expressive)، و `:presentation-widget` (ودجت الشاشة الرئيسية)، وباقي الشاشات والمكونات المكتوبة بـ Compose داخل الوحدة `:app`.
   - تعتمد كلياً على **Jetpack Compose** للواجهات، مع مكتبة **Voyager** للتنقل بين الشاشات وتتبع التكدس (Navigation/Stack)، وإدارة الحالة عبر `ScreenModel` / `ViewModel`.

2. **طبقة المجال (Domain Layer)**:
   - تمثلها وحدات `:domain` بالإضافة إلى `:source-api`.
   - كود بلغة Kotlin نقي بدون أي اعتمادات على إطار عمل Android أو واجهات المستخدم.
   - الكيانات الأساسية: `Manga`، `Chapter`، `Category`، `Source`، `Track`، `ExtensionStore` وغيرها.
   - تحتوي على واجهات المستودعات (Repository Interfaces) وحالات الاستخدام/التفاعلات (Interactors / Use Cases مثل `GetLibraryManga`، `SyncChaptersWithSource`، `GetExtensionStores`، `DownloadChapter`).

3. **طبقة البيانات (Data Layer)**:
   - تمثلها وحدات `:data` بالإضافة إلى `:source-local` و `:core-metadata`.
   - تتكفل بتنفيذ واجهات المستودعات باستخدام قاعدة البيانات **SQLDelight**، عملاء الشبكة (**OkHttp**)، معالجة الصور والتكشيش، وإدارة التفضيلات عبر `PreferenceStore`.

4. **وحدة التطبيق الرئيسية (`:app`)**:
   - تعتبر نقطة التجميع والتنسيق العليا؛ تحتوي على كلاس التطبيق الرئيسي (`App`)، إعدادات حقن التبعيات (DI عبر Injekt & Koin)، الخدمات والمهام الخلفية (`WorkManager Jobs`)، الأنشطة الرئيسية (`MainActivity`)، وكافة الشاشات التفصيلية وتعديلات فرع SY (`exh/`).

---

## 2. أدوار وحدات Gradle الرئيسية وتصنيفات الإصدارات (Gradle Modules & Version Catalogs)

يتكون المشروع من **12 وحدة subproject** بالإضافة إلى البناء المركب `build-logic`:

- **`:app`**: التطبيق الرئيسي النهائي؛ يحتوي على الشاشات وواجهات Compose، والـ ViewModels/ScreenModels، والأنشطة والخدمات والعمال (Jobs/Workers)، وإعدادات DI الخاصة بـ Injekt و Koin وتعديلات TachiyomiSY الحصرية (`exh/`).
- **`:baseline-profile`**: وحدة اختبارات وتقييم أداء Android (Macrobenchmark & Baseline Profiles) لتسريع تشغيل التطبيق.
- **`:core-metadata`**: أدوات استخراج وقراءة واسترجاع البيانات الوصفية الخاصة بالمانغا والكومكس (مثل صيغ `ComicInfo.xml` وميتا Tachiyomi).
- **`:core:common`**: الأدوات الأساسية والخدمات المساعدة المشتركة (`PreferenceStore`، `Coroutines Dispatchers`، ممرضات IO، ممرضات i18n، والطباعة عبر Logcat).
- **`:data`**: التنفيذ العملي لطبقة البيانات؛ تحتوي على محرك قاعدة بيانات SQLDelight، الاتصالات عبر OkHttp، التخزين الداخلي، وتوفير نماذج البيانات للـ Domain.
- **`:domain`**: طبقة الأعمال الصافية (Domain Model)؛ تحتوي على الكيانات، وواجهات المستودعات، والتفاعلات (Interactors/Use Cases).
- **`:i18n`**: النصوص والترجمات القياسية للغات المعتمدة المبنية على Moko Resources.
- **`:i18n-sy`**: النصوص والترجمات الحصرية المخصصة لفرع TachiyomiSY المبنية على Moko Resources.
- **`:presentation-core`**: نظام التصميم الموحد (Design System) المكتوب بـ Compose (الثيمات، M3 Expressive، الألوان، الأيقونات، المكونات القابلة لإعادة الاستخدام).
- **`:presentation-widget`**: إدارات ومكونات ودجت الشاشة الرئيسية الخاصة بـ Android (Glance Widgets).
- **`:source-api`**: الواجهات النظيفة والنماذج التي تلتزم بها الإضافات والمصادر (`Source`، `HttpSource`، `SChapter`، `SManga`، `FilterList`).
- **`:source-local`**: إدارة وتوفير المانغا المحفوظة محلياً على الجهاز عبر المجلدات أو الملفات المضغوطة (ZIP/CBZ).
- **`gradle/build-logic`**: مشروع بناء مستقل (Composite Build) يوفر بلاجنات Gradle مخصصة (`tachiyomi.android.application`, `tachiyomi.android.library`, `tachiyomi.android.library.compose`, `tachiyomi.spotless`...).

### كاتالوجات التبعيات (Version Catalogs)
تدار التبعيات والمكتبات عبر 3 ملفات `.toml` في مجلد `gradle/`:
1. **`gradle/libs.versions.toml`** (`libs`): مكتبات Android و Compose و Kotlin الأساسية.
2. **`gradle/mihon.versions.toml`** (`mihonx`): المكتبات المضافة من مشروع Mihon الأم.
3. **`gradle/sy.versions.toml`** (`sylibs`): المكتبات والاعتماديات المخصصة لفرع TachiyomiSY.

---

## 3. الأنظمة المركزية الرئيسية (Core Systems)

### أ. نظام تحديث المكتبة وجلب الفصول الجديدة (Library Update)
- **`LibraryUpdateJob.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/library/LibraryUpdateJob.kt`): عامل خلفي (`WorkManager CoroutineWorker`) لتشغيل التحديث الدوري أو اليدوي للمكتبة، مع مراعاة القيود (الشبكة، الشحن) وتنظيم التحديث الموازي عبر `Semaphore`.
- **`SyncChaptersWithSource.kt`** (`app/src/main/java/eu/kanade/domain/chapter/interactor/SyncChaptersWithSource.kt`): التفاعل المسؤول عن جلب قائمة الفصول من المصدر ومقارنتها بقاعدة البيانات المحلية لإدراج الفصول الجديدة وتحديث أو حذف المزال منها.
- **`LibraryUpdateNotifier.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/library/LibraryUpdateNotifier.kt`): إدارة إشعارات النظام الخاصة بالتقدم وعدد الفصول الجديدة والأخطاء.
- **`FilterChaptersForDownload.kt`** (`domain/src/main/java/mihon/domain/chapter/interactor/FilterChaptersForDownload.kt`): تصفية الفصول المكتشفة حديثاً وتمريرها للتنزيل التلقائي وفق تفضيلات المستخدم.

### ب. نظام الإضافات والمتاجر (Extensions & Extension Stores)
- **`ExtensionManager.kt`** (`app/src/main/java/eu/kanade/tachiyomi/extension/ExtensionManager.kt`): المدير المركزي لتتبع الإضافات المثبتة والمتاحة والمستودعات والتحديثات.
- **`ExtensionStore` / Interactors** (`domain/src/main/java/mihon/domain/extension/`): إدارة متاجر الإضافات (`GetExtensionStores`، `AddExtensionStore`، `RemoveExtensionStore`، `UpdateExtensionStores`).
- **`ExtensionLoader.kt`** (`app/src/main/java/eu/kanade/tachiyomi/extension/util/ExtensionLoader.kt`): اكتشاف حزم APK المثبتة وفحص التوقيعات وتحميل كلاساتها ديناميكياً بواسطة `PathClassLoader`.
- **`ExtensionInstaller.kt` & Installers** (`app/src/main/java/eu/kanade/tachiyomi/extension/installer/`): تنزيل وتثبيت ملفات APK للإضافات عبر `PackageInstaller` أو `Shizuku`.
- **`ExtensionStoresScreen.kt`** (`app/src/main/java/eu/kanade/presentation/more/settings/screen/browse/ExtensionStoresScreen.kt`): واجهة إدارة إضافة وتعديل روابط متاجر الإضافات.
- **`Source.kt`** (`source-api/src/commonMain/kotlin/eu/kanade/tachiyomi/source/Source.kt`): الواجهة الرئيسية التي تنفذها كافة مصادر المانغا.

### ج. نظام الثيمات والتصميم (Themes & Design System - M3 Expressive)
- **`AppTheme.kt`** (`app/src/main/java/eu/kanade/domain/ui/model/AppTheme.kt`): `Enum` يحتوي على كافة الثيمات المدعومة (Default, Monet, Catppuccin, Nord, Midnight Dusk, Yotsuba, Strawberry, Monochrome, YinYang, Tako, TidalWave, TealTurquoise, Lavender, GreenApple).
- **`TachiyomiTheme.kt`** (`app/src/main/java/eu/kanade/presentation/theme/TachiyomiTheme.kt`): المكون الأساسي المحتوي على `MaterialExpressiveTheme` لتطبيق ألوان ماتيريال 3 التعليمية Expressive على الثيم المختار، الوضع الليلي، وخيار AMOLED (الأسود الكامل).
- **مجلد `colorscheme/`** (`app/src/main/java/eu/kanade/presentation/theme/colorscheme/`): يحتوي على الكلاسات الخاصة بكل ثيم (مثل `MonetColorScheme.kt`، `CatppuccinColorScheme.kt`، `TachiyomiColorScheme.kt`...).
- **`UiPreferences.kt`** (`app/src/main/java/eu/kanade/domain/ui/UiPreferences.kt`): حفظ واسترجاع تفضيلات المظهر والألوان.

### د. نظام الإعدادات والتفضيلات وكيفية إضافة إعداد جديد
- **`SettingsMainScreen.kt`** (`app/src/main/java/eu/kanade/presentation/more/settings/screen/SettingsMainScreen.kt`): الشاشة الرئيسية للإعدادات الموزعة على الأقسام (مظهر، مكتبة، قارئ، تنزيلات، تصفح، بيانات، أمان، إعدادات متقدمة، إعدادات EHentai...).
- **`SearchableSettings.kt`** (`app/src/main/java/eu/kanade/presentation/more/settings/screen/SearchableSettings.kt`): الواجهة الخاصة بالبحث في كافة عناصر الإعدادات.
- **`Preference.kt`** (`app/src/main/java/eu/kanade/presentation/more/settings/Preference.kt`): هيكل `Sealed Class` يمثل أنواع عناصر الإعدادات (`PreferenceGroup`, `SwitchPreference`, `ListPreference`, `CustomPreference`...).
- **خطوات إضافة إعداد جديد**:
  1. إنشاء مفتاح التفضيل في كلاس التفضيلات المناسب (مثل `UiPreferences.kt` أو `ExhPreferences.kt`) وربطه مع `PreferenceStore`.
  2. في شاشة الإعدادات الخاصة بالقسم (مثل `SettingsAppearanceScreen.kt`)، قم بجمع قيمة التفضيل بـ `collectAsState()`.
  3. أضف عنصر `Preference.PreferenceItem` جديد داخل دالة `getPreferences()` ضمن المجموعة المخصصة (`PreferenceGroup`).

### هـ. نظام تنزيل الفصول (Downloader System)
- **`DownloadManager.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/DownloadManager.kt`): الواجهة البرمجية العالية لإدارة التنزيلات وحذف الفصول المنزلة.
- **`Downloader.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/Downloader.kt`): المحرك التنفيذي للطابور؛ يحمل صور الصفحات عبر OkHttp من الـ `HttpSource` مع المعالجة وإعادة المحاولة والضغط في ملفات CBZ مع كتابة `ComicInfo.xml`.
- **`DownloadProvider.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/DownloadProvider.kt`): تحديد مسارات التخزين وتسمية الملفات والمجلدات.
- **`DownloadCache.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/DownloadCache.kt`): ذاكرة مؤقتة للتحقق السريع من الفصول المكتملة على القرص الصلب.
- **`DownloadJob.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/DownloadJob.kt`): خدمة `WorkManager` خلفية تشغل `Downloader` وتحدث إشعار التنزيل المباشر عبر `DownloadNotifier`.

---

## 4. نقطة الدخول وحقن التبعيات (Application Entry Point & DI)

- **كلاس التطبيق الرئيسي (`App.kt`)**:
  - الكلاس الرئيسي هو **`eu.kanade.tachiyomi.App`** الموجود في `app/src/main/java/eu/kanade/tachiyomi/App.kt` والوارث من `android.app.Application`.
  - الميزات المسجلة في `onCreate()`:
    - تهيئة Firebase (`FirebaseConfig.init`).
    - معالج الانهيارات العالمي (`GlobalExceptionHandler` -> `CrashActivity`).
    - دعم TLS 1.3 للأجهزة القديمة عبر Conscrypt.
    - تهيئة محمل الصور العالمي `Coil 3` (`SingletonImageLoader.Factory`).
    - تهيئة نظام السجلات XLog و Redirect Logcat.

- **نظام حقن التبعيات (Injekt & Koin Bridge)**:
  - يستخدم التطبيق **Injekt** (`uy.kohesive.injekt.Injekt`) لإدارة الخدمات والـ ViewModels، مع **Koin** عبر جسر الربط `InjektKoinBridge`.
  - تسجيل الموديولات داخل `App.kt`:
    ```kotlin
    Injekt.importModule(PreferenceModule(this))
    Injekt.importModule(AppModule(this))
    Injekt.importModule(DomainModule())
    Injekt.importModule(SYPreferenceModule(this)) // موديول تفضيلات SY
    Injekt.importModule(SYDomainModule())         // موديول مجالات SY
    InjektKoinBridge.startKoin(this)               // تشغيل Koin
    ```

---

## 5. خريطة مجلدات الواجهات والشاشات (`ui/` & `presentation/`)

تحتوي وحدة `:app` على تقسيم مزدوج بين المنطق والتنقل (`ui/`) والواجهات الرسومية المصممة بـ Compose (`presentation/`):

1. **المكتبة (Library)**:
   - **المنطق والتنقل**: `app/src/main/java/eu/kanade/tachiyomi/ui/library/` (`LibraryScreenModel.kt`, `LibraryTab.kt`)
   - **واجهات Compose**: `app/src/main/java/eu/kanade/presentation/library/` (`LibraryContent.kt`, `LibraryToolbar.kt`, `components/`)

2. **القارئ ومحرك العرض (Reader Engine)**:
   - **النشاط والـ ViewModel**: `app/src/main/java/eu/kanade/tachiyomi/ui/reader/` (`ReaderActivity.kt`, `ReaderViewModel.kt`)
   - **محركات العرض والتحميل**: `app/src/main/java/eu/kanade/tachiyomi/ui/reader/viewer/` (Webtoon, Pager), `app/src/main/java/eu/kanade/tachiyomi/ui/reader/loader/` (Http, Archive, Directory, Download)
   - **واجهات Compose والتحكم**: `app/src/main/java/eu/kanade/presentation/reader/` (`ReaderScreen.kt`, `appbars/`, `controls/`)

3. **التصفح والإضافات والتغذية (Browse, Extensions & Feed)**:
   - **المنطق والتنقل**: `app/src/main/java/eu/kanade/tachiyomi/ui/browse/` (`BrowseTab.kt`, `source/`, `extension/`, `feed/`, `migration/`)
   - **واجهات Compose**: `app/src/main/java/eu/kanade/presentation/browse/` (`SourcesScreen.kt`, `ExtensionsScreen.kt`, `FeedScreen.kt`, `BrowseSourceScreen.kt`)

4. **تفاصيل المانغا (Manga Details)**:
   - **المنطق والتنقل**: `app/src/main/java/eu/kanade/tachiyomi/ui/manga/` (`MangaScreen.kt`, `MangaScreenModel.kt`, `EditMangaDialog.kt`, `merged/`)
   - **واجهات Compose**: `app/src/main/java/eu/kanade/presentation/manga/` (`MangaScreen.kt`, `components/`)

5. **السجل والتحديثات (History & Updates)**:
   - **السجل (History)**: `app/src/main/java/eu/kanade/tachiyomi/ui/history/` & `app/src/main/java/eu/kanade/presentation/history/`
   - **التحديثات (Updates)**: `app/src/main/java/eu/kanade/tachiyomi/ui/updates/` & `app/src/main/java/eu/kanade/presentation/updates/`

6. **نظام التصميم العام (`:presentation-core`)**:
   - `presentation-core/src/main/java/tachiyomi/presentation/core/`: يحتوي على الثيمات والقطع البرمجية الصغرى (`components/` مثل `Scaffold`, `LoadingScreen`, `EmptyScreen`, `Buttons`).

---

## 6. خريطة الميزات الحصرية لفرع TachiyomiSY (`exh/`)

تتركز الميزات الخاصة بـ SY في مجلد `app/src/main/java/exh/` والملفات المرتبطة بها في `:data` و `:domain`:

1. **تكاملات E-Hentai / ExHentai / NHentai ومرضيات البيانات الوصفية**:
   - **المحرك والأدوات**: `app/src/main/java/exh/eh/` (`EHentaiClient.kt`, `EHentaiUpdateWorker.kt`, `Metadata.kt`)
   - **عرض البيانات الوصفية (Metadata Viewer)**: `app/src/main/java/exh/ui/metadata/` (`MetadataViewScreen.kt`)
   - **تسجيل الدخول والتكوين**: `app/src/main/java/exh/ui/login/EhLoginActivity.kt`, `exh/uconfig/`
   - **مفضلات E-Hentai الأونلاين**: `app/src/main/java/exh/favorites/`
   - **جداول SQLDelight الخاصة بـ EH**: `data/src/main/sqldelight/tachiyomi/data/eh.sq`, `eh_favorites.sq`, `search_metadata.sq`, `search_tags.sq`, `search_titles.sq`

2. **التغذية المخصصة، البحث الذكي والمصدر المدمج (Feed, Smart Search & Merged Source)**:
   - **التغذية والأبحاث المحفوظة (Feed & Saved Search)**: `app/src/main/java/eu/kanade/tachiyomi/ui/browse/feed/`, `app/src/main/java/eu/kanade/presentation/browse/FeedScreen.kt`, `data/src/main/sqldelight/tachiyomi/data/feed_saved_search.sq`
   - **البحث الذكي (Smart Library Search)**: `app/src/main/java/exh/smartsearch/SmartLibrarySearchEngine.kt`, `app/src/main/java/exh/ui/smartsearch/`
   - **المصدر المدمج (Merged Source)**: `app/src/main/java/eu/kanade/tachiyomi/source/online/all/MergedSource.kt`, `app/src/main/java/eu/kanade/tachiyomi/ui/manga/merged/`, `data/src/main/sqldelight/tachiyomi/data/merged.sq`
   - **نظام الاقتراحات (Recommendations)**: `app/src/main/java/exh/recs/` (`RecommendsScreen.kt`, `sources/`)
   - **معاينة الصفحات قبل القراءة (Page Previews)**: `app/src/main/java/exh/pagepreview/`

3. **إعدادات وتكشيش SY الحصرية**:
   - **تفضيلات SY / ExHentai**: `domain/src/main/java/exh/source/ExhPreferences.kt`
   - **حقن تبعيات SY**: `app/src/main/java/eu/kanade/tachiyomi/di/SYPreferenceModule.kt`, `app/src/main/java/eu/kanade/domain/SYDomainModule.kt`
   - **شاشات إعدادات SY**: `app/src/main/java/eu/kanade/presentation/more/settings/screen/SettingsEhScreen.kt`
   - **هجرات قاعدة البيانات والتفضيلات المخصصة لـ SY**: `app/src/main/java/exh/EXHMigrations.kt`

---

## 7. دليل التنفيذ والتعديلات السريعة (Quick Development Guide)

| المهمة المطلوبة | المسار والملفات المعنية |
| :--- | :--- |
| **إضافة شاشة جديدة (Screen / Voyager / ScreenModel)** | 1. إنشاء الـ `Screen` والـ `ScreenModel` في `app/src/main/java/eu/kanade/tachiyomi/ui/<feature>/`<br>2. بناء واجهة Compose في `app/src/main/java/eu/kanade/presentation/<feature>/`<br>3. ربط التنقل عبر Voyager في التبويب أو الشاشة الأب |
| **إضافة أو تعديل جدول / استعلام في قاعدة البيانات (SQLDelight)** | 1. تعديل ملف `.sq` المناسب في `data/src/main/sqldelight/tachiyomi/data/`<br>2. إنشاء ملف هجرة جديد `.sqm` بزيادة المفهوم العددي في `data/src/main/sqldelight/tachiyomi/migrations/`<br>3. تحديث واجهات الـ Repository في `:domain` والتنفيذ في `:data` |
| **إضافة خيار جديد في الإعدادات (Preferences & Settings)** | 1. تعريف مفتاح التفضيل في كلاس التفضيلات المناسب في `:domain` (مثل `UiPreferences.kt` أو `ExhPreferences.kt`)<br>2. تجميع القيمة بـ `collectAsState()` في شاشة الإعدادات المناسبة بـ `app/src/main/java/eu/kanade/presentation/more/settings/screen/`<br>3. إضافة `Preference.PreferenceItem` ضمن القائمة المعنية |
| **تعديل أو إضافة ثيم / ألوان جديدة (Themes & Color Schemes)** | 1. إضافة اسم الثيم في `app/src/main/java/eu/kanade/domain/ui/model/AppTheme.kt`<br>2. إنشاء ملف ألوان في `app/src/main/java/eu/kanade/presentation/theme/colorscheme/<Name>ColorScheme.kt`<br>3. ربط الملف في `app/src/main/java/eu/kanade/presentation/theme/TachiyomiTheme.kt` |
| **تعديل القارئ أو محرك التصفح (Reader Engine)** | 1. النشاط والـ ViewModel الرئيسي: `app/src/main/java/eu/kanade/tachiyomi/ui/reader/ReaderActivity.kt` و `ReaderViewModel.kt`<br>2. محركات عرض الصفحات (Pager/Webtoon): `app/src/main/java/eu/kanade/tachiyomi/ui/reader/viewer/`<br>3. محملات الصفحات (Http/Download/Archive): `app/src/main/java/eu/kanade/tachiyomi/ui/reader/loader/` |
| **إضافة أو تعديل متجر إضافات (Extension Stores)** | 1. الكيانات والتفاعلات: `domain/src/main/java/mihon/domain/extension/`<br>2. شاشة المتاجر والإدارة: `app/src/main/java/eu/kanade/presentation/more/settings/screen/browse/ExtensionStoresScreen.kt`<br>3. قاعدة البيانات: `data/src/main/sqldelight/tachiyomi/data/extension_store.sq` |
| **إضافة أو تعديل منصة تتبع (Tracking Integration)** | 1. التعريف والخدمة: `app/src/main/java/eu/kanade/tachiyomi/data/track/` (`TrackerManager.kt`, `TrackService.kt`)<br>2. النماذج والتفاعلات: `domain/src/main/java/eu/kanade/domain/track/`<br>3. الواجهات الحوارية: `app/src/main/java/eu/kanade/presentation/manga/components/TrackInfoDialog.kt` |

---

## 8. نظام التتبع والخدمات الخارجية (Tracking System)

- **المنطق والتفاعلات (Domain)**: `domain/src/main/java/eu/kanade/domain/track/` (مجلدات `interactor/`, `model/`, `service/`).
- **خدمات التتبع والمدراء**: `app/src/main/java/eu/kanade/tachiyomi/data/track/` (`TrackerManager.kt`، `TrackService.kt`، `TrackStatus.kt` والمشغلات مثل `AniList.kt`، `MyAnimeList.kt`، `Kitsu.kt`، `MangaUpdates.kt`، `Bangumi.kt`، `Shikimori.kt`).
- **المهمة الخلفية للتحديث المؤجل**: `eu.kanade.domain.track.service.DelayedTrackingUpdateJob`.
- **واجهة ربط المانغا بالخدمات**: `app/src/main/java/eu/kanade/presentation/manga/components/TrackInfoDialog.kt`.

---

## 9. نظام النسخ الاحتياطي والاستعادة (Backup & Restore)

- **إنشاء النسخ الاحتياطية (Backup Creation)**: `app/src/main/java/eu/kanade/tachiyomi/data/backup/create/creators/` (`MangaBackupCreator.kt`، `CategoriesBackupCreator.kt`، `ExtensionStoresBackupCreator.kt`...).
- **استعادة البيانات (Backup Restore)**: `app/src/main/java/eu/kanade/tachiyomi/data/backup/restore/restorers/` (`MangaRestorer.kt`، `CategoriesRestorer.kt`، `ExtensionStoreRestorer.kt`، `PreferenceRestorer.kt`...).
- **نماذج البيانات والمخططات (Models & Proto)**: `app/src/main/java/eu/kanade/tachiyomi/data/backup/models/` (`BackupManga.kt`، `BackupChapter.kt`، `BackupSource.kt`، `BackupExtensionStore.kt`).
- **المهام الخلفية للنسخ والاستعادة**: `BackupCreateJob.kt` و `BackupRestoreJob.kt`.

---

## 10. نظام المزامنة المباشرة (Sync System)

- **المحرك ومدير المزامنة**: `app/src/main/java/eu/kanade/tachiyomi/data/sync/` (`SyncManager.kt`، `SyncDataJob.kt`، `SyncService.kt`).
- **تفضيلات ونماذج المزامنة**: `domain/src/main/java/eu/kanade/domain/sync/` (`SyncPreferences.kt`، `models/SyncSettings.kt`، `SyncNotifier.kt`).

---

## 11. الأمان والخصوصية (Security & Privacy)

- **قفل البيومترية والبصمة**: `app/src/main/java/eu/kanade/tachiyomi/ui/security/UnlockActivity.kt`.
- **حماية الشاشة ومنع الالتقاط (Secure Screen)**: `app/src/main/java/eu/kanade/tachiyomi/ui/base/delegate/SecureActivityDelegate.kt`.
- **وضع التخفي (Incognito Mode)**: `app/src/main/java/eu/kanade/tachiyomi/core/security/PrivacyPreferences.kt` و `BasePreferences.incognitoMode`.

---

## 12. معالجة وتحميل الصور والذاكرة المؤقتة (Coil 3 & Image Caching)

- **محمل الصور العالمي (Coil 3 Engine)**:
  - تهيئة `SingletonImageLoader.Factory` في `App.kt`.
  - جالبات ومفاتيح وفك ضغط الصور المخصصة في `app/src/main/java/eu/kanade/tachiyomi/data/coil/`:
    - `MangaCoverFetcher.kt` & `MangaCoverKeyer.kt` (تحميل أغلفة المانغا)
    - `PagePreviewFetcher.kt` & `PagePreviewKeyer.kt` (تحميل معاينات الصفحات)
    - `BufferedSourceFetcher.kt` & `TachiyomiImageDecoder.kt` (فك ضغط ومعالجة الصور)
- **الذاكرة المؤقتة للقرص (Disk Caches)**:
  - ذاكرة الأغلفة: `app/src/main/java/eu/kanade/tachiyomi/data/cache/CoverCache.kt`
  - ذاكرة الفصول والصور: `app/src/main/java/eu/kanade/tachiyomi/data/cache/ChapterCache.kt`

---

## 13. الهجرات ومهام خلفية WorkManager (Migrations & WorkManager Jobs)

- **هجرات الأكواد والتفضيلات (App Migrations)**:
  - هجرات Mihon الرسمية: `app/src/main/java/mihon/core/migration/Migrator.kt` ومجلد `migrations/`.
  - هجرات فرع TachiyomiSY الحصرية: `app/src/main/java/exh/EXHMigrations.kt`.
- **قائمة مهام WorkManager الرئيسية في المشروع**:
  - `LibraryUpdateJob.kt`: تحديث الفصول والمكتبة دورياً.
  - `DownloadJob.kt`: تشغيل طابور تنزيل الفصول.
  - `EHentaiUpdateWorker.kt`: تحديث ميتا ووسوم E-Hentai في الخلفية.
  - `MetadataUpdateJob.kt`: تحديث الميتا العامة للمانغا.
  - `BackupCreateJob.kt` / `BackupRestoreJob.kt`: إنشاء واستعادة النسخ الاحتياطية.
  - `SyncDataJob.kt`: مزامنة البيانات المباشرة بين الأجهزة.
  - `DelayedTrackingUpdateJob.kt`: تحديث تقدم القراءة المؤجل على خدمات التتبع.
  - `AppUpdateDownloadJob.kt`: تنزيل تحديثات التطبيق.

---

## 14. مخطط قاعدة البيانات SQLDelight (SQLDelight Data Schemas & Views)

تتواجد كافة ملفات مخططات SQLDelight داخل المجلد `data/src/main/sqldelight/tachiyomi/`:

### أ. الجداول (`data/`):
1. **`categories.sq`**: جدول الفئات والتصنيفات وترتيبها وخصائص العرض.
2. **`chapters.sq`**: جدول فصول المانغا (رقم الفصل، الاسم، حالة القراءة، العلامات المرجعية، رقم آخر صفحة، تاريخ التنزيل).
3. **`eh.sq`**: جدول بيانات وميتا E-Hentai/ExHentai (gid, token, title, rating, uploader, posted...).
4. **`eh_favorites.sq`**: جدول مفضلات E-Hentai أونلاين ومزامنة المجلدات البعيدة.
5. **`excluded_scanlators.sq`**: جدول استثناء وتصفية فرق الترجمة المحددة لكل مانغا.
6. **`extension_store.sq`**: جدول متاجر ومستودعات الإضافات الخارجية المثبتة (رابط المؤشر، الاسم، المفتاح...).
7. **`feed_saved_search.sq`**: جدول ربط الأبحاث المحفوظة بتبويب التغذية الشاملة (Feed).
8. **`history.sq`**: جدول سجل القراءة المحددة زمنياً وتدقيق التوقف.
9. **`manga_sync.sq`**: جدول تزامن وربط المانغا مع خدمات التتبع الخارجي (AniList, MAL...).
10. **`mangas.sq`**: جدول المانغا الرئيسي (العنوان، المؤلف، الوصف، المصدر، الحالة، الغلاف، التفضيل، تاريخ الإضافة).
11. **`mangas_categories.sq`**: جدول العلاقات متعدد-إلى-متعدد بين المانغا والفئات.
12. **`merged.sq`**: جدول دمج المانغا من مصادر متعددة (Merged Sources).
13. **`saved_search.sq`**: جدول الأبحاث والفلاتر المحفوظة لكل مصدر.
14. **`search_metadata.sq`**: جدول بيانات البحث المتقدم للميتا الخاص بـ ExHentai/MangaDex.
15. **`search_tags.sq`**: جدول الوسوم والأوسام التفصيلية للبحث المتقدم.
16. **`search_titles.sq`**: جدول العناوين البديلة والمترجمة للمانغا للبحث.
17. **`sources.sq`**: جدول المصادر المثبتة والمفضلة والمقطوعة (Stub Sources).

### ب. العروض (`view/`):
- **`historyView.sq`**: عرض مدمج بين سجل القراءة والفصول والمانغا.
- **`libraryView.sq`**: عرض مدمج للمكتبة يجمع المانغا مع التصنيفات وعدادات الفصول غير المقروءة والمنزلة.
- **`updatesView.sq`**: عرض مدمج لأحدث الفصول المضافة مع بيانات المانغا للتحديثات.

---

## 15. فهرس مجلد `exh/` التفصيلي (SY Directory Index)

كافة المجلدات والملفات داخل `app/src/main/java/exh/`:

- **`eh/`**: المحرك التنفيذي، العميل، الوسوم، وتحديث البيانات الخاصة بـ E-Hentai/ExHentai.
- **`md/`**: تكاملات ومحركات API الخاصة بـ MangaDex (Auth, Follows, Ratings, Parser).
- **`favorites/`**: المزامنة والتبديل المباشر لمفضلات E-Hentai أونلاين.
- **`uconfig/`**: معالج وتكوين تفضيلات إعدادات حساب E-Hentai أونلاين (EH UConfig Parser).
- **`pagepreview/`**: شاشات ومكونات المعاينة السريعة لشاشات الفصول قبل أو أثناء القراءة.
- **`recs/`**: محرك المقترحات وتوصيات القراءة المجلوبة من الخدمات الخارحية (MangaDex, MAL).
- **`search/`**: محرك تحليل بنيات الوسوم المتقدمة والرمز البديل (Wildcards Query Parser).
- **`smartsearch/`**: خوارزميات البحث الذكي (`SmartLibrarySearchEngine.kt`) لمطابقة المانغا عبر المصادر.
- **`source/`**: إدارة ومعالجات وتصفية المصادر الخاصة بفرع SY و `MergedSource`.
- **`ui/`**: الشاشات المتنوعة الخاصة بـ SY (تسجيل الدخول، المعاينة، الميتا، إضافة الدفعة، البحث الذكي).
- **`log/`**: تسجيل الأحداث، توجيه Logcat لـ XLog، والطباعة على الملفات و Crashlytics.
- **`util/`**: أدوات مساعدة عامة لفرع SY (معالجة النصوص، روابط EH، التصفية).
- **`debug/`**: أدوات وشاشات التصحيح والتلوين للمطورين.
- **`assets/`**: الشعارات والأيقونات المضمنة كوداً (مثل شعار ExHentai و MangaDex).
- **`EXHMigrations.kt`**: هجرات قاعدة البيانات والتفضيلات المخصصة لـ SY.
- **`GalleryAdder.kt`**: استيراد وتنزيل المعارض بالروابط أو الأوامر الضمنية (Intents).

---

## 16. نظام البناء وبلاجنات Gradle (Build Logic & Catalogs)

الملفات داخل `gradle/build-logic/src/main/kotlin/`:

- **`PluginAndroidApplication.kt`**: إعدادات تطبيق Android الرئيسي (`tachiyomi.android.application`).
- **`PluginAndroidBase.kt`**: الخصائص والإعدادات المشتركة لجميع موديولات Android.
- **`PluginAndroidLibrary.kt`**: الإعدادات الموحدة لموديولات المكتبات (`tachiyomi.android.library`).
- **`PluginAndroidTest.kt`**: إعدادات موديولات الاختبار والتجريب القياسية.
- **`PluginComposeAndroid.kt`**: إعدادات إضافة Jetpack Compose وكومبايلر الواجهات (`tachiyomi.android.library.compose`).
- **`PluginKotlinMultiplatform.kt`**: تهيئة موديولات Kotlin Multiplatform (`tachiyomi.kmp`).
- **`PluginSpotless.kt`**: ضبط وتطبيق معايير تنسيق الكود عبر Spotless (`tachiyomi.spotless`).

---

## 17. فهرس مجلدات الشاشات والواجهات (`ui/` & `presentation/`)

المجلدات داخل `app/src/main/java/eu/kanade/tachiyomi/ui/` و `app/src/main/java/eu/kanade/presentation/`:

### أ. المنطق والتنقل والـ ViewModels (`ui/`):
- **`base/`**: الكلاسات المرجعية والمفوضات المشتركة (`BaseActivity`, `SecureActivityDelegate`).
- **`browse/`**: إدارة التصفح، قائمة المصادر، الإضافات، التغذية، والاندماج.
- **`category/`**: إدارة تصنيفات وفئات المكتبة.
- **`deeplink/`**: استقبال ومعالجة الروابط العميقة (Deep Links).
- **`download/`**: قائمة وطابور التنزيلات الحالية.
- **`history/`**: منطق سجل القراءة والتفاعل مع التاريخ.
- **`home/`**: الشاشة الحاضنة الرئيسية والشريط السفلي للتنقل.
- **`library/`**: منطق المكتبة وإعدادات الفرز والتصفية.
- **`main/`**: النشاط الرئيسي (`MainActivity`).
- **`manga/`**: منطق شاشة تفاصيل المانغا، التعديل، الدمج، والتتبع.
- **`more/`**: شاشة المزيد، تحديثات التطبيق، والبدء.
- **`reader/`**: منطق القارئ، الشاشات، وتحميل الصفحات.
- **`security/`**: شاشات القفل والأمان والبصمة (`UnlockActivity`).
- **`setting/`**: أنشطة تسجيل الدخول وتسجيل التتبع OAuth.
- **`stats/`**: إحصائيات قراءة المستخدم.
- **`updates/`**: منطق شاشة أحدث الفصول المضافة.
- **`webview/`**: شاشات المتصفح المدمج لتجاوز التحديات.

### ب. واجهات Compose العرضية (`presentation/`):
- **`browse/`**: واجهات شبكات المصادر، الإضافات، التغذية، والبحث.
- **`category/`**: واجهات قائمة وترتيب الفئات.
- **`components/`**: المكونات الموحدة العامة (الأشرطة، الأزرار، البطاقات، الحوارات).
- **`crash/`**: شاشة عرض تفاصيل الانهيارات وتصدير السجلات.
- **`history/`**: واجهات بطاقات وسجل القراءة.
- **`library/`**: شبكات وقوائم عرض المكتبة والأشرطة والتصفية.
- **`manga/`**: واجهة تفاصيل المانغا، الهيدر، الفصول، والتتبع.
- **`more/`**: شاشات المزيد والخيارات العامة.
- **`reader/`**: أشرطة القارئ، خيارات العرض، وعوارض اللمس.
- **`stats/`**: الرسوم البيانية والإحصائيات.
- **`theme/`**: بناء ثيم التطبيق ومجموعات الألوان.
- **`track/`**: حوارات ومكونات خدمات التتبع.
- **`updates/`**: بطاقات وقائمة التحديثات الجديدة.
- **`webview/`**: واجهة الحاضنة للمتصفح المدمج.

---

## 18. أمثلة تدفق البيانات الحقيقية (Data Flow Examples)

1. **سيناريو تحديث المكتبة وجلب الفصول خلفياً**:
   - `LibraryUpdateJob.startNow()` -> `WorkManager` تشغل `LibraryUpdateJob.doWork()`
   - `SyncChaptersWithSource.await()` -> الاتصال بـ `HttpSource.getChapterList(manga)`
   - تقييم الفصول ومقارنتها بقاعدة البيانات المحليه عبر الاستعلامات
   - `UpdateChapter` -> كتابة الفصول الجديدة في SQLDelight (`chapters.sq`)
   - `FilterChaptersForDownload` -> تصفية الفصول المكتشفة وتمريرها لـ `DownloadManager.downloadChapters()`

2. **سيناريو تنزيل فصل مانغا وحفظه محلياً**:
   - `MangaScreen` يرسل حدث التنزيل -> `DownloadManager.downloadChapters()`
   - `Downloader.enqueue()` -> إدراج التنزيلات في الطابور وتشغيل `DownloadJob`
   - `Downloader.downloadChapter()` -> الاتصال بـ `source.getPageList()` ثم `source.fetchImage()` لكل صفحة
   - `ZipWriter` ضغط الصور وصياغة ملف `ComicInfo.xml` في المجلد المحلي عبر `DownloadProvider`
   - `DownloadCache` تحديث الذاكرة المحلية وإطلاق الإشعار عبر `DownloadNotifier`

3. **سيناريو البحث الذكي والمصدر المدمج (Smart Search & Merged Source Flow)**:
   - `SmartSearchScreen` ينادي `SmartSearchScreenModel.search()`
   - `SmartLibrarySearchEngine.search()` يرسل طلبات البحث الموازية للمصادر المختارة
   - حساب مطابقة العناوين وتحديد المانغا المناسبة
   - `MergedSource` تدمج نتائج الفصول والبيانات وتكتبها في `merged.sq`
   - `MangaScreenModel` يعرض المانغا المدمجة بفصولها المشتركة

---

## 19. الموارد والنصوص والترجمة (Resources & Localization)

- **بنية موديولات الموارد (Moko Resources)**:
  - **`:i18n`**: النصوص العامة للتطبيق القياسي (`i18n/src/commonMain/moko-resources/base/strings.xml` والترجمات في `ar/`, `es/`, `fr/`...).
  - **`:i18n-sy`**: النصوص الحصرية لفرع SY (`i18n-sy/src/commonMain/moko-resources/base/strings.xml` والترجمات).
- **خطوات إضافة نص مترجم جديد**:
  1. إضافة العنصر في `base/strings.xml` بالموديول المناسب: `<string name="my_key">English Text</string>`.
  2. إضافة نفس المفتاح في ترجمة اللغة المستهدفة (مثل `ar/strings.xml` للغة العربية): `<string name="my_key">النص العربي</string>`.
  3. استدعاء النص في واجهات Compose عبر `stringResource(MR.strings.my_key)` أو `stringResource(SYMR.strings.my_key)`.

---

## 20. نظام المصادر والإضافات الخارجي (Keiyoushi Extensions Ecosystem)

⚠️ **تنبيه هاديء للأداء والتطوير**: كود المصادر والمواقع (المواقع التي تُجلب منها المانغا) **غير موجود** داخل هذا المشروع نفسه. موديول `:source-api` يحتوي فقط على الواجهات (Interfaces) والنماذج العامة التي يجب أن تلتزم بها أي إضافة.

تدار الإضافات عبر مشروعين خارجيين منفصلين:
- **الكود المصدري للإضافات**: [Keiyoushi Extensions Source](https://github.com/keiyoushi/extensions-source) (مشروع Gradle مستقل بالكامل، يُعدّل لإصلاح أو إضافة أي مصدر معطل).
- **فهرس الإضافات (Extension Stores)**: يتم إضافة رابط متجر الإضافات (مثل Keiyoushi Store) في شاشة **تصفح -> متاجر الإضافات** (`ExtensionStoresScreen.kt`) ليقوم التطبيق بقراءة قائمة ملفات APK المتاحة وتنزيلها وتثبيتها برمجياً.
