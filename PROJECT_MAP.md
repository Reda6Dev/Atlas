# PROJECT_MAP.md — خريطة بنية مشروع TachiyomiSY

---

## 1. نظرة عامة على المعمارية (Architecture Overview)

يعتمد المشروع على معمارية الطبقات الأحادية الموجهة (Clean Architecture) مع الفصل التام بين منطق الأعمال والبيانات والواجهات:

1. **طبقة العرض (Presentation Layer)**:
   - تضم `:presentation-core` (نظام التصميم المشترك الثيمات والألوان والمكونات الأساسية)، و `:presentation-widget` (ودجت الشاشة الرئيسية)، وباقي الشاشات والمكونات الموجودة داخل وحدة `:app`.
   - تعتمد كلياً على **Jetpack Compose** لبناء الواجهات مع مكتبة **Voyager** لإدارة التنقل بين الشاشات، وإدارة الحالة عبر `ScreenModel` / `ViewModel`.

2. **طبقة المجال (Domain Layer)**:
   - تمثلها وحدة `:domain` بالإضافة إلى `:source-api`.
   - كود بلغة Kotlin صافية بدون أي اعتماديات على إطار عمل Android أو واجهات المستخدم.
   - تضم الكائنات الأساسية (Entities: `Manga`, `Chapter`, `Category`, `Source`, `Track`)، وواجهات المستودعات (Repository Interfaces)، والمجالات والتفاعلات (Interactors / Use Cases مثل `GetLibraryManga`, `SyncChaptersWithSource`, `UpdateManga`, `DownloadChapter`).

3. **طبقة البيانات (Data Layer)**:
   - تمثلها وحدة `:data` ووحدات مساعدة مثل `:source-local` و `:core-metadata`.
   - تنفذ واجهات مستودعات الـ Domain باستخدام قاعدة بيانات **SQLDelight**، وعملاء الشبكة (**OkHttp**)، وإدارة الملفات والتخزين المحلي، وحفظ التفضيلات عبر `PreferenceStore`.

4. **وحدة التطبيق الرئيسية (`:app`)**:
   - تعتبر نقطة التجميع والتنسيق المركبة؛ تحتوي على صنف التطبيق (`App`), وإعدادات حقن التبعيات (DI)، والخدمات والمهام الخلفية (`WorkManager Services`), والأنشطة الرئيسية (`MainActivity`), والشاشات التنفيذية.

---

## 2. أدوار وحدات Gradle الرئيسية (Gradle Modules)

- **`app`**: التطبيق الرئيسي؛ يحتوي على الشاشات وواجهات Compose، والـ ViewModels، والأنشطة والخدمات الخلفية (Workers/Services)، وإعدادات DI.
- **`domain`**: منطق الأعمال الصافي؛ يحتوي على النماذج (Models)، وواجهات المستودعات (Repositories)، والتفاعلات (Interactors).
- **`data`**: التنفيذ الفعلي لطبقة البيانات؛ يضم قاعدة بيانات SQLDelight، الاتصالات الشبكية، التخزين الداخلي، وتغذية الـ Domain بالبيانات.
- **`core` (`:core:common`)**: الأدوات الأساسية والخدمات المساعدة المشتركة (Loggers, Threading Dispatchers, IO Helpers, `PreferenceStore`).
- **`core-metadata`**: معالجة واستخراج واسترجاع البيانات الوصفية الخاصة بالمانغا والكومكس (مثل صيغ `ComicInfo.xml` وميتا Tachiyomi).
- **`i18n` & `i18n-sy`**: النصوص والموارد المترجمة لغات متعددة (مبنية على Moko Resources ونصوص النسخة الخاصة SY).
- **`source-api`**: الواجهات البرمجية العامة التي تنفذها الإضافات والمصادر (`Source`, `HttpSource`, `SChapter`, `SManga`).
- **`source-local`**: إدارة وتوفير المانغا المحفوظة محلياً على الجهاز عبر المجلدات أو الملفات المضغوطة (ZIP/CBZ).
- **`presentation-core`**: نظام التصميم الموحد (Design System) المكتوب بـ Compose (الثيمات، الألوان، الخطوط، المكونات Reusable).
- **`presentation-widget`**: إدارات ومكونات الودجت للشاشة الرئيسية لنظام آندرويد.

