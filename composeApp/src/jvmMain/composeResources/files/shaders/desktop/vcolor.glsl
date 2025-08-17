#version 330 core
layout (location = 0) in vec2 aPosition;

uniform mat4 uMVP;

void main() {
    gl_Position = uMVP * vec4(aPosition, 0.0, 1.0);
}