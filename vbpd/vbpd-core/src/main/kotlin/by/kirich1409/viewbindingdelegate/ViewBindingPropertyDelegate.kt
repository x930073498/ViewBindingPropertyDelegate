@file:Suppress("unused")
@file:JvmName("ViewBindingPropertyDelegateUtils")

package by.kirich1409.viewbindingdelegate

import androidx.annotation.MainThread
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.internal.core.checkMainThread

/**
 * Setting for ViewBindingPropertyDelegate library
 */
object ViewBindingPropertyDelegate {

    /**
     * Enable strict checks for how ViewBindingPropertyDelegate is accessed. Throws an [Exception]
     * if a [ViewBinding] is accessed outside the view lifecycle. As an example, if you try
     * accessing a [Fragment]'s one before [Fragment.onViewCreated] has been called or after
     * [Fragment.onDestroyView] was called, you will get a crash.
     *
     * **By default strict mode is enabled**
     */
    @set:MainThread
    var strictMode = true
        set(value) {
            checkMainThread()
            field = value
        }
}

/**
 * Create new [ViewBindingPropertyDelegate] that will be initialized lazy and
 * not associated with any host. You need to manually clear reference with
 * [ViewBindingProperty.clear] or not to keep it at all.
 *
 * @param viewBinder Function that create new instance of [ViewBinding]
 */
fun <R : Any, VB : ViewBinding> viewBindingLazy(
    viewBinder: (R) -> VB,
): LazyViewBindingProperty<R, VB> {
    return LazyViewBindingProperty(viewBinder)
}
