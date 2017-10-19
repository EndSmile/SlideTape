package com.ldy.slidetape.slidetape;

import android.support.annotation.NonNull;

/**
 * Created by ldy on 2017/10/19.
 */

public interface TapeTextCalculator {
    @NonNull
    String calculateLongLineText(int progress);

    @NonNull
    String calculateProgressText(int progress);
}
