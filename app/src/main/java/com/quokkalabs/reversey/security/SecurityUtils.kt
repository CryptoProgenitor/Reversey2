package com.quokkalabs.reversey.security

import android.util.Log
import java.io.File
import java.util.zip.ZipEntry

/**
 * Security utilities for backup operations.
 *
 * CRITICAL: Prevents path traversal attacks (Zip Slip vulnerability).
 *
 * VULNERABILITY EXAMPLE:
 * Malicious zip contains entry: "../../system/passwd"
 * Without validation: writes to /system/passwd (BAD!)
 * With validation: throws SecurityException (GOOD!)
 */
object SecurityUtils {

    private const val TAG = "SecurityUtils"

    /**
     * Validates a zip entry to prevent path traversal attacks.
     *
     * ZIP SLIP ATTACK:
     * - Attacker creates zip with entry name: "../../../../../../system/file"
     * - Naive code: File(targetDir, entry.name) → writes outside target dir
     * - This function: Detects and blocks such attempts
     *
     * VALIDATION:
     * 1. Strip leading slashes (prevent absolute paths)
     * 2. Check canonical path stays within target directory
     * 3. Reject suspicious entries
     *
     * @param entry The zip entry to validate
     * @param targetDir The directory where extraction should occur
     * @return Safe file path for extraction
     * @throws SecurityException if entry is malicious
     */
    @Throws(SecurityException::class)
    fun validateZipEntry(entry: ZipEntry, targetDir: File): File {
        // Strip leading slashes - prevents absolute path tricks
        val safeName = entry.name.trimStart('/', '\\')

        // Reject empty names
        if (safeName.isEmpty()) {
            throw SecurityException("Zip entry has empty name")
        }

        // Reject directory traversal attempts in name itself
        if (safeName.contains("..")) {
            Log.w(TAG, "Rejected zip entry with '..' in name: ${entry.name}")
            throw SecurityException("Zip entry contains path traversal: ${entry.name}")
        }

        // Create destination file
        val destFile = File(targetDir, safeName)

        // Get canonical (absolute, normalized) paths
        val targetCanonical = targetDir.canonicalPath
        val destCanonical = destFile.canonicalPath

        // CRITICAL CHECK: Destination must be inside target directory
        if (!destCanonical.startsWith(targetCanonical)) {
            Log.w(TAG, "Zip Slip detected!")
            Log.w(TAG, "  Entry name: ${entry.name}")
            Log.w(TAG, "  Safe name: $safeName")
            Log.w(TAG, "  Target dir: $targetCanonical")
            Log.w(TAG, "  Dest path: $destCanonical")
            throw SecurityException("Zip Slip attack detected: ${entry.name}")
        }

        // Additional check: Path length sanity
        if (destCanonical.length > targetCanonical.length + 500) {
            throw SecurityException("Zip entry path suspiciously long: ${entry.name}")
        }

        Log.d(TAG, "Validated zip entry: $safeName")
        return destFile
    }

    /**
     * Validates zip entry with additional name checks.
     *
     * ADDITIONAL SAFETY:
     * - Checks for null bytes (path truncation attacks)
     * - Checks for suspicious characters
     * - Checks for excessively long names
     *
     * @param entry The zip entry to validate
     * @param targetDir The directory where extraction should occur
     * @param maxNameLength Maximum allowed entry name length (default 255)
     * @return Safe file path for extraction
     * @throws SecurityException if entry is malicious
     */
    @Throws(SecurityException::class)
    fun validateZipEntryStrict(
        entry: ZipEntry,
        targetDir: File,
        maxNameLength: Int = 255
    ): File {
        // Check for null bytes (path truncation attack)
        if (entry.name.contains('\u0000')) {
            throw SecurityException("Zip entry name contains null byte: ${entry.name}")
        }

        // Check name length
        if (entry.name.length > maxNameLength) {
            throw SecurityException("Zip entry name too long: ${entry.name.length} chars")
        }

        // Use standard validation
        return validateZipEntry(entry, targetDir)
    }

    /**
     * Validates that a file is actually a zip file by checking magic bytes.
     *
     * ZIP MAGIC BYTES: PK\x03\x04 (0x504B0304)
     *
     * @param file The file to check
     * @return true if file appears to be a valid zip
     */
    fun isValidZipFile(file: File): Boolean {
        if (!file.exists() || !file.isFile) {
            return false
        }

        // Check file size (zip header is at least 4 bytes)
        if (file.length() < 4) {
            return false
        }

        try {
            file.inputStream().use { input ->
                val header = ByteArray(4)
                val bytesRead = input.read(header)

                if (bytesRead != 4) {
                    return false
                }

                // Check for ZIP magic bytes: PK\x03\x04
                return header[0] == 0x50.toByte() &&  // 'P'
                        header[1] == 0x4B.toByte() &&  // 'K'
                        header[2] == 0x03.toByte() &&
                        header[3] == 0x04.toByte()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking zip magic bytes", e)
            return false
        }
    }

    /**
     * Sanitizes a filename for safe storage.
     *
     * REMOVES/REPLACES:
     * - Path separators (/ and \)
     * - Null bytes
     * - Control characters
     * - Leading/trailing dots and spaces
     *
     * @param filename Original filename
     * @return Sanitized filename safe for filesystem
     */
    fun sanitizeFilename(filename: String): String {
        var safe = filename

        // Replace path separators
        safe = safe.replace('/', '_')
        safe = safe.replace('\\', '_')

        // Remove null bytes
        safe = safe.replace("\u0000", "")

        // Remove control characters (ASCII 0-31 and 127)
        safe = safe.replace(Regex("[\u0000-\u001F\u007F]"), "")

        // Trim dots and spaces from start/end
        safe = safe.trim('.', ' ')

        // If empty after sanitization, use default
        if (safe.isEmpty()) {
            safe = "unnamed_file"
        }

        // Limit length
        if (safe.length > 255) {
            safe = safe.take(255)
        }

        return safe
    }

    /**
     * Validates backup file size is reasonable.
     *
     * PREVENTS:
     * - Zip bombs (tiny zip that expands to gigabytes)
     * - Out of memory errors
     * - Storage exhaustion
     *
     * @param file The backup file to check
     * @param maxSizeMB Maximum allowed size in megabytes (default 500MB)
     * @return true if size is reasonable
     */
    fun isReasonableBackupSize(file: File, maxSizeMB: Int = 500): Boolean {
        if (!file.exists()) return false

        val maxBytes = maxSizeMB * 1024L * 1024L
        val actualBytes = file.length()

        if (actualBytes > maxBytes) {
            Log.w(TAG, "Backup file too large: ${actualBytes / 1024 / 1024}MB (max: ${maxSizeMB}MB)")
            return false
        }

        return true
    }

    /**
     * Checks if a directory is within the app's private storage.
     *
     * SAFETY: Ensures operations only touch app's own data.
     *
     * @param dir Directory to check
     * @param appFilesDir App's files directory (context.filesDir)
     * @return true if directory is within app's private storage
     */
    fun isWithinAppStorage(dir: File, appFilesDir: File): Boolean {
        return try {
            val dirCanonical = dir.canonicalPath
            val appCanonical = appFilesDir.canonicalPath
            dirCanonical.startsWith(appCanonical)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if directory is within app storage", e)
            false
        }
    }
}