@file:Suppress("RedundantVisibilityModifier", "unused")
@file:JvmName("FragmentViewBindings")

package by.kirich1409.viewbindingdelegate

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.annotation.RestrictTo
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.internal.getRootView
import by.kirich1409.viewbindingdelegate.internal.requireViewByIdCompat
import by.kirich1409.viewbindingdelegate.internal.weakReference
import kotlin.reflect.KProperty

private class FragmentViewBindingProperty<F : Fragment, T : ViewBinding>(
    private val viewNeedsInitialization: Boolean,
    viewBinder: (F) -> T,
) : BaseViewBindingProperty<F, T>(viewBinder) {

    private var lifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks? = null
    private var fragmentManager: FragmentManager? = null

    @MainThread
    override fun getValue(thisRef: F, property: KProperty<*>): T {
        val viewBinding = super.getValue(thisRef, property)
        registerLifecycleCallbacksIfNeeded(thisRef)
        return viewBinding
    }

    /**
     * Register callbacks to listen event about Fragment's View
     */
    private fun registerLifecycleCallbacksIfNeeded(fragment: Fragment) {
        if (lifecycleCallbacks != null) return

        val fragmentManager = fragment.parentFragmentManager
            .also { fm -> this.fragmentManager = fm }
        lifecycleCallbacks = VBFragmentLifecycleCallback(fragment).also { callbacks ->
            fragmentManager.registerFragmentLifecycleCallbacks(callbacks, false)
        }
    }

    override fun clear() {
        super.clear()

        val lifecycleCallbacks = lifecycleCallbacks
        if (lifecycleCallbacks != null) {
            fragmentManager?.unregisterFragmentLifecycleCallbacks(lifecycleCallbacks)
        }

        fragmentManager = null
        this.lifecycleCallbacks = null
    }

    internal inner class VBFragmentLifecycleCallback(
        fragment: Fragment,
    ) : FragmentManager.FragmentLifecycleCallbacks() {

        private val fragment by weakReference(fragment)

        override fun onFragmentViewDestroyed(
            fm: FragmentManager,
            f: Fragment,
        ) {
            if (fragment === f) clear()
        }
    }
}

/**
 * Create new [ViewBinding] associated with the [Fragment]
 */
@Suppress("UnusedReceiverParameter")
@JvmName("viewBindingFragmentWithCallbacks")
public fun <F : Fragment, T : ViewBinding> Fragment.viewBinding(
    viewBinder: (F) -> T,
): ViewBindingProperty<F, T> {
    return fragmentViewBinding(viewBinder)
}

/**
 * Create new [ViewBinding] associated with the [Fragment]
 *
 * @param vbFactory Function that creates a new instance of [ViewBinding]. `MyViewBinding::bind` can be used
 * @param viewProvider Provide a [View] from the Fragment. By default call [Fragment.requireView]
 */
@JvmName("viewBindingFragment")
public inline fun <F : Fragment, T : ViewBinding> Fragment.viewBinding(
    crossinline vbFactory: (View) -> T,
    crossinline viewProvider: (F) -> View = Fragment::requireView,
): ViewBindingProperty<F, T> {
    return viewBinding(viewBinder = { fragment -> vbFactory(viewProvider(fragment)) })
}

/**
 * Create new [ViewBinding] associated with the [Fragment]
 *
 * @param vbFactory Function that creates a new instance of [ViewBinding]. `MyViewBinding::bind` can be used
 * @param viewBindingRootId Root view's id that will be used as a root for the view binding
 */
@Suppress("UNCHECKED_CAST")
@JvmName("viewBindingFragmentWithCallbacks")
public inline fun <F : Fragment, T : ViewBinding> Fragment.viewBinding(
    crossinline vbFactory: (View) -> T,
    @IdRes viewBindingRootId: Int,
): ViewBindingProperty<F, T> {
    return when (this) {
        is DialogFragment -> {
            viewBinding<DialogFragment, T>(
                vbFactory,
                viewProvider = { fragment -> fragment.getRootView(viewBindingRootId) }
            ) as ViewBindingProperty<F, T>
        }

        else -> {
            viewBinding(vbFactory, { fragment: F ->
                fragment.requireView().requireViewByIdCompat(viewBindingRootId)
            })
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <F : Fragment, T : ViewBinding> fragmentViewBinding(
    viewBinder: (F) -> T,
    viewNeedsInitialization: Boolean = true,
): ViewBindingProperty<F, T> {
    return FragmentViewBindingProperty(viewNeedsInitialization, viewBinder)
}
