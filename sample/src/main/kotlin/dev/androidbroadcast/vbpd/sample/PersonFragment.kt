package dev.androidbroadcast.vbpd.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import dev.androidbroadcast.vbpd.sample.databinding.FragmentPersonDetailBinding
import dev.androidbroadcast.vbpd.viewBinding

class PersonFragment : DialogFragment(R.layout.fragment_person_detail) {

    // Without reflection
    private val viewBinding by viewBinding(FragmentPersonDetailBinding::bind)

    // With reflection
    // private val viewBinding: FragmentPersonDetailBinding by viewBinding()

    private val person: Person by parcelableArgument(ARG_PERSON)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            firstName.text = person.name
            lastName.text = person.surname
            email.text = person.email
        }
    }

    companion object {

        private const val ARG_PERSON = "PERSON"

        fun newInstance(person: Person): PersonFragment {
            return PersonFragment().apply {
                arguments = arguments(person)
            }
        }

        fun arguments(person: Person): Bundle {
            return Bundle(1).apply {
                putParcelable(ARG_PERSON, person)
            }
        }
    }
}
