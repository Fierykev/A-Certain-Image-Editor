//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.saliency;

import org.opencv.saliency.Saliency;

// C++: class MotionSaliency
//javadoc: MotionSaliency

public class MotionSaliency extends Saliency {

    protected MotionSaliency(long addr) { super(addr); }

    // internal usage only
    public static MotionSaliency __fromPtr__(long addr) { return new MotionSaliency(addr); }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // native support for java finalize()
    private static native void delete(long nativeObj);

}
