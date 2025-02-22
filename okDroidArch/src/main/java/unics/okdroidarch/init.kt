package unics.okdroidarch

import android.app.Application
import unics.okdroidarch.app.DroidExceptionMessageTransformer
import unics.okcore.exceptionMessageTransformer
import unics.okdroid.initOkDroid


fun initOkDroidArch(app: Application) {
    initOkDroid(app)
    exceptionMessageTransformer = DroidExceptionMessageTransformer()
}