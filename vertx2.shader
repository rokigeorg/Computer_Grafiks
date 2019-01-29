 #version 410

layout (location=0) in vec3 position;
layout (location=1) in vec3 color;

out vec4 different_colors;

void main(void)
{	
	gl_Position = vec4(position, 1.0f);
	different_colors = vec4(color,1.0f);

} 