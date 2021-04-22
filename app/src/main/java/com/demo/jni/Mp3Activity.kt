package com.demo.jni

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

class Mp3Activity : AppCompatActivity() {

    private val sampleRate = 44100
    private val channel = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferMinSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat)

    //编码
    val mp3Encoder = Mp3Encoder()

    private lateinit var status: Status

    private var filename = "record.pcm"
    private var fileMp3name = "record.mp3"

    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private var audioRecord: AudioRecord? = null

    private var thread: Thread? = null

    private var mediaPlayer: MediaPlayer? = null

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3)

        ActivityCompat.requestPermissions(
            this, permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )

        val record = findViewById<Button>(R.id.record)
        val stopRecord = findViewById<Button>(R.id.stop_record)
        val encode = findViewById<Button>(R.id.encode)
        val play = findViewById<Button>(R.id.play)
        val path = findViewById<Button>(R.id.path)

        status = Status.PREPARING

        record.setOnClickListener {
            when (status) {
                Status.PREPARING -> {
                    createRecord()
                    startRecord()
                    record.text = "停止"
                }
                Status.STARTING -> {
                    stopRecord()
                    record.text = "录音"
                    path.text = "输出目录: " + (externalCacheDir?.absolutePath + File.separator + filename)
                }
            }
        }

        encode.setOnClickListener {
            val pcmPath = File(externalCacheDir, "record.pcm").absolutePath
            val target = File(externalCacheDir, "target.mp3").absolutePath
            if (!File(pcmPath).exists()) {
                Toast.makeText(this, "请先进行录制PCM音频", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            thread {
                val ret = mp3Encoder.init(pcmPath, 2, 128, 44100, target)
                if (ret == 0) {
                    mp3Encoder.encode()
                    mp3Encoder.destroy()
                    handler.post {
                        Toast.makeText(this, "PCM->MP3编码完成", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    handler.post {
                        Toast.makeText(this, "Lame初始化失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        play.setOnClickListener {
            val target = File(externalCacheDir, "target.mp3").absolutePath
            if(File(target).exists()){
                if(mediaPlayer==null) {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    }
                }
                mediaPlayer?.setDataSource(target)
                mediaPlayer?.prepareAsync()
                mediaPlayer?.setOnCompletionListener {

                }
                mediaPlayer?.setOnErrorListener { mp, what, extra ->
                    Log.e("zll","$what   --- $extra")
                    false
                }
            }else{
                Toast.makeText(this, "暂无mp3文件", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecord() {
        if (audioRecord != null && audioRecord?.state != AudioRecord.STATE_UNINITIALIZED) {
            audioRecord?.startRecording()
            status = Status.STARTING
            thread = thread {
                writeToPcm()
            }
        }
    }

    private fun stopRecord() {
        if (audioRecord != null && audioRecord?.state != AudioRecord.STATE_UNINITIALIZED) {
            audioRecord?.stop()
            status = Status.PREPARING
            release()
        }
    }

    private fun release() {
        thread?.join()
        thread = null
        audioRecord?.release()
        audioRecord = null
    }


    private fun writeToPcm() {
        var code = 0
        val byteArray = ByteArray(bufferMinSize)
        val file = File(externalCacheDir?.absoluteFile, filename)
        if (file.exists()) {
            file.delete()
        } else {
            file.createNewFile()
        }
        val fos = FileOutputStream(file)

        while (status == Status.STARTING) {
            code = audioRecord?.read(byteArray, 0, bufferMinSize) ?: -1
            if (code != AudioRecord.ERROR_BAD_VALUE || code != AudioRecord.ERROR_INVALID_OPERATION || code != AudioRecord.ERROR_DEAD_OBJECT) {
                fos.write(byteArray)
            }
        }
        fos.close()
    }

    private fun createRecord() {
        if (audioRecord == null) {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channel,
                audioFormat,
                bufferMinSize
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val code = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!code) {
            finish()
        }
    }

}


sealed class Status {

    constructor()

    object PREPARING : Status()
    object STARTING : Status()
    object STOP : Status()
}
