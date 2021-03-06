package week5;



import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;



public class TwoLinesDifferentProjectionsDemo extends JFrame implements GLEventListener{
    private int rendering_program;
    private int vao[] = new int[1];
    private int vbo[] = new int[2];
    
    private GLSLUtils util = new GLSLUtils();
    
    private GLCanvas myCanvas;
    private float cameraX, cameraY, cameraZ;
    
    public TwoLinesDifferentProjectionsDemo(){
        setTitle("Week 5 - draw 2 lines for distance viewing");
        setSize(600, 400);
        setLocation(200, 200);

        // this is new (to ensure compatibility with other OS)
        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities capable = new GLCapabilities(prof);
        // note myCanvas now has the capabilities of GL4
        myCanvas = new GLCanvas(capable);
        
        myCanvas.addGLEventListener(this);
        this.getContentPane().add(myCanvas);
        setVisible(true);
    }
    
    
    private Matrix3D perspective(float fovy, float aspect, float n, float f)
    {   float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
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
    
    private Matrix3D orthographic(float R, float L, float T, float B, float n, float f){
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
    
    // init finishes then display is called
    public void display(GLAutoDrawable drawable){
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glLineWidth(10.0f);
        
        gl.glClear(GL_DEPTH_BUFFER_BIT);
        
        // not needed if bkg is black
        float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
        FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
        gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

        
        gl.glUseProgram(rendering_program); // loads program from init with the two shaders

        // to be used later to put in uniform variables
        // see uniform variables in fragment shader
        int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
        int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
        
        // creates perspective matrix, with fovy=60
        // aspect corresponds to screen dimensions
        // alternatively, we could put the perspective matrix in init()
        float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
        Matrix3D pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
        // if we want to use the orthographic projection:
        Matrix3D oMat = orthographic(1.0f, -1.0f, 1.0f, -1.0f, 0.1f, 1000.0f);
        
        
        // build the view matrix
        Matrix3D vMat = new Matrix3D();
        vMat.translate(-cameraX, -cameraY, -cameraZ);
        
        // build the model matrix
        Matrix3D mMat = new Matrix3D();
        // here we dont do anything to the lines
        
        // put together to build MV matrix
        Matrix3D mvMat = new Matrix3D();
        mvMat.concatenate(vMat);
        mvMat.concatenate(mMat);


        // copies matrices into uniform variables
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        
        // comment out proj_loc above and uncomment below to demonstrate orthographic projection
        //gl.glUniformMatrix4fv(proj_loc, 1, false, oMat.getFloatValues(), 0);
        
        //be sure to bind the vbo before assigning the attribute pointer
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); 
        // now set the "location = 0" info in the shaders to the point info from vbo[0]
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(1);
        
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDrawArrays(GL_LINES, 0, 4);

    }
    
    public static void main(String[ ] args){
        new TwoLinesDifferentProjectionsDemo();
    }
    
    // first init is called
    public void init(GLAutoDrawable drawable){
        GL4 gl = (GL4) GLContext.getCurrentGL();
        rendering_program = createShaderProgram();
        cameraX = 0.0f; cameraY = 0.0f; cameraZ = 20.0f;
        setupVertices();
        
    }
    
    
    
    private void setupVertices()
    {   GL4 gl = (GL4) GLContext.getCurrentGL();
        
        float[] point_positions = {
           -0.5f,  -0.5f, 18.0f, 
           -0.5f,  -0.5f, -18.0f,
           0.5f,  -0.5f, 18.0f, 
           0.5f,  -0.5f, -18.0f
        };
        
        float[] point_colors = {
                1.0f,  0.0f, 0.0f, 
                0.0f, 0.0f, 1.0f,
                0.0f,  1.0f, 0.0f, 
                1.0f, 0.0f, 1.0f
             };
        

        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);
        
        gl.glGenBuffers(vbo.length, vbo, 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer ptsBuf = Buffers.newDirectFloatBuffer(point_positions);
        gl.glBufferData(GL_ARRAY_BUFFER, ptsBuf.limit()*4, ptsBuf, GL_STATIC_DRAW);     
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        FloatBuffer pcolorBuf = Buffers.newDirectFloatBuffer(point_colors);
        gl.glBufferData(GL_ARRAY_BUFFER, pcolorBuf.limit()*4, pcolorBuf, GL_STATIC_DRAW);   
        


    }
    
    private int createShaderProgram()
    {   GL4 gl = (GL4) GLContext.getCurrentGL();

        // we will now read from files (see function readShaderSource below)
        String vshaderSource[] = util.readShaderSource("vert5_1.shader");
        String fshaderSource[] = util.readShaderSource("frag5_1.shader");

        int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
        int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

        gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
        gl.glCompileShader(vShader);

        gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
        gl.glCompileShader(fShader);

        int vfprogram = gl.glCreateProgram();
        gl.glAttachShader(vfprogram, vShader);
        gl.glAttachShader(vfprogram, fShader);
        gl.glLinkProgram(vfprogram);

        gl.glDeleteShader(vShader);
        gl.glDeleteShader(fShader);
        return vfprogram;
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int
    height) {
        
    }
    
    public void dispose(GLAutoDrawable drawable){
        
    }
    
}