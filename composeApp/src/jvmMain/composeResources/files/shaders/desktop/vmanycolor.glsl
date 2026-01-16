#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec4 aModelMatrixR0; // First row of matrix
layout (location = 3) in vec4 aModelMatrixR1; // Second row
layout (location = 4) in vec4 aModelMatrixR2; // Third row
layout (location = 5) in vec4 aModelMatrixR3; // Fourth row

uniform mat4 uProjection;
uniform mat4 uView;


out vec4 vColor;

void main() {

    mat4 modelMatrix = mat4(
    aModelMatrixR0,
    aModelMatrixR1,
    aModelMatrixR2,
    aModelMatrixR3
    );


    gl_Position = (uProjection * uView * modelMatrix) * vec4(aPos, 0.0, 1.0);
    vColor = aColor;
}