

package uebung;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

import java.nio.FloatBuffer;

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
import week5.TwoPointsRotate;

public class ThreePointsSeedUpRotation extends JFrame implements GLEventListener  {

	private int rendering_program;
	private int vao[] = new int[1];
	private int vbo[] = new int[2];

	private GLSLUtils util = new GLSLUtils();

	private GLCanvas myCanvas;

	private float cameraX, cameraY, cameraZ;
	private double rotationSpeed;

	public ThreePointsSeedUpRotation() {
		setTitle("HW 4 - draw 3 points in diff colors and rotate them in diff speed ");
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

		// create an animator
		// frames per second 50
		FPSAnimator animator = new FPSAnimator(myCanvas, 50);
		animator.start();
	}

	
	// first init is called
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		cameraX = 0.0f;
		cameraY = 0.0f;
		cameraZ = 10.0f;
		rotationSpeed = 10000.0;
		setupVertices();

	}
	
	
	// init finishes then display is called
	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glPointSize(30.0f);

		// clear the buffer / clear screen from last drawn frame picels
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		// not needed if bkg is black
		float bkg[] = { 1.0f, 1.0f, 1.0f, 1.0f };
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

		// build the view matrix
		Matrix3D vMat = new Matrix3D();
		vMat.translate(-cameraX, -cameraY, -cameraZ);

		// build the model matrix
		Matrix3D mMat = new Matrix3D();
		double rotateDeg = genRotationDegree();
		mMat.rotateZ(rotateDeg);

		// put together to build MV matrix
		Matrix3D mvMat = new Matrix3D();
		mvMat.concatenate(vMat);
		mvMat.concatenate(mMat);

		// copies matrices into uniform variables
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
	    gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
	    gl.glEnableVertexAttribArray(1);
	    
	    
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_POINTS, 0, 3);

	}

	
	private double genRotationDegree() {
		double rotateDeg = (double) System.currentTimeMillis() / rotationSpeed;
		rotationSpeed = rotationSpeed - 1.0;
		if(rotationSpeed <= 100.0)
			rotationSpeed = 1000.0;	
		return rotateDeg;
	}


	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
	    
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

	private Matrix3D perspective(float fovy, float aspect, float n, float f) {
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

	private int createShaderProgram() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// we will now read from files (see function readShaderSource below)
		String vshaderSource[] = util.readShaderSource("uVert5_2.shader");
		String fshaderSource[] = util.readShaderSource("uFrag5_2.shader");

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

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

	}

	public void dispose(GLAutoDrawable drawable) {

	}
	
	public static void main(String[] args) {
		new ThreePointsSeedUpRotation();
	}


}
