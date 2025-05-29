package jovanovicdima.encryptlink.utils

class Bifid {
    private val key: String

    constructor(key: String) {
        this.key = key
        this.square = Array(5) { CharArray(5) }
        this.charToCoords = mutableMapOf<Char, Pair<Int, Int>>()
        val uniqueKey = key.uppercase().replace("J", "I").toSet().joinToString("")
        val alphabet = "ABCDEFGHIKLMNOPQRSTUVWXYZ"
        val remainingChars = alphabet.filter { it !in uniqueKey }
        val fullKey = uniqueKey + remainingChars
        var index = 0
        for (row in 0..4) {
            for (col in 0..4) {
                if (index < fullKey.length) {
                    square[row][col] = fullKey[index]
                    charToCoords[fullKey[index]] = Pair(row, col)
                    index++
                }
            }
        }
    }

    // Create 5x5 Polybius square from key
    private val square: Array<CharArray>
    private val charToCoords: MutableMap<Char, Pair<Int, Int>>

    fun encrypt(plaintext: String, period: Int = 5): String {
        val cleanText = plaintext.uppercase().replace("[^A-Z]".toRegex(), "").replace("J", "I")
        if (cleanText.isEmpty()) return ""

        val result = StringBuilder()

        // Process text in chunks of specified period
        for (i in cleanText.indices step period) {
            val chunk = cleanText.substring(i, minOf(i + period, cleanText.length))
            result.append(encryptChunk(chunk))
        }

        return result.toString()
    }

    fun decrypt(ciphertext: String, period: Int = 5): String {
        val cleanText = ciphertext.uppercase().replace("[^A-Z]".toRegex(), "")
        if (cleanText.isEmpty()) return ""

        val result = StringBuilder()

        // Process text in chunks of specified period
        for (i in cleanText.indices step period) {
            val chunk = cleanText.substring(i, minOf(i + period, cleanText.length))
            result.append(decryptChunk(chunk))
        }

        return result.toString()
    }

    private fun encryptChunk(chunk: String): String {
        val rows = mutableListOf<Int>()
        val cols = mutableListOf<Int>()

        // Extract coordinates for each character
        for (char in chunk) {
            val coords = charToCoords[char] ?: continue
            rows.add(coords.first)
            cols.add(coords.second)
        }

        // Combine coordinates: all rows first, then all columns
        val combined = rows + cols

        // Convert back to characters using coordinate pairs
        val result = StringBuilder()
        for (i in combined.indices step 2) {
            if (i + 1 < combined.size) {
                val row = combined[i]
                val col = combined[i + 1]
                result.append(square[row][col])
            }
        }

        return result.toString()
    }

    private fun decryptChunk(chunk: String): String {
        val coords = mutableListOf<Int>()

        // Extract coordinates for each character
        for (char in chunk) {
            val charCoords = charToCoords[char] ?: continue
            coords.add(charCoords.first)
            coords.add(charCoords.second)
        }

        if (coords.size % 2 != 0) return chunk // Invalid chunk

        val halfSize = coords.size / 2
        val rows = coords.subList(0, halfSize)
        val cols = coords.subList(halfSize, coords.size)

        // Reconstruct original characters
        val result = StringBuilder()
        for (i in rows.indices) {
            if (i < cols.size) {
                result.append(square[rows[i]][cols[i]])
            }
        }

        return result.toString()
    }

    fun printSquare() {
        println("Polybius Square:")
        println("  0 1 2 3 4")
        for (i in 0..4) {
            print("$i ")
            for (j in 0..4) {
                print("${square[i][j]} ")
            }
            println()
        }
    }
}

// Example usage and testing
fun bifidmain() {
    // Create cipher with default key
    val cipher = Bifid("TEST")
    cipher.printSquare()

    // Test encryption and decryption
    val plaintext = "HELLO WORLD"
    println("\nOriginal text: $plaintext")

    val encrypted = cipher.encrypt(plaintext, 5)
    println("Encrypted: $encrypted")

    val decrypted = cipher.decrypt(encrypted, 5)
    println("Decrypted: $decrypted")

    println("\n--- Testing with custom key ---")

    // Test with custom key
    val customCipher = Bifid("KEYWORD")
    customCipher.printSquare()

    val customEncrypted = customCipher.encrypt("ATTACK AT DAWN", 6)
    println("\nCustom encrypted: $customEncrypted")

    val customDecrypted = customCipher.decrypt(customEncrypted, 6)
    println("Custom decrypted: $customDecrypted")
}