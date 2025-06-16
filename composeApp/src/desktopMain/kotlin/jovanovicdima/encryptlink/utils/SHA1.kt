package jovanovicdima.encryptlink.utils

object SHA1 {
    fun hash(input: ByteArray): ByteArray {
        var h0 = 0x67452301.toInt()
        var h1 = 0xEFCDAB89.toInt()
        var h2 = 0x98BADCFE.toInt()
        var h3 = 0x10325476.toInt()
        var h4 = 0xC3D2E1F0.toInt()

        val message = input.toMutableList()
        message.add(0x80.toByte())

        while (message.size % 64 != 56) {
            message.add(0x00.toByte())
        }

        val originalLengthBits = input.size.toLong() * 8
        for (i in 7 downTo 0) {
            message.add(((originalLengthBits shr (i * 8)) and 0xFF).toByte())
        }

        for (chunkStart in message.indices step 64) {
            val w = IntArray(80)

            for (i in 0..15) {
                val offset = chunkStart + i * 4
                w[i] = ((message[offset].toInt() and 0xFF) shl 24) or
                        ((message[offset + 1].toInt() and 0xFF) shl 16) or
                        ((message[offset + 2].toInt() and 0xFF) shl 8) or
                        (message[offset + 3].toInt() and 0xFF)
            }

            for (i in 16..79) {
                w[i] = leftRotate(w[i - 3] xor w[i - 8] xor w[i - 14] xor w[i - 16], 1)
            }

            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4

            for (i in 0..79) {
                val f: Int
                val k: Int

                when {
                    i < 20 -> {
                        f = (b and c) or ((b.inv()) and d)
                        k = 0x5A827999.toInt()
                    }
                    i < 40 -> {
                        f = b xor c xor d
                        k = 0x6ED9EBA1.toInt()
                    }
                    i < 60 -> {
                        f = (b and c) or (b and d) or (c and d)
                        k = 0x8F1BBCDC.toInt()
                    }
                    else -> {
                        f = b xor c xor d
                        k = 0xCA62C1D6.toInt()
                    }
                }

                val temp = leftRotate(a, 5) + f + e + k + w[i]
                e = d
                d = c
                c = leftRotate(b, 30)
                b = a
                a = temp
            }

            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
        }

        val result = ByteArray(20)
        intToBytes(h0, result, 0)
        intToBytes(h1, result, 4)
        intToBytes(h2, result, 8)
        intToBytes(h3, result, 12)
        intToBytes(h4, result, 16)

        return result
    }

    private fun leftRotate(value: Int, amount: Int): Int {
        return (value shl amount) or (value ushr (32 - amount))
    }

    private fun intToBytes(value: Int, bytes: ByteArray, offset: Int) {
        bytes[offset] = (value shr 24).toByte()
        bytes[offset + 1] = (value shr 16).toByte()
        bytes[offset + 2] = (value shr 8).toByte()
        bytes[offset + 3] = value.toByte()
    }
}

fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}