---

## 3. الأنظمة المحورية الرئيسية (Core Systems)

### أ. نظام تحديث المكتبة وجلب الفصول الجديدة (Library Update)
- **`LibraryUpdateJob.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/library/LibraryUpdateJob.kt`):
  عامل خلفي (`WorkManager CoroutineWorker`) للتحديث الدوري أو اليدوي للمكتبة. يتعامل مع القيود (الشبكة، الشحن) وينظم التحديث الموازي مع التحكم بالسرعة عبر `Semaphore`.
- **`SyncChaptersWithSource.kt`** (`app/src/main/java/eu/kanade/domain/chapter/interactor/SyncChaptersWithSource.kt`):
  التفاعل المسؤول عن جلب قائمة الفصول من المصدر (`Source.getChapterList`) ومقارنتها بقاعدة البيانات المحلية لإدراج الفصول الجديدة وتحديث القديمة أو حذف المزال منها.
- **`LibraryUpdateNotifier.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/library/LibraryUpdateNotifier.kt`):
  إدارة إشعارات النظام الخاصة بالتقدم، وعدد الفصول الجديدة المكتشفة، والأخطاء التي تحدث أثناء التحديث.
- **`FilterChaptersForDownload.kt`** (`app/src/main/java/mihon/domain/chapter/interactor/FilterChaptersForDownload.kt`):
  تصفية الفصول المكتشفة حديثاً وتمريرها لنظام التنزيل بناءً على تفضيلات التنزيل التلقائي للمستخدم.

### ب. نظام الإضافات والمصادر (Extensions / Sources)
- **`ExtensionManager.kt`** (`app/src/main/java/eu/kanade/tachiyomi/extension/ExtensionManager.kt`):
  المُدير المركزي لتتبع الإضافات المثبتة والمتاحة والمستودعات والتحديثات، وإدارة حالات التثبيت والتحقق من الموثوقية.
- **`ExtensionLoader.kt`** (`app/src/main/java/eu/kanade/tachiyomi/extension/util/ExtensionLoader.kt`):
  اكتشاف حزم APK المثبتة على النظام وفحص التوقيعات (Signatures) وتحميل كلاسات المصادر ديناميكياً باستخدام `PathClassLoader`.
- **`ExtensionInstaller.kt`** (`app/src/main/java/eu/kanade/tachiyomi/extension/util/ExtensionInstaller.kt`):
  مسؤول عن تنزيل وتثبيت ملفات APK الخاصة بالإضافات عبر `PackageInstaller` أو نظام الأوامر الضمنية في Android.
- **`ExtensionApi.kt`** (`app/src/main/java/eu/kanade/tachiyomi/extension/api/ExtensionApi.kt`):
  جلب واستعلام الفهارس البرمجية من مستودعات الإضافات (`ExtensionRepo`) لمعرفة الإضافات الجديدة والتحديثات المتاحة.
- **`Source.kt`** (`source-api/src/commonMain/kotlin/eu/kanade/tachiyomi/source/Source.kt`):
  الواجهة البرمجية الأساسية التي تُعرف عمليات جلب المانغا، التفاصيل، الفصول، وصور الصفحات.

### ج. نظام الثيمات والألوان (Themes & Colors)
- **`AppTheme.kt`** (`app/src/main/java/eu/kanade/domain/ui/model/AppTheme.kt`):
  `Enum` يحتوي على كافة الثيمات المدعومة في التطبيق (Default, Monet, Catppuccin, Nord, Midnight Dusk, Yotsuba, Strawberry, Monochrome, Pure Red...).
