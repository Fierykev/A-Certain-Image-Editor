precision mediump float;

varying vec2 textureCoordFS;

uniform sampler2D texture;

void main()
{
	gl_FragColor = vec4(texture2D(texture, textureCoordFS).xyz, 1);
}