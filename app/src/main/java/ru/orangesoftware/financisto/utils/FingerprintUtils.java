package ru.orangesoftware.financisto.utils;

import android.content.Context;

// TODO: Commented out due to dependency resolution issues
// import com.mtramin.rxfingerprint.RxFingerprint;

import ru.orangesoftware.financisto.R;

public class FingerprintUtils {

    // TODO: Stubbed due to missing rxfingerprint dependency
    public static boolean fingerprintUnavailable(Context context) {
        // return RxFingerprint.isUnavailable(context);
        return true; // Stub: fingerprint unavailable when library is missing
    }

    // TODO: Stubbed due to missing rxfingerprint dependency
    public static String reasonWhyFingerprintUnavailable(Context context) {
        // if (!RxFingerprint.isHardwareDetected(context)) {
        //     return context.getString(R.string.fingerprint_unavailable_hardware);
        // } else if (!RxFingerprint.hasEnrolledFingerprints(context)) {
        //     return context.getString(R.string.fingerprint_unavailable_enrolled_fingerprints);
        // } else {
            return context.getString(R.string.fingerprint_unavailable_unknown);
        // }
    }

}
