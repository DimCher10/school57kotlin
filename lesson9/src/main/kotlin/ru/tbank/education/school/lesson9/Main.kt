package ru.tbank.education.school.lesson9

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
fun createZipArchive(sourceDirPath: String, archivePath: String) {
    val sourceDir = File(sourceDirPath)
    val archiveFile = File(archivePath)
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        println("Ошибка: исходный каталог '$sourceDirPath' не существует или не является каталогом")
        return
    }
    ZipOutputStream(FileOutputStream(archiveFile)).use { zipOut ->
        sourceDir.walk().filter { it.isFile }.forEach { file ->
            val relativePath = file.relativeTo(sourceDir).path
            if (file.extension.lowercase() in listOf("txt", "log")) {
                println("Добавляем файл: $relativePath (размер: ${file.length()} байт)")
                val zipEntry = ZipEntry(relativePath)
                zipOut.putNextEntry(zipEntry)
                FileInputStream(file).use { fileIn ->
                    fileIn.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }
    println("\nАрхив успешно создан: ${archiveFile.absolutePath}")
    println("Размер архива: ${archiveFile.length()} байт")
}