#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aUV;


uniform mat4 uTextureMatrix;
uniform mat4 uMVP;

out vec2 vUVMapping;

void main() {
    gl_Position = uMVP * vec4(aPos, 0.0, 1.0);

    vUVMapping = (uTextureMatrix * vec4(aUV, 0.0, 1.0)).xy;
}