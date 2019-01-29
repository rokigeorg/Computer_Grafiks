package week4;



import graphicslib3D.*;
import graphicslib3D.GLSLUtils.*;
import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.common.nio.Buffers;

public class Code4_1 extends JFrame implements GLEventListener
{   private GLCanvas myCanvas;
    private int rendering_program;
    private int vao[] = new int[1];  // vertex array object
    private int vbo[] = new int[2];  // vertex buffer object
    private float cameraX, cameraY, cameraZ;
    private float cubeLocX, cubeLocY, cubeLocZ;
    private GLSLUtils util = new GLSLUtils();

    public Code4_1()
    {   setTitle("Chapter4 - program1");
        setSize(600, 600);
        // this is new (to ensure compatibility with other OS)
        GLProfile prof = GLProfile.get(GLProfile.GL4);
        GLCapabilities capable = new GLCapabilities(prof);
        // note myCanvas now has the capabilities of GL4
        myCanvas = new GLCanvas(capable);
        myCanvas.addGLEventListener(this);
        getContentPane().add(myCanvas);
        this.setVisible(true);
    }

    public void display(GLAutoDrawable drawable)
    {   GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glClear(GL_DEPTH_BUFFER_BIT); // if not cleared all objects may be hidden (out of depth)

        gl.glUseProgram(rendering_program); // enables shaders so we can read the vertex attributes and uniform locations

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
        mMat.translate(cubeLocX, cubeLocY, cubeLocZ);

        // put together to build MV matrix
        Matrix3D mvMat = new Matrix3D();
        mvMat.concatenate(vMat);
        mvMat.concatenate(mMat);

        // copies matrices into uniform variables
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // activates buffer
        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate active buffer with the 0th vertex attribute in shader
            // note the first 0 is the location=0 from the vertex shader
        gl.glEnableVertexAttribArray(0); // enables the 0th vertex attribute, again, 0 comes from location=0

        // adjust settings for depth testing 
        gl.glEnable(GL_DEPTH_TEST); 
        gl.glDepthFunc(GL_LEQUAL);
        
        // draw
        gl.glDrawArrays(GL_TRIANGLES, 0, 36); // note the 36 vertices
    }

    public void init(GLAutoDrawable drawable)
    {   GL4 gl = (GL4) drawable.getGL();
        rendering_program = createShaderProgram();
        setupVertices(); // creates and copies vertices into buffer
        cameraX = 0.0f; cameraY = 0.0f; cameraZ = 8.0f;
        cubeLocX = 0.0f; cubeLocY = -2.0f; cubeLocZ = 0.0f; // note cube location shifted down y-axis
    }

    private void setupVertices()
    {   GL4 gl = (GL4) GLContext.getCurrentGL();
        // note there are 36 vertices but a cube only has 8
        // reason for extra is the triangularization: 6x2=12 (x3=36)
        float[] vertex_positions =
        {   -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
            1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
            1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
            1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
        };

        gl.glGenVertexArrays(vao.length, vao, 0);  // creates the VAO; parameters are: (how many, array to hold, offset)
        gl.glBindVertexArray(vao[0]);  // activates the VAO so buffers can be associated with it
        gl.glGenBuffers(vbo.length, vbo, 0);  // creates the VBO

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);  // activates buffer
        FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(vertex_positions);  // copies vertices into java float buffer
        gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);  // copies floatBuffer into active VBO buffer
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

    public static void main(String[] args) { new Code4_1(); }
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    public void dispose(GLAutoDrawable drawable) {}

    private int createShaderProgram()
    {   GL4 gl = (GL4) GLContext.getCurrentGL();

        String vshaderSource[] = util.readShaderSource("vert4_1.shader");
        String fshaderSource[] = util.readShaderSource("frag4_1.shader");

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
}