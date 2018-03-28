//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.saliency;

import org.opencv.core.Mat;
import org.opencv.saliency.StaticSaliency;
import org.opencv.saliency.StaticSaliencyFineGrained;

// C++: class StaticSaliencyFineGrained
//javadoc: StaticSaliencyFineGrained

public class StaticSaliencyFineGrained extends StaticSaliency {

    protected StaticSaliencyFineGrained(long addr) { super(addr); }

    // internal usage only
    public static StaticSaliencyFineGrained __fromPtr__(long addr) { return new StaticSaliencyFineGrained(addr); }

    //
    // C++: static Ptr_StaticSaliencyFineGrained create()
    //

    //javadoc: StaticSaliencyFineGrained::create()
    public static StaticSaliencyFineGrained create()
    {
        
        StaticSaliencyFineGrained retVal = StaticSaliencyFineGrained.__fromPtr__(create_0());
        
        return retVal;
    }


    //
    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    //

    //javadoc: StaticSaliencyFineGrained::computeSaliency(image, saliencyMap)
    public  boolean computeSaliency(Mat image, Mat saliencyMap)
    {
        
        boolean retVal = computeSaliency_0(nativeObj, image.nativeObj, saliencyMap.nativeObj);
        
        return retVal;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++: static Ptr_StaticSaliencyFineGrained create()
    private static native long create_0();

    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    private static native boolean computeSaliency_0(long nativeObj, long image_nativeObj, long saliencyMap_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
