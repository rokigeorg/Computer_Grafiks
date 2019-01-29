package week4;

//WORKS
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



public class TriangleThreeColors extends JFrame implements GLEventListener{
private int rendering_program;
private int vao[] = new int[1];
private int vbo[] = new int[3];

private GLSLUtils util = new GLSLUtils();

private GLCanvas myCanvas;

public TriangleThreeColors(){
    setTitle("Week 2 - draw 2 points different colors");
    setSize(600, 400);
    setLocation(200, 200);

    // this is new (to ensure compatibility with other OS)
    GLProfile prof = GLProfile.get(GLProfile.GL4);
    GLCapabilities capable = new GLCapabilities(prof);
    // note myCanvas now has the capabilities of GL4
    myCanvas = new GLCanvas(capable);
    
    
    myCanvas.addGLEventListener(this);
    this.add(myCanvas);
    setVisible(true);
}

// first init is called
public void init(GLAutoDrawable drawable){
    GL4 gl = (GL4) GLContext.getCurrentGL();
    rendering_program = createShaderProgram();
    setupVertices();
}

// init finishes then display is called
public void display(GLAutoDrawable drawable){
    GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glPointSize(30.0f);
    
    gl.glClear(GL_DEPTH_BUFFER_BIT);
    
    // not needed if bkg is black
    float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
    FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
    gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

    
    gl.glUseProgram(rendering_program); // loads program from init with the two shaders
    // here we would normally prepare vertices for a 3d model
    
    //be sure to bind the vbo before assigning the attribute pointer
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); 
    // now set the "location = 0" info in the shaders to the point info from vbo[0]
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
    
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(1);
    
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDrawArrays(GL_TRIANGLES, 0, 3);

}

private void setupVertices()
{   GL4 gl = (GL4) GLContext.getCurrentGL();
    
    float[] point_positions = {
       -0.5f,  0.0f, 0.0f, 
        0.5f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f
    };
    
    float[] point_colors = {
            1.0f,  0.0f, 0.0f, 
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f 
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
    String vshaderSource[] = util.readShaderSource("vertx2.shader");
    String fshaderSource[] = util.readShaderSource("fragment2.shader");

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


public static void main(String[ ] args){
    new TriangleThreeColors();
}

}
