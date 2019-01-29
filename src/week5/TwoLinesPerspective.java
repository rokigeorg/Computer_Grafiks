package week5;

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
import graphicslib3D.Matrix3D;



public class TwoLinesPerspective extends JFrame implements GLEventListener{
  private int rendering_program;
  private int vao[] = new int[1];
  private int vbo[] = new int[2];
  
  private GLSLUtils util = new GLSLUtils();
  
  private GLCanvas myCanvas;
  
  private float cameraX, cameraY, cameraZ;		// position of the camera
  
  public TwoLinesPerspective(){
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
  public void init(GLAutoDrawable drawable){
      GL4 gl = (GL4) GLContext.getCurrentGL();
      rendering_program = createShaderProgram();
      
      //setup the camera position
      cameraX = 0.0f;
      cameraY = 0.0f;
      cameraZ = 2.0f;

      setupVertices();
      
  }
  
  // init finishes then display is called
  public void display(GLAutoDrawable drawable){
      GL4 gl = (GL4) GLContext.getCurrentGL();
      
      gl.glClear(GL_DEPTH_BUFFER_BIT);
      
      // not needed if bkg is black
      float bkg[] = { 1.0f, 1.0f, 1.0f, 1.0f };
      FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
      gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

      
      gl.glUseProgram(rendering_program); // loads program from init with the two shaders
      
      // define perspective matrix 
      //perspective
      float aspect = (float) myCanvas.getWidth()/(float) myCanvas.getHeight();
      Matrix3D pMat = this.perspectiveMethod(60.0f, aspect, 0.1f, 100.0f);		//for persective projection /zentrische Projektion
      Matrix3D oMat = this.orthographicMethod(1.0f, -1.0f, 1.0f, -1.0f, 0.1f, 100.0f); //for orthographic projection
      
      
      //copy perspective and MV matricies to corressponding uniform variables
      int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
      int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
      
      //build view matrix
      Matrix3D vMat = new Matrix3D();
      vMat.translate(-cameraX, -cameraY, -cameraZ);
      
      //build model matrix
      Matrix3D mMat = new Matrix3D();       //for right now we dont do anything (rotate, translate etc.) in with the mMat in this project

      Matrix3D mvMat = new Matrix3D();
      mvMat.concatenate(vMat);			//first view matrix 
      mvMat.concatenate(mMat);			//second we put the model matrix on it (A * B)
      

      //plug the mvMat and pMat into the shader uniform variables 
      gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(),0);
      gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(),0);
      
      
      //be sure to bind the vbo before assigning the attribute pointer
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); 
      // now set the "location = 0" info in the shaders to the point info from vbo[0]
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
      
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
      gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(1);
      
      gl.glLineWidth(10.0f);
      
      gl.glEnable(GL_DEPTH_TEST);
      gl.glDrawArrays(GL_LINES, 0, 4);

  }
  
  private void setupVertices()
  {   GL4 gl = (GL4) GLContext.getCurrentGL();
      
      float[] point_positions = {
         -0.5f,  -0.5f, 0.0f, // start point x,y,z of the 1. line
          -0.5f, -0.5f, -4.0f,// endpoint x,y,z of the 1.line
          0.5f,  -0.5f, 0.0f, //startpoint xyz of the 2. line
          0.5f, 0.0f, -1.0f// endpoint xyz of the 2. line
      };
      
      float[] point_colors = {// color 
              1.0f,  0.0f, 0.0f, 
              0.0f, 0.0f, 1.0f,
              0.0f,  1.0f, 0.0f, 
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
  
  
  
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int
  height) {
      
  }
  
  public void dispose(GLAutoDrawable drawable){
      
  }
  
  
  public static void main(String[ ] args){
      new TwoLinesPerspective();
  }
  
}
