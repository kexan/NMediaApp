package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.SignInFragmentBinding
import ru.netology.nmedia.viewmodel.SignInViewModel

class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = SignInFragmentBinding.inflate(inflater, container, false)

        binding.signinButton.setOnClickListener {
            viewModel.updateUser(binding.login.text.toString(), binding.password.text.toString())
        }

        viewModel.loginSuccess.observe(viewLifecycleOwner) {
            if (it == false) {
                Toast.makeText(context, "Wrong login/password", Toast.LENGTH_SHORT).show()
                return@observe
            }
            Toast.makeText(context, "Signed in!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        return binding.root
    }


}