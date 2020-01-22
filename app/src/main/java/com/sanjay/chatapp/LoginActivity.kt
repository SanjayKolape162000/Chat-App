package com.sanjay.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        back_to_registration.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        login_btn.setOnClickListener {
            loginUser()
            Log.d("LoginActivity","End of login user")
        }
    }

    private fun loginUser() {


        if (login_email.text.toString().isEmpty()) {
            username_register.error = "Please Enter Email"
            username_register.requestFocus()
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(login_email.text.toString()).matches()) {
            email_register.error = "Please Enter Valid Email"
            email_register.requestFocus()
            return
        } else if (login_password.text.toString().isEmpty()) {
            password_register.error = "Please Enter password"
            password_register.requestFocus()
            return
        }
        auth.signInWithEmailAndPassword(login_email.text.toString(), login_password.text.toString())
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    Log.d("LoginActivity","task is successful")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    updateUI(null)
                }
            }

    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)

    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(this, LatestMessagesActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(baseContext, "Login failed", Toast.LENGTH_SHORT).show()
        }

    }
}
