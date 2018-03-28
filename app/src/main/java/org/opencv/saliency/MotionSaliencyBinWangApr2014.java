//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.saliency;

import org.opencv.core.Mat;
import org.opencv.saliency.MotionSaliency;
import org.opencv.saliency.MotionSaliencyBinWangApr2014;

// C++: class MotionSaliencyBinWangApr2014
//javadoc: MotionSaliencyBinWangApr2014

public class MotionSaliencyBinWangApr2014 extends MotionSaliency {

    protected MotionSaliencyBinWangApr2014(long addr) { super(addr); }

    // internal usage only
    public static MotionSaliencyBinWangApr2014 __fromPtr__(long addr) { return new MotionSaliencyBinWangApr2014(addr); }

    //
    // C++: static Ptr_MotionSaliencyBinWangApr2014 create()
    //

    //javadoc: MotionSaliencyBinWangApr2014::create()
    public static MotionSaliencyBinWangApr2014 create()
    {
        
        MotionSaliencyBinWangApr2014 retVal = MotionSaliencyBinWangApr2014.__fromPtr__(create_0());
        
        return retVal;
    }


    //
    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    //

    //javadoc: MotionSaliencyBinWangApr2014::computeSaliency(image, saliencyMap)
    public  boolean computeSaliency(Mat image, Mat saliencyMap)
    {
        
        boolean retVal = computeSaliency_0(nativeObj, image.nativeObj, saliencyMap.nativeObj);
        
        return retVal;
    }


    //
    // C++:  bool init()
    //

    //javadoc: MotionSaliencyBinWangApr2014::init()
    public  boolean init()
    {
        
        boolean retVal = init_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  int getImageHeight()
    //

    //javadoc: MotionSaliencyBinWangApr2014::getImageHeight()
    public  int getImageHeight()
    {
        
        int retVal = getImageHeight_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  int getImageWidth()
    //

    //javadoc: MotionSaliencyBinWangApr2014::getImageWidth()
    public  int getImageWidth()
    {
        
        int retVal = getImageWidth_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  void setImageHeight(int val)
    //

    //javadoc: MotionSaliencyBinWangApr2014::setImageHeight(val)
    public  void setImageHeight(int val)
    {
        
        setImageHeight_0(nativeObj, val);
        
        return;
    }


    //
    // C++:  void setImageWidth(int val)
    //

    //javadoc: MotionSaliencyBinWangApr2014::setImageWidth(val)
    public  void setImageWidth(int val)
    {
        
        setImageWidth_0(nativeObj, val);
        
        return;
    }


    //
    // C++:  void setImagesize(int W, int H)
    //

    //javadoc: MotionSaliencyBinWangApr2014::setImagesize(W, H)
    public  void setImagesize(int W, int H)
    {
        
        setImagesize_0(nativeObj, W, H);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++: static Ptr_MotionSaliencyBinWangApr2014 create()
    private static native long create_0();

    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    private static native boolean computeSaliency_0(long nativeObj, long image_nativeObj, long saliencyMap_nativeObj);

    // C++:  bool init()
    private static native boolean init_0(long nativeObj);

    // C++:  int getImageHeight()
    private static native int getImageHeight_0(long nativeObj);

    // C++:  int getImageWidth()
    private static native int getImageWidth_0(long nativeObj);

    // C++:  void setImageHeight(int val)
    private static native void setImageHeight_0(long nativeObj, int val);

    // C++:  void setImageWidth(int val)
    private static native void setImageWidth_0(long nativeObj, int val);

    // C++:  void setImagesize(int W, int H)
    private static native void setImagesize_0(long nativeObj, int W, int H);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
