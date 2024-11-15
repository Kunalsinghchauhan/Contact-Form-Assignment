package com.ksc.contactformassignment

import android.content.ContentValues
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class GenderActivity : AppCompatActivity() {

    private lateinit var genderSpinner: Spinner
    private lateinit var nextButton: Button
    private lateinit var audioRecorder: MediaRecorder
    private var isRecording = false
    private var recordingFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gender)
        initializeAudioRecorder()
        genderSpinner = findViewById(R.id.spinner_gender)
        nextButton = findViewById(R.id.btn_next_gender)

        // Set up gender spinner options
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Only start recording if a valid gender is selected
                if (position != 0 && !isRecording) {  // Skip position 0 if it's a default "Select Gender" option
                    startRecordingAudio()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Optional: Handle case when nothing is selected (for example, show a message or handle it accordingly)
            }
        }

        nextButton.setOnClickListener {

            val selectedPosition = genderSpinner.selectedItemPosition

            if (selectedPosition == 0) {
                Toast.makeText(this, "Please select a valid gender option", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val intent = Intent(this, AgeActivity::class.java)
                intent.putExtra("selectedGender", selectedPosition)
                startActivity(intent)
            }
        }
    }

    private fun initializeAudioRecorder() {
        try {
            // Check if we are targeting Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use MediaStore to save audio in shared storage
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, "audio_recording.3gp")
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/AudioRecordings") // Optional
                }

                val contentResolver = applicationContext.contentResolver
                val audioUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

                audioUri?.let { uri ->
                    recordingFilePath = uri.toString() // Use the Uri to save the file
                    audioRecorder = MediaRecorder().apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        setOutputFile(contentResolver.openFileDescriptor(uri, "w")?.fileDescriptor)
                    }
                }
            } else {
                // For Android versions below 13, use the traditional method
                val audioFile = getExternalFilesDir(null)?.absolutePath + "/audio_recording.3gp"
                recordingFilePath = audioFile
                audioRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(audioFile)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing audio recorder: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRecordingAudio() {
        if (!isRecording) {
            try {
                audioRecorder.prepare()
                audioRecorder.start()
                isRecording = true
                Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error starting audio recording", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecordingAudio() {
        if (isRecording) {
            try {
                audioRecorder.stop()
                audioRecorder.release()
                isRecording = false
                Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
            } catch (e: RuntimeException) {
                e.printStackTrace()
                Toast.makeText(this, "Error stopping audio recording", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopRecordingAudio()
    }

    override fun onPause() {
        super.onPause()
        stopRecordingAudio()
    }
}
