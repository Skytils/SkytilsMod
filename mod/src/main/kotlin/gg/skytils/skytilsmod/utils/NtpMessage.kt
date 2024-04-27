/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.skytils.skytilsmod.utils

import java.text.DecimalFormat
import kotlin.math.pow

/**
 * This class represents a NTP message, as specified in RFC 2030.  The message
 * format is compatible with all versions of NTP and SNTP.
 *
 * This class does not support the optional authentication protocol, and
 * ignores the key ID and message digest fields.
 *
 * For convenience, this class exposes message values as native Java types, not
 * the NTP-specified data formats.  For example, timestamps are
 * stored as doubles (as opposed to the NTP unsigned 64-bit fixed point
 * format).
 *
 * However, the contructor NtpMessage(byte[]) and the method toByteArray()
 * allow the import and export of the raw NTP message format.
 *
 *
 * Usage example
 *
 * // Send message
 * DatagramSocket socket = new DatagramSocket();
 * InetAddress address = InetAddress.getByName("ntp.cais.rnp.br");
 * byte[] buf = new NtpMessage().toByteArray();
 * DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
 * socket.send(packet);
 *
 * // Get response
 * socket.receive(packet);
 * System.out.println(msg.toString());
 *
 *
 * This code is copyright (c) Adam Buckley 2004
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.  A HTML version of the GNU General Public License can be
 * seen at http://www.gnu.org/licenses/gpl.html
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 *
 * Comments for member variables are taken from RFC2030 by David Mills,
 * University of Delaware.
 *
 * Number format conversion code in NtpMessage(byte[] array) and toByteArray()
 * inspired by http://www.pps.jussieu.fr/~jch/enseignement/reseaux/
 * NTPMessage.java which is copyright (c) 2003 by Juliusz Chroboczek
 *
 * @author Adam Buckley
 */
class NtpMessage {
    /**
     * This is a two-bit code warning of an impending leap second to be
     * inserted/deleted in the last minute of the current day.  It's values
     * may be as follows:
     *
     * Value     Meaning
     * -----     -------
     * 0         no warning
     * 1         last minute has 61 seconds
     * 2         last minute has 59 seconds)
     * 3         alarm condition (clock not synchronized)
     */
    var leapIndicator: Byte = 0

    /**
     * This value indicates the NTP/SNTP version number.  The version number
     * is 3 for Version 3 (IPv4 only) and 4 for Version 4 (IPv4, IPv6 and OSI).
     * If necessary to distinguish between IPv4, IPv6 and OSI, the
     * encapsulating context must be inspected.
     */
    var version: Byte = 3

    /**
     * This value indicates the mode, with values defined as follows:
     *
     * Mode     Meaning
     * ----     -------
     * 0        reserved
     * 1        symmetric active
     * 2        symmetric passive
     * 3        client
     * 4        server
     * 5        broadcast
     * 6        reserved for NTP control message
     * 7        reserved for private use
     *
     * In unicast and anycast modes, the client sets this field to 3 (client)
     * in the request and the server sets it to 4 (server) in the reply. In
     * multicast mode, the server sets this field to 5 (broadcast).
     */
    var mode: Byte = 0

    /**
     * This value indicates the stratum level of the local clock, with values
     * defined as follows:
     *
     * Stratum  Meaning
     * ----------------------------------------------
     * 0        unspecified or unavailable
     * 1        primary reference (e.g., radio clock)
     * 2-15     secondary reference (via NTP or SNTP)
     * 16-255   reserved
     */
    var stratum: Short = 0

    /**
     * This value indicates the maximum interval between successive messages,
     * in seconds to the nearest power of two. The values that can appear in
     * this field presently range from 4 (16 s) to 14 (16284 s); however, most
     * applications use only the sub-range 6 (64 s) to 10 (1024 s).
     */
    var pollInterval: Byte = 0

    /**
     * This value indicates the precision of the local clock, in seconds to
     * the nearest power of two.  The values that normally appear in this field
     * range from -6 for mains-frequency clocks to -20 for microsecond clocks
     * found in some workstations.
     */
    var precision: Byte = 0

    /**
     * This value indicates the total roundtrip delay to the primary reference
     * source, in seconds.  Note that this variable can take on both positive
     * and negative values, depending on the relative time and frequency
     * offsets. The values that normally appear in this field range from
     * negative values of a few milliseconds to positive values of several
     * hundred milliseconds.
     */
    var rootDelay = 0.0

    /**
     * This value indicates the nominal error relative to the primary reference
     * source, in seconds.  The values  that normally appear in this field
     * range from 0 to several hundred milliseconds.
     */
    var rootDispersion = 0.0

