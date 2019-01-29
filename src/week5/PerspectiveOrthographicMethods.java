package week5;

import java.nio.*;

import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;

import static java.lang.System.out; 



/**
 * this class is just a collection of the three viewing transformations
 * see Section 3.8 in Gordon and Clevenger book
 * Spillner's slides also have the orthographic projection matrix
 * 
 * orthographic is used mainly for engineering presentations 
 * prespective is used in 3D drawing to get depth sensation 
 *      example: two lines seem to meet in the distance at infinity
 * 
 * 
 * we will be using the perspective projection matrix
 */

public class PerspectiveOrthographicMethods{

    
    /**
     * @param fovy: angle (from x-z plane; i.e. in y-direction) of the field of view
     * @param aspect: ratio of width to height of viewing box
     * @param n: z coordinate of near cutoff plane (can't see anything in front of near plane)
     * @param f: z coordinate of far cutoff plane (can't see anything beyond)
     * 
     * @return the perspective projection matrix
     */
    private Matrix3D perspectiveMethod(float fovy, float aspect, float n, float f){
        float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
        float A = q / aspect;
        float B = (n + f) / (n - f);
        float C = (2.0f * n * f) / (n - f);
        Matrix3D r = new Matrix3D();
        r.setElementAt(0,0,A);
        r.setElementAt(1,1,q);
        r.setElementAt(2,2,B);
        r.setElementAt(3,2,-1.0f);
        r.setElementAt(2,3,C);
        r.setElementAt(3,3,0.0f);
        return r;
    }
    
    
    /**
     * @param R: x coordinate of right boundary
     * @param L: x coordinate of left boundary
     * @param T: y coordinate of top 
     * @param B: y coordinate of bottom
     * @param n: z coordinate of near cutoff plane (can't see anything in front of near plane)
     * @param f: z coordinate of far cutoff plane (can't see anything beyond)
     * 
     * @return the orthographic projection matrix
     */
    private Matrix3D orthographicMethod(float R, float L, float T, float B, float n, float f){
        Matrix3D r = new Matrix3D();
        
        r.setElementAt(0,0,2/(R-L));
        r.setElementAt(1,1,2/(T-B));
        r.setElementAt(2,2,1/(f-n));
        r.setElementAt(0,3,-(R+L)/(R-L));
        r.setElementAt(1,3,-(T+B)/(T-B));
        r.setElementAt(2,3,-n/(f-n));
        r.setElementAt(3,3,1.0f);
        return r;
    }
    
    
}
