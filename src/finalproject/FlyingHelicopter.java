package finalproject;

import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;
import graphicslib3D.MatrixStack;

public class FlyingHelicopter extends JFrame implements GLEventListener {
	private int rendering_program;
	// we have 2 objects (2 floors)
	private int numberObjects = 18;
	private int numberPerObject = 3;
	private int vao[] = new int[numberObjects];
	// for each object we will 3 attributes: position, color, indices
	// so need 3*2 = 6 buffers
	private int vbo[] = new int[numberObjects * numberPerObject];
	private boolean isGoingUp;
	private boolean isGoingUpY;
	private boolean isGoingUpX;


	// for each object we can assign a type from the listed GL Primitives
	enum Types {
		TRIANGLES, POINTS, LINES
	}

	// see
	// https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/constant-values.html#com.jogamp.opengl.GL.GL_TRIANGLES
	// for the field values of GL_TRIANGLES etc.
	private int PrimType[] = new int[numberObjects];
	private int NumIndices[] = new int[numberObjects];

	private GLSLUtils util = new GLSLUtils();

	private GLCanvas myCanvas;
	private float cameraX, cameraY, cameraZ;

	private float b1floorX, b1floorY, b1floorZ;
	private float b1lSideX, b1lSideY, b1lSideZ;
	private float b1rSideX, b1rSideY, b1rSideZ;
	private float b1ceilX, b1ceilY, b1ceilZ;
	private float b1frontX, b1frontY, b1frontZ;
	private float b1backX, b1backY, b1backZ;

	private float b2floorX, b2floorY, b2floorZ;
	private float b2lSideX, b2lSideY, b2lSideZ;
	private float b2rSideX, b2rSideY, b2rSideZ;
	private float b2ceilX, b2ceilY, b2ceilZ;
	private float b2frontX, b2frontY, b2frontZ;
	private float b2backX, b2backY, b2backZ;
	
	private float b3floorX, b3floorY, b3floorZ;
	private float b3lSideX, b3lSideY, b3lSideZ;
	private float b3rSideX, b3rSideY, b3rSideZ;
	private float b3ceilX, b3ceilY, b3ceilZ;
	private float b3frontX, b3frontY, b3frontZ;
	private float b3backX, b3backY, b3backZ;

	private MatrixStack mvStack = new MatrixStack(40);

	public FlyingHelicopter() {
		setTitle("Final Project- draw helicopter flight");
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

	/**
	 * @param objectInt: the object index as used by the VAO (starts at 0...)
	 * @param vertices: the vertices used to draw the object
	 * @param colors: the color of the object
	 * @param indices: the indices of the input vertices in order used to draw
	 * 
	 * @return nothing; sets up the initialization to draw: binding buffers and
	 *         setting up vertexAttributePointers to be used by shaders
	 */
	public void initObject(int objectInt, float[] vertices, float[] colors, int[] indices, Types objectType) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		int offset = objectInt * numberPerObject;

		// bind the VAOs and VBOs
		// these were in the setupVertices earlier
		gl.glBindVertexArray(vao[objectInt]);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[offset]);
		FloatBuffer ptsBuf = Buffers.newDirectFloatBuffer(vertices);
		gl.glBufferData(GL_ARRAY_BUFFER, ptsBuf.limit() * 4, ptsBuf, GL_STATIC_DRAW);

