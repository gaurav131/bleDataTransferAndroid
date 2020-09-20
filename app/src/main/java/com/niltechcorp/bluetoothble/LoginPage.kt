package com.niltechcorp.bluetoothble

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.niltechcorp.bluetoothble.databinding.FragmentLoginPageBinding
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class LoginPage : Fragment() {
    lateinit var binding: FragmentLoginPageBinding
    private val userData = listOf("16891A0401", "16891A0402", "16891A0407", "16891A04G4", "QWERTY")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_page, container, false)
        binding.verify.setOnClickListener { view: View ->
            var roll: String = binding.rollNumber.text.toString()
            if (roll.toUpperCase(Locale.ROOT) !in userData) {
                binding.rollNumber.error = "wrong roll number"
            } else {
                val intent = Intent(context, Main2Activity::class.java)
                startActivity(intent)
            }
        }
        return binding.root
    }

}
