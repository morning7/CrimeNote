package com.android.study.ui

import android.app.Activity
import android.content.ClipDescription
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.FileUtils
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.study.R
import com.android.study.control.CrimeDetailsViewModel
import com.android.study.databinding.FragmentDetailBinding
import com.android.study.model.Crime
import com.android.study.utils.PhotoUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener as OnGlobalLayoutListener1

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "dialog_date"
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeDetailsFragment: Fragment() {
    private lateinit var bind: FragmentDetailBinding
    private lateinit var crime: Crime
    private lateinit var contactsLaunch: ActivityResultLauncher<Intent>
    private lateinit var photoLaunch: ActivityResultLauncher<Intent>
    private lateinit var albumLaunch: ActivityResultLauncher<Intent>
    private lateinit var permissionLaunch: ActivityResultLauncher<Array<String>>
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val crimeDetailsViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailsViewModel::class.java]
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeDetailsFragment {
            return CrimeDetailsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CRIME_ID, crimeId)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val activity = activity as AppCompatActivity
        val toolbar = activity.supportActionBar
        toolbar?.title = "Crime Details"

        bind.etTitle.addTextChangedListener {
            crime.title = it.toString()
        }

        bind.cb.setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved = isChecked
        }

        bind.btnDate.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                val fragmentManager = this@CrimeDetailsFragment.parentFragmentManager
                fragmentManager.setFragmentResultListener(REQUEST_DATE, this,
                    { requestKey, result ->
                        if (requestKey == REQUEST_DATE) {
                            crime.date = result.getSerializable(ARG_DATE) as Date
                            updateUI()
                        }
                    })
                show(fragmentManager, DIALOG_DATE)
            }
        }

        bind.btnSend.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = ClipDescription.MIMETYPE_TEXT_PLAIN
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                Intent.createChooser(intent, getString(R.string.send_report))
                    .also {
                        startActivity(it)
                    }
            }
        }

        bind.btnChoose.apply {

            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                contactsLaunch.launch(intent)
            }
            val packageManager = requireActivity().packageManager
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo == null) {
//                isEnabled = false
            }
        }

        bind.btnCall.apply {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$text"))
            setOnClickListener {
//                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                    permissionLaunch.launch()
//                } else {
                    startActivity(intent)
//                }
            }
        }

        bind.ibPhoto.apply {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val packageManager = requireActivity().packageManager
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo == null) {
                isEnabled = false
            }
            setOnClickListener {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                cameraActivities.forEach {
                    Log.e("=====", it.activityInfo.packageName)
                    requireActivity().grantUriPermission(requireActivity().packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                photoLaunch.launch(intent)
            }
        }

        bind.ibAlbum.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            albumLaunch.launch(intent)
        }

        bind.ivPhoto.setOnClickListener {
            PhotoDialogFragment.newInstance(photoFile.path)
                .show(parentFragmentManager, "photo_dialog")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailsViewModel.loadCrime(crimeId)
        contactsLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when {
                result.resultCode != Activity.RESULT_OK -> return@registerForActivityResult
                result.data == null -> return@registerForActivityResult
                result.data != null -> {
                    val contactUri: Uri? = result.data?.data
                    val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)
                    contactUri?.let {
                        val contactCursor = requireContext().contentResolver.query(contactUri, queryFields,
                            null, null, null, null)
                        contactCursor?.let {
                            if (it.count == 0) {
                               return@registerForActivityResult
                            }
                            it.moveToFirst()
                            val name = it.getString(0)
                            val id = it.getString(1)
                            crime.suspect = name
                            crimeDetailsViewModel.updateCrime(crime)
                            bind.btnChoose.text = name

                            val phoneCursor = requireActivity().contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                                null,
                                null)
                            phoneCursor?.let { _phoneCursor ->
                                if (_phoneCursor.count == 0) {
                                    return@registerForActivityResult
                                }
                                _phoneCursor.moveToFirst()
                                val phone = _phoneCursor.getString(0)
                                bind.btnCall.text = phone
                            }
                            phoneCursor?.close()
                        }
                        contactCursor?.close()
                    }
                }
            }
        }

        photoLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            updatePhotoView()
            requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        albumLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            if (it.data == null || it.data!!.data == null) {
                return@registerForActivityResult
            }
            photoUri = it.data!!.data!!
            val cursor = requireActivity().contentResolver.query(photoUri, arrayOf(MediaStore.Images.ImageColumns.DATA),
                null, null, null)
            var srcPath = ""
            cursor?.let {
                cursor.moveToFirst()
                srcPath = cursor.getString(0)
                Log.e("=====", "album path $srcPath")
                cursor.close()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(FileInputStream(File(srcPath)), FileOutputStream(photoFile))
            }
            updatePhotoView()
        }

        permissionLaunch = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        }
        permissionLaunch.launch(
            arrayOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentDetailBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailsViewModel.crimeLiveData.observe(viewLifecycleOwner){ crime ->
            if (crime != null) {
                this.crime = crime
                photoFile = crimeDetailsViewModel.getPhotoFile(crime)
                updateUI()
            } else {
                photoFile = crimeDetailsViewModel.getPhotoFile(this.crime)
            }
            photoUri = FileProvider.getUriForFile(requireActivity(), "com.android.study.fileprovider", photoFile)
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailsViewModel.insertOrUpdate(crime)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    private fun updateUI() {
        bind.apply {
            etTitle.setText(crime.title)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            btnDate.text = format.format(crime.date)
            cb.apply {
                isChecked = crime.isSolved
                jumpDrawablesToCurrentState()
            }
            if (crime.suspect.isNotEmpty()) {
                btnChoose.text = crime.suspect
            }
        }
        updatePhotoView()
    }



    private fun updatePhotoView() {
        bind.ivPhoto.apply {
            if (!photoFile.exists()) {
                setImageBitmap(null)
                return@apply
            }
            viewTreeObserver.addOnGlobalLayoutListener(object: OnGlobalLayoutListener1 {
                override fun onGlobalLayout() {
                    setImageBitmap(PhotoUtils.getScaledBitmap(photoFile.path, width, height))
                    if (viewTreeObserver.isAlive) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun getCrimeReport(): String{
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, solvedString, dateString, suspect)
    }

}