#version 330 core
layout (location = 0)  in vec2 aPos;
layout (location = 1)  in vec4 aModelMatrixR0; // First row of matrix
layout (location = 2)  in vec4 aModelMatrixR1; // Second row
layout (location = 3)  in vec4 aModelMatrixR2; // Third row
layout (location = 4)  in vec4 aModelMatrixR3; // Fourth row
layout (location = 5)  in vec4 aColor;
layout (location = 6)  in float aRadius;
layout (location = 7)  in float aInnerRadius;
layout (location = 8)  in float aStartAngle; // in radians
layout (location = 9)  in float aEndAngle; // in radians
layout (location = 10) in vec2 aCenter;

uniform mat4 uProjection;
uniform mat4 uView;


out vec2 vPos;
out vec4  vColor;
out float vRadius;
out float vInnerRadius;
out float vStartAngle;    // in radians
out float vEndAngle;      // in radians
out vec2  vCenter;

void main() {

    mat4 modelMatrix = mat4(
    aModelMatrixR0,
    aModelMatrixR1,
    aModelMatrixR2,
    aModelMatrixR3
    );


    gl_Position = (uProjection * uView * modelMatrix) * vec4(aPos, 0.0, 1.0);
//    vPos         = (modelMatrix * vec4(aPos, 0.0, 1.0)).xy;
    vPos         = aPos;
    vColor       = aColor;
    vRadius      = aRadius;
    vInnerRadius = aInnerRadius;
    vStartAngle  = aStartAngle; // in radians
    vEndAngle    = aEndAngle; // in radians
    vCenter      = aCenter;
}