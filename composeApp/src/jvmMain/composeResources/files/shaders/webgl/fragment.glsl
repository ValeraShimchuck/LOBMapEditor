#define SHADER_NAME batch-fragment


        #ifdef GL_ES // This checks if it is WebGL1
        #define in varying
        #define finalColor gl_FragColor
        #define texture texture2D
        #endif
        precision mediump float;


    in vec4 vColor;
    in vec2 vUV;



    //-----header START-----//
//----texture-batch-bit----//

                in float vTextureId;

                uniform sampler2D uTextures[32];


//----header FINISH----//

    void main(void) {



        vec4 outColor;

        //-----main START-----//
//----texture-batch-bit----//


                if(vTextureId < 0.5)
{
	outColor = texture(uTextures[0], vUV);
}
else
if(vTextureId < 1.5)
{
	outColor = texture(uTextures[1], vUV);
}
else
if(vTextureId < 2.5)
{
	outColor = texture(uTextures[2], vUV);
}
else
if(vTextureId < 3.5)
{
	outColor = texture(uTextures[3], vUV);
}
else
if(vTextureId < 4.5)
{
	outColor = texture(uTextures[4], vUV);
}
else
if(vTextureId < 5.5)
{
	outColor = texture(uTextures[5], vUV);
}
else
if(vTextureId < 6.5)
{
	outColor = texture(uTextures[6], vUV);
}
else
if(vTextureId < 7.5)
{
	outColor = texture(uTextures[7], vUV);
}
else
if(vTextureId < 8.5)
{
	outColor = texture(uTextures[8], vUV);
}
else
if(vTextureId < 9.5)
{
	outColor = texture(uTextures[9], vUV);
}
else
if(vTextureId < 10.5)
{
	outColor = texture(uTextures[10], vUV);
}
else
if(vTextureId < 11.5)
{
	outColor = texture(uTextures[11], vUV);
}
else
if(vTextureId < 12.5)
{
	outColor = texture(uTextures[12], vUV);
}
else
if(vTextureId < 13.5)
{
	outColor = texture(uTextures[13], vUV);
}
else
if(vTextureId < 14.5)
{
	outColor = texture(uTextures[14], vUV);
}
else
if(vTextureId < 15.5)
{
	outColor = texture(uTextures[15], vUV);
}
else
if(vTextureId < 16.5)
{
	outColor = texture(uTextures[16], vUV);
}
else
if(vTextureId < 17.5)
{
	outColor = texture(uTextures[17], vUV);
}
else
if(vTextureId < 18.5)
{
	outColor = texture(uTextures[18], vUV);
}
else
if(vTextureId < 19.5)
{
	outColor = texture(uTextures[19], vUV);
}
else
if(vTextureId < 20.5)
{
	outColor = texture(uTextures[20], vUV);
}
else
if(vTextureId < 21.5)
{
	outColor = texture(uTextures[21], vUV);
}
else
if(vTextureId < 22.5)
{
	outColor = texture(uTextures[22], vUV);
}
else
if(vTextureId < 23.5)
{
	outColor = texture(uTextures[23], vUV);
}
else
if(vTextureId < 24.5)
{
	outColor = texture(uTextures[24], vUV);
}
else
if(vTextureId < 25.5)
{
	outColor = texture(uTextures[25], vUV);
}
else
if(vTextureId < 26.5)
{
	outColor = texture(uTextures[26], vUV);
}
else
if(vTextureId < 27.5)
{
	outColor = texture(uTextures[27], vUV);
}
else
if(vTextureId < 28.5)
{
	outColor = texture(uTextures[28], vUV);
}
else
if(vTextureId < 29.5)
{
	outColor = texture(uTextures[29], vUV);
}
else
if(vTextureId < 30.5)
{
	outColor = texture(uTextures[30], vUV);
}
else
{
	outColor = texture(uTextures[31], vUV);
}

//----main FINISH----//

        finalColor = outColor * vColor;
    }