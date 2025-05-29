package jovanovicdima.encryptlink.utils

import java.io.File

fun encryptFileRC6(outputFolderPath: String, encryptionFilePath: String, key: String, iv: String) {
    val rc6 = RC6(key = key.toByteArray())

    val oldFile = File(encryptionFilePath)

    val ciphertext = rc6.encrypt(oldFile.readBytes(), iv.toByteArray())

    val newFile = File("$outputFolderPath/${oldFile.name}-RC6Encrypted")

    newFile.writeBytes(ciphertext)
}

fun decryptFileRC6(outputFolderPath: String, encryptionFilePath: String, key: String, iv: String) {
    val rc6 = RC6(key = key.toByteArray())

    val oldFile = File(encryptionFilePath)

    val ciphertext = rc6.decrypt(oldFile.readBytes(), iv.toByteArray())

    val newFileName = oldFile.name.removeSuffix("-RC6Encrypted")

    val newFile = File("$outputFolderPath/$newFileName")

    newFile.writeBytes(ciphertext)
}

fun encryptFileBifid(outputFolderPath: String, encryptionFilePath: String, key: String) {
    val bifid = Bifid(key)

    val oldFile = File(encryptionFilePath)

    if (oldFile.extension != "txt") {
        throw Exception("Only .txt files are allowed.")
    }

    val ciphertext = bifid.encrypt(oldFile.readText(Charsets.UTF_8))

    val newFile = File("$outputFolderPath/${oldFile.nameWithoutExtension}-BifidEncrypted.${oldFile.extension}")

    newFile.writeText(ciphertext)
}

fun decryptFileBifid(outputFolderPath: String, encryptionFilePath: String, key: String) {
    val bifid = Bifid(key)

    val oldFile = File(encryptionFilePath)

    if (oldFile.extension != "txt") {
        throw Exception("Only .txt files are allowed.")
    }

    val ciphertext = bifid.decrypt(oldFile.readText(Charsets.UTF_8))

    val newFileName = oldFile.name.replace("-BifidEncrypted", "")

    val newFile = File("$outputFolderPath/$newFileName")

    newFile.writeText(ciphertext)
}