    /**
     * This is a 4-byte array identifying the particular reference source.
     * In the case of NTP Version 3 or Version 4 stratum-0 (unspecified) or
     * stratum-1 (primary) servers, this is a four-character ASCII string, left
     * justified and zero padded to 32 bits. In NTP Version 3 secondary
     * servers, this is the 32-bit IPv4 address of the reference source. In NTP
     * Version 4 secondary servers, this is the low order 32 bits of the latest
     * transmit timestamp of the reference source. NTP primary (stratum 1)
     * servers should set this field to a code identifying the external
     * reference source according to the following list. If the external
     * reference is one of those listed, the associated code should be used.
     * Codes for sources not listed can be contrived as appropriate.
     *
     * Code     External Reference Source
     * ----     -------------------------
     * LOCL     uncalibrated local clock used as a primary reference for
     * a subnet without external means of synchronization
     * PPS      atomic clock or other pulse-per-second source
     * individually calibrated to national standards
     * ACTS     NIST dialup modem service
     * USNO     USNO modem service
     * PTB      PTB (Germany) modem service
     * TDF      Allouis (France) Radio 164 kHz
     * DCF      Mainflingen (Germany) Radio 77.5 kHz
     * MSF      Rugby (UK) Radio 60 kHz
     * WWV      Ft. Collins (US) Radio 2.5, 5, 10, 15, 20 MHz
     * WWVB     Boulder (US) Radio 60 kHz
     * WWVH     Kaui Hawaii (US) Radio 2.5, 5, 10, 15 MHz
     * CHU      Ottawa (Canada) Radio 3330, 7335, 14670 kHz
     * LORC     LORAN-C radionavigation system
     * OMEG     OMEGA radionavigation system
     * GPS      Global Positioning Service
     * GOES     Geostationary Orbit Environment Satellite
     */
    var referenceIdentifier = byteArrayOf(0, 0, 0, 0)

    /**
     * This is the time at which the local clock was last set or corrected, in
     * seconds since 00:00 1-Jan-1900.
     */
    var referenceTimestamp = 0.0

    /**
     * This is the time at which the request departed the client for the
     * server, in seconds since 00:00 1-Jan-1900.
     */
    var originateTimestamp = 0.0

    /**
     * This is the time at which the request arrived at the server, in seconds
     * since 00:00 1-Jan-1900.
     */
    var receiveTimestamp = 0.0

    /**
     * This is the time at which the reply departed the server for the client,
     * in seconds since 00:00 1-Jan-1900.
     */
    var transmitTimestamp = 0.0

    /**
     * Constructs a new NtpMessage from an array of bytes.
     */
    constructor(array: ByteArray) {
        // See the packet format diagram in RFC 2030 for details
        leapIndicator = (array[0].toInt() shr 6 and 0x3).toByte()
        version = (array[0].toInt() shr 3 and 0x7).toByte()
        mode = (array[0].toInt() and 0x7).toByte()
        stratum = unsignedByteToShort(array[1])
        pollInterval = array[2]
        precision = array[3]
        rootDelay = array[4] * 256.0 +
                unsignedByteToShort(array[5]) + unsignedByteToShort(array[6]) / 256.0 + unsignedByteToShort(array[7]) / 65536.0
        rootDispersion = unsignedByteToShort(array[8]) * 256.0 +
                unsignedByteToShort(array[9]) + unsignedByteToShort(array[10]) / 256.0 + unsignedByteToShort(array[11]) / 65536.0
        referenceIdentifier[0] = array[12]
        referenceIdentifier[1] = array[13]
        referenceIdentifier[2] = array[14]
        referenceIdentifier[3] = array[15]
        referenceTimestamp = decodeTimestamp(array, 16)
        originateTimestamp = decodeTimestamp(array, 24)
        receiveTimestamp = decodeTimestamp(array, 32)
        transmitTimestamp = decodeTimestamp(array, 40)
    }

    /**
     * Constructs a new NtpMessage in client -> server mode, and sets the
     * transmit timestamp to the current time.
     */
    constructor() {
        // Note that all the other member variables are already set with
        // appropriate default values.
        mode = 3
        transmitTimestamp = now()
    }

    /**
     * This method constructs the data bytes of a raw NTP packet.
     */
    fun toByteArray(): ByteArray {
        // All bytes are automatically set to 0
        val p = ByteArray(48)
        p[0] = (leapIndicator.toInt() shl 6 or (version.toInt() shl 3) or mode.toInt()).toByte()
        p[1] = stratum.toByte()
        p[2] = pollInterval
        p[3] = precision

        // root delay is a signed 16.16-bit FP, in Java an int is 32-bits
        val l = (rootDelay * 65536.0).toInt()
        p[4] = (l shr 24 and 0xFF).toByte()
        p[5] = (l shr 16 and 0xFF).toByte()
        p[6] = (l shr 8 and 0xFF).toByte()
        p[7] = (l and 0xFF).toByte()

        // root dispersion is an unsigned 16.16-bit FP, in Java there are no
        // unsigned primitive types, so we use a long which is 64-bits
        val ul = (rootDispersion * 65536.0).toLong()
        p[8] = (ul shr 24 and 0xFFL).toByte()
        p[9] = (ul shr 16 and 0xFFL).toByte()
        p[10] = (ul shr 8 and 0xFFL).toByte()
        p[11] = (ul and 0xFFL).toByte()
        p[12] = referenceIdentifier[0]
        p[13] = referenceIdentifier[1]
        p[14] = referenceIdentifier[2]
        p[15] = referenceIdentifier[3]
        encodeTimestamp(p, 16, referenceTimestamp)
        encodeTimestamp(p, 24, originateTimestamp)
        encodeTimestamp(p, 32, receiveTimestamp)
        encodeTimestamp(p, 40, transmitTimestamp)
        return p
    }

