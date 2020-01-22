package com.sanjay.chatapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var createaccount: Button
    private lateinit var alreadyaccount: TextView
    private lateinit var imagebtn: Button

    companion object {
        val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        username = findViewById(R.id.username_register)
        email = findViewById(R.id.email_register)
        password = findViewById(R.id.password_register)
        createaccount = findViewById(R.id.register_btn)
        alreadyaccount = findViewById(R.id.already_have_an_account_text)
        imagebtn = findViewById(R.id.image_btn)


        alreadyaccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        imagebtn.setOnClickListener {
            Log.d(TAG, "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }


        createaccount.setOnClickListener {
            CreateAccount()
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed and check what the selected image was....
            Log.d(TAG, "Photo was selected")

            selectedPhotoUri = data.data

            @Suppress("DEPRECATION") val bitmap =
                MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circular_image_view.setImageBitmap(bitmap)

            imagebtn.alpha = 0f

//      val bitmapDrawable = BitmapDrawable(bitmap)
//      selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun CreateAccount() {

        val name = username.text.toString()
        val email = email.text.toString()
        val password = password.text.toString()

        if (username_register.text.toString().isEmpty()) {
            username_register.error = "Please Enter username"
            username_register.requestFocus()
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email_register.text.toString()).matches()) {
            email_register.error = "Please Enter Valid Email"
            email_register.requestFocus()
            return
        } else if (password_register.text.toString().isEmpty()) {
            password_register.error = "Please Enter password"
            password_register.requestFocus()
            return
        } else {

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {

                        Log.d(TAG, "username is: $name")
                        Log.d(TAG, "email is: $email")
                        Log.d(TAG, "password is: $password")


                        uploadImageToFirebaseStorage()

                    } else {

                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

        }


    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { it ->
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File Location: $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to upload image to storage: ${it.message}")
            }
    }

    private fun saveUserToFirebaseDatabase(profileimageurl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(
            uid,
            username_register.text.toString(),
            email_register.text.toString(),
            password_register.text.toString(),
            profileimageurl
        )
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "successfully upload user to database")
                val intent = Intent(this, LatestMessagesActivity::class.java)
                startActivity(intent)
                finish()
                Log.d(TAG, "entering into latest message activity")

            }
    }
}



