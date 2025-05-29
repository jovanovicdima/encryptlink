package jovanovicdima.encryptlink.utils

import kotlin.math.log2

class RC6 {
    companion object {
        private const val W = 32 // Word size in bits
        private const val R = 20 // Number of rounds
        private const val B = 16 // Key length in bytes (128-bit key)
        private const val BLOCK_SIZE = 16 // Block size in bytes (128 bits)

        private const val P32 = 0xB7E15163.toInt()
        private const val Q32 = 0x9E3779B9.toInt()
    }

    private var s = IntArray(2 * R + 4) // Key schedule array

    fun init(key: ByteArray) {
        if (key.size != B) {
            throw IllegalArgumentException("Key must be exactly $B bytes (128 bits)")
        }
        keySchedule(key)
    }

    private fun keySchedule(key: ByteArray) {
        val l = IntArray(B / 4)
        for (i in l.indices) {
            l[i] = (key[4 * i].toInt() and 0xFF) or
                    ((key[4 * i + 1].toInt() and 0xFF) shl 8) or
                    ((key[4 * i + 2].toInt() and 0xFF) shl 16) or
                    ((key[4 * i + 3].toInt() and 0xFF) shl 24)
        }

        s[0] = P32
        for (i in 1 until s.size) {
            s[i] = s[i - 1] + Q32
        }

        var a = 0
        var b = 0
        var i = 0
        var j = 0
        val n = 3 * maxOf(s.size, l.size)

        (0 until n).forEach { k ->
            a = rotateLeft(s[i] + a + b, 3)
            s[i] = a
            b = rotateLeft(l[j] + a + b, a + b)
            l[j] = b
            i = (i + 1) % s.size
            j = (j + 1) % l.size
        }
    }

    private fun encryptBlock(plaintext: ByteArray): ByteArray {
        if (plaintext.size != BLOCK_SIZE) {
            throw IllegalArgumentException("Block must be exactly $BLOCK_SIZE bytes")
        }

        var a = bytesToInt(plaintext, 0)
        var b = bytesToInt(plaintext, 4)
        var c = bytesToInt(plaintext, 8)
        var d = bytesToInt(plaintext, 12)

        // Pre-whitening
        b += s[0]
        d += s[1]

        for (i in 1..R) {
            val t = rotateLeft(b * (2 * b + 1), log2(W.toDouble()).toInt())
            val u = rotateLeft(d * (2 * d + 1), log2(W.toDouble()).toInt())
            a = rotateLeft(a xor t, u) + s[2 * i]
            c = rotateLeft(c xor u, t) + s[2 * i + 1]

            val temp = a
            a = b
            b = c
            c = d
            d = temp
        }

        a += s[2 * R + 2]
        c += s[2 * R + 3]

        val result = ByteArray(BLOCK_SIZE)
        intToBytes(a, result, 0)
        intToBytes(b, result, 4)
        intToBytes(c, result, 8)
        intToBytes(d, result, 12)

        return result
    }

    fun encrypt(plaintext: ByteArray, iv: ByteArray): ByteArray {
        if (iv.size != BLOCK_SIZE) {
            throw IllegalArgumentException("IV must be exactly $BLOCK_SIZE bytes")
        }

        val paddedPlaintext = addPKCS7Padding(plaintext)
        val result = ByteArray(paddedPlaintext.size)
        var currentIV = iv.copyOf()
        var offset = 0

        while (offset < paddedPlaintext.size) {
            val keystream = encryptBlock(currentIV)

            val bytesToProcess = minOf(BLOCK_SIZE, paddedPlaintext.size - offset)
            for (i in 0 until bytesToProcess) {
                result[offset + i] = (paddedPlaintext[offset + i].toInt() xor keystream[i].toInt()).toByte()
            }

            currentIV = keystream
            offset += BLOCK_SIZE
        }

        return result
    }

    fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        if (iv.size != BLOCK_SIZE) {
            throw IllegalArgumentException("IV must be exactly $BLOCK_SIZE bytes")
        }

        val result = ByteArray(ciphertext.size)
        var currentIV = iv.copyOf()
        var offset = 0

        while (offset < ciphertext.size) {
            val keystream = encryptBlock(currentIV)

            val bytesToProcess = minOf(BLOCK_SIZE, ciphertext.size - offset)
            for (i in 0 until bytesToProcess) {
                result[offset + i] = (ciphertext[offset + i].toInt() xor keystream[i].toInt()).toByte()
            }

            currentIV = keystream
            offset += BLOCK_SIZE
        }

        return removePKCS7Padding(result)
    }

    private fun rotateLeft(value: Int, shift: Int): Int {
        val normalizedShift = shift and 31
        return (value shl normalizedShift) or (value ushr (32 - normalizedShift))
    }

    private fun bytesToInt(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun addPKCS7Padding(data: ByteArray): ByteArray {
        val padding = BLOCK_SIZE - (data.size % BLOCK_SIZE)
        val padded = ByteArray(data.size + padding)
        System.arraycopy(data, 0, padded, 0, data.size)
        for (i in data.size until padded.size) {
            padded[i] = padding.toByte()
        }
        return padded
    }

    private fun removePKCS7Padding(data: ByteArray): ByteArray {
        if (data.isEmpty()) throw IllegalArgumentException("Data is empty")

        val padding = data.last().toInt() and 0xFF
        if (padding < 1 || padding > BLOCK_SIZE) {
            throw IllegalArgumentException("Invalid PKCS7 padding")
        }

        for (i in data.size - padding until data.size) {
            if ((data[i].toInt() and 0xFF) != padding) {
                throw IllegalArgumentException("Invalid PKCS7 padding")
            }
        }

        return data.copyOfRange(0, data.size - padding)
    }

    private fun intToBytes(value: Int, bytes: ByteArray, offset: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value shr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }
}

fun main() {
    val key = byteArrayOf(
        0x01, 0x23, 0x45, 0x67, 0x89.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(),
        0xFE.toByte(), 0xDC.toByte(), 0xBA.toByte(), 0x98.toByte(), 0x76, 0x54, 0x32, 0x10
    )

    val iv = byteArrayOf(
        0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xBC.toByte(), 0xDE.toByte(), 0xF0.toByte(),
        0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88.toByte()
    )

    val plaintext = "Hello, RC6 with OFB mode!".toByteArray()

    val rc6 = RC6()
    rc6.init(key)

    val ciphertext = rc6.encrypt(plaintext, iv)
    println("Plaintext: ${plaintext.decodeToString()}")
    println("Ciphertext (hex): ${ciphertext.joinToString("") { "%02x".format(it) }}")

    val decrypted = rc6.decrypt(ciphertext, iv)
    println("Decrypted: ${decrypted.decodeToString()}")

    println("Decryption successful: ${plaintext.contentEquals(decrypted)}")
}