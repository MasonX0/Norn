package ru.bpo.norn.jwmMain.services

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import ru.bpo.norn.commonMain.models.AppSettings
import ru.bpo.norn.commonMain.models.SummaryReportData
import ru.bpo.norn.commonMain.models.OrderData
import java.io.File

/**
 * Сервис для сохранения и загрузки настроек приложения
 * Использует JSON формат для хранения конфигурации в файле .norn-settings.json
 */
class AppSettingsService {

    companion object {
        private const val SETTINGS_FILE_NAME = ".norn-settings.json"

        /**
         * Получает путь к файлу настроек в домашней директории пользователя
         */
        private fun getSettingsFile(): File {
            val userHome = File(System.getProperty("user.home"))
            return File(userHome, SETTINGS_FILE_NAME)
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true // Игнорируем неизвестные поля для обратной совместимости
        prettyPrint = true // Красивое форматирование JSON
        encodeDefaults = true // Включаем значения по умолчанию
    }

    /**
     * Сохраняет настройки приложения в файл
     */
    fun saveSettings(settings: AppSettings): Boolean {
        return try {
            val settingsFile = getSettingsFile()
            val jsonString = json.encodeToString(settings)
            settingsFile.writeText(jsonString, Charsets.UTF_8)
            println("✅ Настройки сохранены в: ${settingsFile.absolutePath}")
            true
        } catch (e: Exception) {
            println("❌ Ошибка сохранения настроек: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Загружает настройки приложения из файла
     * Если файла нет или произошла ошибка, возвращает настройки по умолчанию
     */
    fun loadSettings(): AppSettings {
        return try {
            val settingsFile = getSettingsFile()
            if (!settingsFile.exists()) {
                println("ℹ️ Файл настроек не найден, используются настройки по умолчанию")
                return AppSettings()
            }

            val jsonString = settingsFile.readText(Charsets.UTF_8)
            val loadedSettings = json.decodeFromString<AppSettings>(jsonString)
            println("✅ Настройки загружены из: ${settingsFile.absolutePath}")
            loadedSettings
        } catch (e: Exception) {
            println("❌ Ошибка загрузки настроек: ${e.message}")
            println("ℹ️ Используются настройки по умолчанию")
            e.printStackTrace()
            AppSettings()
        }
    }

    /**
     * Проверяет, существует ли файл настроек
     */
    fun settingsFileExists(): Boolean {
        return getSettingsFile().exists()
    }

    /**
     * Удаляет файл настроек (сброс к дефолтным значениям)
     */
    fun resetSettings(): Boolean {
        return try {
            val settingsFile = getSettingsFile()
            if (settingsFile.exists()) {
                settingsFile.delete()
                println("✅ Настройки сброшены")
                true
            } else {
                println("ℹ️ Файл настроек не существует")
                false
            }
        } catch (e: Exception) {
            println("❌ Ошибка сброса настроек: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Преобразует AppSettings в SummaryReportData для использования в отчетах
     */
    fun toSummaryReportData(settings: AppSettings): SummaryReportData {
        return SummaryReportData(
            departmentName = settings.departmentName,
            academicYear = settings.academicYear,
            field2_excursions = settings.field2_excursions,
            field3_teachers = settings.field3_teachers,
            field4_absentStudents = settings.field4_absentStudents,
            field5_additionalInfo = settings.field5_additionalInfo,
            field6_preliminaryEvents = settings.field6_preliminaryEvents,
            field8_shortcomings = settings.field8_shortcomings,
            field9_improvements = settings.field9_improvements,
            field10_conclusion = settings.field10_conclusion
        )
    }

    /**
     * Преобразует AppSettings в OrderData для использования в приказах
     */
    fun toOrderData(settings: AppSettings): OrderData {
        return OrderData(
            headerText = settings.orderHeaderText,
            titleText = settings.orderTitleText,
            instituteText = settings.orderInstituteText,
            streamName = settings.orderStreamName,
            basisText = settings.orderBasisText,
            agreeText = settings.orderAgreeText,
            proposerText = settings.orderProposerText,
            prorectorName = settings.orderProrectorName,
            studyDepartmentHead = settings.orderStudyDepartmentHead,
            partnershipDepartmentHead = settings.orderPartnershipDepartmentHead,
            legalDepartmentDeputy = settings.orderLegalDepartmentDeputy,
            practiceManager = settings.orderPracticeManager,
            instituteDirector = settings.orderInstituteDirector,
            departmentHead = settings.orderDepartmentHead
        )
    }

    /**
     * Создает AppSettings из текущих данных приложения
     */
    fun fromCurrentData(
        baseDirectory: File?,
        isDarkTheme: Boolean,
        reportFile: File?,
        directionFile: File?,
        directionTemplateFile: File?,
        orderFile: File?,
        enterprisesFile: File?,
        studentsListFile: File?,
        statementsFile: File?,
        directionsOutputFolder: File?,
        dateOfDirectionIssue: String,
        dateOfTaskReceived: String,
        dateOfDepartmentReview: String,
        summaryReportData: SummaryReportData,
        orderData: OrderData,
        loadedGroups: List<ru.bpo.norn.commonMain.models.Group>,
        loadedEnterprises: List<ru.bpo.norn.commonMain.models.Enterprise>
    ): AppSettings {
        return AppSettings(
            baseDirectory = baseDirectory?.absolutePath,
            isDarkTheme = isDarkTheme,
            lastReportFile = reportFile?.absolutePath,
            lastDirectionFile = directionFile?.absolutePath,
            lastDirectionTemplateFile = directionTemplateFile?.absolutePath,
            lastOrderFile = orderFile?.absolutePath,
            lastEnterprisesFile = enterprisesFile?.absolutePath,
            lastStudentsListFile = studentsListFile?.absolutePath,
            lastStatementsFile = statementsFile?.absolutePath,
            lastDirectionsOutputFolder = directionsOutputFolder?.absolutePath,
            loadedGroups = loadedGroups,
            loadedEnterprises = loadedEnterprises,
            dateOfDirectionIssue = dateOfDirectionIssue,
            dateOfTaskReceived = dateOfTaskReceived,
            dateOfDepartmentReview = dateOfDepartmentReview,
            departmentName = summaryReportData.departmentName,
            academicYear = summaryReportData.academicYear,
            field2_excursions = summaryReportData.field2_excursions,
            field3_teachers = summaryReportData.field3_teachers,
            field4_absentStudents = summaryReportData.field4_absentStudents,
            field5_additionalInfo = summaryReportData.field5_additionalInfo,
            field6_preliminaryEvents = summaryReportData.field6_preliminaryEvents,
            field8_shortcomings = summaryReportData.field8_shortcomings,
            field9_improvements = summaryReportData.field9_improvements,
            field10_conclusion = summaryReportData.field10_conclusion,
            orderHeaderText = orderData.headerText,
            orderTitleText = orderData.titleText,
            orderInstituteText = orderData.instituteText,
            orderStreamName = orderData.streamName,
            orderBasisText = orderData.basisText,
            orderAgreeText = orderData.agreeText,
            orderProposerText = orderData.proposerText,
            orderProrectorName = orderData.prorectorName,
            orderStudyDepartmentHead = orderData.studyDepartmentHead,
            orderPartnershipDepartmentHead = orderData.partnershipDepartmentHead,
            orderLegalDepartmentDeputy = orderData.legalDepartmentDeputy,
            orderPracticeManager = orderData.practiceManager,
            orderInstituteDirector = orderData.instituteDirector,
            orderDepartmentHead = orderData.departmentHead
        )
    }
}