- **`TachiyomiTheme.kt`** (`app/src/main/java/eu/kanade/presentation/theme/TachiyomiTheme.kt`):
  المكون الأساسي المشتمل على `MaterialExpressiveTheme` لتطبيق ألوان الماتيريال 3 بناءً على الثيم المختار، والوضع الليلي، وخيار AMOLED (الأسود الكامل).
- **مجلد `colorscheme/`** (`app/src/main/java/eu/kanade/presentation/theme/colorscheme/`):
  يحتوي على الفئات المحددة لكل ثيم (مثل `MonetColorScheme.kt`, `CatppuccinColorScheme.kt`, `TachiyomiColorScheme.kt`) لتوليد ألوان الوضع الفاتح والداكن.
- **`UiPreferences.kt`** (`app/src/main/java/eu/kanade/domain/ui/model/UiPreferences.kt`):
  حفظ واسترجاع تفضيلات المظهر المحددة من قبل المستخدم.

### د. شاشة الإعدادات وكيفية إضافة إعداد جديد
- **`SettingsMainScreen.kt`** (`app/src/main/java/eu/kanade/presentation/more/settings/screen/SettingsMainScreen.kt`):
  الشاشة الرئيسية للإعدادات التي تجمع الشاشات الفرعية (مظهر، مكتبة، قارئ، تنزيلات، إضافات، بيانات، أمان، إعدادات متقدمة...).
- **`SearchableSettings.kt`** (`app/src/main/java/eu/kanade/presentation/more/settings/screen/SearchableSettings.kt`):
  الواجهة التي تنفذها كل شاشة إعدادات فرعية وتستخدم `PreferenceScaffold` للرسم التلقائي للواجهات.
- **`Preference.kt`** (`app/src/main/java/eu/kanade/presentation/more/settings/Preference.kt`):
  هيكل `Sealed Class` يُمثل أنواع عناصر الإعدادات (`PreferenceGroup`, `SwitchPreference`, `ListPreference`, `CustomPreference` وغيرها).
- **خطوات إضافة إعداد جديد**:
  1. إنشاء حقل التفضيل في الواجهة المناسبة بـ `Preferences` (مثل `UiPreferences.kt`) واستخدامه مع `PreferenceStore`.
  2. في شاشة الإعدادات المعنية (مثل `SettingsAppearanceScreen.kt`)، جمع حالة التفضيل باستخدام `collectAsState()`.
  3. إضافة عنصر `Preference.PreferenceItem` جديد داخل قائمة `getPreferences()` ضمن المجموعة المخصصة (`PreferenceGroup`).

### هـ. نظام تحميل الفصول والصور (Downloader)
- **`DownloadManager.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/DownloadManager.kt`):
  الواجهة البرمجية الرئيسية (Façade) التي تستخدمها الشاشات والخدمات للتحكم بفرز وتنزيل وإيقاف واستئناف وحذف الفصول.
- **`Downloader.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/Downloader.kt`):
  المحرك التنفيذي المسير لطابور التحميل؛ يجلب صور الصفحات عبر OkHttp من الـ `HttpSource` المخصص مع معالجة إعادة المحاولة والأخطاء، وحفظ الملفات محلياً أو ضغطها في ملف CBZ وإضافة `ComicInfo.xml`.
- **`DownloadProvider.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/DownloadProvider.kt`):
  تحديد مسارات المجلدات والملفات وتسميتها بعد تنظيف العناوين من الرموز غير المسموحة.
- **`DownloadCache.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/DownloadCache.kt`):
  ذاكرة مؤقتة تحتفظ بقائمة الفصول المكتملة على القرص لتسريع الاستعلام.
- **`DownloadJob.kt`** (`app/src/main/java/eu/kanade/tachiyomi/data/download/DownloadJob.kt`):
  خدمة خلفية (`WorkManager` / `Foreground Service`) تشغل الـ `Downloader` وتحدث إشعار التنزيل المباشر عبر `DownloadNotifier`.

---

## 4. نقطة الدخول الرئيسية وتأسيس الحقن (Application & DI)

