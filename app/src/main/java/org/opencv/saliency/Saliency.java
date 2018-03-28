//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.saliency;

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;

// C++: class Saliency
//javadoc: Saliency

public class Saliency extends Algorithm {

    protected Saliency(long addr) { super(addr); }

    // internal usage only
    public static Saliency __fromPtr__(long addr) { return new Saliency(addr); }

    //
    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    //

    //javadoc: Saliency::computeSaliency(image, saliencyMap)
    public  boolean computeSaliency(Mat image, Mat saliencyMap)
    {
        
        boolean retVal = computeSaliency_0(nativeObj, image.nativeObj, saliencyMap.nativeObj);
        
        return retVal;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    private static native boolean computeSaliency_0(long nativeObj, long image_nativeObj, long saliencyMap_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
