package tachiyomi.domain.category.interactor

import logcat.LogPriority
import tachiyomi.core.common.util.lang.withNonCancellableContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.service.LibraryPreferences

class InitializeDefaultCategories(
    private val categoryRepository: CategoryRepository,
    private val preferences: LibraryPreferences,
) {
    suspend fun await(defaultNames: List<String>) = withNonCancellableContext {
        if (preferences.defaultCategoriesInitialized.get()) return@withNonCancellableContext

        val existingCategories = try {
            categoryRepository.getAll()
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to query categories for default initialization" }
            return@withNonCancellableContext
        }

        if (existingCategories.isNotEmpty()) {
            preferences.defaultCategoriesInitialized.set(true)
            return@withNonCancellableContext
        }

        try {
            defaultNames.forEachIndexed { index, name ->
                val category = Category(
                    id = 0,
                    name = name,
                    order = index.toLong(),
                    flags = 0L,
                )
                categoryRepository.insert(category)
            }
            preferences.defaultCategoriesInitialized.set(true)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to initialize default categories" }
        }
    }
}
