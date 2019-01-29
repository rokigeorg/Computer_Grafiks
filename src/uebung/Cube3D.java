package uebung;

import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;

import java.nio.*;
import javax.swing.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.*;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;

public class Cube3D extends JFrame implements GLEventListener {
	private int rendering_program;
	private int vao[] = new int[1];
	private int vbo[] = new int[1];
	private Matrix3D pMat;
	private GLSLUtils util = new GLSLUtils();
	private float cameraX, cameraY, cameraZ;
	private float cubeLocX, cubeLocY, cubeLocZ;

	private GLCanvas myCanvas;

	public Cube3D() {
		setTitle("Übung 2 - Rotation one Point ");
		setSize(400, 400);
		setLocation(200, 200);

		// this is new (to ensure compatibility with other OS)
		GLProfile prof = GLProfile.get(GLProfile.GL4);
		GLCapabilities capable = new GLCapabilities(prof);
		// note myCanvas now has the capabilities of GL4
		myCanvas = new GLCanvas(capable);

		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.add(myCanvas);
		setVisible(true);
	}

	/**
	 * INIT FUNC
	 */
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		setupVertices();
		
		cameraX = 0.0f;
		cameraY = 0.0f;
		cameraZ = 8.0f;

		cubeLocX = 0.0f;
		cubeLocY = -2.0f;
		cubeLocZ = 0.0f;
		
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat = perspective(60.0f, aspect, 0.1f, 100.0f);
	}

	/**
	 * SETUP
	 */
	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		float[] point_positions = { 0.9f, -0.9f, 0.0f };

		// done automatically, but needed in earlier versions
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(vbo.length, vbo, 0); // generate

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // set vbo item i aktive to load values to it
		FloatBuffer ptsBuf = Buffers.newDirectFloatBuffer(point_positions); // collect data
		gl.glBufferData(GL_ARRAY_BUFFER, ptsBuf.limit() * 4, ptsBuf, GL_STATIC_DRAW); // add data to buffer

	}

	// init finishes then display is called
	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		/*
		// bkg is automatically black, but we can change it below
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		*/
		gl.glUseProgram(rendering_program); // loads program from init with the one shaders

		//build view matrix
		Matrix3D vMat = new Matrix3D();
		vMat.translate(-cameraX, -cameraY, -cameraZ);
		//build model matrix
		Matrix3D mMat = new Matrix3D();
		mMat.translate(cubeLocX, cubeLocY, cubeLocZ);
		
		//concatinate both matricies
		Matrix3D mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);
		
		//copy perspective and MV matricies to corressponding uniform variables
		int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);			
		gl.glUniformMatrix4fv(mv_loc, 1, false,mvMat.getFloatValues(),0);
		
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, 1);			//TODO

	}

	private int createShaderProgram() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// we will now read from files (see function readShaderSource below)
		String vshaderSource[] = util.readShaderSource("mVertex.shader");
		String fshaderSource[] = util.readShaderSource("mFragment.shader");

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

	private Matrix3D perspective(float fovy, float aspect, float n, float f) {
		float q = 0.1f / ((float)Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B =(n+f)/(n-f);
		float C = (2.0f * n * f) / (n-f);
		Matrix3D r =new Matrix3D();
		r.setElementAt(0, 0, A);
		r.setElementAt(1, 1, q);
		r.setElementAt(2, 2, B);
		r.setElementAt(3, 2, -1.0f);
		r.setElementAt(2, 3, C);
		r.setElementAt(3, 3, 0.0f);
		return r;
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

	}

	public void dispose(GLAutoDrawable drawable) {

	}
	
	

	public static void main(String[] args) {
		new PointRotation();
	}
}