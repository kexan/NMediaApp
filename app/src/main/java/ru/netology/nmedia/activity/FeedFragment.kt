package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FullsizePhotoFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val postViewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
    private val authViewModel: AuthViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val notAuthorizedDialog: AlertDialog.Builder = AlertDialog.Builder(context)
            .setMessage("Want to sign in?")
            .setTitle("Not authorized")
            .setPositiveButton("Sign up") { _, _ ->
                findNavController().navigate(R.id.action_feedFragment_to_signUpFragment)
            }
            .setNegativeButton("Sign in") { _, _ ->
                findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
            }

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                postViewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply { textArg = post.content })
            }

            override fun onAttachment(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_fullsizePhotoFragment,
                    Bundle().apply {
                        textArg = post.attachment?.url
                    })
            }

            override fun onLike(post: Post) {

                if (!postViewModel.authenticated) {
                    notAuthorizedDialog.show()
                    return
                }

                if (post.likedByMe) {
                    postViewModel.unLikeById(post.id)
                } else {
                    postViewModel.likeById(post.id)
                }
            }

            override fun onRemove(post: Post) {
                postViewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })

        binding.list.adapter = adapter

        postViewModel.dataState.observe(
            viewLifecycleOwner
        ) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { postViewModel.loadPosts() }
                    .show()
            }
        }

        viewLifecycleOwner.lifecycle.coroutineScope.launchWhenCreated {
            postViewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        viewLifecycleOwner.lifecycle.coroutineScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swiperefresh.isRefreshing =
                    it.refresh is LoadState.Loading ||
                            it.prepend is LoadState.Loading ||
                            it.append is LoadState.Loading
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            adapter.refresh()
        }

        binding.fab.setOnClickListener {

            if (!postViewModel.authenticated) {
                notAuthorizedDialog.show()
                return@setOnClickListener
            }

            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root


    }
}
