attribute vec4 position;

attribute vec2 texCoord;

varying vec2 textureCoordFS;

uniform mat4 world;

void main()
{
	textureCoordFS = texCoord;
	gl_Position = world * position;
}