- **نقطة الدخول (Application Class)**:
  - الكلاس الرئيسي للتطبيق هو **`eu.kanade.tachiyomi.App`** الموجود في `app/src/main/java/eu/kanade/tachiyomi/App.kt` والوارث من `android.app.Application`.

- **نظام حقن التبعيات (Dependency Injection)**:
  - يعتمد التطبيق بشكل رئيسي على مكتبة **Injekt** (`uy.kohesive.injekt.Injekt`) لإدارة الحقن الخفيف (Service Locator / DI).
  - يتم تسجيل الموديولات داخل دالة `onCreate()` في `App.kt` بالتسلسل التالي:
    ```kotlin
    Injekt.importModule(PreferenceModule(this))
    Injekt.importModule(AppModule(this))
    Injekt.importModule(DomainModule())
    Injekt.importModule(SYPreferenceModule(this)) // الموديول الخاص بـ SY
    Injekt.importModule(SYDomainModule())         // الموديول الخاص بـ SY
    InjektKoinBridge.startKoin(this)              // جسر تكامل Koin
    ```
  - يستعين التطبيق بـ `InjektKoinBridge.startKoin(this)` كجسر لتشغيل Koin للمكونات المعتمدة عليه.

---

## 5. خريطة مجلدات الواجهات والشاشات الرئيسية (UI Directory Map)

تحتوي وحدة `:app` على تقسيم ثنائي بين منطق الشاشة والتنقل (`ui/`) والواجهات العرضية المكتوبة بـ Compose (`presentation/`):

1. **المكتبة (Library)**:
   - **المنطق والحالة**: `app/src/main/java/eu/kanade/tachiyomi/ui/library/` (`LibraryScreenModel.kt`, `LibraryTab.kt`, `LibrarySettingsScreenModel.kt`)
   - **مكونات Compose**: `app/src/main/java/eu/kanade/presentation/library/` (`LibraryContent.kt`, `LibraryToolbar.kt`, `components/`)

2. **القارئ ومحرك العرض (Reader Engine)**:
   - **النشاط والـ ViewModel**: `app/src/main/java/eu/kanade/tachiyomi/ui/reader/` (`ReaderActivity.kt`, `ReaderViewModel.kt`)
   - **محركات الصفحة والأنماط (Viewers & Loaders)**: `app/src/main/java/eu/kanade/tachiyomi/ui/reader/viewer/` (Webtoon, Pager), `app/src/main/java/eu/kanade/tachiyomi/ui/reader/loader/` (Http, Archive, Directory, Download)
   - **واجهات التحكم والمظهر**: `app/src/main/java/eu/kanade/presentation/reader/` (`ReaderScreen.kt`, `appbars/`, `controls/`)

3. **التصفح والإضافات (Browse & Extensions)**:
   - **المنطق والتنقل**: `app/src/main/java/eu/kanade/tachiyomi/ui/browse/` (`BrowseTab.kt`, `source/`, `extension/`, `migration/`, `feed/`)
   - **مكونات Compose**: `app/src/main/java/eu/kanade/presentation/browse/` (`SourcesScreen.kt`, `ExtensionsScreen.kt`, `BrowseSourceScreen.kt`, `FeedScreen.kt`)

4. **تفاصيل المانغا (Manga Details)**:
   - **المنطق والتنقل**: `app/src/main/java/eu/kanade/tachiyomi/ui/manga/` (`MangaScreen.kt`, `MangaScreenModel.kt`, `EditMangaDialog.kt`, `merged/`)
   - **مكونات Compose**: `app/src/main/java/eu/kanade/presentation/manga/` (`MangaScreen.kt`, `components/`)

5. **السجل والتحديثات (History & Updates)**:
   - **السجل (History)**: `app/src/main/java/eu/kanade/tachiyomi/ui/history/` (`HistoryScreenModel.kt`, `HistoryTab.kt`) & `app/src/main/java/eu/kanade/presentation/history/`
   - **التحديثات (Updates)**: `app/src/main/java/eu/kanade/tachiyomi/ui/updates/` (`UpdatesScreenModel.kt`, `UpdatesTab.kt`) & `app/src/main/java/eu/kanade/presentation/updates/`