    /**
     * Returns a string representation of a NtpMessage
     */
    override fun toString(): String {
        val precisionStr: String = DecimalFormat("0.#E0").format(2.0.pow(precision.toDouble()))
        return """|Leap indicator: $leapIndicator
                  |Version: $version
                  |Mode: $mode
                  |Stratum: $stratum
                  |Poll: $pollInterval
                  |Precision: $precision ($precisionStr seconds)
                  |Root delay: ${DecimalFormat("0.00").format(rootDelay * 1000)} ms
                  |Root dispersion: ${DecimalFormat("0.00").format(rootDispersion * 1000)} ms
                  |Reference identifier: ${referenceIdentifierToString(referenceIdentifier, stratum, version)}
                  |Reference timestamp: ${timestampToString(referenceTimestamp)}
                  |Originate timestamp: ${timestampToString(originateTimestamp)}
                  |Receive timestamp:   ${timestampToString(receiveTimestamp)}
                  |Transmit timestamp:  ${timestampToString(transmitTimestamp)}""".trimMargin()
    }

    companion object {
        private const val DAYS: Long = 25567 // 1 Jan 1900 to 1 Jan 1970
        private const val SECS = 60 * 60 * 24 * DAYS

        // Translate Java/Unix's epoch (1 Jan 1970) to NTP's epoch
        // (1 Jan 1900) and convert from milliseconds to fractions of seconds
        fun now(): Double {
            return System.currentTimeMillis() / 1000.0 + SECS
        }

        /**
         * Converts an unsigned byte to a short.  By default, Java assumes that
         * a byte is signed.
         */
        fun unsignedByteToShort(b: Byte): Short {
            return if (b.toInt() and 0x80 == 0x80) (128 + (b.toInt() and 0x7f)).toShort() else b.toShort()
        }

        /**
         * Will read 8 bytes of a message beginning at `pointer`
         * and return it as a double, according to the NTP 64-bit timestamp
         * format.
         */
        fun decodeTimestamp(array: ByteArray, pointer: Int): Double {
            var r = 0.0
            for (i in 0..7) {
                r += unsignedByteToShort(array[pointer + i]) * 2.0.pow((3 - i) * 8)
            }
            return r
        }

        /**
         * Encodes a timestamp in the specified position in the message
         */
        fun encodeTimestamp(array: ByteArray, pointer: Int, timestamp: Double) {
            // Converts a double into a 64-bit fixed point
            var timestamp = timestamp
            for (i in 0..7) {
                // 2^24, 2^16, 2^8, .. 2^-32
                val base: Double = 2.0.pow((3 - i) * 8)
                // Capture byte value
                array[pointer + i] = (timestamp / base).toInt().toByte()

                // Subtract captured value from remaining total
                timestamp -= (unsignedByteToShort(array[pointer + i]) * base)
            }

            // From RFC 2030: It is advisable to fill the non-significant
            // low order bits of the timestamp with a random, unbiased
            // bitstring, both to avoid systematic roundoff errors and as
            // a means of loop detection and replay detection.
            array[7] = (Math.random() * 255.0).toInt().toByte()
        }

        /**
         * Returns a timestamp (number of seconds since 00:00 1-Jan-1900) as a
         * formatted date/time string.
         */
        private const val dtf = "%1\$ta, %1\$td %1\$tb %1\$tY, %1\$tI:%1\$tm:%1\$tS.%1\$tL %1\$tp %1\$tZ"
        fun timestampToString(timestamp: Double): String {
            return if (timestamp == 0.0) "0" else millisToDate(Math.round(1000.0 * (timestamp - SECS)))
            // timestamp is relative to 1900, utc is used by Java and is relative to 1970
        }

        fun millisToDate(ms: Long): String {
            return String.format(dtf, ms)
        }

        /**
         * Returns a string representation of a reference identifier according
         * to the rules set out in RFC 2030.
         */
        fun referenceIdentifierToString(ref: ByteArray, stratum: Short, version: Byte): String {
            // From the RFC 2030:
            // In the case of NTP Version 3 or Version 4 stratum-0 (unspecified)
            // or stratum-1 (primary) servers, this is a four-character ASCII
            // string, left justified and zero padded to 32 bits.
            if (stratum.toInt() == 0 || stratum.toInt() == 1) {
                return String(ref)
            } else if (version.toInt() == 3) {
                return unsignedByteToShort(ref[0]).toString() + "." +
                        unsignedByteToShort(ref[1]) + "." +
                        unsignedByteToShort(ref[2]) + "." +
                        unsignedByteToShort(ref[3])
            } else if (version.toInt() == 4) {
                return "" + (unsignedByteToShort(ref[0]) / 256.0 + unsignedByteToShort(ref[1]) / 65536.0 + unsignedByteToShort(
                    ref[2]
                ) / 16777216.0 + unsignedByteToShort(ref[3]) / 4294967296.0)
            }
            return ""
        }
    }
}



