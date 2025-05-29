package jovanovicdima.encryptlink.utils

import java.io.File

fun encryptFileRC6(outputFolderPath: String, encryptionFilePath: String, key: String, iv: String) {
    val rc6 = RC6()
    rc6.init(key.toByteArray())

    val oldFile = File(encryptionFilePath)

    val ciphertext = rc6.encrypt(oldFile.readBytes(), iv.toByteArray())

    val newFile = File("$outputFolderPath/${oldFile.name}-RC6encrypted")

    newFile.writeBytes(ciphertext)
}

fun decryptFileRC6(outputFolderPath: String, encryptionFilePath: String, key: String, iv: String) {
    val rc6 = RC6()
    rc6.init(key.toByteArray())

    val oldFile = File(encryptionFilePath)

    val ciphertext = rc6.decrypt(oldFile.readBytes(), iv.toByteArray())

    val newFileName = oldFile.name.removeSuffix("-RC6encrypted")

    val newFile = File("$outputFolderPath/$newFileName")

    newFile.writeBytes(ciphertext)
}