---

## 6. خريطة الميزات الخاصة بنسخة TachiyomiSY الحصرية (SY Specific Features Map)

تتركز معظم الميزات الحصرية لفرع TachiyomiSY في مجلد `exh/` والملفات المرتبطة بها في `data/` و `domain/`:

1. **تكاملات E-Hentai / ExHentai / NHentai ومرضيات الميتا**:
   - **المحرك والأدوات**: `app/src/main/java/exh/` (`eh/`, `uconfig/`, `favorites/`, `ui/login/EhLoginActivity.kt`)
   - **عرض البيانات الوصفية (Metadata Viewer)**: `app/src/main/java/exh/ui/metadata/` (`MetadataViewScreen.kt`, `adapters/`)
   - **جدول واستعلامات الميتا في SQLDelight**: `data/src/main/sqldelight/tachiyomi/data/eh.sq`, `search_metadata.sq`, `search_tags.sq`

2. **التجميع والبحث الذكي والتغذية المخصصة (Smart Searching, Grouping & Feed)**:
   - **التغذية والبحث المحفوظ (Feed & Saved Search)**: `app/src/main/java/eu/kanade/tachiyomi/ui/browse/feed/`, `app/src/main/java/eu/kanade/presentation/browse/FeedScreen.kt`, `data/src/main/sqldelight/tachiyomi/data/feed_saved_search.sq`
   - **البحث الذكي (Smart Search)**: `app/src/main/java/exh/smartsearch/` (`SmartLibrarySearchEngine.kt`), `app/src/main/java/exh/ui/smartsearch/`
   - **دمج المصادر (Merged Source)**: `source-api/src/commonMain/kotlin/eu/kanade/tachiyomi/source/online/all/MergedSource.kt`, `app/src/main/java/eu/kanade/tachiyomi/ui/manga/merged/`, `data/src/main/sqldelight/tachiyomi/data/merged.sq`
   - **نظام التوصيات والمقترحات (Recommends)**: `app/src/main/java/exh/recs/` (`RecommendsScreen.kt`, `sources/`)

3. **إعدادات وتفضيلات SY المخصصة**:
   - **تفضيلات ExHentai/SY**: `domain/src/main/java/exh/source/ExhPreferences.kt`
   - **حقن التبعيات الخاص بـ SY**: `app/src/main/java/eu/kanade/tachiyomi/di/SYPreferenceModule.kt`, `app/src/main/java/eu/kanade/domain/SYDomainModule.kt`
   - **شاشة إعدادات EHentai**: `app/src/main/java/eu/kanade/presentation/more/settings/screen/SettingsEhScreen.kt`

---

## 7. دليل التوجيه السريع للتطوير (Quick Navigation & Task Guide)

