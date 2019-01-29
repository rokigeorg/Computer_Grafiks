#version 410

layout (location=0) in vec3 position;

uniform mat4 m_matrix; 
uniform mat4 v_matrix; // note model and view are now separate
uniform mat4 proj_matrix;
uniform float tf; // time factor for animation

out vec4 varyingColor;

// need to declare functions (like in C++)
mat4 buildRotateX(float rad);  
mat4 buildRotateY(float rad);
mat4 buildRotateZ(float rad);
mat4 buildTranslate(float x, float y, float z);

void main(void)
{	float i = gl_InstanceID + tf; // value based on time, but varies for each cube
	float a = sin(203.0 * i/4000.0) * 403.0;	//when 100000 instances
	float b = cos(301.0 * i/2001.0) * 401.0;
	float c = sin(400.0 * i/3003.0) * 405.0;
	
//	float a = sin(2.0 * i) * 8.0;				// when 24 instances
//	float b = cos(3.0 * i) * 8.0;
//	float c = sin(4.0 * i) * 8.0;
	
	// rotation and translation for model matrix
	mat4 localRotX = buildRotateX(1000*i);
	mat4 localRotY = buildRotateY(1000*i);
	mat4 localRotZ = buildRotateZ(1000*i);
	mat4 localTrans = buildTranslate(a,b,c);

	// build model then model-view
	mat4 newM_matrix = m_matrix * localTrans * localRotX * localRotY * localRotZ;
	mat4 mv_matrix = v_matrix * newM_matrix;
	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0);
	varyingColor = vec4(position,1.0)*0.5 + vec4(0.5, 0.5, 0.5, 0.5);
}

mat4 buildTranslate(float x, float y, float z)
{	mat4 trans = mat4(	1.0, 0.0, 0.0, 0.0,
		0.0, 1.0, 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0,
		x, y, z, 1.0 );
	return trans;
}

//  rotation around the X axis
mat4 buildRotateX(float deg)
{	float rad = radians(deg);
	mat4 xrot = mat4(	1.0, 0.0, 0.0, 0.0,
		0.0, cos(rad), -sin(rad), 0.0,
		0.0, sin(rad), cos(rad), 0.0,
		0.0, 0.0, 0.0, 1.0 );
	return xrot;
}

//  rotation around the Y axis
mat4 buildRotateY(float deg)
{	float rad = radians(deg);
	mat4 yrot = mat4(	cos(rad), 0.0, sin(rad), 0.0,
		0.0, 1.0, 0.0, 0.0,
		-sin(rad), 0.0, cos(rad), 0.0,
		0.0, 0.0, 0.0, 1.0 );
	return yrot;
}

//  rotation around the Z axis
mat4 buildRotateZ(float deg)
{	float rad = radians(deg);
	mat4 zrot = mat4(	cos(rad), sin(rad), 0.0, 0.0,
		-sin(rad), cos(rad), 0.0, 0.0,
		0.0, 0.0, 1.0, 0.0,
		0.0, 0.0, 0.0, 1.0 );
	return zrot;
}