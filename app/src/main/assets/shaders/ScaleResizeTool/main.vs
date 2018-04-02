attribute vec4 position;

attribute vec2 texCoord;

varying vec2 textureCoordFS;

void main()
{
	textureCoordFS = texCoord;
	gl_Position = position;
}