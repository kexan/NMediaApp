package ru.netology.nmedia.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.SignUpFragmentBinding
import ru.netology.nmedia.viewmodel.SignUpViewModel

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = SignUpFragmentBinding.inflate(inflater, container, false)

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

        binding.signupButton.setOnClickListener {

            if (binding.password.text.toString() != binding.confirmPassword.text.toString()) {
                Toast.makeText(context, "Please check you password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.registerUser(
                binding.login.text.toString(),
                binding.password.text.toString(),
                binding.name.text.toString()
            )

        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            binding.photo.setImageURI(it.uri)
        }

        viewModel.registrationSuccess.observe(viewLifecycleOwner) {
            if (it == false) {
                Toast.makeText(context, "Something wrong, please try again", Toast.LENGTH_SHORT)
                    .show()
                return@observe
            }
            Toast.makeText(context, "Signed up!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        return binding.root
    }


}