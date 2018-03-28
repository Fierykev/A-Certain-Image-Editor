//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.saliency;

import org.opencv.core.Mat;
import org.opencv.saliency.Saliency;

// C++: class StaticSaliency
//javadoc: StaticSaliency

public class StaticSaliency extends Saliency {

    protected StaticSaliency(long addr) { super(addr); }

    // internal usage only
    public static StaticSaliency __fromPtr__(long addr) { return new StaticSaliency(addr); }

    //
    // C++:  bool computeBinaryMap(Mat _saliencyMap, Mat& _binaryMap)
    //

    //javadoc: StaticSaliency::computeBinaryMap(_saliencyMap, _binaryMap)
    public  boolean computeBinaryMap(Mat _saliencyMap, Mat _binaryMap)
    {
        
        boolean retVal = computeBinaryMap_0(nativeObj, _saliencyMap.nativeObj, _binaryMap.nativeObj);
        
        return retVal;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  bool computeBinaryMap(Mat _saliencyMap, Mat& _binaryMap)
    private static native boolean computeBinaryMap_0(long nativeObj, long _saliencyMap_nativeObj, long _binaryMap_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