| المهمة المطلوبة | المسار / الملفات المعنية |
| :--- | :--- |
| **إضافة شاشة جديدة (Screen / Voyager / ScreenModel)** | 1. إنشاء الـ `Screen` والـ `ScreenModel` في `app/src/main/java/eu/kanade/tachiyomi/ui/<feature>/`<br>2. بناء الواجهة في `app/src/main/java/eu/kanade/presentation/<feature>/`<br>3. ربط الشاشة في شريط الملاحة أو التنقل عبر Voyager |
| **إضافة أو تعديل جدول / استعلام في قاعدة البيانات (SQLDelight)** | 1. إضافة أو تعديل جداول واستعلامات `.sq` في `data/src/main/sqldelight/tachiyomi/data/`<br>2. إنشاء ملف هجرة جديد `.sqm` في `data/src/main/sqldelight/tachiyomi/migrations/`<br>3. تحديث واجهة المستودع في `domain/src/main/java/` وتنفيذها في `data/src/main/java/` |
| **إضافة خيار جديد في الإعدادات (Preferences Store & Settings)** | 1. تعريف التفضيل في ملف الـ `Preferences` المعني في `domain/src/main/java/` (مثل `UiPreferences.kt` أو `ExhPreferences.kt`)<br>2. مراقبة وحفظ الحالة بالدالة `collectAsState()` داخل شاشة الإعدادات المناسبة في `app/src/main/java/eu/kanade/presentation/more/settings/screen/`<br>3. إضافة عنصر `Preference.PreferenceItem` ضمن القائمة |
| **تعديل أو إضافة ثيم / ألوان جديدة (Design System / Themes)** | 1. إضافة اسم الثيم في `app/src/main/java/eu/kanade/domain/ui/model/AppTheme.kt`<br>2. إنشاء ملف ألوان جديد في `app/src/main/java/eu/kanade/presentation/theme/colorscheme/<Name>ColorScheme.kt`<br>3. ربط الملف في `app/src/main/java/eu/kanade/presentation/theme/TachiyomiTheme.kt` |
| **تعديل سلوك القارئ أو محرك التصفح (Reader Engine)** | 1. التحكم الرئيسي بالنشاط: `app/src/main/java/eu/kanade/tachiyomi/ui/reader/ReaderActivity.kt` و `ReaderViewModel.kt`<br>2. تعديل طريقة عرض الصفحات والتحكم بها: `app/src/main/java/eu/kanade/tachiyomi/ui/reader/viewer/` (سواء Pager أو Webtoon)<br>3. تعديل آليات تحميل الصفحات من الشبكة أو الملفات المحلية: `app/src/main/java/eu/kanade/tachiyomi/ui/reader/loader/` |
| **إضافة أو تعديل منصات التتبع (Tracker Integration)** | 1. تعريف المنصة والواجهة: `app/src/main/java/eu/kanade/tachiyomi/data/track/` (`TrackerManager.kt`, `TrackService.kt`)<br>2. إضافة التفاعلات ونموذج التتبع: `domain/src/main/java/eu/kanade/domain/track/`<br>3. تحديث واجهة الربط: `app/src/main/java/eu/kanade/presentation/manga/components/TrackInfoDialog.kt` |
| **تعديل نظام النسخ الاحتياطي والاستعادة (Backup & Restore)** | 1. تعديل منشئي النسخ: `app/src/main/java/eu/kanade/tachiyomi/data/backup/create/creators/`<br>2. تعديل مسترجعي البيانات: `app/src/main/java/eu/kanade/tachiyomi/data/backup/restore/restorers/`<br>3. تحديث النماذج والمخطط: `app/src/main/java/eu/kanade/tachiyomi/data/backup/models/` |

---

## 8. نظام التتبع وتزامن الحسابات (Tracking System)

- **المنطق والتفاعلات (Domain)**: `domain/src/main/java/eu/kanade/domain/track/` (مجلدات `interactor/`, `model/`, `service/`).
- **خدمات التتبع وإدارة الجلسات**: `app/src/main/java/eu/kanade/tachiyomi/data/track/` (`TrackerManager.kt`, `TrackService.kt`, `TrackStatus.kt`, والمشغلات مثل `AniList.kt`, `MyAnimeList.kt`, `Kitsu.kt`, `MangaUpdates.kt`, `Bangumi.kt`, `Shikimori.kt`).
- **شاشات تسجيل الدخول وترافق المانغا**:
  - نافذة ربط وتحديث المانغا: `app/src/main/java/eu/kanade/presentation/manga/components/TrackInfoDialog.kt`.
  - أنشطة تسجيل الدخول OAuth: `app/src/main/java/eu/kanade/tachiyomi/ui/setting/track/` (`TrackLoginActivity.kt`, `BaseOAuthLoginActivity.kt`).

---

## 9. نظام النسخ الاحتياطي والاستعادة (Backup & Restore System)

