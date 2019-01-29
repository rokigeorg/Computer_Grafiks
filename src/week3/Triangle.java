package week3;

import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL4.*;

import java.nio.FloatBuffer;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;

import graphicslib3D.*;
import graphicslib3D.GLSLUtils.*;

public class Triangle extends JFrame implements GLEventListener
{	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[] = new int[1];
	private int vbo[] = new int[2];
	
	private GLSLUtils util = new GLSLUtils();

	public Triangle()
	{	setTitle("week 2 - triangle");
		setSize(600, 400);
		setLocation(400,400);
		 // this is new (to ensure compatibility with other OS)
        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities capable = new GLCapabilities(prof);
        // note myCanvas now has the capabilities of GL4
        myCanvas = new GLCanvas(capable);
      	myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		setVisible(true);
	}
	
	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		setupVertices();
		
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(rendering_program);
		
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        
		gl.glDrawArrays(GL_TRIANGLES,0,3);
	}
    
    private void setupVertices()
    {   GL4 gl = (GL4) GLContext.getCurrentGL();
        
        float[] point_positions = {
        	-0.9f,  0.0f, 0.0f,
            0.5f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f
        };

        // done automatically, but needed in earlier versions
        gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
        
        gl.glGenBuffers(vbo.length, vbo, 0); // generate

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); 							// bind, gets it ready
        FloatBuffer ptsBuf = Buffers.newDirectFloatBuffer(point_positions); // collect data
        gl.glBufferData(GL_ARRAY_BUFFER, ptsBuf.limit()*4, ptsBuf, GL_STATIC_DRAW); // add data to buffer       

    }
    
	private int createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		String vshaderSource[] = util.readShaderSource("vert.shader");//("tri_vert.shader");
		String fshaderSource[] = util.readShaderSource("frag.shader");
		int lengths[];

		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);

		gl.glCompileShader(vShader);
		gl.glCompileShader(fShader);

		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);
		return vfprogram;
	}

	public static void main(String[] args) { 
	    new Triangle(); 
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	
	public void dispose(GLAutoDrawable drawable) {}
}