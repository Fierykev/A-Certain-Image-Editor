//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.saliency;

import org.opencv.core.Mat;
import org.opencv.saliency.StaticSaliency;
import org.opencv.saliency.StaticSaliencySpectralResidual;

// C++: class StaticSaliencySpectralResidual
//javadoc: StaticSaliencySpectralResidual

public class StaticSaliencySpectralResidual extends StaticSaliency {

    protected StaticSaliencySpectralResidual(long addr) { super(addr); }

    // internal usage only
    public static StaticSaliencySpectralResidual __fromPtr__(long addr) { return new StaticSaliencySpectralResidual(addr); }

    //
    // C++: static Ptr_StaticSaliencySpectralResidual create()
    //

    //javadoc: StaticSaliencySpectralResidual::create()
    public static StaticSaliencySpectralResidual create()
    {
        
        StaticSaliencySpectralResidual retVal = StaticSaliencySpectralResidual.__fromPtr__(create_0());
        
        return retVal;
    }


    //
    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    //

    //javadoc: StaticSaliencySpectralResidual::computeSaliency(image, saliencyMap)
    public  boolean computeSaliency(Mat image, Mat saliencyMap)
    {
        
        boolean retVal = computeSaliency_0(nativeObj, image.nativeObj, saliencyMap.nativeObj);
        
        return retVal;
    }


    //
    // C++:  int getImageHeight()
    //

    //javadoc: StaticSaliencySpectralResidual::getImageHeight()
    public  int getImageHeight()
    {
        
        int retVal = getImageHeight_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  int getImageWidth()
    //

    //javadoc: StaticSaliencySpectralResidual::getImageWidth()
    public  int getImageWidth()
    {
        
        int retVal = getImageWidth_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  void read(FileNode fn)
    //

    // Unknown type 'FileNode' (I), skipping the function


    //
    // C++:  void setImageHeight(int val)
    //

    //javadoc: StaticSaliencySpectralResidual::setImageHeight(val)
    public  void setImageHeight(int val)
    {
        
        setImageHeight_0(nativeObj, val);
        
        return;
    }


    //
    // C++:  void setImageWidth(int val)
    //

    //javadoc: StaticSaliencySpectralResidual::setImageWidth(val)
    public  void setImageWidth(int val)
    {
        
        setImageWidth_0(nativeObj, val);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++: static Ptr_StaticSaliencySpectralResidual create()
    private static native long create_0();

    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    private static native boolean computeSaliency_0(long nativeObj, long image_nativeObj, long saliencyMap_nativeObj);

    // C++:  int getImageHeight()
    private static native int getImageHeight_0(long nativeObj);

    // C++:  int getImageWidth()
    private static native int getImageWidth_0(long nativeObj);

    // C++:  void setImageHeight(int val)
    private static native void setImageHeight_0(long nativeObj, int val);

    // C++:  void setImageWidth(int val)
    private static native void setImageWidth_0(long nativeObj, int val);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
