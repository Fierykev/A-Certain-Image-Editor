//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.saliency;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.saliency.Objectness;
import org.opencv.saliency.ObjectnessBING;
import org.opencv.utils.Converters;

// C++: class ObjectnessBING
//javadoc: ObjectnessBING

public class ObjectnessBING extends Objectness {

    protected ObjectnessBING(long addr) { super(addr); }

    // internal usage only
    public static ObjectnessBING __fromPtr__(long addr) { return new ObjectnessBING(addr); }

    //
    // C++: static Ptr_ObjectnessBING create()
    //

    //javadoc: ObjectnessBING::create()
    public static ObjectnessBING create()
    {
        
        ObjectnessBING retVal = ObjectnessBING.__fromPtr__(create_0());
        
        return retVal;
    }


    //
    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    //

    //javadoc: ObjectnessBING::computeSaliency(image, saliencyMap)
    public  boolean computeSaliency(Mat image, Mat saliencyMap)
    {
        
        boolean retVal = computeSaliency_0(nativeObj, image.nativeObj, saliencyMap.nativeObj);
        
        return retVal;
    }


    //
    // C++:  double getBase()
    //

    //javadoc: ObjectnessBING::getBase()
    public  double getBase()
    {
        
        double retVal = getBase_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  int getNSS()
    //

    //javadoc: ObjectnessBING::getNSS()
    public  int getNSS()
    {
        
        int retVal = getNSS_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  int getW()
    //

    //javadoc: ObjectnessBING::getW()
    public  int getW()
    {
        
        int retVal = getW_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  vector_float getobjectnessValues()
    //

    //javadoc: ObjectnessBING::getobjectnessValues()
    public  MatOfFloat getobjectnessValues()
    {
        
        MatOfFloat retVal = MatOfFloat.fromNativeAddr(getobjectnessValues_0(nativeObj));
        
        return retVal;
    }


    //
    // C++:  void read()
    //

    //javadoc: ObjectnessBING::read()
    public  void read()
    {
        
        read_0(nativeObj);
        
        return;
    }


    //
    // C++:  void setBBResDir(String resultsDir)
    //

    //javadoc: ObjectnessBING::setBBResDir(resultsDir)
    public  void setBBResDir(String resultsDir)
    {
        
        setBBResDir_0(nativeObj, resultsDir);
        
        return;
    }


    //
    // C++:  void setBase(double val)
    //

    //javadoc: ObjectnessBING::setBase(val)
    public  void setBase(double val)
    {
        
        setBase_0(nativeObj, val);
        
        return;
    }


    //
    // C++:  void setNSS(int val)
    //

    //javadoc: ObjectnessBING::setNSS(val)
    public  void setNSS(int val)
    {
        
        setNSS_0(nativeObj, val);
        
        return;
    }


    //
    // C++:  void setTrainingPath(String trainingPath)
    //

    //javadoc: ObjectnessBING::setTrainingPath(trainingPath)
    public  void setTrainingPath(String trainingPath)
    {
        
        setTrainingPath_0(nativeObj, trainingPath);
        
        return;
    }


    //
    // C++:  void setW(int val)
    //

    //javadoc: ObjectnessBING::setW(val)
    public  void setW(int val)
    {
        
        setW_0(nativeObj, val);
        
        return;
    }


    //
    // C++:  void write()
    //

    //javadoc: ObjectnessBING::write()
    public  void write()
    {
        
        write_0(nativeObj);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++: static Ptr_ObjectnessBING create()
    private static native long create_0();

    // C++:  bool computeSaliency(Mat image, Mat& saliencyMap)
    private static native boolean computeSaliency_0(long nativeObj, long image_nativeObj, long saliencyMap_nativeObj);

    // C++:  double getBase()
    private static native double getBase_0(long nativeObj);

    // C++:  int getNSS()
    private static native int getNSS_0(long nativeObj);

    // C++:  int getW()
    private static native int getW_0(long nativeObj);

    // C++:  vector_float getobjectnessValues()
    private static native long getobjectnessValues_0(long nativeObj);

    // C++:  void read()
    private static native void read_0(long nativeObj);

    // C++:  void setBBResDir(String resultsDir)
    private static native void setBBResDir_0(long nativeObj, String resultsDir);

    // C++:  void setBase(double val)
    private static native void setBase_0(long nativeObj, double val);

    // C++:  void setNSS(int val)
    private static native void setNSS_0(long nativeObj, int val);

    // C++:  void setTrainingPath(String trainingPath)
    private static native void setTrainingPath_0(long nativeObj, String trainingPath);

    // C++:  void setW(int val)
    private static native void setW_0(long nativeObj, int val);

    // C++:  void write()
    private static native void write_0(long nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
