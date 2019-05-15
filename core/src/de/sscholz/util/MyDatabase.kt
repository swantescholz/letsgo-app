package de.sscholz.util

import com.badlogic.gdx.Gdx

object MyDatabase {

    private const val localBasePath = "userdata/"

    fun writeStringToLocalFile(filePath: String, text: String, append: Boolean = false) {
        val file = Gdx.files.local(localBasePath + filePath)
        file.writeString(text, append)
    }

    // only for stuff that your app has written before (read/write).
    // files that should be available to the app from the very beginning (i.e. assets)
    // can only be placed in internal storage
    fun readLocalFile(filePath: String): String {
        val file = Gdx.files.local(localBasePath + filePath)
        return file.readString()
    }

    // for stuff in the assets folder (read only)
    fun readInternalReadOnlyFile(filePath: String): String {
        val file = Gdx.files.internal(filePath)
        return file.readString()
    }

    // for stuff in the assets folder (read only)
    fun readInternalReadOnlyBinaryFile(filePath: String): ByteArray {
        val file = Gdx.files.internal(filePath)
        return file.readBytes()
    }

    // throws exception if neither file is found
    fun readLocalFileOrElseInternalFile(filePath: String): String {
        try {
            return readLocalFile(filePath)
        } catch (e: Exception) {
        }
        return readInternalReadOnlyFile(filePath)
    }


}