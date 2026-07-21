package core.extensions

import core.utils.UpdateProperty
import io.konform.validation.ValidationBuilder

fun <T : Any> ValidationBuilder<UpdateProperty<T>>.requiredUpdate(
    init: ValidationBuilder<T>.() -> Unit
) {
    constrain("must not be null when updated") { !it.update || it.value != null }

    // Konform uses the property reference to build the validation tree
    UpdateProperty<T>::valueIfUpdated ifPresent {
        init()
    }
}

fun <T : Any> ValidationBuilder<UpdateProperty<T>>.optionalUpdate(
    init: ValidationBuilder<T>.() -> Unit
) {
    UpdateProperty<T>::valueIfUpdated ifPresent {
        init()
    }
}

// 2. NEW extension (handles UpdateProperty<String?>)
@JvmName("optionalUpdateNullable")
fun <T : Any> ValidationBuilder<UpdateProperty<T?>>.optionalUpdate(
    init: ValidationBuilder<T>.() -> Unit
) {
    // valueIfUpdated naturally returns T? here, and ifPresent unwraps it to T
    UpdateProperty<T?>::valueIfUpdated ifPresent { init() }
}