		// put also the vertexAttributePointers here
		// before they were separately in the display method
		// set the "location = 0" info in the shaders to the vertices info from
		// vbo[offset]
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[offset + 1]);
		FloatBuffer colorBuf = Buffers.newDirectFloatBuffer(colors);
		gl.glBufferData(GL_ARRAY_BUFFER, colorBuf.limit() * 4, colorBuf, GL_STATIC_DRAW);
		// set the "location = 1" info in the shaders to the colors info from
		// vbo[offset+1]
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[offset + 2]);
		IntBuffer indBuf = Buffers.newDirectIntBuffer(indices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indBuf.limit() * 4, indBuf, GL_STATIC_DRAW);

		// get values of the objectType
		switch (objectType) {
		case TRIANGLES:
			PrimType[objectInt] = GL_TRIANGLES;
			break; // dont forget this
		case POINTS:
			PrimType[objectInt] = 0;
			break;
		case LINES:
			PrimType[objectInt] = 1;
			break;
		}

		NumIndices[objectInt] = indices.length;

		// not necessary but safe practice:
		// since there is another object, the next line will
		// prevent further modification of the VAO:
		gl.glBindVertexArray(0);
	}

	public void drawObject(int objectInt) {
		int offset = objectInt * numberPerObject;

		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glBindVertexArray(vao[objectInt]); // need this because we disabled it at end of initObject method
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[offset + 2]);
		gl.glDrawElements(PrimType[objectInt], NumIndices[objectInt], GL_UNSIGNED_INT, 0);
	}

	// init finishes then display is called
	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

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

		
		cameraZ = genCameraZZoom(cameraZ);
		cameraY = genCameraYZoom(cameraY);

		// here we will build the stack of the vMat and mMat
		// TODO 01
		mvStack.pushMatrix();
		mvStack.translate(-cameraX, -cameraY, -cameraZ); // this is the vMat

		// copies matrices into uniform variables
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);

		// is now handled in mvStack (see TODO 01)
		// build the view matrix
		// Matrix3D vMat = new Matrix3D();
		// vMat.translate(-cameraX, -cameraY, -cameraZ);
		


		this.drawBox1(gl, pMat,mv_loc, proj_loc);
		this.drawBox2(gl, pMat,mv_loc, proj_loc);
		this.drawBox3(gl, pMat, mv_loc, proj_loc);

		
		//mvStack.popMatrix(); // pop floor mMat from stack
		mvStack.popMatrix(); // pop vMat from stack
	}
	
	private void drawBox2(GL4 gl,Matrix3D pMat, int mv_loc, int proj_loc) {
		double rotateDeg = (double) System.currentTimeMillis() / 0.5;

		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b2ceilX, b2ceilY, b2ceilZ);
		
		// get openGL ready to draw model now
		gl.glEnable(GL_DEPTH_TEST);
		// draw floor
		drawObject(0);

		// pop model matrix for first object from stack //ceil
		mvStack.popMatrix();

		// now create second model matrix on the stack //floor
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b2floorX, b2floorY, b2floorZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0); //

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw floor 2
		drawObject(1);

		// pop model matrix for first object from stack //side left
		mvStack.popMatrix();
		//mvStack.popMatrix();

		// now create second model matrix on the stack //side left
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b2lSideX, b2lSideY, b2lSideZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw left side
		drawObject(2);

		// pop model matrix for first object from stack //side right
		mvStack.popMatrix();

		// now create second model matrix on the stack //side right
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b2rSideX, b2rSideY, b2rSideZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw left side
		drawObject(3);

		// pop model matrix for first object from stack //front
		mvStack.popMatrix();

		// now create second model matrix on the stack //front
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b2frontX, b2frontY, b2frontZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw front side
		drawObject(4);

		// pop model matrix for first object from stack //back
		mvStack.popMatrix();

		// now create second model matrix on the stack //back
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b2backX, b2backY, b2backZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw back side
		drawObject(5);
		mvStack.popMatrix();
	}

	private void drawBox1(GL4 gl,Matrix3D pMat, int mv_loc, int proj_loc) {
		double rotateDeg = (double) System.currentTimeMillis() / 50.0;

		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b1ceilX, b1ceilY, b1ceilZ);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// get openGL ready to draw model now
		gl.glEnable(GL_DEPTH_TEST);
		// draw floor
		drawObject(6);

		// pop model matrix for first object from stack //ceil
		mvStack.popMatrix();

		// now create second model matrix on the stack //floor
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b1floorX, b1floorY, b1floorZ);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw floor 2
		drawObject(7);

		// pop model matrix for first object from stack //side left
		mvStack.popMatrix();

		// now create second model matrix on the stack //side left
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b1lSideX, b1lSideY, b1lSideZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw left side
		drawObject(8);

		// pop model matrix for first object from stack //side right
		mvStack.popMatrix();

		// now create second model matrix on the stack //side right
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b1rSideX, b1rSideY, b1rSideZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw left side
		drawObject(9);

		// pop model matrix for first object from stack //front
		mvStack.popMatrix();

		// now create second model matrix on the stack //front
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b1frontX, b1frontY, b1frontZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw front side
		drawObject(10);

		// pop model matrix for first object from stack //back
		mvStack.popMatrix();

		// now create second model matrix on the stack //back
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b1backX, b1backY, b1backZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw back side
		drawObject(11);
		
		mvStack.popMatrix();
	}
	
	private void drawBox3(GL4 gl,Matrix3D pMat, int mv_loc, int proj_loc) {
		double rotateDeg = (double) System.currentTimeMillis() / 0.5;

		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b3ceilX, b3ceilY, b3ceilZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
		
		// get openGL ready to draw model now
		gl.glEnable(GL_DEPTH_TEST);
		// draw floor
		drawObject(12);

		// pop model matrix for first object from stack //ceil
		mvStack.popMatrix();

		// now create second model matrix on the stack //floor
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b3floorX, b3floorY, b3floorZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw floor 2
		drawObject(13);

		// pop model matrix for first object from stack //side left
		mvStack.popMatrix();

		// now create second model matrix on the stack //side left
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b3lSideX, b3lSideY, b3lSideZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw left side
		drawObject(14);

		// pop model matrix for first object from stack //side right
		mvStack.popMatrix();

		// now create second model matrix on the stack //side right
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b3rSideX, b3rSideY, b3rSideZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw left side
		drawObject(15);

		// pop model matrix for first object from stack //front
		mvStack.popMatrix();

		// now create second model matrix on the stack //front
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b3frontX, b3frontY, b3frontZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw front side
		drawObject(16);

		// pop model matrix for first object from stack //back
		mvStack.popMatrix();

		// now create second model matrix on the stack //back
		mvStack.pushMatrix();
		mvStack.rotate(rotateDeg / 3, 0.0, 1.0, 0.0);
		mvStack.translate(b3backX, b3backY, b3backZ);

		gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

		// now draw:
		gl.glEnable(GL_DEPTH_TEST);
		// draw back side
		drawObject(17);
		mvStack.popMatrix();
	}
	
	// first init is called
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		cameraX = 0.0f;
		cameraY = 0.0f;
		cameraZ = 4.0f;

		initBox1();
		initBox2();
		initBox3();

		// these were earlier in the display method
		// but we need them here before we call initObject
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glGenBuffers(vbo.length, vbo, 0);

		// the vertices used to be in setupVertices
		// another common practice is to declare the vertices as
		// global class attributes
		
		  float[] b1floor_verts = { 
				  0.5f, 0.0f, -0.5f, // 
				  -0.5f, 0.0f, -0.5f, // 
				  -0.5f,0.0f, 0.5f, // 
				  0.5f, 0.0f, 0.5f };
		 
		 
		  float[] b1floor_col = { //
					0.89f, 1.0f, 0.0f, //
					0.89f, 0.2f, 0.0f, //
					0.89f, 0.6f, 0.0f, //
					0.89f, 0.3f, 0.0f };
		  
		  float[] b1side_verts = { // 
				  0.0f, -0.5f, -0.5f, // 
				  0.0f, -0.5f, 0.5f, // 
				  0.0f,0.5f, 0.5f, // 
				  0.0f, 0.5f, -0.5f };
		  
		 float[] b1side_col = { //
					0.82f, 1.0f, 0.0f, //
					0.82f, 1.0f, 0.0f, //
					0.82f, 1.0f, 0.0f, //
					0.82f, 1.0f, 0.0f };
		 
		 float[] b1front_verts = { // 
				 0.5f, -0.5f, 0.0f, // 
				 -0.5f, -0.5f, 0.0f, //
				 -0.5f, 0.5f, 0.f, //
				 0.5f, 0.5f, 0.0f };
		  
		  float[] b1front_col = { //
					0.85f, 1.0f, 0.0f, //
					0.85f, 1.0f, 0.0f, //
					0.85f, 1.0f, 0.0f, //
					0.85f, 1.0f, 0.0f };
		 //

		float[] floor_verts = { //
				0.1f, 0.0f, -0.1f, //
				-0.1f, 0.0f, -0.1f, //
				-0.1f, 0.0f, 0.1f, //
				0.1f, 0.0f, 0.1f };

		float[] floor_col = { //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.0f };

		float[] side_verts = { //
				0.0f, -0.2f, -0.1f, //
				0.0f, -0.2f, 0.1f, //
				0.0f, 0.2f, 0.1f, //
				0.0f, 0.2f, -0.1f };

		float[] side_col = { //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.0f };

		float[] front_verts = { //
				0.1f, -0.2f, 0.0f, //
				-0.1f, -0.2f, 0.0f, //
				-0.1f, 0.2f, 0.f, //
				0.1f, 0.2f, 0.0f };

		float[] front_col = { //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.0f };
		//rotor
		float[] b3floor_verts = { //
				0.9f, 0.0f, -0.1f, //
				-0.9f, 0.0f, -0.1f, //
				-0.9f, 0.0f, 0.1f, //
				0.9f, 0.0f, 0.1f };

		float[] b3floor_col = { //
				1.0f, 0.5f, 1.0f, //
				1.0f, 0.1f, 0.9f, //
				1.0f, 0.3f, 0.9f, //
				1.0f, 0.0f, 0.9f };

		float[] b3side_verts = { //
				0.0f, -0.1f, -0.1f, //
				0.0f, -0.1f, 0.1f, //
				0.0f, 0.1f, 0.1f, //
				0.0f, 0.1f, -0.1f };

		float[] b3side_col = { //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.6f, //
				1.0f, 0.3f, 0.4f, //
				1.0f, 0.2f, 0.7f };

		float[] b3front_verts = { //
				0.9f, -0.1f, 0.0f, //
				-0.9f, -0.1f, 0.0f, //
				-0.9f, 0.1f, 0.f, //
				0.9f, 0.1f, 0.0f };

		float[] b3front_col = { //
				1.0f, 1.0f, 0.0f, //
				1.0f, 1.0f, 0.6f, //
				1.0f, 0.3f, 0.4f, //
				1.0f, 0.2f, 0.7f };

		int[] indices = { 0, 1, 2, 0, 2, 3 };

		initObject(0, floor_verts, floor_col, indices, Types.TRIANGLES);
		// init ceil
		initObject(1, floor_verts, floor_col, indices, Types.TRIANGLES);
		// init right side
		initObject(2, side_verts, side_col, indices, Types.TRIANGLES);
		// left side
		initObject(3, side_verts, side_col, indices, Types.TRIANGLES);
		// init front
		initObject(4, front_verts, front_col, indices, Types.TRIANGLES);
		// init back
		initObject(5, front_verts, front_col, indices, Types.TRIANGLES);
		
		//box1 initObj
		initObject(6, b1floor_verts, b1floor_col, indices, Types.TRIANGLES);
		// init ceil
		initObject(7, b1floor_verts, b1floor_col, indices, Types.TRIANGLES);
		// init right side
		initObject(8, b1side_verts, b1side_col, indices, Types.TRIANGLES);
		// left side
		initObject(9, b1side_verts, b1side_col, indices, Types.TRIANGLES);
		// init front
		initObject(10, b1front_verts, b1front_col, indices, Types.TRIANGLES);
		// init back
		initObject(11, b1front_verts, b1front_col, indices, Types.TRIANGLES);
		
		//box3 initObj
		initObject(12, b3floor_verts, b3floor_col, indices, Types.TRIANGLES);
		// init ceil
		initObject(13, b3floor_verts, b3floor_col, indices, Types.TRIANGLES);
		// init right side
		initObject(14, b3side_verts, b3side_col, indices, Types.TRIANGLES);
		// left side
		initObject(15, b3side_verts, b3side_col, indices, Types.TRIANGLES);
		// init front
		initObject(16, b3front_verts, b3front_col, indices, Types.TRIANGLES);
		// init back
		initObject(17, b3front_verts, b3front_col, indices, Types.TRIANGLES);
	}

	private void initBox1() {
		// box 1
		b1floorX = 0.0f;
		b1floorY = 0.0f - 0.5f;
		b1floorZ = 0.0f;

		b1ceilX = 0.0f;
		b1ceilY = 0.5f  -0.5f;
		b1ceilZ = 0.0f;

		this.b1lSideX = -0.5f;
		this.b1lSideY = 0.0f - 0.5f;
		this.b1lSideZ = 0.0f;

		this.b1rSideX = 0.5f;
		this.b1rSideY = 0.0f - 0.5f;
		this.b1rSideZ = 0.0f;

		this.b1backX = 0.0f;
		this.b1backY = 0.0f - 0.5f;
		this.b1backZ = -0.5f;

		this.b1frontX = 0.0f;
		this.b1frontY = 0.0f - 0.5f;
		this.b1frontZ = 0.5f;

	}
	

	private void initBox2() {
		// box 2
		b2floorX = 0.0f;
		b2floorY = -0.2f;
		b2floorZ = 0.0f;

		b2ceilX = 0.0f;
		b2ceilY = 0.2f;
		b2ceilZ = 0.0f;

		this.b2lSideX = -0.1f;
		this.b2lSideY = 0.0f;
		this.b2lSideZ = 0.0f;

		this.b2rSideX = 0.1f;
		this.b2rSideY = 0.0f;
		this.b2rSideZ = 0.0f;

		this.b2backX = 0.0f;
		this.b2backY = 0.0f;
		this.b2backZ = -0.1f;

		this.b2frontX = 0.0f;
		this.b2frontY = 0.0f;
		this.b2frontZ = 0.1f;
	}
	
	private void initBox3() {
		// box 1
		b3floorX = 0.0f;
		b3floorY = 0.3f;
		b3floorZ = 0.0f;

		b3ceilX = 0.0f;
		b3ceilY = 0.3f + 0.1f;
		b3ceilZ = 0.0f;

		this.b3lSideX = -0.9f;
		this.b3lSideY = 0.3f ;
		this.b3lSideZ = 0.0f;

		this.b3rSideX = 0.9f;
		this.b3rSideY = 0.3f ;
		this.b3rSideZ = 0.0f;

		this.b3backX = 0.0f;
		this.b3backY = 0.3f ;
		this.b3backZ = -0.1f;

		this.b3frontX = 0.0f;
		this.b3frontY = 0.3f;
		this.b3frontZ = 0.1f;

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

	private float genCameraZZoom(float c) {
		if(c < 20.0f && isGoingUp) {
			c = c+ 0.1f;
			if(c >= 20.0f)
				isGoingUp = false;
		}else {
			c = c -0.1f;
			if(c <= 2.0f)
				isGoingUp = true;
		}
		return c;
	}
	

	
	private float genCameraYZoom(float c) {
		if(c < 4.0f && isGoingUpY) {
			c = c+ 0.1f;
			if(c >= 4.0f)
				isGoingUpY = false;
		}else {
			c = c -0.1f;
			if(c <= 2.0f)
				isGoingUpY = true;
		}
		return c;
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

	}

	public void dispose(GLAutoDrawable drawable) {

	}

	public static void main(String[] args) {
		new FlyingHelicopter();
	}

}