- **إنشاء النسخ الاحتياطية (Create Backup)**: `app/src/main/java/eu/kanade/tachiyomi/data/backup/create/creators/` (`MangaBackupCreator.kt`, `CategoriesBackupCreator.kt`, `ExtensionRepoBackupCreator.kt`, إلخ).
- **استعادة البيانات (Restore Backup)**: `app/src/main/java/eu/kanade/tachiyomi/data/backup/restore/restorers/` (`MangaRestorer.kt`, `CategoriesRestorer.kt`, `PreferenceRestorer.kt`, إلخ).
- **نماذج ومخططات البيانات (Models & Proto)**: `app/src/main/java/eu/kanade/tachiyomi/data/backup/models/` (`BackupManga.kt`, `BackupChapter.kt`, `BackupSource.kt`).
- **المهام الخلفية لشغل Backup**: `app/src/main/java/eu/kanade/tachiyomi/data/backup/BackupCreateJob.kt` و `BackupRestoreJob.kt`.

---

## 10. نظام التزامن المباشر بين الأجهزة (Sync System)

- **المحرك ومدير المزامنة**: `app/src/main/java/eu/kanade/tachiyomi/data/sync/` (`SyncManager.kt`, `SyncDataJob.kt`, `SyncService.kt`).
- **تفضيلات ونماذج المزامنة**: `domain/src/main/java/eu/kanade/domain/sync/` (`SyncPreferences.kt`, `models/SyncSettings.kt`).

---

## 11. نظام الأمان والحماية وخصوصية الشاشة (Security & Privacy System)

- **قفل الشاشة والبصمة (Biometrics)**: `app/src/main/java/eu/kanade/tachiyomi/ui/security/UnlockActivity.kt` وشاشة الخيارات في `app/src/main/java/eu/kanade/presentation/category/components/biometric/`.
- **منع الالتقاط وحماية الأنشطة**: `app/src/main/java/eu/kanade/tachiyomi/ui/base/delegate/SecureActivityDelegate.kt`.
- **تفضيلات الخصوصية ووضع التخفي (Incognito)**: `app/src/main/java/eu/kanade/tachiyomi/core/security/PrivacyPreferences.kt` و `BasePreferences.incognitoMode`.

---

## 12. نظام معالجة وتحميل الصور والذاكرة المؤقتة (Coil 3 & Image Caching)

- **محمل الصور القائم على Coil 3**: `app/src/main/java/eu/kanade/tachiyomi/data/coil/` (`MangaCoverFetcher.kt`, `MangaCoverKeyer.kt`, `PagePreviewFetcher.kt`, `BufferedSourceFetcher.kt`, `TachiyomiImageDecoder.kt`).
- **الذاكرة المؤقتة للقرص (Disk Caches)**:
  - غلاف المانغا: `app/src/main/java/eu/kanade/tachiyomi/data/cache/CoverCache.kt`.
  - الفصول والصور المؤقتة: `app/src/main/java/eu/kanade/tachiyomi/data/cache/ChapterCache.kt`.

---

## 13. نظام الهجرة والترقيات والمهام الخلفية (Migrations & WorkManager Jobs)

- **هجرات الأكواد والتفضيلات (App Code Migrations)**:
  - هجرات Mihon الرسمية: `app/src/main/java/mihon/core/migration/Migrator.kt` ومجلد `migrations/`.
  - هجرات فرع TachiyomiSY: `app/src/main/java/exh/EXHMigrations.kt`.
- **جدول العمال الخلفيين (WorkManager Jobs Summary)**:
  - تحديث المكتبة: `LibraryUpdateJob.kt`
  - تنزيل الفصول: `DownloadJob.kt`
  - تحديث EHentai الميتا: `EHentaiUpdateWorker.kt`
  - النسخ الاحتياطي: `BackupCreateJob.kt` / `BackupRestoreJob.kt`
  - تزامن البيانات: `SyncDataJob.kt`
  - تحديث التتبع المؤجل: `DelayedTrackingUpdateJob.kt`
