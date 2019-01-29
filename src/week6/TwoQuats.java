package week6;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;

import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;

public class TwoQuats extends JFrame implements GLEventListener {
	private int rendering_program;
	private int vao[] = new int[2];
	private int vbo[] = new int[6];

	private GLSLUtils util = new GLSLUtils();

	private GLCanvas myCanvas;

	private float cameraX, cameraY, cameraZ; // position of the camera

	public TwoQuats() {
		setTitle("Week 5 - perspect of two lines");
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
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();

		// setup the camera position
		cameraX = 0.0f;
		cameraY = 0.0f;
		cameraZ = 2.0f;

		setupVertices();

	}

	// init finishes then display is called
	// init finishes then display is called
	public void display(GLAutoDrawable drawable) {
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
		Matrix3D pMat = perspectiveMethod(60.0f, aspect, 0.1f, 1000.0f);

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

		// be sure to bind the vbo before assigning the attribute pointer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		// now set the "location = 0" info in the shaders to the point info from vbo[0]
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// each object was described by 6 indices
		int numIndices = 6;
		// draw floor
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[2]);
		gl.glDrawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_INT, 0);

		// same for floor2: position in vbo[3], color in vbo[4]
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		// draw floor 2
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[5]);
		gl.glDrawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_INT, 0);

	}

	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		float[] floor_verts = { 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f };

		float[] floor_col = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.1f, 0.0f, 0.0f, 0.1f };

		int[] floor_indices = { 0, 1, 2, 0, 2, 3 };

		float[] floor2_verts = { 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f };

		float[] floor2_col = { 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f };

		int[] floor2_indices = { 0, 1, 2, 0, 2, 3 };

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(vbo.length, vbo, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer floorPtsBuf = Buffers.newDirectFloatBuffer(floor_verts);
		gl.glBufferData(GL_ARRAY_BUFFER, floorPtsBuf.limit() * 4, floorPtsBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer floorColorBuf = Buffers.newDirectFloatBuffer(floor_col);
		gl.glBufferData(GL_ARRAY_BUFFER, floorColorBuf.limit() * 4, floorColorBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[2]);
		IntBuffer floorIndBuf = Buffers.newDirectIntBuffer(floor_indices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, floorIndBuf.limit() * 4, floorIndBuf, GL_STATIC_DRAW);

		// since there is another object, the next line will
		// prevent further modification of the VAO:
		gl.glBindVertexArray(0);

		// NOTE: this is bad practice (copy and paste) it would be better to define a
		// method which initializes each object automatically (later?)

		gl.glBindVertexArray(vao[1]);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer floor2PtsBuf = Buffers.newDirectFloatBuffer(floor2_verts);
		gl.glBufferData(GL_ARRAY_BUFFER, floor2PtsBuf.limit() * 4, floor2PtsBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer floor2ColorBuf = Buffers.newDirectFloatBuffer(floor2_col);
		gl.glBufferData(GL_ARRAY_BUFFER, floor2ColorBuf.limit() * 4, floor2ColorBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[5]);
		IntBuffer floor2IndBuf = Buffers.newDirectIntBuffer(floor2_indices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, floor2IndBuf.limit() * 4, floor2IndBuf, GL_STATIC_DRAW);

	}

	private int createShaderProgram() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

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

	/**
	 * @param fovy: angle (from x-z plane; i.e. in y-direction) of the field of view
	 * @param aspect: ratio of width to height of viewing box
	 * @param n: z coordinate of near cutoff plane (can't see anything in front of
	 *        near plane)
	 * @param f: z coordinate of far cutoff plane (can't see anything beyond)
	 * 
	 * @return the perspective projection matrix
	 */
	private Matrix3D perspectiveMethod(float fovy, float aspect, float n, float f) {
		float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0, 0, A);
		r.setElementAt(1, 1, q);
		r.setElementAt(2, 2, B);
		r.setElementAt(3, 2, -1.0f);
		r.setElementAt(2, 3, C);
		r.setElementAt(3, 3, 0.0f);
		return r;
	}

	/**
	 * @param R: x coordinate of right boundary
	 * @param L: x coordinate of left boundary
	 * @param T: y coordinate of top
	 * @param B: y coordinate of bottom
	 * @param n: z coordinate of near cutoff plane (can't see anything in front of
	 *        near plane)
	 * @param f: z coordinate of far cutoff plane (can't see anything beyond)
	 * 
	 * @return the orthographic projection matrix
	 */
	private Matrix3D orthographicMethod(float R, float L, float T, float B, float n, float f) {
		Matrix3D r = new Matrix3D();

		r.setElementAt(0, 0, 2 / (R - L));
		r.setElementAt(1, 1, 2 / (T - B));
		r.setElementAt(2, 2, 1 / (f - n));
		r.setElementAt(0, 3, -(R + L) / (R - L));
		r.setElementAt(1, 3, -(T + B) / (T - B));
		r.setElementAt(2, 3, -n / (f - n));
		r.setElementAt(3, 3, 1.0f);
		return r;
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

	}

	public void dispose(GLAutoDrawable drawable) {

	}

	public static void main(String[] args) {
		new TwoQuats();
	}

}
