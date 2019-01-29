#version 410

layout (location=0) in vec3 position;
layout (location=1) in vec3 vertex_color;


uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

out vec4 different_colors;

void main(void)
{	gl_Position =proj_matrix * mv_matrix * vec4(position, 1.0);
	different_colors = vec4(vertex_color, 1.0f);
} 