package jovanovicdima.encryptlink.utils

import java.io.File

fun encryptDataRC6(data: ByteArray, key: String, iv: ByteArray): ByteArray {
    val rc6 = RC6(key = key.toByteArray())
    return rc6.encrypt(data, iv)
}

fun encryptFileRC6(outputFolderPath: String, encryptionFilePath: String, key: String, iv: String) {
    val oldFile = File(encryptionFilePath)

    val ciphertext = encryptDataRC6(oldFile.readBytes(), key = key, iv.toByteArray())

    val newFile = File("$outputFolderPath/${oldFile.nameWithoutExtension}-RC6Encrypted.${oldFile.extension}")

    newFile.writeBytes(ciphertext)
}

fun decryptFileRC6(outputFolderPath: String, encryptionFilePath: String, key: String, iv: String) {
    val rc6 = RC6(key = key.toByteArray())

    val oldFile = File(encryptionFilePath)

    val ciphertext = rc6.decrypt(oldFile.readBytes(), iv.toByteArray())

    val newFileName = oldFile.name.replace("-RC6Encrypted", "")

    val newFile = File("$outputFolderPath/$newFileName")

    newFile.writeBytes(ciphertext)
}

fun encryptDataBifid(data: String, key: String): String {
    val bifid = Bifid(key)
    return bifid.encrypt(data)
}
fun encryptFileBifid(outputFolderPath: String, encryptionFilePath: String, key: String) {
    val oldFile = File(encryptionFilePath)

    if (oldFile.extension != "txt") {
        throw Exception("Only .txt files are allowed.")
    }

    val ciphertext = encryptDataBifid(oldFile.readText(Charsets.UTF_8), key)

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